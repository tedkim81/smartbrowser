package com.teuskim.sbrowser

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView

class LockscreenActivity : Activity() {

    private val mHandler = Handler()
    private var mBtnUnlockLeft: Button? = null
    private var mBtnUnlockRight: Button? = null
    private var mIsTouchedLeft = false
    private var mIsTouchedRight = false

    private var mLocale: Locale? = null
    private var mCurrentTime: TextView? = null
    private var mCurrentDate: TextView? = null

    private var mWebView: SbWebView? = null
    private var mContentView: ContentView? = null
    private var mSourceView: SourceView? = null

    private val mOnTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> when (v.id) {
                R.id.btn_unlock_left, R.id.btnlayout_unlock_left -> if (mIsTouchedLeft) {
                    dismissKeyguard()
                } else {
                    mIsTouchedLeft = true
                    mBtnUnlockLeft!!.setBackgroundResource(R.drawable.btn_lockscreen_unlock_02)
                    mHandler.postDelayed({
                        mIsTouchedLeft = false
                        mBtnUnlockLeft!!.setBackgroundResource(R.drawable.btn_lockscreen_unlock_01)
                    }, 2000)
                }

                R.id.btn_unlock_right, R.id.btnlayout_unlock_right -> if (mIsTouchedRight) {
                    dismissKeyguard()
                } else {
                    mIsTouchedRight = true
                    mBtnUnlockRight!!.setBackgroundResource(R.drawable.btn_lockscreen_unlock_02)
                    mHandler.postDelayed({
                        mIsTouchedRight = false
                        mBtnUnlockRight!!.setBackgroundResource(R.drawable.btn_lockscreen_unlock_01)
                    }, 2000)
                }
            }
        }
        true
    }

    private val mTimeRunnable = object : Runnable {

        override fun run() {
            val cal = Calendar.getInstance()
            var hour = cal.get(Calendar.HOUR_OF_DAY)
            var ampm = ""
            if (DateFormat.is24HourFormat(applicationContext) == false) {
                if (mLocale!!.language == Locale.KOREAN.toString()) {
                    if (hour < 12)
                        ampm = "오전"
                    else
                        ampm = "오후"
                } else {
                    if (hour < 12)
                        ampm = "AM"
                    else
                        ampm = "PM"
                }

                if (hour == 0)
                    hour = 12
                else if (hour > 12)
                    hour -= 12
            }
            val time = get2Digit(hour) + ":" + get2Digit(cal.get(Calendar.MINUTE)) + " " + ampm
            mCurrentTime!!.text = time

            mHandler.postDelayed(this, 10000)
        }

        private fun get2Digit(num: Int): String {
            return if (num < 10) "0" + num else "" + num
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_Wallpaper_NoTitleBar)

        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        setContentView(R.layout.lockscreen)
        mBtnUnlockLeft = findViewById<View>(R.id.btn_unlock_left) as Button
        mBtnUnlockRight = findViewById<View>(R.id.btn_unlock_right) as Button
        mCurrentTime = findViewById<View>(R.id.current_time) as TextView
        mCurrentDate = findViewById<View>(R.id.current_date) as TextView
        mWebView = findViewById<View>(R.id.webview) as SbWebView
        mContentView = findViewById<View>(R.id.contentview) as ContentView
        mSourceView = findViewById<View>(R.id.sourceview) as SourceView

        mBtnUnlockLeft!!.setOnTouchListener(mOnTouchListener)
        mBtnUnlockRight!!.setOnTouchListener(mOnTouchListener)
        findViewById<View>(R.id.btnlayout_unlock_left).setOnTouchListener(mOnTouchListener)
        findViewById<View>(R.id.btnlayout_unlock_right).setOnTouchListener(mOnTouchListener)

        mLocale = resources.configuration.locale

        val extras = intent.extras
        val viewMode = extras!!.getInt("view_mode")

        when (viewMode) {
            WebActivity.VIEW_MODE_WEBVIEW -> viewWebView(extras.getString("url"))
            WebActivity.VIEW_MODE_CONTENT -> viewContentView(extras.getString("content"))
            WebActivity.VIEW_MODE_SOURCE -> viewSourceView(extras.getString("source"))
        }
    }

    private fun viewWebView(url: String?) {
        mWebView!!.visibility = View.VISIBLE
        mContentView!!.visibility = View.GONE
        mSourceView!!.visibility = View.GONE

        mWebView!!.setClients(SbWebView.SbWebChromeClient(this), object : SbWebView.SbWebViewClient(this) {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val js = ("function disableClickAll(el){"
                        + "for(var i=0; i<el.length; i++){"
                        + "if((el[i] instanceof Text) == false && (el[i] instanceof HTMLButtonElement) == false){"
                        + "el[i].onclick = disableClick;"
                        + "if(el[i] instanceof HTMLInputElement)"
                        + "el[i].disabled=true;"
                        + "if(el[i].childNodes instanceof NodeList)"
                        + "disableClickAll(el[i].childNodes);"
                        + "}"
                        + "}"
                        + "}"
                        + "function disableClick(e){"
                        + "e.stopPropagation();"
                        + "e.returnValue = false;"
                        + "}"
                        + "disableClickAll(document.getElementsByTagName('body')[0].childNodes);")
                mWebView!!.injectJs(js)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return true
            }

        })
        if (url != null) {
            mWebView!!.loadUrl(url)
        }
    }

    private fun viewContentView(content: String?) {
        mWebView!!.visibility = View.GONE
        mContentView!!.visibility = View.VISIBLE
        mSourceView!!.visibility = View.GONE

        // 본문 읽기 관련 셋팅
        ContentView.adjustPref(this, mContentView!!)
        mContentView!!.content = content!!
    }

    private fun viewSourceView(source: String?) {
        mWebView!!.visibility = View.GONE
        mContentView!!.visibility = View.GONE
        mSourceView!!.visibility = View.VISIBLE

        mSourceView!!.source = source!!
    }

    override fun onResume() {
        super.onResume()
        mHandler.post(mTimeRunnable)
        setCurrentDate()
    }

    override fun onPause() {
        mHandler.removeCallbacks(mTimeRunnable)
        super.onPause()
    }

    private fun dismissKeyguard() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        mHandler.postDelayed({ finish() }, 500)
    }

    private fun setCurrentDate() {
        var date: String
        val cal = Calendar.getInstance()
        if (mLocale!!.language == Locale.KOREAN.toString()) {
            date = transformDate(Date(), "MM월 dd일 ")
            val weeks = arrayOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")
            date += weeks[cal.get(Calendar.DAY_OF_WEEK) - 1]
        } else {
            val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            val weeks = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            date = weeks[cal.get(Calendar.DAY_OF_WEEK) - 1] + " " + months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH)
        }
        mCurrentDate!!.text = date
    }

    fun transformDate(date: Date, toFormatString: String): String {

        val toFormat = SimpleDateFormat(toFormatString, Locale.KOREAN)
        return toFormat.format(date)
    }

}
