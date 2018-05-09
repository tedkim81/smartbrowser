package com.teuskim.sbrowser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class SbWebView : WebView {
    private var mContext: Context? = null
    private var mIsFunctionInjected = false

    val isPageFinished: Boolean
        get() = sIsPageFinished

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        webViewClient = SbWebViewClient(mContext!!)
    }

    @SuppressLint("AddJavascriptInterface")
    fun setAndroidBridge(androidBridge: AndroidBridge) {
        addJavascriptInterface(androidBridge, "sbrowser")
    }

    override fun loadUrl(url: String) {
        super.loadUrl(url)
        pageInit()
        MiscPref.getInstance(mContext!!).lastUrl = url
    }

    fun loadLocalfile(filename: String) {
        super.loadUrl("file:///" + Environment.getExternalStorageDirectory() + "/sbrowser/" + filename)
    }

    private fun injectJsFunctions() {
        if (mIsFunctionInjected)
            return

        val js = ("javascript:"
                + "var selectedObj;"
                + "var indexSet='';"
                + "function listenerClick(e){"
                + "setBorder(this);"
                + "e.stopPropagation();"
                + "e.returnValue = false;"
                + "window.sbrowser.showBtnset();"
                + "}"
                + "function setBorder(el){"
                + "if(typeof el != 'undefined' && typeof el.style != 'undefined'){"
                + "var temp = el;"
                + "var left = 0;"
                + "var top = 0;"
                + "if(temp.offsetParent){"
                + "do{"
                + "left += temp.offsetLeft;"
                + "top += temp.offsetTop;"
                + "}while(temp=temp.offsetParent);"
                + "}"
                + "selectedCover.style.width = (el.offsetWidth-4)+'px';"
                + "selectedCover.style.height = (el.offsetHeight-4)+'px';"
                + "selectedCover.style.left = left+'px';"
                + "selectedCover.style.top = top+'px';"
                + "selectedCover.style.display = 'block';"
                + "selectedObj = el;"
                + "}"
                + "}"
                + "function expand(){"
                + "if(typeof selectedObj.parentNode.tagName != 'undefined'){"
                + "var beforeW = selectedObj.offsetWidth;"
                + "var beforeH = selectedObj.offsetHeight;"
                + "setBorder(selectedObj.parentNode);"
                + "if(beforeW*1.2 > selectedObj.offsetWidth && beforeH*1.2 > selectedObj.offsetHeight)"
                + "expand();"
                + "}"
                + "}"
                + "function clip(){"
                + "hide(document.getElementsByTagName('body')[0].childNodes);"
                + "showParent(selectedObj);"
                + "showChild(selectedObj.childNodes);"
                + "window.sbrowser.saveValues(indexSet,window.innerWidth,window.innerHeight,selectedObj.offsetWidth,selectedObj.offsetHeight);"
                + "window.scrollTo(0,0);"
                + "}"
                + "function hide(el){"
                + "for(var i=0; i<el.length; i++){"
                + "if(typeof el[i].style != 'undefined')"
                + "el[i].style.display = 'none';"
                + "if(el[i].childNodes instanceof NodeList){"
                + "hide(el[i].childNodes);"
                + "}"
                + "}"
                + "}"
                + "function showChild(el){"
                + "for(var i=0; i<el.length; i++){"
                + "if(typeof el[i].style != 'undefined')"
                + "el[i].style.display = '';"
                + "if(el[i].childNodes instanceof NodeList)"
                + "showChild(el[i].childNodes);"
                + "}"
                + "}"
                + "function showParent(el){"
                + "if(typeof el.style != 'undefined')"
                + "el.style.display = '';"
                + "if((el instanceof HTMLBodyElement) == false && el.parentNode && typeof el.parentNode != 'undefined'){"
                + "indexSet = getIndex(el) + ',' + indexSet;"
                + "showParent(el.parentNode);"
                + "}"
                + "}"
                + "function getIndex(el){"
                + "for(var i=0; i<el.parentNode.childNodes.length; i++)"
                + "if(el.parentNode.childNodes[i] == el) return i;"
                + "}"
                + "function resetClick(el){"
                + "for(var i=0; i<el.length; i++){"
                + "if((el[i] instanceof Text) == false && (el[i] instanceof HTMLButtonElement) == false){"
                + "el[i].onclick = listenerClick;"
                + "if(el[i].childNodes instanceof NodeList)"
                + "resetClick(el[i].childNodes);"
                + "}"
                + "}"
                + "}"
                + "function redo(idxSet){"
                + "var currObj = document.getElementsByTagName('body')[0];"
                + "hide(currObj.childNodes);"
                + "var arr = idxSet.split(',');"
                + "for(var i=0; i<arr.length-1; i++){"
                + "currObj = currObj.childNodes[arr[i]];"
                + "currObj.style.display='';"
                + "}"
                + "showChild(currObj.childNodes);"
                + "}"
                + "function extractSource(){"
                + "window.sbrowser.showSource(document.getElementsByTagName('html')[0].outerHTML);"
                + "}"
                + "function saveSource(){"
                + "window.sbrowser.saveSource(document.getElementsByTagName('html')[0].outerHTML);"
                + "}"
                + "var mainContEl = null;"
                + "function extractMainContEl(el){"
                + "if(isAvailableElement(el)){"
                + "mainContEl = el;"
                + "for(var i=0; i<el.childNodes.length; i++){"
                + "if(isAvailableElement(el.childNodes[i])){"
                + "if(el.childNodes[i].innerText.length > el.innerText.length * 0.4){"
                + "extractMainContEl(el.childNodes[i]);"
                + "return;"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "function isAvailableElement(el){"
                + "if(typeof el.innerHTML != 'undefined' && typeof el.childNodes != 'undefined' && el instanceof HTMLScriptElement == false)"
                + "return true;"
                + "else return false;"
                + "}"
                + "function extractContent(){"
                + "extractMainContEl(document.getElementsByTagName('body')[0]);"
                + "var title = document.getElementsByTagName('title')[0].innerText;"
                + "var content = mainContEl.innerText.split('\\n\\n').join('\\n').split('\\n').join('\\n\\n');"
                + "window.sbrowser.showContent(title+'\\n\\n'+content);"
                + "}"
                + "var selectedCover = document.createElement('div');"
                + "selectedCover.setAttribute('style','position:absolute;background-color:rgba(208,80,49,0.3);border:2px solid #d05031;display:none;');"
                + "document.getElementsByTagName('body')[0].appendChild(selectedCover);")

        super.loadUrl(js)
        mIsFunctionInjected = true
    }

    fun injectJs(js: String) {
        super.loadUrl("javascript:" + js)
    }

    override fun reload() {
        super.reload()
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

    open class SbWebViewClient(protected var mContext: Context) : WebViewClient() {

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
