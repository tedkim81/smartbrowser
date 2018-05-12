package com.teuskim.sbrowser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class FavoritePartView : LinearLayout {

    private var mTitleView: TextView? = null
    private var mBtnArticle: View? = null
    var webView: SbWebView? = null
        private set
    private var mLoadingWebView: TextView? = null
    private var mOnLoadRunnable: Runnable? = null
    private val mHandler = Handler()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.favorite_part_item, this)
        mTitleView = findViewById<TextView>(R.id.favorite_part_title)
        mBtnArticle = findViewById(R.id.btn_article)
        mLoadingWebView = findViewById<TextView>(R.id.loading_webview)
        webView = findViewById<SbWebView>(R.id.webview)
        webView!!.setClients(SbWebView.SbWebChromeClient(context), object : SbWebView.SbWebViewClient(context, webView!!) {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                mLoadingWebView!!.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (mOnLoadRunnable != null)
                    mOnLoadRunnable!!.run()

                mHandler.postDelayed({ mLoadingWebView!!.visibility = View.GONE }, 1000)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.i(TAG, "override url: " + url)
                if (mLoadingWebView!!.visibility == View.GONE) {
                    val i = Intent(mContext, WebActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    i.putExtra("url", url)
                    mContext.startActivity(i)
                }
                return true
            }

        })
    }

    fun setTitle(title: String) {
        mTitleView!!.text = title
    }

    fun setWebViewLayoutParams(lp: RelativeLayout.LayoutParams) {
        webView!!.layoutParams = lp
        mLoadingWebView!!.layoutParams = lp

    }

    fun setOnLoadRunnable(runnable: Runnable) {
        mOnLoadRunnable = runnable
    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        mBtnArticle!!.setOnClickListener(l)
    }

    companion object {

        private val TAG = "FavoritePartView"
    }

}
