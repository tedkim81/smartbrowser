package com.teuskim.sbrowser

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView

class TextButton : TextView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> setTextColor(sPressed)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> setTextColor(sNormal)
        }
        return super.onTouchEvent(event)
    }

    companion object {

        private var sNormal = -0x1000000
        private var sPressed = -0x1000000

        fun setTextColor(normal: Int, pressed: Int) {
            sNormal = normal
            sPressed = pressed
        }
    }

}
