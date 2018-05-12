package com.teuskim.sbrowser

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class RemoconView : RelativeLayout {

    private var mRemoconView: View? = null
    private var mIcDrag: View? = null
    private var mRemoconBody: View? = null
    private var mRemoconBottom: View? = null
    private var mBtnInputBack: ImageView? = null
    private var mBtnInputFoward: ImageView? = null

    private var mIsRemoconBodyTouchable: Boolean = false
    private var mRemoconAlpha: Int = 0
    private var mMinLeft: Int = 0
    private var mMaxLeft: Int = 0
    private var mMinBottom: Int = 0
    private var mMaxBottom: Int = 0
    private var mWindowWidth: Int = 0
    private var mWindowHeight: Int = 0
    private var mCurrentLeft: Int = 0
    private var mCurrentBottom: Int = 0
    private var mTouchMoveX: Int = 0
    private var mTouchMoveY: Int = 0

    private var mPref: MiscPref? = null
    private var mShowAnimation: AnimationSet? = null
    private var mHideAnimation: AnimationSet? = null
    private val mShowShadowHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mRemoconView!!.setBackgroundResource(R.drawable.shadow_remote)
        }

    }

    val remoconWidth: Int
        get() = mRemoconBody!!.width

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
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.remocon, this)

        mPref = MiscPref.getInstance(context)
        mRemoconAlpha = mPref!!.remoconAlpha

        mIsRemoconBodyTouchable = false
        mMinLeft = Integer.MIN_VALUE
        mMaxLeft = Integer.MAX_VALUE
        mMinBottom = Integer.MIN_VALUE
        mMaxBottom = Integer.MAX_VALUE

        mRemoconView = findViewById(R.id.remocon_layout)
        mRemoconBody = findViewById(R.id.remocon_body)
        mBtnInputBack = findViewById<View>(R.id.btn_input_back) as ImageView
        mBtnInputFoward = findViewById<View>(R.id.btn_input_foward) as ImageView
        mIcDrag = findViewById(R.id.ic_drag)
        mIcDrag!!.setOnTouchListener(object : View.OnTouchListener {

            private var mTouchedX = Integer.MIN_VALUE
            private var mTouchedY = Integer.MIN_VALUE
            private var mTouchedLeft: Int = 0
            private var mTouchedBottom: Int = 0
            private var mDidShow: Boolean = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (mWindowWidth == 0 || mWindowHeight == 0)
                    return false

                val x = event.x.toInt()
                val y = event.y.toInt()
                if (mTouchedX == Integer.MIN_VALUE) mTouchedX = x
                if (mTouchedY == Integer.MIN_VALUE) mTouchedY = y

                val left = mCurrentLeft + x - mTouchedX
                val bottom = mCurrentBottom - y + mTouchedY

                val action = event.action
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        mTouchMoveX = 0
                        mTouchMoveY = 0
                        mTouchedX = x
                        mTouchedY = y
                        mTouchedLeft = left
                        mTouchedBottom = bottom
                        if (mRemoconBody!!.visibility == View.VISIBLE) {
                            mDidShow = true
                        } else {
                            mRemoconBody!!.startAnimation(mShowAnimation)
                            showRemocon()
                            mDidShow = false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mTouchMoveX += left - mCurrentLeft
                        mTouchMoveY += bottom - mCurrentBottom
                        setRemoconPosition(left, bottom)
                    }
                    MotionEvent.ACTION_UP -> {
                        // 마지막 위치 저장
                        mPref!!.remoconLeft = left
                        mPref!!.remoconBottom = bottom

                        // 클릭이었다면(별로 움직이지 않았다면) 리모콘 toggle
                        if (Math.abs(mTouchMoveX) + Math.abs(mTouchMoveY) < v.width/2) {
                            if (mDidShow) {
                                mRemoconBody!!.startAnimation(mHideAnimation)
                                hideRemocon()
                            } else {
                                showRemocon()
                            }
                        }
                    }
                }
                return true
            }
        })

        mRemoconView!!.setOnTouchListener { v, event -> mIsRemoconBodyTouchable }

        createAnimations()
    }

    fun setBottomView(v: View) {
        mRemoconBottom = v
        (mRemoconBody as ViewGroup).addView(v)
    }

    private fun setBoundary() {
        mMinLeft = 0
        mMinBottom = 0
        mMaxLeft = (mWindowWidth - mRemoconBody!!.width * 0.6).toInt()
        mMaxBottom = mWindowHeight - mRemoconBody!!.height
    }

    fun setWindowSize(width: Int, height: Int) {
        mWindowWidth = width
        mWindowHeight = height

        setBoundary()
    }

    fun initAlphaAndPosition() {
        if (mRemoconBody!!.visibility == View.VISIBLE)
            setRemoconAlpha(255, mIcDrag)
        else
            setRemoconAlpha(mRemoconAlpha, mIcDrag)

        val left = mPref!!.remoconLeft
        val bottom = mPref!!.remoconBottom
        setRemoconPosition(left, bottom)
    }

    fun showRemocon() {
        mRemoconBody!!.visibility = View.VISIBLE
        mShowShadowHandler.sendEmptyMessageDelayed(0, ANIMATION_DURATION.toLong())
        setRemoconAlpha(255, mIcDrag)
        mIsRemoconBodyTouchable = true
    }

    fun hideRemocon() {
        mRemoconBody!!.visibility = View.INVISIBLE
        mRemoconView!!.setBackgroundColor(Color.TRANSPARENT)
        setRemoconAlpha(mRemoconAlpha, mIcDrag)
        mIsRemoconBodyTouchable = false
    }

    private fun setRemoconPosition(left: Int, bottom: Int) {
        var left = left
        var bottom = bottom
        if (left == Integer.MIN_VALUE)
            left = 50
        else if (left < mMinLeft)
            left = mMinLeft
        else if (left > mMaxLeft) left = mMaxLeft

        if (bottom == Integer.MIN_VALUE)
            bottom = 50
        else if (bottom < mMinBottom)
            bottom = mMinBottom
        else if (bottom > mMaxBottom) bottom = mMaxBottom

        val lp = mRemoconView!!.layoutParams as RelativeLayout.LayoutParams
        lp.setMargins(left, 0, 0, bottom)
        mRemoconView!!.layoutParams = lp
        mRemoconView!!.invalidate()

        mCurrentLeft = left
        mCurrentBottom = bottom
    }

    fun setRemoconAlpha(alpha: Int) {
        mRemoconAlpha = alpha
        invalidate()
        mPref!!.remoconAlpha = alpha
    }

    private fun setRemoconAlpha(alpha: Int, view: View?): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setRemoconAlpha(alpha, view.getChildAt(i))
                if (view.background != null) view.background.alpha = alpha
            }
        } else {
            if (view is ImageView)
                if (view.drawable != null)
                    view.drawable.alpha = alpha
                else if (view is TextView)
                    (view as TextView).setTextColor((view as TextView).textColors.withAlpha(alpha))

            if (view!!.background != null) view.background.alpha = alpha
        }
        return true
    }

    fun setOnBackClickListener(listener: View.OnClickListener?) {
        if (listener != null) {
            mBtnInputBack!!.setImageResource(R.drawable.btn_input_back)
            mBtnInputBack!!.isClickable = true
            mBtnInputBack!!.setOnClickListener(listener)
        } else {
            mBtnInputBack!!.setImageResource(R.drawable.btn_input_back_disable)
            mBtnInputBack!!.isClickable = false
        }
    }

    fun setOnFowardClickListener(listener: View.OnClickListener?) {
        if (listener != null) {
            mBtnInputFoward!!.setImageResource(R.drawable.btn_input_foward)
            mBtnInputFoward!!.isClickable = true
            mBtnInputFoward!!.setOnClickListener(listener)
        } else {
            mBtnInputFoward!!.setImageResource(R.drawable.btn_input_foward_disable)
            mBtnInputFoward!!.isClickable = false
        }
    }

    private fun createAnimations() {
        if (mShowAnimation == null) {
            mShowAnimation = AnimationSet(false)
            mShowAnimation!!.interpolator = AccelerateInterpolator()
            mShowAnimation!!.addAnimation(ScaleAnimation(0.2f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f))
            mShowAnimation!!.duration = ANIMATION_DURATION.toLong()
        }

        if (mHideAnimation == null) {
            mHideAnimation = AnimationSet(false)
            mHideAnimation!!.interpolator = AccelerateInterpolator()
            mHideAnimation!!.addAnimation(ScaleAnimation(1.0f, 0.2f, 1.0f, 0.5f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f))
            mHideAnimation!!.duration = ANIMATION_DURATION.toLong()
        }
    }

    companion object {

        private val ANIMATION_DURATION = 100
    }

}