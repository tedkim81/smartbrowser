package com.teuskim.sbrowser

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.LinkObject
import com.kakao.message.template.TextTemplate
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback

abstract class BaseRemoconActivity : Activity() {

    private var mRemocon: RemoconView? = null
    protected var settingsView: SettingsView? = null
        private set
    private var mBtnGoText: TextView? = null
    private var mBtnGoImg: ImageView? = null

    private var mDisplayMetrics: DisplayMetrics? = null
    private var mPref: MiscPref? = null
    private var mShareDialog: ShareDialog? = null
    private var mKillReceiver: KillReceiver? = null

    private val mListener = OnClickListener { v ->
        when (v.id) {
            R.id.input_query_form -> btnInputQueryForm()
            R.id.btn_go -> btnGo()
        }
    }
    protected abstract val shareUrl: String?

    protected val remoconWidth: Int
        get() = mRemocon!!.remoconWidth

    protected val isShowSettingsView: Boolean
        get() = settingsView!!.visibility == View.VISIBLE

    protected val windowWidth: Int
        get() = mDisplayMetrics!!.widthPixels

    protected val windowHeight: Int
        get() = mDisplayMetrics!!.heightPixels

    protected abstract fun btnGo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPref = MiscPref.getInstance(applicationContext)
        mKillReceiver = KillReceiver()
        val ift = IntentFilter()
        ift.addAction("sbrowser.kill")
        registerReceiver(mKillReceiver, ift)
    }

    override fun onDestroy() {
        unregisterReceiver(mKillReceiver)
        super.onDestroy()
    }

    override fun setContentView(layoutResID: Int) {
        val inflater = LayoutInflater.from(this)
        val parent = inflater.inflate(R.layout.base_remocon, null)
        inflater.inflate(layoutResID, parent.findViewById<View>(R.id.body) as ViewGroup)
        super.setContentView(parent)

        mDisplayMetrics = resources.displayMetrics

        mRemocon = findViewById<View>(R.id.remocon_view) as RemoconView
        findViewById<View>(R.id.input_query_form).setOnClickListener(mListener)
        findViewById<View>(R.id.btn_go).setOnClickListener(mListener)
        mBtnGoText = findViewById<View>(R.id.btn_go_text) as TextView
        mBtnGoImg = findViewById<View>(R.id.btn_go_img) as ImageView

        settingsView = findViewById<View>(R.id.settings_view) as SettingsView
        initSettingsView(settingsView)
    }

    override fun onBackPressed() {
        if (isShowSettingsView) {
            hideSettingsView()
            return
        }
        super.onBackPressed()
    }

    protected fun setRemoconBottomView(remoconBottom: View) {
        mRemocon!!.setBottomView(remoconBottom)
    }

    protected fun setBtnGoImage(resId: Int) {
        mBtnGoImg!!.setImageResource(resId)
        mBtnGoImg!!.visibility = View.VISIBLE
        mBtnGoText!!.visibility = View.GONE
    }

    protected fun setBtnGoText(text: String) {
        mBtnGoText!!.text = text
        mBtnGoImg!!.visibility = View.GONE
        mBtnGoText!!.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()

        setShareInfo()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            initRemocon()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        initRemocon()
    }

    private fun initRemocon() {
        // 리모콘 위치 초기화
        val rectgle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectgle)
        mRemocon!!.setWindowSize(windowWidth, windowHeight - rectgle.top)
        mRemocon!!.initAlphaAndPosition()
    }

    protected fun btnInputQueryForm() {
        val i = Intent(applicationContext, InputActivity::class.java)
        startActivity(i)
    }

    fun setOnBackClickListener(listener: OnClickListener?) {
        mRemocon!!.setOnBackClickListener(listener)
    }

    fun setOnFowardClickListener(listener: OnClickListener?) {
        mRemocon!!.setOnFowardClickListener(listener)
    }

    protected open fun btnSettings() {
        if (isShowSettingsView) {
            hideSettingsView()
        } else {
            showSettingsView()
        }
    }

    protected open fun initSettingsView(settingsView: SettingsView?) {
        var width = (mDisplayMetrics!!.densityDpi * 1.8).toInt()
        val cmpWidth = (windowWidth * 0.9).toInt()
        if (width >= cmpWidth)
            width = cmpWidth
        var height = (mDisplayMetrics!!.densityDpi * 2.5).toInt()
        val cmpHeight = (windowHeight * 0.9).toInt()
        if (height >= cmpHeight)
            height = cmpHeight

        val settingsLayoutParams = RelativeLayout.LayoutParams(width, height)
        settingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        settingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        settingsView!!.layoutParams = settingsLayoutParams
        settingsView.setRemoconAlpha(mPref!!.remoconAlpha)
        settingsView.setOnAlphaChangedListener(object: SettingsView.OnAlphaChangedListener {
            override fun onAlphaChanged(alpha: Int) {
                mRemocon!!.setRemoconAlpha(alpha)
                mRemocon!!.hideRemocon()
            }
        })
    }

    protected fun showRemocon() {
        mRemocon!!.showRemocon()
    }

    protected fun hideRemocon() {
        mRemocon!!.hideRemocon()
    }

    protected fun hideSettingsView() {
        settingsView!!.close()
    }

    private fun showSettingsView() {
        settingsView!!.open()
        mRemocon!!.hideRemocon()
    }

    protected fun setSettingsPageUrl(url: String) {
        settingsView!!.setPageUrl(url)
    }

    protected fun getPixelFromDip(dip: Int): Int {
        return (dip * mDisplayMetrics!!.density + 0.5).toInt()
    }

    protected fun showCVMSettings(isShow: Boolean) {
        settingsView!!.showCVMSettings(isShow)
    }

    private fun setShareInfo() {
        mShareDialog = ShareDialog(this)
        if (ShareDialog.canShow(ShareLinkContent::class.java)) {
            settingsView!!.setFacebookClickListener(OnClickListener {
                if (shareUrl == null || !shareUrl!!.startsWith("http")) {
                    Toast.makeText(applicationContext, R.string.share_home_text, Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                val content = ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareUrl))
                        .build()
                mShareDialog!!.show(content)
            })
        }
        settingsView!!.setKakaoClickListener(OnClickListener {
            if (shareUrl == null || !shareUrl!!.startsWith("http")) {
                Toast.makeText(applicationContext, R.string.share_home_text, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val params = TextTemplate.newBuilder(
                    shareUrl!!,
                    LinkObject.newBuilder().setAndroidExecutionParams("").build()
            ).setButtonTitle(getString(R.string.go)).build()
            KakaoLinkService.getInstance().sendDefault(this@BaseRemoconActivity, params, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Toast.makeText(applicationContext, R.string.share_kakao_fail, Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(result: KakaoLinkResponse) {
                    Toast.makeText(applicationContext, R.string.share_kakao_ok, Toast.LENGTH_SHORT).show()
                }
            })
        })
    }

    private inner class KillReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }

    }

}
