package com.teuskim.sbrowser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.view.MotionEvent
import android.view.View

class ColorPickerView(c: Context, private val mWidth: Int, private val mHeight: Int) : View(c) {

    private var mPaint: Paint? = null
    private var mCurrentHue = 0f
    private var mCurrentX = 0
    private var mCurrentY = 0
    private var mCurrentColor: Int = 0
    private var mDefaultColor: Int = 0   // mDefaultColor 는 기존 색상으로 취소시 적용된다.
    private val mHueBarColors = IntArray(258)
    private val mMainColors = IntArray(65536)
    private var mListener: OnColorChangedListener? = null

    private val currentMainColor: Int
        get() {
            val translatedHue = 255 - (mCurrentHue * 255 / 360).toInt()
            var index = 0
            run {
                var i = 0f
                while (i < 256) {
                    if (index == translatedHue)
                        return Color.rgb(255, 0, i.toInt())
                    index++
                    i += (256 / 42).toFloat()
                }
            }
            run {
                var i = 0f
                while (i < 256) {
                    if (index == translatedHue)
                        return Color.rgb(255 - i.toInt(), 0, 255)
                    index++
                    i += (256 / 42).toFloat()
                }
            }
            run {
                var i = 0f
                while (i < 256) {
                    if (index == translatedHue)
                        return Color.rgb(0, i.toInt(), 255)
                    index++
                    i += (256 / 42).toFloat()
                }
            }
            run {
                var i = 0f
                while (i < 256) {
                    if (index == translatedHue)
                        return Color.rgb(0, 255, 255 - i.toInt())
                    index++
                    i += (256 / 42).toFloat()
                }
            }
            run {
                var i = 0f
                while (i < 256) {
                    if (index == translatedHue)
                        return Color.rgb(i.toInt(), 255, 0)
                    index++
                    i += (256 / 42).toFloat()
                }
            }
            var i = 0f
            while (i < 256) {
                if (index == translatedHue)
                    return Color.rgb(255, 255 - i.toInt(), 0)
                index++
                i += (256 / 42).toFloat()
            }
            return Color.RED
        }

    interface OnColorChangedListener {
        fun colorChanged(colorChange: ColorChange, color: Int)
    }

    enum class ColorChange {
        CHANGE, OK, CANCEL
    }

    fun init(color: Int, l: OnColorChangedListener) {
        mDefaultColor = color
        mListener = l

        // Get the current hue from the current color and update the main color field
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        mCurrentHue = hsv[0]
        updateMainColors()

        mCurrentColor = color

        // Initialize the colors of the hue slider bar
        var index = 0
        run {
            var i = 0f
            while (i < 256)
            // Red (#f00) to pink (#f0f)
            {
                mHueBarColors[index] = Color.rgb(255, 0, i.toInt())
                index++
                i += (256 / 42).toFloat()
            }
        }
        run {
            var i = 0f
            while (i < 256)
            // Pink (#f0f) to blue (#00f)
            {
                mHueBarColors[index] = Color.rgb(255 - i.toInt(), 0, 255)
                index++
                i += (256 / 42).toFloat()
            }
        }
        run {
            var i = 0f
            while (i < 256)
            // Blue (#00f) to light blue (#0ff)
            {
                mHueBarColors[index] = Color.rgb(0, i.toInt(), 255)
                index++
                i += (256 / 42).toFloat()
            }
        }
        run {
            var i = 0f
            while (i < 256)
            // Light blue (#0ff) to green (#0f0)
            {
                mHueBarColors[index] = Color.rgb(0, 255, 255 - i.toInt())
                index++
                i += (256 / 42).toFloat()
            }
        }
        run {
            var i = 0f
            while (i < 256)
            // Green (#0f0) to yellow (#ff0)
            {
                mHueBarColors[index] = Color.rgb(i.toInt(), 255, 0)
                index++
                i += (256 / 42).toFloat()
            }
        }
        var i = 0f
        while (i < 256)
        // Yellow (#ff0) to red (#f00)
        {
            mHueBarColors[index] = Color.rgb(255, 255 - i.toInt(), 0)
            index++
            i += (256 / 42).toFloat()
        }

        // Initializes the Paint that will draw the View
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.textAlign = Paint.Align.CENTER
        mPaint!!.textSize = (mWidth / 20).toFloat()
    }

