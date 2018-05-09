package com.teuskim.sbrowser

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class RemoconMenuView : LinearLayout {

    private var mImage: ImageView? = null
    private var mTitle: TextView? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.remocon_menu_view, this)
        mImage = findViewById<ImageView>(R.id.menu_image)
        mTitle = findViewById<TextView>(R.id.menu_title)
    }

    fun setIconAndTitle(imageResId: Int, title: String) {
        mImage!!.setImageResource(imageResId)
        mTitle!!.text = title
    }

    fun setTitle(title: String) {
        mTitle!!.text = title
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mImage!!.isPressed = true
                mTitle!!.setTextColor(-0x16b7e0)
            }
            MotionEvent.ACTION_UP -> {
                mImage!!.isPressed = false
                mTitle!!.setTextColor(-0x9f9d9f)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if (selected) {
            mImage!!.isPressed = true
            mTitle!!.setTextColor(-0x16b7e0)
        } else {
            mImage!!.isPressed = false
            mTitle!!.setTextColor(-0x9f9d9f)
        }
    }

}
