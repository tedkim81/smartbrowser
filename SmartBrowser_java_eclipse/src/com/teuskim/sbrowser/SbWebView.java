package com.teuskim.sbrowser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SbWebView extends WebView {
	
	private static final String TAG = "SbWebView";
	private Context mContext;
	private boolean mIsFunctionInjected = false;
	private static boolean sIsPageFinished = false;
	private CookieManager mCookieManager;
	private Handler mHandler = new Handler();

	public SbWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SbWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SbWebView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mContext = context;
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setBackgroundColor(Color.WHITE);
		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NORMAL);
		settings.setDatabaseEnabled(true);
		String databasePath = mContext.getDir("database", Context.MODE_PRIVATE).getPath();
		Log.i(TAG, "databasePath: "+databasePath);
		settings.setDatabasePath(databasePath);
		settings.setDomStorageEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSavePassword(true);
		settings.setSaveFormData(true);
		settings.setGeolocationEnabled(true);
		settings.setGeolocationDatabasePath(databasePath);
		settings.setPluginState(PluginState.ON);
		
		mCookieManager = CookieManager.getInstance();
	}
	
	public void pageInit(){
		mIsFunctionInjected = false;
	}
	
	public void setClients(WebChromeClient webChromeClient, WebViewClient webViewClient){
		setWebChromeClient(webChromeClient);
		setWebViewClient(webViewClient);
	}
	
	public void setDefaultClients(){
		setWebChromeClient(new SbWebChromeClient(mContext));
		setWebViewClient(new SbWebViewClient(mContext,this));
	}
	
	public void setAndroidBridge(AndroidBridge androidBridge){
		addJavascriptInterface(androidBridge, "sbrowser");
	}
	
	public boolean isPageFinished(){
		return sIsPageFinished;
	}
	
	public String getCookie(){
		try{
			URL url = new URL(getOriginalUrl());
			return mCookieManager.getCookie(url.getProtocol() + "://" + url.getHost() + "/");
		}catch(Exception e){
			return null;
		}
	}
	
	public void setCookie(String urlStr, String cookie){
		if(cookie == null)
			return;
		
		try{
			URL url = new URL(urlStr);
			mCookieManager.setCookie(url.getProtocol() + "://" + url.getHost() + "/", cookie);
		}catch(Exception e){}
	}
	
	@Override
	public void loadUrl(final String url) {
		if(url != null){
			if(url.startsWith("http://") || url.startsWith("https://")){
				super.loadUrl(url);
				pageInit();
				MiscPref.getInstance(mContext).setLastUrl(url);
				
				// 비디오 스트리밍이거나, 바이너리 파일인 경우에 대한 처리
				new AsyncTask<Void, Void, String>() {

					@Override
					protected String doInBackground(Void... params) {
						try{
							String contType = new URL(url).openConnection().getContentType();
							return contType;
						}catch(Exception e){
							Log.e(TAG, "ex", e);
						}
						return null;
					}

					@Override
					protected void onPostExecute(String result) {
						if(result != null){
							if(result.contains("video")){
								Intent i = new Intent(mContext, VideoActivity.class);
								i.putExtra("url", url);
								mContext.startActivity(i);
							}
							else if(result.equals("application/octet-stream")){
								Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
								i.addCategory(Intent.CATEGORY_BROWSABLE);
								mContext.startActivity(i);
							}
						}
					}
					
				}.execute();
			}
			else if(url.startsWith("file:///") || url.equals("about:blank")) return;
			else{
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				i.addCategory(Intent.CATEGORY_BROWSABLE);
				mContext.startActivity(i);
			}
		}
	}
	
	public void loadLocalfile(String filename){
		super.loadUrl("file:///"+Environment.getExternalStorageDirectory()+"/sbrowser/"+filename);
	}

	private void injectJsFunctions(){
		if(mIsFunctionInjected)
			return;
		
		injectJs(getInjectedFunctions());
		mIsFunctionInjected = true;
	}
	
	private String getInjectedFunctions() {
		try{
			AssetManager am = mContext.getAssets();
			InputStream is = am.open("injected_functions.js");
			InputStreamReader isr = new InputStreamReader(is);
			char[] buf = new char[10000];
			int readCnt = isr.read(buf);
			return new String(buf, 0, readCnt);
		}catch(Exception e){
			return null;
		}
	}
	
	public void injectJs(String js){
		super.loadUrl("javascript:"+js);
	}
	
	@Override
	public void reload() {
		super.reload();
		pageInit();
	}

	@Override
	public void goBack() {
		final String beforeUrl = getUrl();
		super.goBack();
		pageInit();
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(beforeUrl.equals(getUrl())) goBackOrForward(-2);
			}
		}, 500);
	}

	@Override
	public void goForward() {
		super.goForward();
		pageInit();
	}

	/**
	 * 웹페이지 로드후 조각으로 만들기
	 */
	public void injectRedo(String indexSet){
		injectJsFunctions();
		injectJs("redo('"+indexSet+"');");
	}
	
	/**
	 * 웹조각 영역 선택할때
	 */
	public void injectResetClick(){
		injectJsFunctions();
		injectJs("resetClick(document.getElementsByTagName('body')[0].childNodes);");
	}
	
	/**
	 * 웹조각 선택 영역 확장
	 */
	public void injectExpand(){
		injectJs("expand();");
	}
	
	/**
	 * 웹조각 영역선택후 자르기
	 */
	public void injectClip(){
		injectJs("clip();");
	}
	
	/**
	 * 소스보기
	 */
	public void injectExtractSource(){
		injectJsFunctions();
		injectJs("extractSource();");
	}
	
	/**
	 * 소스저장
	 */
	public void injectSaveSource(){
		injectJsFunctions();
		injectJs("saveSource();");
	}
	
	/**
	 * 본문 추출
	 */
	public void injectExtractMainCont(){
		injectJsFunctions();
		injectJs("extractContent();");
	}
	
	public static class SbWebChromeClient extends WebChromeClient {
		
		private Context mContext;
		
		public SbWebChromeClient(Context context){
			mContext = context;
		}
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
	    	
			try{
				new AlertDialog.Builder(mContext)
	            .setTitle(R.string.notice)
	            .setMessage(message)
	            .setPositiveButton(android.R.string.ok,
	                    new AlertDialog.OnClickListener()
	                    {
	                        public void onClick(DialogInterface dialog, int which)
	                        {
	                            result.confirm();
	                        }
	                    })
	            .setCancelable(false)
	            .create()
	            .show();
			}catch(Exception e){}		        

	        return true;
	    }
	}
	
	public static class SbWebViewClient extends WebViewClient {
		
		protected Context mContext;
		protected SbWebView mWebView;
		
		public SbWebViewClient(Context context, SbWebView webView){
			mContext = context;
			mWebView = webView;
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "override url: "+url);
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			sIsPageFinished = false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			sIsPageFinished = true;
		}

	}
	
	public static class AndroidBridge {
		
		private Context mContext;
		private Handler mHandler;
		
		public AndroidBridge(Context context){
			mContext = context;
			mHandler = new Handler();
		}
		
		public void alert(final String message) { // must be final    
			mHandler.post(new Runnable() {
				
				public void run() {
					showDialog(message);
				}
			});
		}
		
		private void showDialog(String message){
			try{
				new AlertDialog.Builder(mContext)
	            .setTitle(R.string.notice)
	            .setMessage(message)
	            .setPositiveButton(android.R.string.ok,
	                    new AlertDialog.OnClickListener()
	                    {
	                        public void onClick(DialogInterface dialog, int which)
	                        {
	                            dialog.dismiss();
	                        }
	                    })
	            .setCancelable(true)
	            .create()
	            .show();
			}catch(Exception e){
				Log.e(TAG, "showDialog", e);
			}
		}
		
		public void showContent(String cont){
			// override 해야 한다.
		}
		
	}

}