    private fun updateMainColors() {
        val mainColor = currentMainColor
        var index = 0
        val topColors = IntArray(256)
        for (y in 0..255) {
            for (x in 0..255) {
                if (y == 0) {
                    mMainColors[index] = Color.rgb(255 - (255 - Color.red(mainColor)) * x / 255, 255 - (255 - Color.green(mainColor)) * x / 255, 255 - (255 - Color.blue(mainColor)) * x / 255)
                    topColors[x] = mMainColors[index]
                } else
                    mMainColors[index] = Color.rgb((255 - y) * Color.red(topColors[x]) / 255, (255 - y) * Color.green(topColors[x]) / 255, (255 - y) * Color.blue(topColors[x]) / 255)
                index++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val translatedHue = 255 - (mCurrentHue * 255 / 360).toInt()
        // Display all the colors of the hue bar with lines
        val thick = mWidth / 256f
        for (x in 0..255) {
            // If this is not the current selected hue, display the actual color
            if (translatedHue != x) {
                mPaint!!.color = mHueBarColors[x]
                mPaint!!.strokeWidth = thick
            } else
            // else display a slightly larger black line
            {
                mPaint!!.color = Color.BLACK
                mPaint!!.strokeWidth = thick * 3
            }
            canvas.drawLine(x * thick, 0f, x * thick, (mHeight * 0.15).toFloat(), mPaint!!)
        }

        // Display the main field colors using LinearGradient
        for (x in 0..255) {
            val colors = IntArray(2)
            colors[0] = mMainColors[x]
            colors[1] = Color.BLACK
            val shader = LinearGradient(0f, (mHeight * 0.16).toFloat(), 0f, (mHeight * 0.85).toFloat(), colors, null, Shader.TileMode.REPEAT)
            mPaint!!.shader = shader
            canvas.drawLine(x * thick, (mHeight * 0.16).toFloat(), x * thick, (mHeight * 0.85).toFloat(), mPaint!!)
        }
        mPaint!!.shader = null

        // Display the circle around the currently selected color in the main field
        if (mCurrentX != 0 && mCurrentY != 0) {
            mPaint!!.style = Paint.Style.STROKE
            if (isDark(mCurrentColor))
                mPaint!!.color = Color.WHITE
            else
                mPaint!!.color = Color.BLACK
            canvas.drawCircle(mCurrentX.toFloat(), mCurrentY.toFloat(), 10f, mPaint!!)
        }

        // Draw a 'button' with the currently selected color
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.color = mCurrentColor
        canvas.drawRect((mWidth * 0.01).toFloat(), (mHeight * 0.86).toFloat(), (mWidth * 0.49).toFloat(), (mHeight * 0.99).toFloat(), mPaint!!)

        // Set the text color according to the brightness of the color
        if (isDark(mCurrentColor))
            mPaint!!.color = Color.WHITE
        else
            mPaint!!.color = Color.BLACK
        canvas.drawText(resources.getString(R.string.confirm), (mWidth * 0.25).toFloat(), (mHeight * 0.94).toFloat(), mPaint!!)

        // Draw a 'button' with the default color
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.color = mDefaultColor
        canvas.drawRect((mWidth * 0.51).toFloat(), (mHeight * 0.86).toFloat(), (mWidth * 0.99).toFloat(), (mHeight * 0.99).toFloat(), mPaint!!)

        // Set the text color according to the brightness of the color
        if (isDark(mDefaultColor))
            mPaint!!.color = Color.WHITE
        else
            mPaint!!.color = Color.BLACK
        canvas.drawText(resources.getString(R.string.cancel), (mWidth * 0.75).toFloat(), (mHeight * 0.94).toFloat(), mPaint!!)
    }

    private fun isDark(color: Int): Boolean {
        return Color.red(color) + Color.green(color) + Color.blue(color) < 384
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x
        val y = event.y

        // If the touch event is located in the hue bar
        if (x > 10 && x < mWidth && y > 0 && y < (mHeight * 0.15).toFloat()) {
            // Update the main field colors
            val thick = mWidth / 256f
            mCurrentHue = (255 - x / thick) * 360 / 255
            updateMainColors()

            // Update the current selected color
            val transX = (mCurrentX - 10) * 256 / (mWidth - 10)
            val transY = ((mCurrentY - mHeight * 0.16) * 256 / (mHeight * 0.69)).toInt()
            val index = 256 * transY + transX
            if (index > 0 && index < mMainColors.size)
                mCurrentColor = mMainColors[index]

            // Force the redraw of the dialog
            invalidate()
        }

        // If the touch event is located in the main field
        if (x > 10 && x < mWidth && y > (mHeight * 0.16).toFloat() && y < (mHeight * 0.85).toFloat()) {
            mCurrentX = x.toInt()
            mCurrentY = y.toInt()
            val transX = (mCurrentX - 10) * 256 / (mWidth - 10)
            val transY = ((mCurrentY - mHeight * 0.16) * 256 / (mHeight * 0.69)).toInt()
            val index = 256 * transY + transX
            if (index > 0 && index < mMainColors.size) {
                // Update the current color
                mCurrentColor = mMainColors[index]
                // Force the redraw of the dialog
                invalidate()
            }
        }

        if (x > (mWidth * 0.04).toFloat() && x < (mWidth * 0.48).toFloat() && y > (mHeight * 0.85).toFloat() && y < mHeight)
            mListener!!.colorChanged(ColorChange.OK, mCurrentColor)
        else if (x > (mWidth * 0.52).toFloat() && x < (mWidth * 0.96).toFloat() && y > (mHeight * 0.85).toFloat() && y < mHeight)
            mListener!!.colorChanged(ColorChange.CANCEL, mDefaultColor)
        else
            mListener!!.colorChanged(ColorChange.CHANGE, mCurrentColor)

        return true
    }

}
