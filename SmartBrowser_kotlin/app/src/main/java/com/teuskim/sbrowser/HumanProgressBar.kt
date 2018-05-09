package com.teuskim.sbrowser

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

class HumanProgressBar : RelativeLayout {
    private var mPushman: ImageView? = null
    private var mBtnCloseProgress: View? = null
    private var mIsHurry: Boolean = false

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
        val lp = mPushman!!.layoutParams as RelativeLayout.LayoutParams
        lp.leftMargin = (width - mPushman!!.width) * progress / MAX_PROGRESS
        mPushman!!.layoutParams = lp

        if (mIsHurry)
            mPushman!!.setImageResource(R.drawable.img_loading_pushman02)
        else
            mPushman!!.setImageResource(R.drawable.img_loading_pushman01)
        mIsHurry = !mIsHurry
    }

    fun setStopListener(listener: View.OnTouchListener) {
        mBtnCloseProgress!!.setOnTouchListener(listener)
    }

    companion object {

        private val MAX_PROGRESS = 100
    }
}
