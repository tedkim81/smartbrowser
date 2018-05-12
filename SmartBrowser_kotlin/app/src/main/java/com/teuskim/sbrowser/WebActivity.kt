package com.teuskim.sbrowser

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.webkit.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.*
import com.teuskim.sbrowser.ColorPickerView.ColorChange
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.util.*

class WebActivity : BaseRemoconActivity() {
    protected var mViewMode = VIEW_MODE_WEBVIEW
    private var mAddType = ADD_TYPE_FAVOR

    private var mWebLayout: RelativeLayout? = null
    private var mScreenPageLayout: LinearLayout? = null

    private var mBtnSettings: RemoconMenuView? = null
    private var mBtnHome: RemoconMenuView? = null
    private var mBtnAdd: RemoconMenuView? = null
    private var mBtnViewMode: RemoconMenuView? = null
    private var mBtnScreen: RemoconMenuView? = null
    private var mBtnsetWebview1: View? = null  // webview 1depth
    private var mArrowRemocon: ImageView? = null
    private var mBtnsetWebview2Add: View? = null  // webview 2depth add
    private var mBtnsetWebview2Add1: View? = null  // webview 2depth add and 1
    private var mBtnAddFavor: View? = null
    private var mBtnAddPart: View? = null
    private var mBtnAddSavedCont: View? = null
    private var mBtnsetWebview2Add2: View? = null  // webview 2depth add and 2
    private var mBtnsetWebview2Add3: View? = null  // webview 2depth add and 3
    private var mBtnExpandPart: View? = null
    private var mBtnAddPartComplete: View? = null
    private var mBtnAddPartReset1: View? = null
    private var mBtnAddPartReset2: View? = null
    private var mBtnsetWebview2Add4: View? = null  // webview 2depth add and 4
    private var mInputAddTitle: EditText? = null
    private var mBtnAddComplete: View? = null
    private var mBtnAddCancel: View? = null
    private var mBtnsetWebview2ViewMode: View? = null  // webview 2depth view mode
    private var mBtnViewModeWeb: Button? = null
    private var mBtnViewModeCont: Button? = null  // content view mode only
    private var mBtnViewModeSource: Button? = null
    private var mBtnsetWebview2Screen: View? = null  // webview 2depth screen

    private var mWebWrapList: MutableList<WebWrap>? = null
    private var mCurrentWebIndex: Int = 0
    private var mIsDeletePage: Boolean = false
    private var mInflater: LayoutInflater? = null
    private var mPref: MiscPref? = null
    private var mIsPageLoading: Boolean = false
    private val mHandler = Handler()

