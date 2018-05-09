package com.teuskim.sbrowser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView

class MoveableListView(private val mContext: Context, attrs: AttributeSet) : ListView(mContext, attrs) {
    private var mDragView: ImageView? = null
    private var mWindowManager: WindowManager? = null
    private var mWindowParams: WindowManager.LayoutParams? = null
    private var mDragPos: Int = 0      // 드래그 아이템의 위치
    private var mFirstDragPos: Int = 0 // 드래그 아이템의 원래 위치
    private var mDragPoint: Int = 0
    private var mCoordOffset: Int = 0  // 스크린에서의 위치와 뷰내에서의 위치의 차이
    private var mDragListener: DragListener? = null
    private var mDropListener: DropListener? = null
    private var mUpperBound: Int = 0
    private var mLowerBound: Int = 0
    private var mHeight: Int = 0
    private val mTempRect = Rect()
    private var mDragBitmap: Bitmap? = null
    private val mTouchSlop: Int
    private var mItemHeightNormal: Int = 0
    private var mItemHeightExpanded: Int = 0
    private var mDndViewId: Int = 0
    private var mDragImageX = 0
    private var mBitmap: Bitmap? = null
    private var mTouchedY: Int = 0
    private var mCurrentY: Int = 0
    private var mForcedItemHeight: Int = 0
    private var mIconWidth: Int = 0
    private val mHandler = Handler()
    private val mScrollRunnable = Runnable {
        if (doScroll())
            scroll(mCurrentY)
    }

    init {
        mTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop
    }

    fun setIconWidth(width: Int) {
        mIconWidth = width
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        if ((mDragListener != null || mDropListener != null) && ev.x < mIconWidth) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {

                    val x = ev.x.toInt()
                    mTouchedY = ev.y.toInt()
                    val itemnum = pointToPosition(x, mTouchedY)
                    if (itemnum != AdapterView.INVALID_POSITION) {
                        val item = getChildAt(itemnum - firstVisiblePosition) as View // 드래그 아이템

                        if (mForcedItemHeight > 0)
                            mItemHeightNormal = mForcedItemHeight
                        else
                            mItemHeightNormal = item.height // 아이템의 높이

                        mItemHeightExpanded = mItemHeightNormal + item.height // 아이템이 드래그 할때 벌어질 높이

                        mDragPoint = mTouchedY - item.top
                        mCoordOffset = ev.rawY.toInt() - mTouchedY

                        var dragger: View? = item.findViewById(mDndViewId) // 드래그 이벤트를 할 아이템내에서의 뷰
                        if (dragger == null)
                            dragger = item
                        val r = mTempRect
                        dragger.getDrawingRect(r)

                        if (x < r.right * 2) {
                            item.isDrawingCacheEnabled = true

                            // 드래그 하는 아이템의 이미지 캡쳐
                            if (mBitmap != null)
                                mBitmap!!.recycle()
                            mBitmap = Bitmap.createBitmap(item.drawingCache)
                            mDragPos = itemnum
                            mFirstDragPos = mDragPos
                            mHeight = height

                            // 스크롤링을 위한 값 획득
                            val touchSlop = mTouchSlop
                            mUpperBound = Math.min(mTouchedY - touchSlop, mItemHeightNormal)
                            mLowerBound = Math.max(mTouchedY + touchSlop, mHeight - mItemHeightNormal)
                            return false
                        }
                        mDragView = null
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun setForcedHeights(height: Int) {
        mForcedItemHeight = height
    }

    fun startDragging() {
        startDragging(mBitmap, mTouchedY)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        if ((mDragListener != null || mDropListener != null) && mDragView != null && ev.x < mIconWidth) {
            val action = ev.action
            when (action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    val r = mTempRect
                    mDragView!!.getDrawingRect(r)
                    stopDragging()
                    if (mDropListener != null && mDragPos >= 0 && mDragPos < count) {
                        mDropListener!!.drop(mFirstDragPos, mDragPos)
                    }
                    unExpandViews()

                    mCurrentY = mHeight / 2
                }

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    dragView(x, y)
                    val itemnum = getItemForPosition(y)
                    if (itemnum >= 0) {

                        scroll(y)

                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            if (mDragListener != null) {
                                mDragListener!!.drag(mDragPos, itemnum)
                            }
                            mDragPos = itemnum
                            doExpansion() // 처음 드래그한 아이템과 다른 위치에 있을 경우 펼쳐지게 한다.
                        }
                    }
                }
            }
            return true
        }
        return super.onTouchEvent(ev)
    }

    private fun getItemForPosition(y: Int): Int {
        val pos: Int
        val adjustedy1 = y - mDragPoint + 10
        val adjustedy2 = y - mDragPoint + (mItemHeightNormal - 10)
        val pos1 = pointToPosition(1, adjustedy1)
        val pos2 = pointToPosition(1, adjustedy2)

        if (pos1 == AdapterView.INVALID_POSITION || pos2 == AdapterView.INVALID_POSITION)
            return AdapterView.INVALID_POSITION

        if (pos1 < mFirstDragPos) {  // 본래 위치보다 위에서 드래그 할때는 아이템의 하단을 기준으로 현재 포지션을 구하고,
            pos = pos2
        } else {  // 본래 위치보다 아래에서 드래그 할때는 아이템의 상단을 기준으로 현재 포지션을 구한다.
            pos = pos1
        }

        return pos
    }

