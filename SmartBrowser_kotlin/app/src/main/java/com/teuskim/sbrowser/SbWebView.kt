package com.teuskim.sbrowser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.PluginState
import android.widget.Toast
import java.io.InputStreamReader
import java.net.URL

class SbWebView : WebView {
    private var mContext: Context? = null
    private var mIsFunctionInjected = false
    private var mCookieManager: CookieManager? = null
    private val mHandler = Handler()

    val isPageFinished: Boolean
        get() = sIsPageFinished

    val cookie: String?
        get() {
            try {
                val url = URL(originalUrl)
                return mCookieManager!!.getCookie(url.protocol + "://" + url.host + "/")
            } catch (e: Exception) {
                return null
            }

        }

    private val injectedFunctions: String?
        get() {
            try {
                val am = mContext!!.assets
                val `is` = am.open("injected_functions.js")
                val isr = InputStreamReader(`is`)
                val buf = CharArray(10000)
                val readCnt = isr.read(buf)
                return String(buf, 0, readCnt)
            } catch (e: Exception) {
                return null
            }

        }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        setBackgroundColor(Color.WHITE)
        val settings = settings
        settings.javaScriptEnabled = true
        settings.cacheMode = WebSettings.LOAD_NORMAL
        settings.databaseEnabled = true
        val databasePath = mContext!!.getDir("database", Context.MODE_PRIVATE).path
        Log.i(TAG, "databasePath: " + databasePath)
        settings.databasePath = databasePath
        settings.domStorageEnabled = true
        settings.builtInZoomControls = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.savePassword = true
        settings.saveFormData = true
        settings.setGeolocationEnabled(true)
        settings.setGeolocationDatabasePath(databasePath)
        settings.pluginState = PluginState.ON

        mCookieManager = CookieManager.getInstance()
    }

    fun pageInit() {
        mIsFunctionInjected = false
    }

    fun setClients(webChromeClient: WebChromeClient, webViewClient: WebViewClient) {
        setWebChromeClient(webChromeClient)
        setWebViewClient(webViewClient)
    }

    fun setDefaultClients() {
        webChromeClient = SbWebChromeClient(mContext!!)
        webViewClient = SbWebViewClient(mContext!!, this)
    }

    @SuppressLint("AddJavascriptInterface")
    fun setAndroidBridge(androidBridge: AndroidBridge) {
        addJavascriptInterface(androidBridge, "sbrowser")
    }

    fun setCookie(urlStr: String, cookie: String?) {
        if (cookie == null)
            return

        try {
            val url = URL(urlStr)
            mCookieManager!!.setCookie(url.protocol + "://" + url.host + "/", cookie)
        } catch (e: Exception) {
        }

    }

    override fun loadUrl(url: String?) {
        if (url != null) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                super.loadUrl(url)
                pageInit()
                MiscPref.getInstance(mContext!!).lastUrl = url

                // 비디오 스트리밍이거나, 바이너리 파일인 경우에 대한 처리
                object : AsyncTask<Void, Void, String>() {

                    override fun doInBackground(vararg params: Void): String? {
                        try {
                            return URL(url).openConnection().contentType
                        } catch (e: Exception) {
                            Log.e(TAG, "ex", e)
                        }

                        return null
                    }

                    override fun onPostExecute(result: String?) {
                        if (result != null) {
                            if (result.contains("video")) {
                                val i = Intent(mContext, VideoActivity::class.java)
                                i.putExtra("url", url)
                                mContext!!.startActivity(i)
                            } else if (result == "application/octet-stream") {
                                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                i.addCategory(Intent.CATEGORY_BROWSABLE)
                                mContext!!.startActivity(i)
                            }
                        }
                    }

                }.execute()
            } else if (url.startsWith("file:///") || url == "about:blank")
                return
            else {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                i.addCategory(Intent.CATEGORY_BROWSABLE)
                mContext!!.startActivity(i)
            }
        }
    }

    fun loadLocalfile(filename: String) {
        super.loadUrl("file:///" + Environment.getExternalStorageDirectory() + "/sbrowser/" + filename)
    }

    private fun injectJsFunctions() {
        if (mIsFunctionInjected)
            return

        injectJs(injectedFunctions)
        mIsFunctionInjected = true
    }

    fun injectJs(js: String?) {
        super.loadUrl("javascript:" + js!!)
    }

    override fun reload() {
        super.reload()
        pageInit()
    }

    override fun goBack() {
        val beforeUrl = url
        super.goBack()
        pageInit()
        mHandler.postDelayed({ if (beforeUrl == url) goBackOrForward(-2) }, 500)
    }

    override fun goForward() {
        super.goForward()
        pageInit()
    }

    /**
     * 웹페이지 로드후 조각으로 만들기
     */
    fun injectRedo(indexSet: String) {
        injectJsFunctions()
        injectJs("redo('$indexSet');")
    }

    /**
     * 웹조각 영역 선택할때
     */
    fun injectResetClick() {
        injectJsFunctions()
        injectJs("resetClick(document.getElementsByTagName('body')[0].childNodes);")
    }

    /**
     * 웹조각 선택 영역 확장
     */
    fun injectExpand() {
        injectJs("expand();")
    }

    /**
     * 웹조각 영역선택후 자르기
     */
    fun injectClip() {
        injectJs("clip();")
    }

    /**
     * 소스보기
     */
    fun injectExtractSource() {
        injectJsFunctions()
        injectJs("extractSource();")
    }

    /**
     * 소스저장
     */
    fun injectSaveSource() {
        injectJsFunctions()
        injectJs("saveSource();")
    }

    /**
     * 본문 추출
     */
    fun injectExtractMainCont() {
        injectJsFunctions()
        injectJs("extractContent();")
    }

    open class SbWebChromeClient(private val mContext: Context) : WebChromeClient() {

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {

            try {
                AlertDialog.Builder(mContext)
                        .setTitle(R.string.notice)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        result.confirm()
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show()
            } catch (e: Exception) {
            }

            return true
        }
    }

    open class SbWebViewClient(protected var mContext: Context, protected var mWebView: SbWebView) : WebViewClient() {

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show()
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.i(TAG, "override url: " + url)
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            sIsPageFinished = false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            sIsPageFinished = true
        }

    }

    open class AndroidBridge(private val mContext: Context) {
        private val mHandler: Handler

        init {
            mHandler = Handler()
        }

        @JavascriptInterface
        fun alert(message: String) { // must be final
            mHandler.post { showDialog(message) }
        }

        private fun showDialog(message: String) {
            try {
                AlertDialog.Builder(mContext)
                        .setTitle(R.string.notice)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        dialog.dismiss()
                                    }
                                })
                        .setCancelable(true)
                        .create()
                        .show()
            } catch (e: Exception) {
                Log.e(TAG, "showDialog", e)
            }

        }

        @JavascriptInterface
        open fun showContent(cont: String) {
            // override 해야 한다.
        }

    }

    companion object {

        private val TAG = "SbWebView"
        private var sIsPageFinished = false
    }

}
