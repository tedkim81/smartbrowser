package com.teuskim.sbrowser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.teuskim.sbrowser.ColorPickerView.ColorChange;
import com.teuskim.sbrowser.SbDb.SavedCont;

public class WebActivity extends BaseRemoconActivity{
	
	private static final String TAG = "WebActivity";
	
	protected static final int VIEW_MODE_WEBVIEW = 0;
	protected static final int VIEW_MODE_CONTENT = 1;
	protected static final int VIEW_MODE_SOURCE = 2;
	protected int mViewMode = VIEW_MODE_WEBVIEW;
	
	protected static final int ADD_TYPE_FAVOR = 0;
	protected static final int ADD_TYPE_PART = 1;
	protected static final int ADD_TYPE_SAVED_CONT = 2;
	private int mAddType = ADD_TYPE_FAVOR;

	private RelativeLayout mWebLayout;
	private LinearLayout mScreenPageLayout;
	
	private RemoconMenuView mBtnSettings;
	private RemoconMenuView mBtnHome;
	private RemoconMenuView mBtnAdd;  
	private RemoconMenuView mBtnViewMode;
	private RemoconMenuView mBtnScreen;
	private View mBtnsetWebview1;  // webview 1depth
	private ImageView mArrowRemocon;
	private View mBtnsetWebview2Add;  // webview 2depth add
	private View mBtnsetWebview2Add1;  // webview 2depth add and 1
	private View mBtnAddFavor;
	private View mBtnAddPart;
	private View mBtnAddSavedCont;
	private View mBtnsetWebview2Add2;  // webview 2depth add and 2
	private View mBtnsetWebview2Add3;  // webview 2depth add and 3
	private View mBtnExpandPart;
	private View mBtnAddPartComplete;
	private View mBtnAddPartReset1;
	private View mBtnAddPartReset2;
	private View mBtnsetWebview2Add4;  // webview 2depth add and 4
	private EditText mInputAddTitle;
	private View mBtnAddComplete;
	private View mBtnAddCancel;
	private View mBtnsetWebview2ViewMode;  // webview 2depth view mode
	private Button mBtnViewModeWeb;
	private Button mBtnViewModeCont;  // content view mode only
	private Button mBtnViewModeSource;
	private View mBtnsetWebview2Screen;  // webview 2depth screen
	
	private List<WebWrap> mWebWrapList;
	private int mCurrentWebIndex;
	private boolean mIsDeletePage;
	private LayoutInflater mInflater;
	private MiscPref mPref;
	private boolean mIsPageLoading;
	private Handler mHandler = new Handler();
	
	private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				
				Intent i = new Intent(context, LockscreenActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(mViewMode == VIEW_MODE_SOURCE)
					i.putExtra("view_mode", VIEW_MODE_WEBVIEW);
				else
					i.putExtra("view_mode", mViewMode);
				
				switch(mViewMode){
				case VIEW_MODE_WEBVIEW:
				case VIEW_MODE_SOURCE:
					i.putExtra("url", getCurrentWeb().getWebView().getOriginalUrl());
					i.putExtra("scrolltop", getCurrentWeb().getWebView().getScrollY());
					break;
				case VIEW_MODE_CONTENT:
					i.putExtra("content", getCurrentWeb().getContentView().getContent());
					i.putExtra("scrolltop", getCurrentWeb().getContentView().getScrollTop());
					break;
				}
				
				PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
				try {
					pi.send();
				} catch (CanceledException e) {
					Log.e(TAG, "lock screen fail", e);
				}
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);