    private fun scroll(y: Int) {
        mCurrentY = y
        mHandler.removeCallbacks(mScrollRunnable)
        mHandler.post(mScrollRunnable)
    }

    private fun doScroll(): Boolean {

        var ref = pointToPosition(1, mHeight / 2)
        if (ref == AdapterView.INVALID_POSITION) {
            ref = pointToPosition(1, mHeight / 2 + dividerHeight + 64)
        }
        val v = getChildAt(ref - firstVisiblePosition)

        if (v != null) {
            val pos = v.top
            if (mCurrentY < mUpperBound) {
                setSelectionFromTop(ref, pos + 10)
                return true
            } else if (mCurrentY > mLowerBound) {
                setSelectionFromTop(ref, pos - 10)
                return true
            }
        }
        return false
    }

    private fun doExpansion() {
        val relativePosition = mDragPos - firstVisiblePosition  // 현재 드래그 되고 있는 점의 현재화면에서의 상대적 포지션
        val first = getChildAt(mFirstDragPos - firstVisiblePosition)  // 드래그하기 위해 선택한 뷰
        var startIndex = 0
        if (firstVisiblePosition < headerViewsCount)
            startIndex = headerViewsCount - firstVisiblePosition

        var i = startIndex
        while (true) {
            var vv = getChildAt(i)
            if (vv == null) {
                break
            }
            vv = vv as LinearLayout
            var height = mItemHeightNormal
            var visibility = View.VISIBLE

            if (vv == first) {  // 선택뷰인 경우,
                if (i == relativePosition) {  // 현재 드래그된 포지션과 같으면 영역은 차지하되 보이지 않게 하고,
                    visibility = View.INVISIBLE
                } else {  // 현재 드래그된 포지션과 다르면 높이를 줄여서 보이지 않게 한다.
                    height = 5
                }
            } else if (i == relativePosition) {  // 선택뷰가 아닌 나머지 중에 현재 드래그된 포지션과 같은 뷰는 두배로 늘린다.
                height = mItemHeightExpanded
            }

            val params = vv.layoutParams
            params.height = height
            vv.layoutParams = params
            if (mFirstDragPos > mDragPos) {  // 본래 위치보다 위로 드래그 할때는 아이템의 상단을 넓히고,
                vv.gravity = Gravity.BOTTOM
                vv.setPadding(0, 0, 0, 4)
            } else {  // 본래 위치보다 아래로 드래그 할때는 아이템의 하단을 넓히고,
                vv.gravity = Gravity.TOP
            }
            vv.visibility = visibility
            i++
        }
    }


    private fun unExpandViews() {
        var i = headerViewsCount
        while (true) {
            var v: View? = getChildAt(i)
            if (v == null) {
                layoutChildren()
                v = getChildAt(i)
                if (v == null) {
                    break
                }
            }
            val params = v.layoutParams
            params.height = mItemHeightNormal
            v.layoutParams = params
            v.visibility = View.VISIBLE
            i++
        }
    }

    // 드래그 시작
    private fun startDragging(bm: Bitmap?, y: Int) {
        stopDragging()

        mWindowParams = WindowManager.LayoutParams()
        mWindowParams!!.gravity = Gravity.TOP
        mWindowParams!!.x = mDragImageX
        mWindowParams!!.y = y - mDragPoint + mCoordOffset

        mWindowParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowParams!!.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        mWindowParams!!.format = PixelFormat.TRANSLUCENT
        mWindowParams!!.windowAnimations = 0

        val v = ImageView(mContext)
        val backGroundColor = Color.parseColor("#ffffffff")
        v.setBackgroundColor(backGroundColor)
        v.setImageBitmap(bm)
        mDragBitmap = bm

        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(v, mWindowParams)
        mDragView = v
    }

    // 드래그를 위해 만들어 준 뷰의 이동
    private fun dragView(x: Int, y: Int) {
        mWindowParams!!.y = y - mDragPoint + mCoordOffset
        mWindowManager!!.updateViewLayout(mDragView, mWindowParams)
    }

    // 드래그 종료 처리
    private fun stopDragging() {
        if (mDragView != null) {
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(mDragView)
            mDragView!!.setImageDrawable(null)
            mDragView = null
        }
        if (mDragBitmap != null) {
            mDragBitmap!!.recycle()
            mDragBitmap = null
        }
    }

    /**
     * 드래그 이벤트 리스너 등록
     * @param l 드래그 이벤트 리스너
     */
    fun setDragListener(l: DragListener) {
        mDragListener = l
    }

    /**
     * 드랍 이벤트 리스너 등록
     * @param l 드랍 이벤트 리스너
     */
    fun setDropListener(l: DropListener) {
        mDropListener = l
    }

    /**
     * 리스트 아이템에 있는 뷰들 중 드래그 드랍 이벤트를 발생시킬 뷰의 아이
     * @param id 드래그 드랍 이벤트를 발생시킬 뷰의 아이디
     */
    fun setDndView(id: Int) {
        mDndViewId = id
    }

    /**
     * 드래그시 표시되는 뷰의 스크린에서의 left padding
     * @param x 스크린에서의 left padding, 설정안하면 0
     */
    fun setDragImageX(x: Int) {
        mDragImageX = x
    }

    interface DragListener {
        fun drag(from: Int, to: Int)
    }

    interface DropListener {
        fun drop(from: Int, to: Int)
    }
}
