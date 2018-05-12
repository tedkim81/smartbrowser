package com.teuskim.sbrowser;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class MiscPref {
	
	private static MiscPref sInstance;
	
	private static final String KEY_MYPAGE = "mypage";
	private static final String KEY_REMOCON_LEFT = "remoconleft";
	private static final String KEY_REMOCON_BOTTOM = "remoconbottom";
	private static final String KEY_REMOCON_ALPHA = "remoconalpha";
	private static final String KEY_CONT_BG_COLOR = "contbgcolor";
	private static final String KEY_CONT_FONT_COLOR = "contfontcolor1";
	private static final String KEY_CONT_FONT_SIZE = "contfontsize";
	private static final String KEY_CONT_LINE_SPACE = "contlinespace";
	private static final String KEY_CONT_PADDING = "contpadding";
	private static final String KEY_LAST_URL = "lasturl";
	
	public static final int INIT_REMOCON_ALPHA = 130;
	public static final int INIT_CONT_BG_COLOR = SettingsView.PATTERN_0;
	public static final int INIT_CONT_FONT_COLOR = 0xff000000;
	public static final int INIT_CONT_FONT_SIZE = 20;
	public static final int INIT_CONT_LINE_SPACE = 5;
	public static final int INIT_CONT_PADDING = 20;

	private SharedPreferences mPref;
	private SharedPreferences.Editor mEditor;
	
	private MiscPref(Context context){
		mPref = context.getSharedPreferences("miscpref", 0);
		mEditor = mPref.edit();
	}
	
	public static synchronized MiscPref getInstance(Context context){
		if(sInstance == null){
			sInstance = new MiscPref(context);
		}
		return sInstance;
	}
	
	public void setMypage(String mypage){
		mEditor.putString(KEY_MYPAGE, mypage);
		mEditor.commit();
	}
	
	public String getMypage(){
		return mPref.getString(KEY_MYPAGE, null);
	}
	
	public void setRemoconLeft(int left){
		mEditor.putInt(KEY_REMOCON_LEFT, left);
		mEditor.commit();
	}
	
	public int getRemoconLeft(){
		return mPref.getInt(KEY_REMOCON_LEFT, Integer.MIN_VALUE);
	}
	
	public void setRemoconBottom(int bottom){
		mEditor.putInt(KEY_REMOCON_BOTTOM, bottom);
		mEditor.commit();
	}
	
	public int getRemoconBottom(){
		return mPref.getInt(KEY_REMOCON_BOTTOM, Integer.MIN_VALUE);
	}
	
	public void setRemoconAlpha(int alpha){
		mEditor.putInt(KEY_REMOCON_ALPHA, alpha);
		mEditor.commit();
	}
	
	public int getRemoconAlpha(){
		return mPref.getInt(KEY_REMOCON_ALPHA, INIT_REMOCON_ALPHA);
	}
	
	public void setContBgColor(int color){
		mEditor.putInt(KEY_CONT_BG_COLOR, color);
		mEditor.commit();
	}
	
	public int getContBgColor(){
		return mPref.getInt(KEY_CONT_BG_COLOR, INIT_CONT_BG_COLOR);
	}
	
	public void setContFontColor(int color){
		mEditor.putInt(KEY_CONT_FONT_COLOR, color);
		mEditor.commit();
	}
	
	public int getContFontColor(){
		return mPref.getInt(KEY_CONT_FONT_COLOR, INIT_CONT_FONT_COLOR);
	}
	
	public void setContFontSize(int size){
		mEditor.putInt(KEY_CONT_FONT_SIZE, size);
		mEditor.commit();
	}
	
	public int getContFontSize(){
		return mPref.getInt(KEY_CONT_FONT_SIZE, INIT_CONT_FONT_SIZE);
	}
	
	public void setContLineSpace(int space){
		mEditor.putInt(KEY_CONT_LINE_SPACE, space);
		mEditor.commit();
	}
	
	public int getContLineSpace(){
		return mPref.getInt(KEY_CONT_LINE_SPACE, INIT_CONT_LINE_SPACE);
	}
	
	public void setContPadding(int padding){
		mEditor.putInt(KEY_CONT_PADDING, padding);
		mEditor.commit();
	}
	
	public int getContPadding(){
		return mPref.getInt(KEY_CONT_PADDING, INIT_CONT_PADDING);
	}
	
	public void setLastUrl(String url){
		mEditor.putString(KEY_LAST_URL, url);
		mEditor.commit();
	}
	
	public String getLastUrl(){
		return mPref.getString(KEY_LAST_URL, null);
	}
	
	/*
	 * SNS 관련 부분은 일단 코딩후 다시 다듬어야 한다. 시간이 부족해서..
	 */
	
	public boolean setFacebookId(String facebookId){
		mEditor.putString("facebook_usrid", facebookId);
		return mEditor.commit();
	}
	
	public String getFacebookId(){
		return mPref.getString("facebook_usrid", null);	
	}
	
	private String checkNull(String str){
		if(str != null && str.equals("null") == false)
			return str;
		else
			return null;
	}
	
	public boolean setFbToken(String patamFbToken){
		mEditor.putString("fb_token", checkNull(patamFbToken));
		
		return mEditor.commit();
	}
	
	public String getFbToken(){
		return mPref.getString("fb_token", null);
	}
	
	public void saveTwitter(String usrid, String passwd, AccessToken token){
		
		if( TextUtils.isEmpty(usrid) || TextUtils.isEmpty(passwd) ){
			mEditor.remove("twitter_usrid");
			mEditor.remove("twitter_passwd");
			mEditor.remove("twitter_token");
			mEditor.remove("twitter_token_secret");
		}else{
			mEditor.putString("twitter_usrid", usrid);
			mEditor.putString("twitter_passwd", passwd);
			mEditor.putString("twitter_token", token.getToken() );
			mEditor.putString("twitter_token_secret", token.getTokenSecret());
		}
		
		mEditor.commit();
	}
	
	public String getTwitterId(){
		return mPref.getString("twitter_usrid", null);	
	}
	
	public String getTwitterToken(){
		return mPref.getString("twitter_token", null);
	}
	
	public String getTwitterTokenSecret(){
		return mPref.getString("twitter_token_secret", null);
	}
}
