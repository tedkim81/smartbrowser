package com.teuskim.sbrowser;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.android.api.AsyncFacebookRunner;
import com.facebook.android.api.AsyncRequestListener;
import com.facebook.android.api.BaseRequestListener;
import com.facebook.android.api.DialogError;
import com.facebook.android.api.Facebook;
import com.facebook.android.api.Facebook.DialogListener;
import com.facebook.android.api.FacebookError;
import com.facebook.android.api.SessionEvents;
import com.facebook.android.api.SessionEvents.AuthListener;
import com.facebook.android.api.SessionEvents.LogoutListener;
import com.facebook.android.api.SessionStore;
import com.facebook.android.api.Util;

public class FacebookApi {

	private static final String APP_ID = "511166242228371";
	private static final String[] PERMISSIONS = new String[] {"publish_stream"};
	
	private Facebook mFb;
	private Activity mActivity;
	private MiscPref mPref;
	private Handler mHandler;
	private OnSessionChangeListener mSessionChangeListener;
	private AsyncRequestListener mAsyncRequestListener;
	private AsyncFacebookRunner mAsyncRunner;
	private ProgressDialog mProgressDialog;
	
	public FacebookApi(Activity activity, OnSessionChangeListener listener){

		init(activity);
		mSessionChangeListener = listener;
	}
	
	public FacebookApi(Activity activity, AsyncRequestListener listener){
		
		init(activity);
		mAsyncRequestListener = listener;
	}
	
	private void init(Activity activity){
		mFb = new Facebook(APP_ID);
		mActivity = activity;
		
		mPref = MiscPref.getInstance(mActivity);
		mHandler = new Handler();
		mAsyncRunner = new AsyncFacebookRunner(mFb);
		
		SessionStore.restore(mFb, mActivity);
		SessionEvents.addAuthListener(new FbAuthListener());
		SessionEvents.addLogoutListener(new FbLogoutListener());
	}
	
	public void facebookAuthorizeCallback(int requestCode, int resultCode, Intent data){
		mFb.authorizeCallback(requestCode, resultCode, data);
	}
	
	public boolean isLogged(){
		return mFb.isSessionValid();
	}
	
	public void login(){
		if(!mFb.isSessionValid())
			mFb.authorize(mActivity, PERMISSIONS, new LoginDialogListener());
	}
	
	public void logout(){
		if(mFb.isSessionValid()){
			SessionEvents.onLogoutBegin();
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
			asyncRunner.logout(mActivity.getBaseContext(), new LogoutRequestListener());
		}
	}
	
	public void write(String msg, String link){
		Bundle params = new Bundle();
        params.putString("message", msg);
        params.putString("link", link);
        
        String appName = mActivity.getString(R.string.app_name);
        if(link != null && link.contains(MainActivity.URL_SB)){
        	params.putString("name", appName);
        	params.putString("caption", mActivity.getString(R.string.sns_sbrowser_caption));
        }
        
        params.putString("actions", "{\"name\":\""+appName+"\",\"link\":\"https://play.google.com/store/apps/details?id=com.teuskim.sbrowser\"}");
        mAsyncRunner.request("me/feed", params, "POST", mAsyncRequestListener, null);
	}
	
	public interface OnSessionChangeListener {
		public void OnSessionChange(boolean isLogged);
	}
	
	public class FbAuthListener implements AuthListener {
		
		public void onAuthSucceed() {
			if(mSessionChangeListener != null)
				mSessionChangeListener.OnSessionChange(isLogged());
		}
		
		public void onAuthFail(String error) {
		}
	}
	
	private final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
			SessionStore.save(mFb, mActivity.getBaseContext());
			mAsyncRunner.request("me", new NameRequestListener());
			
			if(mSessionChangeListener != null)
				mSessionChangeListener.OnSessionChange(isLogged());
			
			Toast.makeText(mActivity, R.string.fb_login_ok, Toast.LENGTH_SHORT).show();
			try{
				mProgressDialog = ProgressDialog.show(mActivity, "", "Loading");
			}catch (Exception e) {}
		}
		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}
		
		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}
		
		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}
	
	public class FbLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			try{
				mProgressDialog.dismiss();
			}catch (Exception e) {}			
		}
		
		public void onLogoutFinish() {
			SessionStore.clear(mActivity.getBaseContext());
			if(mSessionChangeListener != null)
				mSessionChangeListener.OnSessionChange(isLogged());
			
			try{
				mProgressDialog.dismiss();
			}catch (Exception e) {}
			
		}
	}
	
	private class LogoutRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			mHandler.post(new Runnable() {
				public void run() {
					SessionEvents.onLogoutFinish();
					if(mSessionChangeListener != null)
						mSessionChangeListener.OnSessionChange(isLogged());
				}
			});
		}
	}
	
	public class NameRequestListener extends BaseRequestListener {
		
		public void onComplete(final String response, final Object state) {
			try{
				mProgressDialog.dismiss();
			}catch (Exception e) {}
			
			try {
				JSONObject json = Util.parseJson(response);
				final String name = json.getString("name");
				mPref.setFacebookId(name);
				
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						if(mSessionChangeListener != null)
							mSessionChangeListener.OnSessionChange(isLogged());
					}
				});
			} catch (JSONException e) {
			} catch (FacebookError e) {
			}
		}
	}
	
}
