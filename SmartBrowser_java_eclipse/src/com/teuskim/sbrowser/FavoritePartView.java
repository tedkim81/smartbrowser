package com.teuskim.sbrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FavoritePartView extends LinearLayout {
	
	private static final String TAG = "FavoritePartView";
	
	private TextView mTitleView;
	private View mBtnArticle;
	private SbWebView mWebView;
	private TextView mLoadingWebView;
	private Runnable mOnLoadRunnable;
	private Handler mHandler = new Handler();

	public FavoritePartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FavoritePartView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.favorite_part_item, this);
		mTitleView = (TextView) findViewById(R.id.favorite_part_title);
		mBtnArticle = findViewById(R.id.btn_article);
		mLoadingWebView = (TextView) findViewById(R.id.loading_webview);
		mWebView = (SbWebView) findViewById(R.id.webview);
		mWebView.setClients(new SbWebView.SbWebChromeClient(context), new SbWebView.SbWebViewClient(context,mWebView){
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mLoadingWebView.setVisibility(VISIBLE);
				super.onPageStarted(view, url, favicon);
			}
	
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if(mOnLoadRunnable != null)
					mOnLoadRunnable.run();
				
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mLoadingWebView.setVisibility(GONE);
					}
				}, 1000);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, "override url: "+url);
				if(mLoadingWebView.getVisibility() == GONE){
					Intent i = new Intent(mContext, WebActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("url", url);
					mContext.startActivity(i);
				}
				return true;
			}
			
		});
	}
	
	public void setTitle(String title){
		mTitleView.setText(title);
	}
	
	public SbWebView getWebView(){
		return mWebView;
	}
	
	public void setWebViewLayoutParams(RelativeLayout.LayoutParams lp){
		mWebView.setLayoutParams(lp);
		mLoadingWebView.setLayoutParams(lp);
		
	}
	
	public void setOnLoadRunnable(Runnable runnable){
		mOnLoadRunnable = runnable;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mBtnArticle.setOnClickListener(l);
	}
	
}
