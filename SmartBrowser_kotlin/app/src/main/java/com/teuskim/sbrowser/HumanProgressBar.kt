package com.teuskim.sbrowser

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

class HumanProgressBar : RelativeLayout {
    private var mPushman: ImageView? = null
    private var mBtnCloseProgress: View? = null

    private var mDestProgress: Int = 0
    private var mCurrProgress: Int = 0
    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if (mCurrProgress < mDestProgress) {
                mCurrProgress++
                val lp = mPushman!!.layoutParams as RelativeLayout.LayoutParams
                lp.leftMargin = (width - mPushman!!.width) * mCurrProgress / MAX_PROGRESS
                mPushman!!.layoutParams = lp
            }

            if (mCurrProgress < MAX_PROGRESS - 5) {
                this.sendEmptyMessage(0)
            } else {
                hide()
            }

            if (mCurrProgress % 20 >= 10)
                mPushman!!.setImageResource(R.drawable.img_loading_pushman02)
            else
                mPushman!!.setImageResource(R.drawable.img_loading_pushman01)

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
        LayoutInflater.from(context).inflate(R.layout.human_progress_bar, this)
        mPushman = findViewById<View>(R.id.pushman) as ImageView
        mBtnCloseProgress = findViewById(R.id.btn_close_progress)
    }

    fun setProgress(progress: Int) {

        mDestProgress = progress
    }

    fun setStopListener(listener: View.OnTouchListener) {
        mBtnCloseProgress!!.setOnTouchListener(listener)
    }

    fun show() {
        visibility = View.VISIBLE
        mDestProgress = 0
        mCurrProgress = 0
        mHandler.sendEmptyMessage(0)
    }

    fun hide() {
        visibility = View.GONE
    }

    companion object {

        private val MAX_PROGRESS = 100
    }
}
