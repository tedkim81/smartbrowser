package com.teuskim.sbrowser;

import java.io.File;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teuskim.sbrowser.MoveableListView.DragListener;
import com.teuskim.sbrowser.MoveableListView.DropListener;
import com.teuskim.sbrowser.SbDb.Favorite;
import com.teuskim.sbrowser.SbDb.SavedCont;

public class MainActivity extends BaseRemoconActivity implements DragListener,DropListener {
	
	private static final String TAG = "MainActivity";
	
	private static final int MAIN_MODE_HOME = 0;
	private static final int MAIN_MODE_SAVED = 1;
	private int mMainMode = MAIN_MODE_HOME;
	
	public static final String URL_SB = "http://smart-browser.appspot.com";
	
	private View mIconDefault;
	private TextView mTitleMain1;
	private TextView mTitleMain2;
	private MoveableListView mListViewHome;
	private MoveableListView mListViewSaved;
	private RemoconMenuView mBtnSettings;
	private RemoconMenuView mBtnMainMode;
	private RemoconMenuView mBtnExit;
	private View mLogo;
	
	private List<Favorite> mListFavorite;
	private HomeAdapter mAdapterHome;
	
	private List<SavedCont> mListSavedCont;
	private SavedAdapter mAdapterSaved;
	
	private LayoutInflater mInflater;
	private SbDb mDb;
	private boolean mIsDnd = false;
	private MiscPref mPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mInflater = LayoutInflater.from(this);
		mDb = SbDb.getInstance(getApplicationContext());
		mPref = MiscPref.getInstance(getApplicationContext());
		HttpManager.init();
		
		findViews();
		
		mListFavorite = mDb.getFavoriteList();
		mAdapterHome = new HomeAdapter();
		mListViewHome.setAdapter(mAdapterHome);
		if(mListFavorite.size() > 0){
			mListViewHome.setVisibility(View.VISIBLE);
			mIconDefault.setVisibility(View.GONE);
		}
		
		mListSavedCont = mDb.getSavedContList();
		mAdapterSaved = new SavedAdapter();
		mListViewSaved.setAdapter(mAdapterSaved);
		
