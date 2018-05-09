package com.teuskim.sbrowser;import org.json.JSONObject;import android.app.Activity;import android.app.ProgressDialog;import android.os.AsyncTask;import android.os.Bundle;import android.os.Handler;import android.text.Editable;import android.text.TextWatcher;import android.view.Gravity;import android.view.KeyEvent;import android.view.View;import android.view.View.OnClickListener;import android.view.WindowManager;import android.view.inputmethod.EditorInfo;import android.view.inputmethod.InputMethodManager;import android.widget.EditText;import android.widget.TextView;import android.widget.TextView.OnEditorActionListener;import android.widget.Toast;import com.facebook.android.api.AsyncRequestListener;public class ShareSNSActivity extends Activity implements OnClickListener {		public static final String KEY_SNS = "sns";	public static final String KEY_URL = "url";		public static final int SNS_FACEBOOK = 1;	public static final int SNS_TWITTER = 2;	public static final int SNS_KAKAO = 3;	private int mSns;		private EditText mShareContent;	private TextView mShareInfo;	private TextView mShareUrl;	private TextView mShareContentCount;	private TextView mBtnSend;		private Handler mHandler = new Handler();	private MiscPref mPref;	private ProgressDialog mProgressDialog;	private InputMethodManager mImm;	private String mUrl;		@Override	public void onCreate(Bundle savedInstanceState) {		super.onCreate(savedInstanceState);		setContentView(R.layout.share_sns);				mSns = getIntent().getIntExtra(KEY_SNS, 0);		mPref = MiscPref.getInstance(this);		mImm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);		findViews();						if(finishIfNotLogged())			return;				mShareContent.setText(R.string.share_web_text);		mShareInfo.setVisibility(View.GONE);  // TODO: 추후 내용 추가				mShareContent.setSelection(mShareContent.length());		mShareContent.setOnEditorActionListener(new OnEditorActionListener() {						@Override			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {				if( actionId == EditorInfo.IME_ACTION_DONE){										send();				}				return false;			}		});				mShareContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {						@Override			public void onFocusChange(View v, boolean hasFocus) {				if(hasFocus){					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);				}			}		});				mShareContent.addTextChangedListener(new TextWatcher(){			@Override			public void afterTextChanged(Editable s) {				//do nothing...			}			@Override			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				//do nothing....			}			@Override			public void onTextChanged(CharSequence s, int start, int before, int count) {				mShareContentCount.setText(""+(mShareContent.getText().length()));		}});				Bundle extras = getIntent().getExtras();		if(extras.containsKey(KEY_URL)){			mUrl = extras.getString(KEY_URL);			mShareUrl.setText(mUrl);		}				mShareContentCount.setText(""+(mShareContent.getText().length() + mShareUrl.getText().length()));	}		@Override	protected void onRestart() {		super.onRestart();				finishIfNotLogged();	}		@Override	protected void onPause() {		mImm.hideSoftInputFromWindow(mShareContent.getWindowToken(), 0);		super.onPause();	}	@Override	protected void onStop() {		super.onStop();		finish();	}	private boolean finishIfNotLogged(){		switch(mSns){		case SNS_TWITTER:			if(mPref != null && mPref.getTwitterId() == null){				finish();				return true;			}			break;		case SNS_FACEBOOK:			if(mPref != null && mPref.getFacebookId() == null){				finish();				return true;			}			break;		}			return false;	}	protected void findViews() {				mShareContentCount = (TextView) findViewById(R.id.share_content_count);		mShareInfo = (TextView) findViewById(R.id.share_info);		mShareContent = (EditText) findViewById(R.id.share_content);				mShareUrl = (TextView) findViewById(R.id.share_url);				mBtnSend = (TextView) findViewById(R.id.btn_send);						mBtnSend.setOnClickListener(this);	}	@Override	public void onClick(View v) {		switch(v.getId()){		case R.id.btn_send:			send();			break;		}	}		private void send(){		try{			mProgressDialog = ProgressDialog.show(this, "", "Loading");			mProgressDialog.setCancelable(true);		}catch(Exception e){}				switch(mSns){		case SNS_TWITTER:			sendTwitter();			break;		case SNS_FACEBOOK:			sendFacebook();			break;		case SNS_KAKAO:			sendKakao();			break;		}	}		private void sendTwitter(){		StringBuilder sb = new StringBuilder();		sb.append(mShareContent.getText().toString());		sb.append(" ");		sb.append(mShareUrl.getText());				new TweetTask().execute(sb.toString());	}		private void showToastMsg(final String msg){		mHandler.post(new Runnable() {						@Override			public void run() {				Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);				toast.setGravity(Gravity.TOP , 0, 100);				toast.show();			}		});	}		private void sendFacebook(){				FacebookApi api = new FacebookApi(this, new AsyncRequestListener() {						@Override			public void onComplete(JSONObject obj, Object state) {				try{					mProgressDialog.dismiss();				}catch(Exception e){}				showToastMsg(getString(R.string.share_fb_ok));				setResult(RESULT_OK);				finish();			}			@Override			public void onError() {				try{					mProgressDialog.dismiss();				}catch(Exception e){}				showToastMsg(getString(R.string.share_fb_fail));			}					});				api.write(mShareContent.getText().toString()				, mUrl);		// 웹페이지 URL	}		private void sendKakao(){		StringBuilder sb = new StringBuilder();		sb.append(mShareContent.getText());		new KakaoTask().execute(sb.toString());	}		private class TweetTask extends AsyncTask<String, Integer, Boolean>{		@Override		protected void onPostExecute(Boolean result){			try{				mProgressDialog.dismiss();			}catch(Exception e){}						if(result == true){				showToastMsg(getString(R.string.share_twitter_ok));				setResult(RESULT_OK);				finish();			}else{				showToastMsg(getString(R.string.share_twitter_fail));			}		}				@Override		protected Boolean doInBackground(String... params) {					TwitterApi api = new TwitterApi();			MiscPref pref = MiscPref.getInstance(getApplicationContext());			String twitterToken = pref.getTwitterToken();			String twitterTokenSecret = pref.getTwitterTokenSecret();			String status = params[0];						return api.updateStatus(twitterToken, twitterTokenSecret, status);				}			}		private class KakaoTask extends AsyncTask<String, Integer, Boolean>{		@Override		protected Boolean doInBackground(String... params) {						String msg = params[0];						String appid = "com.teuskim.sbrowser";			String appver = "1.0";			String appname = getString(R.string.app_name);						try{				KakaoLink link = new KakaoLink(getApplicationContext(), mUrl, appid, appver, msg, appname, null, "UTF-8");				if(link.isAvailable())					startActivity(link.getIntent());			}catch(Exception e){}									return true;		}				@Override		protected void onPostExecute(Boolean result){			try{				mProgressDialog.dismiss();			}catch(Exception e){}						setResult(RESULT_OK);			finish();		}			}	}