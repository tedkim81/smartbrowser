package com.teuskim.sbrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SbWebView extends WebView {
	
	private static final String TAG = "SbWebView";
	private Context mContext;
	private boolean mIsFunctionInjected = false;
	private static boolean sIsPageFinished = false;

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
	
	@SuppressLint("SetJavaScriptEnabled")
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
		setWebViewClient(new SbWebViewClient(mContext));
	}
	
	public void setAndroidBridge(AndroidBridge androidBridge){
		addJavascriptInterface(androidBridge, "sbrowser");
	}
	
	public boolean isPageFinished(){
		return sIsPageFinished;
	}
	
	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		pageInit();
		MiscPref.getInstance(mContext).setLastUrl(url);
	}
	
	public void loadLocalfile(String filename){
		super.loadUrl("file:///"+Environment.getExternalStorageDirectory()+"/sbrowser/"+filename);
	}

	private void injectJsFunctions(){
		if(mIsFunctionInjected)
			return;
		
		String js = "javascript:"
				+"var selectedObj;"
				+"var indexSet='';"
				+"function listenerClick(e){"
					+"setBorder(this);"
					+"e.stopPropagation();"
					+"e.returnValue = false;"
					+"window.sbrowser.showBtnset();"
				+"}"
				+"function setBorder(el){"
					+"if(typeof el != 'undefined' && typeof el.style != 'undefined'){"
						+"var temp = el;"
						+"var left = 0;"
						+"var top = 0;"
						+"if(temp.offsetParent){"
							+"do{"
								+"left += temp.offsetLeft;"
								+"top += temp.offsetTop;"
							+"}while(temp=temp.offsetParent);"
						+"}"
						+"selectedCover.style.width = (el.offsetWidth-4)+'px';"
						+"selectedCover.style.height = (el.offsetHeight-4)+'px';"
						+"selectedCover.style.left = left+'px';"
						+"selectedCover.style.top = top+'px';"
						+"selectedCover.style.display = 'block';"
						+"selectedObj = el;"
					+"}"
				+"}"
				+"function expand(){"
					+"if(typeof selectedObj.parentNode.tagName != 'undefined'){"
						+"var beforeW = selectedObj.offsetWidth;"
						+"var beforeH = selectedObj.offsetHeight;"
						+"setBorder(selectedObj.parentNode);"
						+"if(beforeW*1.2 > selectedObj.offsetWidth && beforeH*1.2 > selectedObj.offsetHeight)"
							+"expand();"
					+"}"
				+"}"
				+"function clip(){"
					+"hide(document.getElementsByTagName('body')[0].childNodes);"
					+"showParent(selectedObj);"
					+"showChild(selectedObj.childNodes);"
					+"window.sbrowser.saveValues(indexSet,window.innerWidth,window.innerHeight,selectedObj.offsetWidth,selectedObj.offsetHeight);"
					+"window.scrollTo(0,0);"
				+"}"
				+"function hide(el){"
					+"for(var i=0; i<el.length; i++){"
						+"if(typeof el[i].style != 'undefined')"
							+"el[i].style.display = 'none';"
						+"if(el[i].childNodes instanceof NodeList){"
							+"hide(el[i].childNodes);"
						+"}"
					+"}"
				+"}"
				+"function showChild(el){"
					+"for(var i=0; i<el.length; i++){"
						+"if(typeof el[i].style != 'undefined')"
							+"el[i].style.display = '';"
						+"if(el[i].childNodes instanceof NodeList)"
							+"showChild(el[i].childNodes);"
					+"}"
				+"}"
				+"function showParent(el){"
					+"if(typeof el.style != 'undefined')"
						+"el.style.display = '';"
					+"if((el instanceof HTMLBodyElement) == false && el.parentNode && typeof el.parentNode != 'undefined'){"
						+"indexSet = getIndex(el) + ',' + indexSet;"
						+"showParent(el.parentNode);"
					+"}"
				+"}"
				+"function getIndex(el){"
					+"for(var i=0; i<el.parentNode.childNodes.length; i++)"
						+"if(el.parentNode.childNodes[i] == el) return i;"
				+"}"
				+"function resetClick(el){"
					+"for(var i=0; i<el.length; i++){"
						+"if((el[i] instanceof Text) == false && (el[i] instanceof HTMLButtonElement) == false){"
							+"el[i].onclick = listenerClick;"
							+"if(el[i].childNodes instanceof NodeList)"
								+"resetClick(el[i].childNodes);"
						+"}"
					+"}"
				+"}"
				+"function redo(idxSet){"
					+"var currObj = document.getElementsByTagName('body')[0];"
					+"hide(currObj.childNodes);"
					+"var arr = idxSet.split(',');"
					+"for(var i=0; i<arr.length-1; i++){"
						+"currObj = currObj.childNodes[arr[i]];"
						+"currObj.style.display='';"
					+"}"
					+"showChild(currObj.childNodes);"
				+"}"
				+"function extractSource(){"
					+"window.sbrowser.showSource(document.getElementsByTagName('html')[0].outerHTML);"
				+"}"
				+"function saveSource(){"
					+"window.sbrowser.saveSource(document.getElementsByTagName('html')[0].outerHTML);"
				+"}"
				+"var mainContEl = null;"
				+"function extractMainContEl(el){"
					+"if(isAvailableElement(el)){"
						+"mainContEl = el;"
						+"for(var i=0; i<el.childNodes.length; i++){"
							+"if(isAvailableElement(el.childNodes[i])){"
								+"if(el.childNodes[i].innerText.length > el.innerText.length * 0.4){"
									+"extractMainContEl(el.childNodes[i]);"
									+"return;"
								+"}"
							+"}"
						+"}"
					+"}"
				+"}"
				+"function isAvailableElement(el){"
					+"if(typeof el.innerHTML != 'undefined' && typeof el.childNodes != 'undefined' && el instanceof HTMLScriptElement == false)"
						+"return true;"
					+"else return false;"
				+"}"
				+"function extractContent(){"
					+"extractMainContEl(document.getElementsByTagName('body')[0]);"
					+"var title = document.getElementsByTagName('title')[0].innerText;"
					+"var content = mainContEl.innerText.split('\\n\\n').join('\\n').split('\\n').join('\\n\\n');"
					+"window.sbrowser.showContent(title+'\\n\\n'+content);"
				+"}"
				+"var selectedCover = document.createElement('div');"
				+"selectedCover.setAttribute('style','position:absolute;background-color:rgba(208,80,49,0.3);border:2px solid #d05031;display:none;');"
				+"document.getElementsByTagName('body')[0].appendChild(selectedCover);";
				
		super.loadUrl(js);
		mIsFunctionInjected = true;
	}
	
	public void injectJs(String js){
		super.loadUrl("javascript:"+js);
	}
	
	@Override
	public void reload() {
		super.reload();
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
		
		public SbWebViewClient(Context context){
			mContext = context;
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
