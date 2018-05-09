package com.teuskim.sbrowser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class LockscreenActivity extends Activity {
	
	private Handler mHandler = new Handler();
	private Button mBtnUnlockLeft;
	private Button mBtnUnlockRight;
	private boolean mIsTouchedLeft = false;
	private boolean mIsTouchedRight = false;
	
	private Locale mLocale;
	private TextView mCurrentTime;
	private TextView mCurrentDate;
	
	private SbWebView mWebView;
	private ContentView mContentView;
	private SourceView mSourceView;
	
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				switch(v.getId()){
				case R.id.btn_unlock_left:
				case R.id.btnlayout_unlock_left:
					if(mIsTouchedLeft){
						dismissKeyguard();
					}
					else{
						mIsTouchedLeft = true;
						mBtnUnlockLeft.setBackgroundResource(R.drawable.btn_lockscreen_unlock_02);
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								mIsTouchedLeft = false;
								mBtnUnlockLeft.setBackgroundResource(R.drawable.btn_lockscreen_unlock_01);
							}
						}, 2000);
					}
					break;
					
				case R.id.btn_unlock_right:
				case R.id.btnlayout_unlock_right:
					if(mIsTouchedRight){
						dismissKeyguard();
					}
					else{
						mIsTouchedRight = true;
						mBtnUnlockRight.setBackgroundResource(R.drawable.btn_lockscreen_unlock_02);
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								mIsTouchedRight = false;
								mBtnUnlockRight.setBackgroundResource(R.drawable.btn_lockscreen_unlock_01);
							}
						}, 2000);
					}
					break;
				}
				break;
			}
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Wallpaper_NoTitleBar);
		
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
							WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		setContentView(R.layout.lockscreen);
		mBtnUnlockLeft = (Button) findViewById(R.id.btn_unlock_left);
		mBtnUnlockRight = (Button) findViewById(R.id.btn_unlock_right);
		mCurrentTime = (TextView) findViewById(R.id.current_time);
		mCurrentDate = (TextView) findViewById(R.id.current_date);
		mWebView = (SbWebView) findViewById(R.id.webview);
		mContentView = (ContentView) findViewById(R.id.contentview);
		mSourceView = (SourceView) findViewById(R.id.sourceview);
		
		mBtnUnlockLeft.setOnTouchListener(mOnTouchListener);
		mBtnUnlockRight.setOnTouchListener(mOnTouchListener);
		findViewById(R.id.btnlayout_unlock_left).setOnTouchListener(mOnTouchListener);
		findViewById(R.id.btnlayout_unlock_right).setOnTouchListener(mOnTouchListener);
		
		mLocale = getResources().getConfiguration().locale;
		
		Bundle extras = getIntent().getExtras();
		int viewMode = extras.getInt("view_mode");
		
		switch(viewMode){
		case WebActivity.VIEW_MODE_WEBVIEW:
			viewWebView(extras.getString("url"));
			break;
		case WebActivity.VIEW_MODE_CONTENT:
			viewContentView(extras.getString("content"));
			break;
		case WebActivity.VIEW_MODE_SOURCE:
			viewSourceView(extras.getString("source"));
			break;
		}
	}
	
	private void viewWebView(String url){
		mWebView.setVisibility(View.VISIBLE);
		mContentView.setVisibility(View.GONE);
		mSourceView.setVisibility(View.GONE);
		
		mWebView.setClients(new SbWebView.SbWebChromeClient(this), new SbWebView.SbWebViewClient(this){

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				String js = "function disableClickAll(el){"
								+"for(var i=0; i<el.length; i++){"
									+"if((el[i] instanceof Text) == false && (el[i] instanceof HTMLButtonElement) == false){"
										+"el[i].onclick = disableClick;"
										+"if(el[i] instanceof HTMLInputElement)"
											+"el[i].disabled=true;"
										+"if(el[i].childNodes instanceof NodeList)"
											+"disableClickAll(el[i].childNodes);"
									+"}"
								+"}"
							+"}"
							+"function disableClick(e){"
								+"e.stopPropagation();"
								+"e.returnValue = false;"
							+"}"
							+"disableClickAll(document.getElementsByTagName('body')[0].childNodes);";
				mWebView.injectJs(js);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return true;
			}
			
		});
		mWebView.loadUrl(url);
	}
	
	private void viewContentView(String content){
		mWebView.setVisibility(View.GONE);
		mContentView.setVisibility(View.VISIBLE);
		mSourceView.setVisibility(View.GONE);
		
		// 본문 읽기 관련 셋팅
		ContentView.adjustPref(this, mContentView);
		mContentView.setContent(content);
	}
	
	private void viewSourceView(String source){
		mWebView.setVisibility(View.GONE);
		mContentView.setVisibility(View.GONE);
		mSourceView.setVisibility(View.VISIBLE);
		
		mSourceView.setSource(source);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.post(mTimeRunnable);
		setCurrentDate();
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mTimeRunnable);
		super.onPause();
	}

	private void dismissKeyguard(){
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
			}
		}, 500);
	}
	
	private Runnable mTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			String ampm = "";
			if(DateFormat.is24HourFormat(getApplicationContext()) == false){
				if(mLocale.getLanguage().equals(Locale.KOREAN.toString())){
					if(hour < 12)
						ampm = "오전";
					else
						ampm = "오후";
				}
				else{
					if(hour < 12)
						ampm = "AM";
					else
						ampm = "PM";
				}				
				
				if(hour == 0)
					hour = 12;
				else if(hour > 12)
					hour -= 12;
			}
			String time = get2Digit(hour)+":"+get2Digit(cal.get(Calendar.MINUTE))+" "+ampm;
			mCurrentTime.setText(time);
			
			mHandler.postDelayed(mTimeRunnable, 10000);
		}
		
		private String get2Digit(int num){
			if(num < 10)
				return "0"+num;
			return ""+num;
		}
	};
	
	private void setCurrentDate(){
		String date;
		Calendar cal = Calendar.getInstance();
		if(mLocale.getLanguage().equals(Locale.KOREAN.toString())){
			date = transformDate(new Date(), "MM월 dd일 ");
			String[] weeks = { "일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일" };
			date += weeks[cal.get(Calendar.DAY_OF_WEEK) - 1];
		}
		else{
			String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
			String[] weeks = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
			date = weeks[cal.get(Calendar.DAY_OF_WEEK) - 1] + " " + months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH);
		}
		mCurrentDate.setText(date);
	}
	
	public String transformDate(Date date, String toFormatString){
		
		SimpleDateFormat toFormat = new SimpleDateFormat(toFormatString, Locale.KOREAN );
		return toFormat.format(date);
	}

}
