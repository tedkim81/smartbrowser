package com.teuskim.sbrowser;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teuskim.sbrowser.FacebookApi.OnSessionChangeListener;

public abstract class BaseRemoconActivity extends Activity {
	
	private RemoconView mRemocon;
	private SettingsView mSettingsView;
	private TextView mBtnGoText;
	private ImageView mBtnGoImg;
	
	private DisplayMetrics mDisplayMetrics;
	private MiscPref mPref;
	private FacebookApi mFacebookApi;
	private KillReceiver mKillReceiver;
	
	private OnClickListener mListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.input_query_form:
				btnInputQueryForm();
				break;
			case R.id.btn_go:
				btnGo();
				break;
			}
		}
	};
	
	protected abstract void btnGo();
	protected abstract String getShareUrl();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPref = MiscPref.getInstance(getApplicationContext());
		mKillReceiver = new KillReceiver();
		IntentFilter ift = new IntentFilter();
		ift.addAction("sbrowser.kill");
		registerReceiver(mKillReceiver, ift);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mKillReceiver);
		super.onDestroy();
	}
	@Override
	public void setContentView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View parent = inflater.inflate(R.layout.base_remocon, null);
		inflater.inflate(layoutResID, (ViewGroup)parent.findViewById(R.id.body));
		super.setContentView(parent);
		
		mDisplayMetrics = getResources().getDisplayMetrics();
		
		mRemocon = (RemoconView) findViewById(R.id.remocon_view);
		findViewById(R.id.input_query_form).setOnClickListener(mListener);
		findViewById(R.id.btn_go).setOnClickListener(mListener);
		mBtnGoText = (TextView) findViewById(R.id.btn_go_text);
		mBtnGoImg = (ImageView) findViewById(R.id.btn_go_img);
		
		mSettingsView = (SettingsView) findViewById(R.id.settings_view);
		initSettingsView(mSettingsView);
	}
	
	@Override
	public void onBackPressed() {
		if(isShowSettingsView()){
    		hideSettingsView();
    		return;
    	}
   		super.onBackPressed();
	}
	
	protected void setRemoconBottomView(View remoconBottom){
		mRemocon.setBottomView(remoconBottom);
	}
	
	protected void setBtnGoImage(int resId){
		mBtnGoImg.setImageResource(resId);
		mBtnGoImg.setVisibility(View.VISIBLE);
		mBtnGoText.setVisibility(View.GONE);
	}
	
	protected void setBtnGoText(String text){
		mBtnGoText.setText(text);
		mBtnGoImg.setVisibility(View.GONE);
		mBtnGoText.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		setShareInfo();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if(hasFocus){
			initRemocon();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		initRemocon();
	}
	
	private void initRemocon(){
		// 리모콘 위치 초기화
		Rect rectgle= new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rectgle);
		mRemocon.setWindowSize(getWindowWidth(), getWindowHeight()-rectgle.top);
		mRemocon.initAlphaAndPosition();
	}
	
	protected void btnInputQueryForm(){
		Intent i = new Intent(getApplicationContext(), InputActivity.class);
		startActivity(i);
	}
	
	public void setOnBackClickListener(OnClickListener listener){
		mRemocon.setOnBackClickListener(listener);
	}
	
	public void setOnFowardClickListener(OnClickListener listener){
		mRemocon.setOnFowardClickListener(listener);
	}
	
	protected void btnSettings(){
		if(isShowSettingsView()){
			hideSettingsView();
		}
		else{
			showSettingsView();
		}
	}
	
	protected void initSettingsView(SettingsView settingsView){
		int width = (int)(mDisplayMetrics.densityDpi * 1.8);
		int cmpWidth = (int)(getWindowWidth()*0.9);
		if(width >= cmpWidth)
			width = cmpWidth;
		int height = (int)(mDisplayMetrics.densityDpi * 2.5);
		int cmpHeight = (int)(getWindowHeight()*0.9);
		if(height >= cmpHeight)
			height = cmpHeight;
		
		RelativeLayout.LayoutParams settingsLayoutParams = new RelativeLayout.LayoutParams(width, height);
		settingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		settingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		settingsView.setLayoutParams(settingsLayoutParams);
		settingsView.setRemoconAlpha(mPref.getRemoconAlpha());
		settingsView.setOnAlphaChangedListener(new SettingsView.OnAlphaChangedListener() {
			
			@Override
			public void onAlphaChanged(int alpha) {
				mRemocon.setRemoconAlpha(alpha);
				mRemocon.hideRemocon();
			}
		});
	}
	
	protected void showRemocon(){
		mRemocon.showRemocon();
	}
	
	protected void hideRemocon(){
		mRemocon.hideRemocon();
	}
	
	protected int getRemoconWidth(){
		return mRemocon.getRemoconWidth();
	}
	
	protected boolean isShowSettingsView(){
		return (mSettingsView.getVisibility() == View.VISIBLE);
	}
	
	protected void hideSettingsView(){
		mSettingsView.close();
	}
	
	private void showSettingsView(){
		mSettingsView.open();
		mRemocon.hideRemocon();
	}
	
	protected void setSettingsPageUrl(String url){
		mSettingsView.setPageUrl(url);
	}
	
	protected int getWindowWidth(){
		return mDisplayMetrics.widthPixels;
	}
	
	protected int getWindowHeight(){
		return mDisplayMetrics.heightPixels;
	}
	
	protected int getPixelFromDip(int dip){
		return (int)(dip*mDisplayMetrics.density+0.5);
	}
	
	protected void showCVMSettings(boolean isShow){
		mSettingsView.showCVMSettings(isShow);
	}
	
	protected SettingsView getSettingsView(){
		return mSettingsView;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(mFacebookApi != null){
			mFacebookApi.facebookAuthorizeCallback(requestCode, resultCode, data);
			mFacebookApi = null;
		}
	}
	private void setShareInfo(){
		if(mFacebookApi == null){
			mFacebookApi = new FacebookApi(BaseRemoconActivity.this, new OnSessionChangeListener(){
				@Override
				public void OnSessionChange(boolean isLogged){
					if(isLogged){
						Intent i = new Intent(getApplicationContext(), ShareSNSActivity.class);
						i.putExtra(ShareSNSActivity.KEY_SNS, ShareSNSActivity.SNS_FACEBOOK);
						i.putExtra(ShareSNSActivity.KEY_URL, getShareUrl());
						startActivity(i);
					}
				}
			});
		}
		setSettingsPageUrl(getShareUrl());
		SettingsView settingsView = getSettingsView();
		settingsView.setFacebookClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mPref.getFacebookId() == null){
					mFacebookApi.login();
				}
				else{
					Intent i = new Intent(getApplicationContext(), ShareSNSActivity.class);
					i.putExtra(ShareSNSActivity.KEY_SNS, ShareSNSActivity.SNS_FACEBOOK);
					i.putExtra(ShareSNSActivity.KEY_URL, getShareUrl());
					startActivity(i);
				}
			}
		});
		
		if(hasKakao()){
			settingsView.setKakaoClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					shareKakao();
				}
			});
		}
	}
	
	private void shareKakao(){
		Intent i = new Intent(getApplicationContext(), ShareSNSActivity.class);
		i.putExtra(ShareSNSActivity.KEY_SNS, ShareSNSActivity.SNS_KAKAO);
		i.putExtra(ShareSNSActivity.KEY_URL, getShareUrl());
		startActivity(i);
	}
	
	private boolean hasKakao(){
		
		Intent i = new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://sendurl"));
		final PackageManager packageManager = getPackageManager();
	    List<ResolveInfo> list = packageManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
	    
	    if(list.size() > 0 )
	    	return true;
	    
	    return false;
	}
	
	private class KillReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
		
	}
	
}