		setBtnGoText(getString(R.string.google));
		setMypageBtnLayout();
	}
	
	protected void findViews(){
		mIconDefault = findViewById(R.id.icon_default);
		mTitleMain1 = (TextView) findViewById(R.id.title_main_1);
		mTitleMain2 = (TextView) findViewById(R.id.title_main_2);
		mListViewHome = (MoveableListView) findViewById(R.id.list_home);
		mListViewSaved = (MoveableListView) findViewById(R.id.list_saved);
		mLogo = findViewById(R.id.logo);
		
		View remoconBottom = mInflater.inflate(R.layout.remocon_bottom_main, null);
		setRemoconBottomView(remoconBottom);
		
		mBtnSettings = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_settings);
		mBtnMainMode = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_main_mode);
		mBtnExit = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_exit);
		
		mBtnSettings.setIconAndTitle(R.drawable.ic_menu_settings, getString(R.string.settings_long));
		mBtnMainMode.setIconAndTitle(R.drawable.ic_menu_save, getString(R.string.saved_cont));
		mBtnExit.setTitle(getString(R.string.exit));
		
		OnClickListener remoconMenuListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.btn_settings:
					btnSettings();
					break;
				case R.id.btn_main_mode:
					btnMainMode();
					break;
				case R.id.btn_exit:
					if(mBtnExit.isSelected()){
						Intent killIntent = new Intent("sbrowser.kill");
						sendBroadcast(killIntent);
					}
					else{
						showToast(R.string.exit_notice);
						mBtnExit.setSelected(true);
					}
					break;
				case R.id.btn_instructions:
					Intent i = new Intent(getApplicationContext(), InstructionsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					break;
				}
			}
		};
		mBtnSettings.setOnClickListener(remoconMenuListener);
		mBtnMainMode.setOnClickListener(remoconMenuListener);
		mBtnExit.setOnClickListener(remoconMenuListener);
		findViewById(R.id.btn_instructions).setOnClickListener(remoconMenuListener);
		
		mListViewHome.setForcedHeights(getPixelFromDip(43));
		mListViewHome.setDragListener(this);
		mListViewHome.setDropListener(this);
		mListViewSaved.setDragListener(this);
		mListViewSaved.setDropListener(this);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if(hasFocus){
			// TODO: 개연성 없지만 일단 급하니까 임시로..
			mListViewHome.setIconWidth(mLogo.getWidth());
			mListViewSaved.setIconWidth(mLogo.getWidth());
		}
	}

	private void refreshList(){
		mListFavorite = mDb.getFavoriteList();
		mListSavedCont = mDb.getSavedContList();
		
		if(mListViewSaved.getVisibility() == View.VISIBLE){
			mAdapterSaved.notifyDataSetChanged();
		}
		else{
			mAdapterHome.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Bundle extras = intent.getExtras();
		if(extras != null){
			if(extras.containsKey("prev_url")){
				final String prevUrl = extras.getString("prev_url");
				setOnBackClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getApplicationContext(), WebActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.putExtra("url", prevUrl);
						startActivity(i);
					}
				});
			}
			else{
				setOnBackClickListener(null);
			}
			
			if(extras.containsKey("next_url")){
				final String nextUrl = extras.getString("next_url");
				setOnFowardClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getApplicationContext(), WebActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.putExtra("url", nextUrl);
						startActivity(i);
					}
				});
			}
			else{
				setOnFowardClickListener(null);
			}
		}
		else{
			final String lastUrl = mPref.getLastUrl();
			if(lastUrl != null){
				setOnFowardClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getApplicationContext(), WebActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.putExtra("url", lastUrl);
						startActivity(i);
					}
				});
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(mMainMode == MAIN_MODE_SAVED)
			setMainModeSaved();
		else
			setMainModeHome();
		
		refreshList();
		mBtnExit.setSelected(false);
	}

	private void btnDeleteFavorite(final int id){
		new AlertDialog.Builder(this)
			.setMessage(R.string.confirm_delete)
			.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i(TAG, "delete favorite id: "+id);
					mDb.deleteFavorite(id);
					mListFavorite = mDb.getFavoriteList();
					mAdapterHome.notifyDataSetChanged();
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.show();
	}
	
	private void btnDeleteSaved(final SavedCont saved){
		new AlertDialog.Builder(this)
		.setMessage(R.string.confirm_delete)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "delete saved cont id: "+saved.mId+" , filename: "+saved.mFilename);
				mDb.deleteSavedCont(saved.mId);
				File file = new File(Environment.getExternalStorageDirectory()+"/sbrowser", saved.mFilename);
				if(file.exists())
					file.delete();
				
				mListSavedCont = mDb.getSavedContList();
				mAdapterSaved.notifyDataSetChanged();
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.show();
	}

	@Override
	protected void btnGo() {
		hideSettingsView();
		
		Intent i = new Intent(getApplicationContext(), WebActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("url", "http://www.google.com");
		startActivity(i);
	}
	
	@Override
	protected void btnSettings() {
		super.btnSettings();
		
		if(isShowSettingsView()){
			mBtnSettings.setSelected(true);
		}
		else{
			mBtnSettings.setSelected(false);
		}
	}
	
	protected void btnMainMode(){
		if(mMainMode == MAIN_MODE_HOME){
			setMainModeSaved();
		}
		else{
			setMainModeHome();
		}
	}
	
	private void setMainModeHome(){
		mTitleMain1.setText(R.string.app_name_1);
		mTitleMain2.setText(R.string.app_name_2);
		mTitleMain1.setVisibility(View.VISIBLE);
		mTitleMain2.setVisibility(View.VISIBLE);
		mListViewHome.setVisibility(View.VISIBLE);
		mListViewSaved.setVisibility(View.GONE);
		if(mListViewHome.getCount() > 0)
			mIconDefault.setVisibility(View.GONE);
		else
			mIconDefault.setVisibility(View.VISIBLE);
		
		mBtnMainMode.setIconAndTitle(R.drawable.ic_menu_save, getString(R.string.saved_cont));
		mMainMode = MAIN_MODE_HOME;
	}
	
	private void setMainModeSaved(){
		mTitleMain1.setText(R.string.saved_cont);
		mTitleMain1.setVisibility(View.VISIBLE);
		mTitleMain2.setVisibility(View.GONE);
		mListViewHome.setVisibility(View.GONE);
		mListViewSaved.setVisibility(View.VISIBLE);
		if(mListViewSaved.getCount() > 0)
			mIconDefault.setVisibility(View.GONE);
		else
			mIconDefault.setVisibility(View.VISIBLE);
		
		mBtnMainMode.setIconAndTitle(R.drawable.ic_menu_home, getString(R.string.home));
		mMainMode = MAIN_MODE_SAVED;
		refreshList();
	}

	@Override
	protected void initSettingsView(SettingsView settingsView) {
		super.initSettingsView(settingsView);
		
		showCVMSettings(false);
		settingsView.setOnCloseClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBtnSettings.setSelected(false);
			}
		});
	}
	
	private void setMypageBtnLayout(){
		getSettingsView().showMypageBtnLayout(
			new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new FavoriteTask().execute();
				}
			}, 
			new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new FavoriteTask().execute("[]");
				}
			},
			new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), WebActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("url", getShareUrl());
					startActivity(i);
				}
			}
		);
		
	}

	private String getDateFromTime(long time){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public void drag(int from, int to) {
		if(!mIsDnd)
			mIsDnd = true;
	}
	
	@Override
	public void drop(int from, int to) {
		if(mIsDnd){
			if(from == to){
				if(mListViewHome.getVisibility() == View.VISIBLE)
					mAdapterHome.notifyDataSetChanged();
				else if(mListViewSaved.getVisibility() == View.VISIBLE)
					mAdapterSaved.notifyDataSetChanged();
				return;
			}
			if(mListViewHome.getVisibility() == View.VISIBLE){
				int betweenOrder;
				int id = mListFavorite.get(from).mId;
				if(from < to){
					int targetOrder = mListFavorite.get(to).mOrderNum;
					if(mListFavorite.size() > to+1){
						betweenOrder = (targetOrder + mListFavorite.get(to+1).mOrderNum) / 2;
					}
					else{
						betweenOrder = targetOrder + 10000;
					}
				}
				else{
					int targetOrder = mListFavorite.get(to).mOrderNum;
					if(to > 0){
						betweenOrder = (targetOrder + mListFavorite.get(to-1).mOrderNum) / 2;
					}
					else{
						betweenOrder = targetOrder / 2;
					}
				}
				mDb.updateFavoriteOrderNum(id, betweenOrder);
				mListFavorite = mDb.getFavoriteList();
				mAdapterHome.notifyDataSetChanged();
			}
			else if(mListViewSaved.getVisibility() == View.VISIBLE){
				int betweenOrder;
				int id = mListSavedCont.get(from).mId;
				if(from < to){
					int targetOrder = mListSavedCont.get(to).mOrderNum;
					if(mListSavedCont.size() > to+1){
						betweenOrder = (targetOrder + mListSavedCont.get(to+1).mOrderNum) / 2;
					}
					else{
						betweenOrder = targetOrder + 10000;
					}
				}
				else{
					int targetOrder = mListSavedCont.get(to).mOrderNum;
					if(to > 0){
						betweenOrder = (targetOrder + mListSavedCont.get(to-1).mOrderNum) / 2;
					}
					else{
						betweenOrder = targetOrder / 2;
					}
				}
				mDb.updateSavedContOrderNum(id, betweenOrder);
				mListSavedCont = mDb.getSavedContList();
				mAdapterSaved.notifyDataSetChanged();
			}
			
			mIsDnd = false;
		}
	}

	@Override
	protected String getShareUrl(){
		String mypage = mPref.getMypage();
		if(mypage == null){
			new MyidTask().execute();
			return URL_SB;
		}
		return mypage;
	}

	private class HomeAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return mListFavorite.size();
		}

		@Override
		public Favorite getItem(int position) {
			return mListFavorite.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Favorite favorite = getItem(position);
			if(favorite.mType == SbDb.Favorite.TYPE_URL){
				View v = mInflater.inflate(R.layout.favorite_url_item, null);
				v.findViewById(R.id.btn_article).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						goWeb(favorite.mUrl);
					}
				});
				
				ImageView faviconView = (ImageView) v.findViewById(R.id.favicon);
				if(favorite.mThumb != null)
					faviconView.setImageBitmap(BitmapFactory.decodeByteArray(favorite.mThumb, 0, favorite.mThumb.length));
				
				ImageView btnChangeOrder = (ImageView) v.findViewById(R.id.btn_change_order);
				btnChangeOrder.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mListViewHome.startDragging();
						return false;
					}
				});
				
				TextView favoriteTitle = (TextView) v.findViewById(R.id.favorite_url_title);
				favoriteTitle.setText(favorite.mTitle);
				
				((Button) v.findViewById(R.id.btn_delete_favorite)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						btnDeleteFavorite(favorite.mId);
					}
				});
				return v;
			}
			else{
				FavoritePartView v = new FavoritePartView(MainActivity.this);
				
				ImageView faviconView = (ImageView) v.findViewById(R.id.favicon);
				if(favorite.mThumb != null)
					faviconView.setImageBitmap(BitmapFactory.decodeByteArray(favorite.mThumb, 0, favorite.mThumb.length));
				
				ImageView btnChangeOrder = (ImageView) v.findViewById(R.id.btn_change_order);
				btnChangeOrder.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mListViewHome.startDragging();
						return false;
					}
				});
				
				v.setLayoutParams(new ListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
				v.setTitle(favorite.mTitle);
				v.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						goWeb(favorite.mUrl);
					}
				});
				v.setWebViewLayoutParams(new RelativeLayout.LayoutParams(favorite.mPartWidth, favorite.mPartHeight));
				final SbWebView wv = v.getWebView();
				wv.loadUrl(favorite.mUrl);
				v.setOnLoadRunnable(new Runnable() {
					
					@Override
					public void run() {
						wv.injectRedo(favorite.mIndexSet);
					}
				});
				Button btnDelete = (Button) v.findViewById(R.id.btn_delete_favorite);
				btnDelete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						btnDeleteFavorite(favorite.mId);
					}
				});
				return v;
			}
			
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			
			if(getCount() > 0)
				mIconDefault.setVisibility(View.GONE);
			else
				mIconDefault.setVisibility(View.VISIBLE);
		}
		
		private void goWeb(String url){
			Intent i = new Intent(getApplicationContext(), WebActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra("url", url);
			startActivity(i);
		}
		
	}
	
	private class SavedAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mListSavedCont.size();
		}

		@Override
		public SavedCont getItem(int position) {
			return mListSavedCont.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final SavedCont saved = getItem(position);
			
			View v = mInflater.inflate(R.layout.saved_cont_item, null);
			v.findViewById(R.id.btn_article).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), WebActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("saved_cont_id", saved.mId);
					startActivity(i);
				}
			});
			
			ImageView faviconView = (ImageView) v.findViewById(R.id.favicon);
			if(saved.mThumb != null)
				faviconView.setImageBitmap(BitmapFactory.decodeByteArray(saved.mThumb, 0, saved.mThumb.length));
			
			ImageView btnChangeOrder = (ImageView) v.findViewById(R.id.btn_change_order);
			btnChangeOrder.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mListViewSaved.startDragging();
					return false;
				}
			});
			
			TextView savedContTitle = (TextView) v.findViewById(R.id.saved_cont_title);
			String dateStr = getDateFromTime(Long.valueOf(saved.mCrtDt));
			savedContTitle.setText(saved.mTitle+" ("+dateStr+")");
			
			((Button) v.findViewById(R.id.btn_delete_saved)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					btnDeleteSaved(saved);
				}
			});
			return v;
		}
		
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			
			if(getCount() > 0)
				mIconDefault.setVisibility(View.GONE);
			else
				mIconDefault.setVisibility(View.VISIBLE);
		}
		
	}
	
	private String getDeviceId(){
		return Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	}
	
	private String callApi(String url, String params){
		Log.i(TAG, "callApi : "+url+" "+params);
		try{
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-type","application/x-www-form-urlencoded");
			post.setEntity(new StringEntity(params, "UTF-8"));
			String result = (String) HttpManager.execute(post, new BasicResponseHandler());
			Log.i(TAG, "result : "+result);
			return result;
		}catch(Exception e){
			Log.e(TAG, url, e);
		}
		
		return null;
	}
	
	private void setMypageFromResult(String result){
		if(result != null && result.replaceAll("[+-]?\\d+", "").equals("")){
			String mypage = URL_SB+"/user/mypage/"+result;
			setSettingsPageUrl(mypage);
			mPref.setMypage(mypage);
		}
	}
	
	private class MyidTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			return callApi(URL_SB+"/user/myid", "device_id="+getDeviceId());
		}

		@Override
		protected void onPostExecute(String result) {
			setMypageFromResult(result);
		}
		
	}
	
	private class FavoriteTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			String favoriteParam;
			if(params != null && params.length > 0){
				favoriteParam = params[0];
			}
			else{
				List<SbDb.Favorite> list = mDb.getFavoriteList();
				JSONArray jarr = new JSONArray();
				if(list != null && list.size() > 0){
					try{
						for(SbDb.Favorite favor : list){
							JSONObject jobj = new JSONObject();
							jobj.put("url", favor.mUrl);
							jobj.put("title", favor.mTitle);
							jobj.put("type", favor.mType);
							jobj.put("index_set", favor.mIndexSet);
							jobj.put("part_width", favor.mPartWidth);
							jobj.put("part_height", favor.mPartHeight);
							jarr.put(jobj);
						}
					}catch(Exception e){}
				}
				favoriteParam = jarr.toString();
				try{ favoriteParam = URLEncoder.encode(favoriteParam, "UTF-8"); }catch(Exception e){}
			}
			String apiparams = "device_id="+getDeviceId()+"&favorite="+favoriteParam;
			return callApi(URL_SB+"/user/favorite", apiparams);
		}

		@Override
		protected void onPostExecute(String result) {
			setMypageFromResult(result);
			showToast(R.string.favorite_sync_ok);
		}
		
	}
	
	private void showToast(int resId){
		Toast toast = Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}