    private val mScreenReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {

                val i = Intent(context, LockscreenActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (mViewMode == VIEW_MODE_SOURCE)
                    i.putExtra("view_mode", VIEW_MODE_WEBVIEW)
                else
                    i.putExtra("view_mode", mViewMode)

                when (mViewMode) {
                    VIEW_MODE_WEBVIEW, VIEW_MODE_SOURCE -> {
                        i.putExtra("url", currentWeb.webView!!.originalUrl)
                        i.putExtra("scrolltop", currentWeb.webView!!.scrollY)
                    }
                    VIEW_MODE_CONTENT -> {
                        i.putExtra("content", currentWeb.contentView!!.content)
                        i.putExtra("scrolltop", currentWeb.contentView!!.scrollTop)
                    }
                }

                val pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
                try {
                    pi.send()
                } catch (e: CanceledException) {
                    Log.e(TAG, "lock screen fail", e)
                }

            }
        }
    }

    private val currentWeb: WebWrap
        get() = mWebWrapList!![mCurrentWebIndex]

    protected val addTitle: String
        get() = mInputAddTitle!!.text.toString()

    protected override val shareUrl: String
        get() = currentWeb.webView!!.originalUrl

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        mInflater = LayoutInflater.from(applicationContext)
        mPref = MiscPref.getInstance(applicationContext)
        window.requestFeature(Window.FEATURE_PROGRESS)
        setContentView(R.layout.web)
        findViews()

        mWebWrapList = ArrayList()
        addWebView(null)

        WebIconDatabase.getInstance().open(getDir("icons", Context.MODE_PRIVATE).path)
        if (savedInstanceState != null)
            currentWeb.webView!!.restoreState(savedInstanceState)
        else
            currentWeb.loadByIntent(intent)

        CookieSyncManager.createInstance(this)
    }

    protected fun findViews() {
        mWebLayout = findViewById<View>(R.id.web_layout) as RelativeLayout

        val remoconBottom = mInflater!!.inflate(R.layout.remocon_bottom_webview, null)
        setRemoconBottomView(remoconBottom)

        mBtnSettings = remoconBottom.findViewById<View>(R.id.btn_settings) as RemoconMenuView
        mBtnHome = remoconBottom.findViewById<View>(R.id.btn_home) as RemoconMenuView
        mBtnAdd = remoconBottom.findViewById<View>(R.id.btn_add) as RemoconMenuView
        mBtnViewMode = remoconBottom.findViewById<View>(R.id.btn_view_mode) as RemoconMenuView
        mBtnScreen = remoconBottom.findViewById<View>(R.id.btn_screen) as RemoconMenuView
        mBtnsetWebview1 = remoconBottom.findViewById(R.id.btnset_webview_1)
        mBtnHome = remoconBottom.findViewById<View>(R.id.btn_home) as RemoconMenuView
        mArrowRemocon = remoconBottom.findViewById<View>(R.id.arrow_remocon) as ImageView
        mBtnAdd = remoconBottom.findViewById<View>(R.id.btn_add) as RemoconMenuView
        mBtnViewMode = remoconBottom.findViewById<View>(R.id.btn_view_mode) as RemoconMenuView
        mBtnScreen = remoconBottom.findViewById<View>(R.id.btn_screen) as RemoconMenuView
        mBtnsetWebview2Add = remoconBottom.findViewById(R.id.btnset_webview_2_add)
        mBtnsetWebview2Add1 = remoconBottom.findViewById(R.id.btnset_webview_2_add_1)
        mBtnAddFavor = remoconBottom.findViewById(R.id.btn_add_favor)
        mBtnAddPart = remoconBottom.findViewById(R.id.btn_add_part)
        mBtnAddSavedCont = remoconBottom.findViewById(R.id.btn_save)
        mBtnsetWebview2Add2 = remoconBottom.findViewById(R.id.btnset_webview_2_add_2)
        mBtnsetWebview2Add3 = remoconBottom.findViewById(R.id.btnset_webview_2_add_3)
        mBtnExpandPart = remoconBottom.findViewById(R.id.btn_expand_part)
        mBtnAddPartComplete = remoconBottom.findViewById(R.id.btn_add_part_complete)
        mBtnAddPartReset1 = remoconBottom.findViewById(R.id.btn_add_part_reset_1)
        mBtnAddPartReset2 = remoconBottom.findViewById(R.id.btn_add_part_reset_2)
        mBtnsetWebview2Add4 = remoconBottom.findViewById(R.id.btnset_webview_2_add_4)
        mInputAddTitle = remoconBottom.findViewById<View>(R.id.input_add_title) as EditText
        mBtnAddComplete = remoconBottom.findViewById(R.id.btn_add_complete)
        mBtnAddCancel = remoconBottom.findViewById(R.id.btn_add_cancel)
        mBtnsetWebview2ViewMode = remoconBottom.findViewById(R.id.btnset_webview_2_view_mode)
        mBtnViewModeWeb = remoconBottom.findViewById<View>(R.id.btn_view_mode_web) as Button
        mBtnViewModeCont = remoconBottom.findViewById<View>(R.id.btn_view_mode_cont) as Button
        mBtnViewModeSource = remoconBottom.findViewById<View>(R.id.btn_view_mode_source) as Button
        mBtnsetWebview2Screen = remoconBottom.findViewById(R.id.btnset_webview_2_screen)
        mScreenPageLayout = remoconBottom.findViewById<View>(R.id.screen_page_layout) as LinearLayout

        mBtnSettings!!.setIconAndTitle(R.drawable.ic_menu_settings, getString(R.string.settings_short))
        mBtnHome!!.setIconAndTitle(R.drawable.ic_menu_home, getString(R.string.home))
        mBtnAdd!!.setIconAndTitle(R.drawable.ic_menu_add, getString(R.string.add))
        mBtnViewMode!!.setIconAndTitle(R.drawable.ic_menu_view, getString(R.string.view))
        mBtnScreen!!.setIconAndTitle(R.drawable.ic_menu_more, getString(R.string.screen))

        val remoconMenuListener = OnClickListener { v ->
            when (v.id) {
                R.id.btn_settings -> btnSettings()
                R.id.btn_home -> {
                    goHome()
                    return@OnClickListener
                }
                R.id.btn_add -> btnAdd()
                R.id.btn_view_mode -> btnViewMode()
                R.id.btn_screen -> btnScreen()
                R.id.btn_add_favor -> {
                    mAddType = ADD_TYPE_FAVOR
                    btnAddFavor()
                }
                R.id.btn_add_part -> {
                    mAddType = ADD_TYPE_PART
                    btnAddPart()
                }
                R.id.btn_save -> {
                    mAddType = ADD_TYPE_SAVED_CONT
                    btnAddFavor()  // 즐찾과 동일한 플로우
                }
                R.id.btn_expand_part -> btnExpandPart()
                R.id.btn_add_part_complete -> btnAddPartComplete()
                R.id.btn_add_part_reset_1, R.id.btn_add_part_reset_2 -> btnAddPartReset()
                R.id.btn_add_complete -> btnAddComplete(mAddType)
                R.id.btn_add_cancel -> btnAddCancel()
                R.id.btn_view_mode_web -> btnViewModeWeb()
                R.id.btn_view_mode_cont -> btnViewModeCont()
                R.id.btn_view_mode_source -> btnViewModeSource()
                R.id.btn_screen_add -> addWebView("http://www.google.com")
            }
        }
        mBtnSettings!!.setOnClickListener(remoconMenuListener)
        mBtnHome!!.setOnClickListener(remoconMenuListener)
        mBtnAdd!!.setOnClickListener(remoconMenuListener)
        mBtnViewMode!!.setOnClickListener(remoconMenuListener)
        mBtnScreen!!.setOnClickListener(remoconMenuListener)
        mBtnAddFavor!!.setOnClickListener(remoconMenuListener)
        mBtnAddPart!!.setOnClickListener(remoconMenuListener)
        mBtnAddSavedCont!!.setOnClickListener(remoconMenuListener)
        mBtnExpandPart!!.setOnClickListener(remoconMenuListener)
        mBtnAddPartComplete!!.setOnClickListener(remoconMenuListener)
        mBtnAddPartReset1!!.setOnClickListener(remoconMenuListener)
        mBtnAddPartReset2!!.setOnClickListener(remoconMenuListener)
        mBtnAddComplete!!.setOnClickListener(remoconMenuListener)
        mBtnAddCancel!!.setOnClickListener(remoconMenuListener)
        mBtnViewModeWeb!!.setOnClickListener(remoconMenuListener)
        mBtnViewModeCont!!.setOnClickListener(remoconMenuListener)
        mBtnViewModeSource!!.setOnClickListener(remoconMenuListener)
        findViewById<View>(R.id.btn_screen_add).setOnClickListener(remoconMenuListener)
    }

    override fun initSettingsView(settingsView: SettingsView?) {
        super.initSettingsView(settingsView)

        settingsView!!.setOnColorChangedListener(
                object: ColorPickerView.OnColorChangedListener {
                    override fun colorChanged(colorChange: ColorChange, color: Int) {
                        onContentViewBgChanged(colorChange, color)
                    }
                },
                object: ColorPickerView.OnColorChangedListener {
                    override fun colorChanged(colorChange: ColorChange, color: Int) {
                        onContentViewFontChanged(color)
                        settingsView.setFontColor(color)
                        mPref!!.contFontColor = color
                        hideColorPickerView(colorChange)
                    }
                }
        )

        settingsView.setContBg(mPref!!.contBgColor)
        settingsView.setFontColor(mPref!!.contFontColor)
        settingsView.setFontSize(mPref!!.contFontSize)
        settingsView.setOnFontSizeChangedListener(object: SettingsView.OnFontSizeChangedListener {
            override fun onFontSizeChanged(size: Int) {
                onContentViewFontSizeChanged(size)
                mPref!!.contFontSize = size
            }
        })
        settingsView.setLineSpace(mPref!!.contLineSpace)
        settingsView.setOnLineSpaceChangedListener(object: SettingsView.OnLineSpaceChangedListener {
            override fun onLineSpaceChanged(space: Int) {
                onContentViewLineSpaceChanged(space)
                mPref!!.contLineSpace = space
            }
        })

        settingsView.setContPadding(mPref!!.contPadding)
        settingsView.setOnPaddingChangedListener(object: SettingsView.OnPaddingChangedListener {
            override fun onPaddingChanged(padding: Int) {
                onContentViewPaddingChanged(padding)
                mPref!!.contPadding = padding
            }
        })

        if (mViewMode == VIEW_MODE_CONTENT) {
            settingsView.showCVMSettings(true)
        } else {
            settingsView.showCVMSettings(false)
        }

        settingsView.setOnCloseClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                mBtnSettings!!.isSelected = false
            }
        })
    }

    private fun addWebView(startUrl: String?) {
        val web = WebWrap(this)
        mWebLayout!!.addView(web)
        mWebWrapList!!.add(web)
        mCurrentWebIndex = mWebWrapList!!.size - 1

        val btnPage = TextView(this)
        btnPage.gravity = Gravity.CENTER
        btnPage.text = "" + (mCurrentWebIndex + 1)
        btnPage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        btnPage.setTextColor(-0x9f9d9f)
        btnPage.setTypeface(null, Typeface.BOLD)
        btnPage.setBackgroundResource(R.drawable.btn_remote_expand_below2)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        btnPage.layoutParams = lp
        btnPage.tag = mCurrentWebIndex
        btnPage.setOnClickListener { v ->
            val webIndex = v.tag as Int
            if (webIndex == mCurrentWebIndex) {
                if (mIsDeletePage) {
                    deleteWebView(webIndex)
                } else {
                    mIsDeletePage = true
                    (v as TextView).text = "X"
                }
            } else {
                showWebView(webIndex, startUrl)
            }
        }

        mScreenPageLayout!!.addView(btnPage)
        showWebView(mCurrentWebIndex, startUrl)
    }

    private fun showWebView(webIndex: Int, startUrl: String?) {
        mCurrentWebIndex = webIndex
        mIsDeletePage = false

        val childCount = mWebLayout!!.childCount
        for (i in 0 until childCount) {
            mWebLayout!!.getChildAt(i).visibility = View.GONE
        }
        mWebLayout!!.getChildAt(mCurrentWebIndex).visibility = View.VISIBLE

        val currentUrl = currentWeb.webView!!.originalUrl
        if (currentUrl == null && startUrl != null)
            currentWeb.webView!!.loadUrl(startUrl)

        val pageChildCount = mScreenPageLayout!!.childCount
        for (i in 0 until pageChildCount) {
            val btn = mScreenPageLayout!!.getChildAt(i) as TextView
            btn.text = "" + (i + 1)
            btn.setTextColor(-0x9f9d9f)
            btn.tag = i
        }
        (mScreenPageLayout!!.getChildAt(mCurrentWebIndex) as TextView).setTextColor(-0x11aed8)
    }

    private fun deleteWebView(webIndex: Int) {
        if (mWebWrapList!!.size > 1) {
            mWebWrapList!!.removeAt(webIndex)
            mWebLayout!!.removeViewAt(webIndex)
            mScreenPageLayout!!.removeViewAt(webIndex)
            showWebView(0, null)
        } else {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentWeb.webView!!.saveState(outState)
    }

    override fun onStart() {
        super.onStart()

        setOnBackClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                hideSettingsView()
                currentWeb.resetWebView()
                val webView = currentWeb.webView
                if (webView!!.canGoBack()) {
                    webView.goBack()
                } else {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    intent.putExtra("next_url", webView.originalUrl)
                    startActivity(intent)
                }
            }
        })

        setOnFowardClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                hideSettingsView()
                currentWeb.resetWebView()
                val webView = currentWeb.webView
                if (webView!!.canGoForward()) {
                    webView.goForward()
                } else {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    intent.putExtra("prev_url", webView.originalUrl)
                    startActivity(intent)
                }
            }
        })

        // screen off 될때 lock screen 띄우기 위해
        val screenFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mScreenReceiver, screenFilter)
    }

    override fun onResume() {
        super.onResume()
        CookieSyncManager.getInstance().startSync()
    }

    override fun onPause() {
        super.onPause()
        CookieSyncManager.getInstance().stopSync()
    }

    override fun onStop() {
        currentWeb.webView!!.stopLoading()
        btnReset()
        try {
            unregisterReceiver(mScreenReceiver)
        } catch (e: Exception) {
        }

        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        currentWeb.hideLayoutWord()
    }

    override fun onNewIntent(intent: Intent) {

        super.onNewIntent(intent)
        currentWeb.loadByIntent(intent)
    }

    override fun onBackPressed() {
        if (isShowSettingsView) {
            hideSettingsView()
            return
        }
        if (currentWeb.onBackPressed() == false)
            super.onBackPressed()
    }

    private fun btnReset() {
        mBtnsetWebview1!!.visibility = View.VISIBLE
        mBtnsetWebview2Add!!.visibility = View.GONE
        mBtnsetWebview2Add1!!.visibility = View.GONE
        mBtnsetWebview2Add2!!.visibility = View.GONE
        mBtnsetWebview2Add3!!.visibility = View.GONE
        mBtnsetWebview2Add4!!.visibility = View.GONE
        mBtnsetWebview2ViewMode!!.visibility = View.GONE
        mBtnsetWebview2Screen!!.visibility = View.GONE

        mArrowRemocon!!.visibility = View.GONE
        mBtnAdd!!.isSelected = false
        mBtnViewMode!!.isSelected = false
        mBtnScreen!!.isSelected = false
    }

    override fun btnSettings() {
        super.btnSettings()

        if (isShowSettingsView) {
            mBtnSettings!!.isSelected = true
        } else {
            mBtnSettings!!.isSelected = false
        }
    }

    override fun btnGo() {
        btnReset()
        hideSettingsView()

        if (mIsPageLoading)
            currentWeb.webView!!.stopLoading()
        else
            currentWeb.webView!!.reload()
    }

    private fun goHome() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        intent.putExtra("prev_url", currentWeb.webView!!.originalUrl)
        startActivity(intent)
    }

    private fun btnAdd() {
        if (mBtnsetWebview2Add!!.visibility == View.VISIBLE) {
            if (mBtnsetWebview2Add2!!.visibility == View.VISIBLE)
                btnAddPartReset()
            else
                btnReset()
        } else {
            btnReset()
            mBtnsetWebview2Add!!.visibility = View.VISIBLE
            mBtnsetWebview2Add1!!.visibility = View.VISIBLE
            showRemocon()
            mBtnAdd!!.isSelected = true
            showArrowRemocon(5, 2)
        }
    }

    private fun btnAddFavor() {
        btnReset()
        mBtnsetWebview2Add!!.visibility = View.VISIBLE
        mBtnsetWebview2Add4!!.visibility = View.VISIBLE
        mInputAddTitle!!.setText(currentWeb.webView!!.title)
    }

    private fun showBtnsetWebview2Add3() {
        btnReset()
        mBtnsetWebview2Add!!.visibility = View.VISIBLE
        mBtnsetWebview2Add3!!.visibility = View.VISIBLE
        mBtnAdd!!.isSelected = true
    }

    private fun showArrowRemocon(total: Int, index: Int) {
        val btnWidth = remoconWidth / total
        val leftMargin = btnWidth * index + btnWidth / 2 - mArrowRemocon!!.width / 2
        val rlp = mArrowRemocon!!.layoutParams as RelativeLayout.LayoutParams
        rlp.leftMargin = leftMargin
        mArrowRemocon!!.layoutParams = rlp
        mArrowRemocon!!.visibility = View.VISIBLE
    }

    private fun btnAddComplete(addType: Int) {
        currentWeb.btnAddComplete(addType)
    }

    private fun btnAddCancel() {
        btnReset()
    }

    private fun btnAddPart() {
        btnReset()
        mBtnsetWebview2Add!!.visibility = View.VISIBLE
        mBtnsetWebview2Add2!!.visibility = View.VISIBLE
        mBtnAdd!!.isSelected = true

        // 페이지에 js 주입한다.
        currentWeb.webView!!.injectResetClick()
    }

    private fun btnAddPartReset() {
        btnReset()

        // 주입된 js 를 초기화한다.
        // TODO: 일단은 리프레쉬로 하고, 가능하면 주입한 js를 취소하는 방식으로 바꾸자.
        currentWeb.webView!!.reload()
    }

    private fun btnExpandPart() {
        currentWeb.webView!!.injectExpand()
    }

    private fun btnAddPartComplete() {
        btnReset()
        mBtnsetWebview2Add!!.visibility = View.VISIBLE
        mBtnsetWebview2Add4!!.visibility = View.VISIBLE
        mInputAddTitle!!.setText(currentWeb.webView!!.title)

        currentWeb.webView!!.injectClip()
    }

    private fun btnViewMode() {
        if (mBtnsetWebview2ViewMode!!.visibility == View.VISIBLE) {
            btnReset()
        } else {
            btnReset()
            mBtnsetWebview2ViewMode!!.visibility = View.VISIBLE
            showRemocon()
            mBtnViewMode!!.isSelected = true
            showArrowRemocon(5, 3)
        }
    }

    private fun btnViewModeWeb() {
        if (mViewMode != VIEW_MODE_WEBVIEW) {
            btnReset()
            hideRemocon()
        }
        mViewMode = VIEW_MODE_WEBVIEW
        showCVMSettings(false)

        currentWeb.btnViewModeWeb()
    }

    fun btnViewModeCont() {
        if (mViewMode != VIEW_MODE_CONTENT) {
            btnReset()
            hideRemocon()
        }
        mViewMode = VIEW_MODE_CONTENT
        showCVMSettings(true)

        currentWeb.btnViewModeCont()
    }

    private fun btnViewModeSource() {
        if (mViewMode != VIEW_MODE_SOURCE) {
            btnReset()
            hideRemocon()
        }
        mViewMode = VIEW_MODE_SOURCE
        showCVMSettings(false)

        currentWeb.btnViewModeSource()
    }

    private fun btnScreen() {
        if (mBtnsetWebview2Screen!!.visibility == View.VISIBLE) {
            btnReset()
        } else {
            btnReset()
            mBtnsetWebview2Screen!!.visibility = View.VISIBLE
            showRemocon()
            mBtnScreen!!.isSelected = true
            showArrowRemocon(5, 4)
        }
    }

    private fun extractSource() {
        currentWeb.webView!!.injectExtractSource()
    }

    private fun onContentViewBgChanged(colorChange: ColorChange, bg: Int) {
        when (colorChange) {
            ColorPickerView.ColorChange.OK -> {
                settingsView!!.setContBg(bg)
                mPref!!.contBgColor = bg
            }
            ColorPickerView.ColorChange.CHANGE -> {
            }
            ColorPickerView.ColorChange.CANCEL -> {
            }
        }
        hideColorPickerView(colorChange)

        currentWeb.contentView!!.setBackground(bg)
    }

    private fun hideColorPickerView(colorChange: ColorChange) {
        when (colorChange) {
            ColorPickerView.ColorChange.OK -> {
                settingsView!!.hideSelectBgView()
                settingsView!!.hideColorPickerView()
            }
            ColorPickerView.ColorChange.CANCEL -> settingsView!!.hideColorPickerView()
            else -> {
            }
        }
    }

    private fun onContentViewFontChanged(color: Int) {
        currentWeb.contentView!!.setFontColor(color)
    }

    private fun onContentViewFontSizeChanged(size: Int) {
        currentWeb.contentView!!.setFontSize(size)
    }

    private fun onContentViewLineSpaceChanged(space: Int) {
        currentWeb.contentView!!.setLineSpace(space)
    }

    private fun onContentViewPaddingChanged(padding: Int) {
        currentWeb.contentView!!.setPadding(padding, padding, padding, padding)
    }


    private inner class WebWrap : RelativeLayout {

        var webView: SbWebView? = null
            private set
        var contentView: ContentView? = null
            private set
        var sourceView: SourceView? = null
            private set
        private var mProgressBar: HumanProgressBar? = null
        private var mLayout: RelativeLayout? = null
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mWebChromeClient: WebChromeClient? = null

        private var mIndexSet: String? = null
        private var mWindowWidth: Int = 0
        private var mWindowHeight: Int = 0
        private var mSelectedWidth: Int = 0
        private var mSelectedHeight: Int = 0

        private val faviconData: ByteArray?
            get() {
                val bm = webView!!.favicon
                var faviconData: ByteArray? = null
                if (bm != null) {
                    val bos = ByteArrayOutputStream()
                    bm.compress(CompressFormat.PNG, 100, bos)
                    faviconData = bos.toByteArray()
                }
                return faviconData
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
            LayoutInflater.from(context).inflate(R.layout.web_wrap, this)

            webView = findViewById<View>(R.id.webview) as SbWebView
            contentView = findViewById<View>(R.id.contentview) as ContentView
            sourceView = findViewById<View>(R.id.sourceview) as SourceView
            mProgressBar = findViewById<View>(R.id.progress) as HumanProgressBar
            mLayout = findViewById<View>(R.id.webview_layout) as RelativeLayout

            mProgressBar!!.setStopListener(object: OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event!!.action == MotionEvent.ACTION_DOWN) {
                        webView!!.stopLoading()
                    }
                    return true
                }
            })

            mWebChromeClient = object : SbWebView.SbWebChromeClient(context) {

                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mProgressBar!!.setProgress(newProgress)
                }

                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    if (mCustomView != null) {
                        mLayout!!.removeView(mCustomView)
                    }
                    mCustomView = view
                    mCustomView!!.setBackgroundColor(Color.BLACK)
                    mCustomView!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    mLayout!!.addView(mCustomView)

                    webView!!.visibility = View.GONE
                    mCustomView!!.visibility = View.VISIBLE
                    mCustomViewCallback = callback
                }

                override fun onHideCustomView() {
                    webView!!.visibility = View.VISIBLE
                    if (mCustomViewCallback != null)
                        mCustomViewCallback!!.onCustomViewHidden()
                    if (mCustomView != null)
                        mCustomView!!.visibility = View.GONE
                }

            }

            webView!!.setClients(mWebChromeClient!!, object : SbWebView.SbWebViewClient(context, webView!!) {

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mProgressBar!!.show()
                    mProgressBar!!.setProgress(0)
                    hideRemocon()
                    setBtnGoImage(R.drawable.ic_remote_cancel)
                    mIsPageLoading = true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    setSettingsPageUrl(url)
                    setBtnGoImage(R.drawable.ic_remote_refresh)
                    mIsPageLoading = false
                }

            })

            webView!!.setAndroidBridge(object : SbWebView.AndroidBridge(context) {

                @JavascriptInterface
                fun showBtnset() {
                    mHandler.post { showBtnsetWebview2Add3() }
                }

                @JavascriptInterface
                fun saveValues(indexSet: String, windowWidth: Int, windowHeight: Int, selectedWidth: Int, selectedHeight: Int) {
                    mIndexSet = indexSet
                    mWindowWidth = windowWidth
                    mWindowHeight = windowHeight
                    mSelectedWidth = selectedWidth
                    mSelectedHeight = selectedHeight
                }

                @JavascriptInterface
                fun showSource(source: String) {
                    mHandler.post { sourceView!!.source = source }
                }

                @JavascriptInterface
                fun saveSource(source: String) {
                    mHandler.post {
                        try {
                            val html: String
                            if (source.matches(".*<meta.*charset=.*".toRegex())) {
                                html = source.replace("<meta.*charset=>$".toRegex(), "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />")
                            } else {
                                html = source.replace("<head>", "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />")
                            }

                            val dir = File(Environment.getExternalStorageDirectory().toString() + "/sbrowser")
                            if (dir.exists() == false)
                                dir.mkdirs()

                            val filename = "sb_" + Date().time + ".html"
                            val file = File(Environment.getExternalStorageDirectory().toString() + "/sbrowser", filename)
                            val fw = FileWriter(file)
                            fw.write(html)
                            fw.close()

                            SbDb.getInstance(applicationContext)!!.insertSavedCont(addTitle, filename, faviconData!!)

                            Toast.makeText(applicationContext, R.string.save_ok, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, R.string.save_fail, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                @JavascriptInterface
                override fun showContent(cont: String) {
                    mHandler.post { contentView!!.content = cont }
                }

            })

            // 본문 읽기 관련 셋팅
            ContentView.adjustPref(applicationContext, contentView!!)
        }

        fun loadByIntent(intent: Intent) {

            val uri = intent.data
            if (uri != null && "http" == uri.scheme) {
                webView!!.loadUrl(uri.toString())
            } else {
                val extras = intent.extras
                if (extras != null) {
                    if (extras.containsKey("url")) {

                        val url = extras.getString("url")
                        webView!!.loadUrl(url)
                    } else if (extras.containsKey("saved_cont_id")) {

                        val savedContId = extras.getInt("saved_cont_id")
                        val saved = SbDb.getInstance(applicationContext)!!.getSavedCont(savedContId)
                        webView!!.loadLocalfile(saved!!.mFilename!!)
                    }
                }
            }
        }

        fun hideLayoutWord() {
            contentView!!.hideLayoutWord()
        }

        fun onBackPressed(): Boolean {
            if (contentView!!.isShowWebViewSearch) {
                contentView!!.hideWebViewSearch()
                return true
            }
            if (mCustomView != null && mCustomView!!.visibility == View.VISIBLE) {
                mWebChromeClient!!.onHideCustomView()
                return true
            }
            if (mViewMode == VIEW_MODE_CONTENT || mViewMode == VIEW_MODE_SOURCE) {
                this@WebActivity.btnViewModeWeb()
                return true
            }
            if (webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
            return false
        }

        fun resetWebView() {
            if (contentView!!.isShowWebViewSearch) {
                contentView!!.hideWebViewSearch()
            }
            if (mCustomView != null && mCustomView!!.visibility == View.VISIBLE) {
                mWebChromeClient!!.onHideCustomView()
            }
            if (mViewMode == VIEW_MODE_CONTENT || mViewMode == VIEW_MODE_SOURCE) {
                btnViewModeWeb()
            }
        }

        fun btnAddComplete(addType: Int) {
            if (addType == ADD_TYPE_SAVED_CONT) {
                webView!!.injectSaveSource()  // 실제저장은 AndroidBridge 에서
                btnReset()
            } else {
                val url = webView!!.originalUrl
                val title = addTitle
                val faviconData = faviconData

                if (addType == ADD_TYPE_PART) {
                    val ratio = windowWidth / mWindowWidth.toFloat()
                    val width = (mSelectedWidth * ratio).toInt()
                    val height = (mSelectedHeight * ratio).toInt()
                    val cookie = webView!!.cookie
                    Log.i(TAG, "indexSet: $mIndexSet , width: $width , height:$height , windowWidth: $mWindowWidth , windowHeight:$mWindowHeight , selectedWidth: $mSelectedWidth , selectedHeight:$mSelectedHeight")
                    if (mIndexSet != null && width > 0 && height > 0) {
                        SbDb.getInstance(applicationContext)!!.insertFavoritePart(url, title, mIndexSet!!, width, height, faviconData!!, cookie!!)
                        goHome()
                        finish()
                    }
                } else {
                    SbDb.getInstance(applicationContext)!!.insertFavoriteUrl(url, title, faviconData!!)
                    btnReset()
                }
                Toast.makeText(applicationContext, R.string.favorite_add_ok, Toast.LENGTH_SHORT).show()
            }
        }

        fun btnViewModeWeb() {
            contentView!!.visibility = View.GONE
            sourceView!!.visibility = View.GONE
            webView!!.visibility = View.VISIBLE
            mViewMode = VIEW_MODE_WEBVIEW

            mBtnViewModeWeb!!.setTextColor(-0x2fafcf)
            mBtnViewModeCont!!.setTextColor(-0x9f9d9f)
            mBtnViewModeSource!!.setTextColor(-0x9f9d9f)
        }

        fun btnViewModeCont() {
            webView!!.injectExtractMainCont()
            contentView!!.visibility = View.VISIBLE
            sourceView!!.visibility = View.GONE
            webView!!.visibility = View.GONE
            mViewMode = VIEW_MODE_CONTENT

            mBtnViewModeWeb!!.setTextColor(-0x9f9d9f)
            mBtnViewModeCont!!.setTextColor(-0x2fafcf)
            mBtnViewModeSource!!.setTextColor(-0x9f9d9f)
        }

        fun btnViewModeSource() {
            extractSource()
            contentView!!.visibility = View.GONE
            sourceView!!.visibility = View.VISIBLE
            webView!!.visibility = View.GONE
            mViewMode = VIEW_MODE_SOURCE

            mBtnViewModeWeb!!.setTextColor(-0x9f9d9f)
            mBtnViewModeCont!!.setTextColor(-0x9f9d9f)
            mBtnViewModeSource!!.setTextColor(-0x2fafcf)
        }

    }

    companion object {

        private val TAG = "WebActivity"

        val VIEW_MODE_WEBVIEW = 0
        val VIEW_MODE_CONTENT = 1
        val VIEW_MODE_SOURCE = 2

        protected val ADD_TYPE_FAVOR = 0
        protected val ADD_TYPE_PART = 1
        protected val ADD_TYPE_SAVED_CONT = 2
    }

}