		mInflater = LayoutInflater.from(getApplicationContext());
		mPref = MiscPref.getInstance(getApplicationContext());
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web);
		findViews();
		
		mWebWrapList = new ArrayList<WebActivity.WebWrap>();
		addWebView(null);

		WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
		if(savedInstanceState != null)
			getCurrentWeb().getWebView().restoreState(savedInstanceState);
		else
			getCurrentWeb().loadByIntent(getIntent());
		
        CookieSyncManager.createInstance(this);
	}
	
	protected void findViews(){
		mWebLayout = (RelativeLayout) findViewById(R.id.web_layout);
		
		View remoconBottom = mInflater.inflate(R.layout.remocon_bottom_webview, null);
		setRemoconBottomView(remoconBottom);
		
		mBtnSettings = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_settings);
		mBtnHome = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_home);
		mBtnAdd = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_add);
		mBtnViewMode = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_view_mode);
		mBtnScreen = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_screen);
		mBtnsetWebview1 = remoconBottom.findViewById(R.id.btnset_webview_1);
		mBtnHome = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_home);
		mArrowRemocon = (ImageView) remoconBottom.findViewById(R.id.arrow_remocon);
		mBtnAdd = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_add);
		mBtnViewMode = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_view_mode);
		mBtnScreen = (RemoconMenuView) remoconBottom.findViewById(R.id.btn_screen);
		mBtnsetWebview2Add = remoconBottom.findViewById(R.id.btnset_webview_2_add);
		mBtnsetWebview2Add1 = remoconBottom.findViewById(R.id.btnset_webview_2_add_1);
		mBtnAddFavor = remoconBottom.findViewById(R.id.btn_add_favor);
		mBtnAddPart = remoconBottom.findViewById(R.id.btn_add_part);
		mBtnAddSavedCont = remoconBottom.findViewById(R.id.btn_save);
		mBtnsetWebview2Add2 = remoconBottom.findViewById(R.id.btnset_webview_2_add_2);
		mBtnsetWebview2Add3 = remoconBottom.findViewById(R.id.btnset_webview_2_add_3);
		mBtnExpandPart = remoconBottom.findViewById(R.id.btn_expand_part);
		mBtnAddPartComplete = remoconBottom.findViewById(R.id.btn_add_part_complete);
		mBtnAddPartReset1 = remoconBottom.findViewById(R.id.btn_add_part_reset_1);
		mBtnAddPartReset2 = remoconBottom.findViewById(R.id.btn_add_part_reset_2);
		mBtnsetWebview2Add4 = remoconBottom.findViewById(R.id.btnset_webview_2_add_4);
		mInputAddTitle = (EditText) remoconBottom.findViewById(R.id.input_add_title);
		mBtnAddComplete = remoconBottom.findViewById(R.id.btn_add_complete);
		mBtnAddCancel = remoconBottom.findViewById(R.id.btn_add_cancel);
		mBtnsetWebview2ViewMode = remoconBottom.findViewById(R.id.btnset_webview_2_view_mode);
		mBtnViewModeWeb = (Button) remoconBottom.findViewById(R.id.btn_view_mode_web);
		mBtnViewModeCont = (Button) remoconBottom.findViewById(R.id.btn_view_mode_cont);
		mBtnViewModeSource = (Button) remoconBottom.findViewById(R.id.btn_view_mode_source);
		mBtnsetWebview2Screen = remoconBottom.findViewById(R.id.btnset_webview_2_screen);
		mScreenPageLayout = (LinearLayout) remoconBottom.findViewById(R.id.screen_page_layout);
		
		mBtnSettings.setIconAndTitle(R.drawable.ic_menu_settings, getString(R.string.settings_short));
		mBtnHome.setIconAndTitle(R.drawable.ic_menu_home, getString(R.string.home));
		mBtnAdd.setIconAndTitle(R.drawable.ic_menu_add, getString(R.string.add));
		mBtnViewMode.setIconAndTitle(R.drawable.ic_menu_view, getString(R.string.view));
		mBtnScreen.setIconAndTitle(R.drawable.ic_menu_more, getString(R.string.screen));
		
		OnClickListener remoconMenuListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.btn_settings:
					btnSettings();
					break;
				case R.id.btn_home:
					goHome();
					return;
				case R.id.btn_add:
					btnAdd();
					break;
				case R.id.btn_view_mode:
					btnViewMode();
					break;
				case R.id.btn_screen:
					btnScreen();
					break;
				case R.id.btn_add_favor:
					mAddType = ADD_TYPE_FAVOR;
					btnAddFavor();
					break;
				case R.id.btn_add_part:
					mAddType = ADD_TYPE_PART;
					btnAddPart();
					break;
				case R.id.btn_save:
					mAddType = ADD_TYPE_SAVED_CONT;
					btnAddFavor();  // 즐찾과 동일한 플로우
					break;
				case R.id.btn_expand_part:
					btnExpandPart();
					break;
				case R.id.btn_add_part_complete:
					btnAddPartComplete();
					break;
				case R.id.btn_add_part_reset_1:
				case R.id.btn_add_part_reset_2:
					btnAddPartReset();
					break;
				case R.id.btn_add_complete:
					btnAddComplete(mAddType);
					break;
				case R.id.btn_add_cancel:
					btnAddCancel();
					break;
				case R.id.btn_view_mode_web:
					btnViewModeWeb();
					break;
				case R.id.btn_view_mode_cont:
					btnViewModeCont();
					break;
				case R.id.btn_view_mode_source:
					btnViewModeSource();
					break;
				case R.id.btn_screen_add:
					addWebView("http://www.google.com");
					break;
				}
			}
		};
		mBtnSettings.setOnClickListener(remoconMenuListener);
		mBtnHome.setOnClickListener(remoconMenuListener);
		mBtnAdd.setOnClickListener(remoconMenuListener);
		mBtnViewMode.setOnClickListener(remoconMenuListener);
		mBtnScreen.setOnClickListener(remoconMenuListener);
		mBtnAddFavor.setOnClickListener(remoconMenuListener);
		mBtnAddPart.setOnClickListener(remoconMenuListener);
		mBtnAddSavedCont.setOnClickListener(remoconMenuListener);
		mBtnExpandPart.setOnClickListener(remoconMenuListener);
		mBtnAddPartComplete.setOnClickListener(remoconMenuListener);
		mBtnAddPartReset1.setOnClickListener(remoconMenuListener);
		mBtnAddPartReset2.setOnClickListener(remoconMenuListener);
		mBtnAddComplete.setOnClickListener(remoconMenuListener);
		mBtnAddCancel.setOnClickListener(remoconMenuListener);
		mBtnViewModeWeb.setOnClickListener(remoconMenuListener);
		mBtnViewModeCont.setOnClickListener(remoconMenuListener);
		mBtnViewModeSource.setOnClickListener(remoconMenuListener);
		findViewById(R.id.btn_screen_add).setOnClickListener(remoconMenuListener);
	}
	
	@Override
	protected void initSettingsView(final SettingsView settingsView) {
		super.initSettingsView(settingsView);
		
		settingsView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
			
			@Override
			public void colorChanged(ColorChange colorChange, int color) {
				onContentViewBgChanged(colorChange, color);
			}
		}, new ColorPickerView.OnColorChangedListener() {
			
			@Override
			public void colorChanged(ColorChange colorChange, int color) {
				onContentViewFontChanged(color);
				settingsView.setFontColor(color);
				mPref.setContFontColor(color);
				hideColorPickerView(colorChange);
			}
		});
		
		settingsView.setContBg(mPref.getContBgColor());
		settingsView.setFontColor(mPref.getContFontColor());
		settingsView.setFontSize(mPref.getContFontSize());
		settingsView.setOnFontSizeChangedListener(new SettingsView.OnFontSizeChangedListener() {
			
			@Override
			public void onFontSizeChanged(int size) {
				onContentViewFontSizeChanged(size);
				mPref.setContFontSize(size);
			}
		});
		settingsView.setLineSpace(mPref.getContLineSpace());
		settingsView.setOnLineSpaceChangedListener(new SettingsView.OnLineSpaceChangedListener() {
			
			@Override
			public void onLineSpaceChanged(int space) {
				onContentViewLineSpaceChanged(space);
				mPref.setContLineSpace(space);
			}
		});
		
		settingsView.setContPadding(mPref.getContPadding());
		settingsView.setOnPaddingChangedListener(new SettingsView.OnPaddingChangedListener() {
			
			@Override
			public void onPaddingChanged(int padding) {
				onContentViewPaddingChanged(padding);
				mPref.setContPadding(padding);
			}
		});
		
		if(mViewMode == VIEW_MODE_CONTENT){
			settingsView.showCVMSettings(true);
		}
		else{
			settingsView.showCVMSettings(false);
		}
		
		settingsView.setOnCloseClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBtnSettings.setSelected(false);
			}
		});
	}
	
	private void addWebView(final String startUrl){
		WebWrap web = new WebWrap(this);
		mWebLayout.addView(web);
		mWebWrapList.add(web);
		mCurrentWebIndex = mWebWrapList.size() - 1;
		
		Button btnPage = new Button(this);
		btnPage.setText(""+(mCurrentWebIndex+1));
		btnPage.setTextSize(12);
		btnPage.setTextColor(0xff606261);
		btnPage.setTypeface(null, Typeface.BOLD);
		btnPage.setBackgroundResource(R.drawable.btn_remote_expand_below2);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
		btnPage.setLayoutParams(lp);
		btnPage.setTag(mCurrentWebIndex);
		btnPage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int webIndex = (Integer)v.getTag();
				if(webIndex == mCurrentWebIndex){
					if(mIsDeletePage){
						deleteWebView(webIndex);
					}
					else{
						mIsDeletePage = true;
						((Button)v).setText("X");
					}
				}
				else{
					showWebView(webIndex, startUrl);
				}
			}
		});
		
		mScreenPageLayout.addView(btnPage);
		showWebView(mCurrentWebIndex, startUrl);
	}
	
	private void showWebView(int webIndex, String startUrl){
		mCurrentWebIndex = webIndex;
		mIsDeletePage = false;
		
		int childCount = mWebLayout.getChildCount();
		for(int i=0; i<childCount; i++){
			mWebLayout.getChildAt(i).setVisibility(View.GONE);
		}
		mWebLayout.getChildAt(mCurrentWebIndex).setVisibility(View.VISIBLE);
		
		String currentUrl = getCurrentWeb().getWebView().getOriginalUrl();
		if(currentUrl == null && startUrl != null)
			getCurrentWeb().getWebView().loadUrl(startUrl);
		
		int pageChildCount = mScreenPageLayout.getChildCount();
		for(int i=0; i<pageChildCount; i++){
			Button btn = (Button)mScreenPageLayout.getChildAt(i);
			btn.setText(""+(i+1));
			btn.setTextColor(0xff606261);
			btn.setTag(i);
		}
		((Button)mScreenPageLayout.getChildAt(mCurrentWebIndex)).setTextColor(0xffee5128);
	}
	
	private void deleteWebView(int webIndex){
		if(mWebWrapList.size() > 1){
			mWebWrapList.remove(webIndex);
			mWebLayout.removeViewAt(webIndex);
			mScreenPageLayout.removeViewAt(webIndex);
			showWebView(0, null);
		}
		else{
			finish();
		}
	}
	
	private WebWrap getCurrentWeb(){
		return mWebWrapList.get(mCurrentWebIndex);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getCurrentWeb().getWebView().saveState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		setOnBackClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideSettingsView();
				getCurrentWeb().resetWebView();
				WebView webView = getCurrentWeb().getWebView();
				if(webView.canGoBack()){
		    		webView.goBack();
		    	}
				else{
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra("next_url", webView.getOriginalUrl());
					startActivity(intent);
				}
			}
		});
		
		setOnFowardClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideSettingsView();
				getCurrentWeb().resetWebView();
				WebView webView = getCurrentWeb().getWebView();
				if(webView.canGoForward()){
		    		webView.goForward();
		    	}
				else{
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra("prev_url", webView.getOriginalUrl());
					startActivity(intent);
				}
			}
		});
		
		// screen off 될때 lock screen 띄우기 위해
		IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	    registerReceiver(mScreenReceiver, screenFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}

	@Override
	protected void onStop() {
		getCurrentWeb().getWebView().stopLoading();
		btnReset();
		try{
			unregisterReceiver(mScreenReceiver);
		}catch(Exception e){}
		
		super.onStop();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getCurrentWeb().hideLayoutWord();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
		getCurrentWeb().loadByIntent(intent);
	}

	@Override
	public void onBackPressed() {
		if(isShowSettingsView()){
    		hideSettingsView();
    		return;
    	}
    	if(getCurrentWeb().onBackPressed() == false)
    		super.onBackPressed();
	}
	
	private void btnReset(){
		mBtnsetWebview1.setVisibility(View.VISIBLE);
		mBtnsetWebview2Add.setVisibility(View.GONE);
		mBtnsetWebview2Add1.setVisibility(View.GONE);
		mBtnsetWebview2Add2.setVisibility(View.GONE);
		mBtnsetWebview2Add3.setVisibility(View.GONE);
		mBtnsetWebview2Add4.setVisibility(View.GONE);
		mBtnsetWebview2ViewMode.setVisibility(View.GONE);
		mBtnsetWebview2Screen.setVisibility(View.GONE);
		
		mArrowRemocon.setVisibility(View.GONE);
		mBtnAdd.setSelected(false);
		mBtnViewMode.setSelected(false);
		mBtnScreen.setSelected(false);
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

	@Override
	protected void btnGo() {
		btnReset();
		hideSettingsView();
		
		if(mIsPageLoading)
			getCurrentWeb().getWebView().stopLoading();
		else
			getCurrentWeb().getWebView().reload();
	}

	private void goHome() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("prev_url", getCurrentWeb().getWebView().getOriginalUrl());
		startActivity(intent);
	}

	private void btnAdd() {
		if(mBtnsetWebview2Add.getVisibility() == View.VISIBLE){
			if(mBtnsetWebview2Add2.getVisibility() == View.VISIBLE)
				btnAddPartReset();
			else
				btnReset();
		}
		else{
			btnReset();
			mBtnsetWebview2Add.setVisibility(View.VISIBLE);
			mBtnsetWebview2Add1.setVisibility(View.VISIBLE);
			showRemocon();
			mBtnAdd.setSelected(true);
			showArrowRemocon(5, 2);
		}
	}
	
	private void btnAddFavor(){
		btnReset();
		mBtnsetWebview2Add.setVisibility(View.VISIBLE);
		mBtnsetWebview2Add4.setVisibility(View.VISIBLE);
		mInputAddTitle.setText(getCurrentWeb().getWebView().getTitle());
	}
	
	private void showBtnsetWebview2Add3(){
		btnReset();
		mBtnsetWebview2Add.setVisibility(View.VISIBLE);
		mBtnsetWebview2Add3.setVisibility(View.VISIBLE);
		mBtnAdd.setSelected(true);
	}
	
	protected String getAddTitle(){ 
		return mInputAddTitle.getText().toString(); 
	}
	
	private void showArrowRemocon(int total, int index){
		int btnWidth = getRemoconWidth() / total;
		int leftMargin = (btnWidth*index) + (btnWidth/2) - (mArrowRemocon.getWidth()/2);
		RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mArrowRemocon.getLayoutParams();
		rlp.leftMargin = leftMargin;
		mArrowRemocon.setLayoutParams(rlp);
		mArrowRemocon.setVisibility(View.VISIBLE);
	}

	private void btnAddComplete(int addType) {
		getCurrentWeb().btnAddComplete(addType);
	}

	private void btnAddCancel() {
		btnReset();
	}

	private void btnAddPart() {
		btnReset();
		mBtnsetWebview2Add.setVisibility(View.VISIBLE);
		mBtnsetWebview2Add2.setVisibility(View.VISIBLE);
		mBtnAdd.setSelected(true);
		
		// 페이지에 js 주입한다.
		getCurrentWeb().getWebView().injectResetClick();
	}

	private void btnAddPartReset() {
		btnReset();
		
		// 주입된 js 를 초기화한다.
		// TODO: 일단은 리프레쉬로 하고, 가능하면 주입한 js를 취소하는 방식으로 바꾸자.
		getCurrentWeb().getWebView().reload();
	}
	
	private void btnExpandPart() {
		getCurrentWeb().getWebView().injectExpand();
	}

	private void btnAddPartComplete() {
		btnReset();
		mBtnsetWebview2Add.setVisibility(View.VISIBLE);
		mBtnsetWebview2Add4.setVisibility(View.VISIBLE);
		mInputAddTitle.setText(getCurrentWeb().getWebView().getTitle());
		
		getCurrentWeb().getWebView().injectClip();
	}

	private void btnViewMode(){
		if(mBtnsetWebview2ViewMode.getVisibility() == View.VISIBLE){
			btnReset();
		}
		else{
			btnReset();
			mBtnsetWebview2ViewMode.setVisibility(View.VISIBLE);
			showRemocon();
			mBtnViewMode.setSelected(true);
			showArrowRemocon(5, 3);
		}
	}
	
	private void btnViewModeWeb() {
		if(mViewMode != VIEW_MODE_WEBVIEW){
			btnReset();
			hideRemocon();
		}
		mViewMode = VIEW_MODE_WEBVIEW;
		showCVMSettings(false);
		
		getCurrentWeb().btnViewModeWeb();
	}

	public void btnViewModeCont() {
		if(mViewMode != VIEW_MODE_CONTENT){
			btnReset();
			hideRemocon();
		}
		mViewMode = VIEW_MODE_CONTENT;
		showCVMSettings(true);
		
		getCurrentWeb().btnViewModeCont();
	}

	private void btnViewModeSource() {
		if(mViewMode != VIEW_MODE_SOURCE){
			btnReset();
			hideRemocon();
		}
		mViewMode = VIEW_MODE_SOURCE;
		showCVMSettings(false);
		
		getCurrentWeb().btnViewModeSource();
	}
	
	private void btnScreen(){
		if(mBtnsetWebview2Screen.getVisibility() == View.VISIBLE){
			btnReset();
		}
		else{
			btnReset();
			mBtnsetWebview2Screen.setVisibility(View.VISIBLE);
			showRemocon();
			mBtnScreen.setSelected(true);
			showArrowRemocon(5, 4);
		}
	}

	private void extractSource(){
		getCurrentWeb().getWebView().injectExtractSource();
	}

	private void onContentViewBgChanged(ColorChange colorChange, int bg) {
		switch(colorChange){
		case OK:
			getSettingsView().setContBg(bg);
			mPref.setContBgColor(bg);
			break;
		case CHANGE:
			break;
		case CANCEL:
			break;
		}
		hideColorPickerView(colorChange);
		
		getCurrentWeb().getContentView().setBackground(bg);
	}
	
	private void hideColorPickerView(ColorChange colorChange){
		switch(colorChange){
		case OK:
			getSettingsView().hideSelectBgView();
		case CANCEL:
			getSettingsView().hideColorPickerView();
			break;
		default:
			break;
		}
	}
	
	private void onContentViewFontChanged(int color) {
		getCurrentWeb().getContentView().setFontColor(color);
	}

	private void onContentViewFontSizeChanged(int size) {
		getCurrentWeb().getContentView().setFontSize(size);
	}
	
	private void onContentViewLineSpaceChanged(int space) {
		getCurrentWeb().getContentView().setLineSpace(space);
	}
	
	private void onContentViewPaddingChanged(int padding) {
		getCurrentWeb().getContentView().setPadding(padding, padding, padding, padding);
	}
	
	@Override
	protected String getShareUrl() {
		return getCurrentWeb().getWebView().getOriginalUrl();
	}


	private class WebWrap extends RelativeLayout {
		
		private SbWebView mWebView;
		private ContentView mContentView;
		private SourceView mSourceView;
		private HumanProgressBar mProgressBar;
		private RelativeLayout mLayout;
		private View mCustomView;
		private CustomViewCallback mCustomViewCallback;
		private WebChromeClient mWebChromeClient;
		
		private String mIndexSet;
		private int mWindowWidth;
		private int mWindowHeight;
		private int mSelectedWidth;
		private int mSelectedHeight;
		
		public WebWrap(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		public WebWrap(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public WebWrap(Context context) {
			super(context);
			init(context);
		}

		private void init(Context context){
			LayoutInflater.from(context).inflate(R.layout.web_wrap, this);
			
			mWebView = (SbWebView) findViewById(R.id.webview);
			mContentView = (ContentView) findViewById(R.id.contentview);
			mSourceView = (SourceView) findViewById(R.id.sourceview);
			mProgressBar = (HumanProgressBar) findViewById(R.id.progress);
			mLayout = (RelativeLayout) findViewById(R.id.webview_layout);
			
			mProgressBar.setStopListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						mWebView.stopLoading();
					}
					return true;
				}
			});
			
			mWebChromeClient = new SbWebView.SbWebChromeClient(context) {
				
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					mProgressBar.setProgress(newProgress);
				}

				@Override
				public void onShowCustomView(View view, CustomViewCallback callback) {
					if(mCustomView != null){
						mLayout.removeView(mCustomView);
					}
					mCustomView = view;
					mCustomView.setBackgroundColor(Color.BLACK);
					mCustomView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
					mLayout.addView(mCustomView);
					
					mWebView.setVisibility(View.GONE);
					mCustomView.setVisibility(View.VISIBLE);
					mCustomViewCallback = callback;
				}

				@Override
				public void onHideCustomView() {
					mWebView.setVisibility(View.VISIBLE);
					if(mCustomViewCallback != null)
						mCustomViewCallback.onCustomViewHidden();
					if(mCustomView != null)
						mCustomView.setVisibility(View.GONE);
				}
			    
			};
			
			mWebView.setClients(mWebChromeClient, new SbWebView.SbWebViewClient(context,mWebView) {
				
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					mProgressBar.show();
					mProgressBar.setProgress(0);
					hideRemocon();
					setBtnGoImage(R.drawable.ic_remote_cancel);
					mIsPageLoading = true;
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					setSettingsPageUrl(url);
					setBtnGoImage(R.drawable.ic_remote_refresh);
					mIsPageLoading = false;
				}

			});
			
			mWebView.setAndroidBridge(new SbWebView.AndroidBridge(context){
				
				@SuppressWarnings("unused")
				public void showBtnset(){
					mHandler.post(new Runnable() {
						
						public void run() {
							showBtnsetWebview2Add3();
						}
					});
				}
				
				@SuppressWarnings("unused")
				public void saveValues(final String indexSet, final int windowWidth, final int windowHeight, final int selectedWidth, final int selectedHeight){
					mIndexSet = indexSet;
					mWindowWidth = windowWidth;
					mWindowHeight = windowHeight;
					mSelectedWidth = selectedWidth;
					mSelectedHeight = selectedHeight;
				}
				
				@SuppressWarnings("unused")
				public void showSource(final String source){
					mHandler.post(new Runnable() {
						
						public void run() {
							mSourceView.setSource(source);
						}
					});
				}
				
				@SuppressWarnings("unused")
				public void saveSource(final String source){
					mHandler.post(new Runnable() {
						
						public void run() {
							try{
								String html;
								if(source.matches(".*<meta.*charset=.*")){
									html = source.replaceAll("<meta.*charset=>$", "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
								}
								else{
									html = source.replace("<head>", "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
								}
								
								File dir = new File(Environment.getExternalStorageDirectory()+"/sbrowser");
								if(dir.exists() == false)
									dir.mkdirs();
								
								String filename = "sb_"+(new Date().getTime())+".html";
								File file = new File(Environment.getExternalStorageDirectory()+"/sbrowser", filename);
								FileWriter fw = new FileWriter(file);
								fw.write(html);
								fw.close();
								
								SbDb.getInstance(getApplicationContext()).insertSavedCont(getAddTitle(), filename, getFaviconData());
								
								Toast.makeText(getApplicationContext(), R.string.save_ok, Toast.LENGTH_SHORT).show();
							}catch(Exception e){
								Toast.makeText(getApplicationContext(), R.string.save_fail, Toast.LENGTH_SHORT).show();
							}
						}
					});
				}

				@Override
				public void showContent(final String cont) {
					mHandler.post(new Runnable() {
						
						public void run() {
							mContentView.setContent(cont);
						}
					});
				}
				
			});
			
			// 본문 읽기 관련 셋팅
			ContentView.adjustPref(getApplicationContext(), mContentView);
		}
		
		public SbWebView getWebView(){
			return mWebView;
		}
		
		public ContentView getContentView(){
			return mContentView;
		}
		
		public SourceView getSourceView(){
			return mSourceView;
		}
		
		public void loadByIntent(Intent intent){
			
			Uri uri = intent.getData();
			if(uri != null && "http".equals(uri.getScheme())){
				mWebView.loadUrl(uri.toString());
			}
			else{
				Bundle extras = intent.getExtras();
				if(extras != null){
					if(extras.containsKey("url")){
						
						String url = extras.getString("url");
						mWebView.loadUrl(url);
					}
					else if(extras.containsKey("saved_cont_id")){
						
						int savedContId = extras.getInt("saved_cont_id");
						SavedCont saved = SbDb.getInstance(getApplicationContext()).getSavedCont(savedContId);
						mWebView.loadLocalfile(saved.mFilename);
					}
				}
			}
		}
		
		public void hideLayoutWord(){
			mContentView.hideLayoutWord();
		}
		
		public boolean onBackPressed(){
			if(mContentView.isShowWebViewSearch()){
	    		mContentView.hideWebViewSearch();
	    		return true;
	    	}
	    	if(mCustomView != null && mCustomView.getVisibility() == View.VISIBLE){
	    		mWebChromeClient.onHideCustomView();
	    		return true;
	    	}
	    	if(mViewMode == VIEW_MODE_CONTENT || mViewMode == VIEW_MODE_SOURCE){
	    		WebActivity.this.btnViewModeWeb();
	    		return true;
	    	}
	    	if(mWebView.canGoBack()){
	    		mWebView.goBack();
	    		return true;
	    	}
	    	return false;
		}
		
		public void resetWebView(){
			if(mContentView.isShowWebViewSearch()){
	    		mContentView.hideWebViewSearch();
	    	}
	    	if(mCustomView != null && mCustomView.getVisibility() == View.VISIBLE){
	    		mWebChromeClient.onHideCustomView();
	    	}
	    	if(mViewMode == VIEW_MODE_CONTENT || mViewMode == VIEW_MODE_SOURCE){
	    		btnViewModeWeb();
	    	}
		}
		
		private byte[] getFaviconData(){
			Bitmap bm = mWebView.getFavicon();
			byte[] faviconData = null;
			if(bm != null){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bm.compress(CompressFormat.PNG, 100, bos);
				faviconData = bos.toByteArray();
			}
			return faviconData;
		}
		
		public void btnAddComplete(int addType) {
			if(addType == ADD_TYPE_SAVED_CONT){
				mWebView.injectSaveSource();  // 실제저장은 AndroidBridge 에서
				btnReset();
			}
			else{
				String url = mWebView.getOriginalUrl();
				String title = getAddTitle();
				byte[] faviconData = getFaviconData();
				
				if(addType == ADD_TYPE_PART){
					float ratio = getWindowWidth() / (float)mWindowWidth;
					int width = (int)(mSelectedWidth * ratio);
					int height = (int)(mSelectedHeight * ratio);
					String cookie = mWebView.getCookie();
					Log.i(TAG, "indexSet: "+mIndexSet+" , width: "+width+" , height:"+height+" , windowWidth: "+mWindowWidth+" , windowHeight:"+mWindowHeight+" , selectedWidth: "+mSelectedWidth+" , selectedHeight:"+mSelectedHeight);
					if(mIndexSet != null && width > 0 && height > 0){
						SbDb.getInstance(getApplicationContext()).insertFavoritePart(url, title, mIndexSet, width, height, faviconData, cookie);
						goHome();
						finish();
					}
				}
				else{
					SbDb.getInstance(getApplicationContext()).insertFavoriteUrl(url, title, faviconData);
					btnReset();
				}
				Toast.makeText(getApplicationContext(), R.string.favorite_add_ok, Toast.LENGTH_SHORT).show();
			}
		}
		
		public void btnViewModeWeb() {
			mContentView.setVisibility(View.GONE);
			mSourceView.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
			mViewMode = VIEW_MODE_WEBVIEW;
			
			mBtnViewModeWeb.setTextColor(0xffd05031);
			mBtnViewModeCont.setTextColor(0xff606261);
			mBtnViewModeSource.setTextColor(0xff606261);
		}

		public void btnViewModeCont() {
			mWebView.injectExtractMainCont();
			mContentView.setVisibility(View.VISIBLE);
			mSourceView.setVisibility(View.GONE);
			mWebView.setVisibility(View.GONE);
			mViewMode = VIEW_MODE_CONTENT;
			
			mBtnViewModeWeb.setTextColor(0xff606261);
			mBtnViewModeCont.setTextColor(0xffd05031);
			mBtnViewModeSource.setTextColor(0xff606261);
		}

		public void btnViewModeSource() {
			extractSource();
			mContentView.setVisibility(View.GONE);
			mSourceView.setVisibility(View.VISIBLE);
			mWebView.setVisibility(View.GONE);
			mViewMode = VIEW_MODE_SOURCE;
			
			mBtnViewModeWeb.setTextColor(0xff606261);
			mBtnViewModeCont.setTextColor(0xff606261);
			mBtnViewModeSource.setTextColor(0xffd05031);
		}
		
	}

}
