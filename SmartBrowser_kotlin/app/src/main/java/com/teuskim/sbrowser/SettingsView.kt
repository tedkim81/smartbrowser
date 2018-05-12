package com.teuskim.sbrowser

import java.util.ArrayList
import java.util.TreeMap

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import com.teuskim.sbrowser.ColorPickerView.ColorChange
import com.teuskim.sbrowser.ColorPickerView.OnColorChangedListener

class SettingsView : LinearLayout {

    private var mContext: Context? = null
    private var mColorPickerLayout: ViewGroup? = null
    private var mOnBgColorChangedListener: ColorPickerView.OnColorChangedListener? = null
    private var mOnFontColor1ChangedListener: ColorPickerView.OnColorChangedListener? = null
    private var mOnAlphaChangedListener: OnAlphaChangedListener? = null  // remocon alpha
    private var mOnFontSizeChangedListener: OnFontSizeChangedListener? = null  // content font size
    private var mOnLineSpaceChangedListener: OnLineSpaceChangedListener? = null  // content line space
    private var mOnPaddingChangedListener: OnPaddingChangedListener? = null  // content area padding
    private var mRemoconAlpha: Int = 0
    private var mSeekBar: SeekBar? = null
    private var mBtnColorBg: View? = null
    private var mBtnColorFont: View? = null
    private var mBgColor: Int = 0
    private var mFontColor: Int = 0
    private var mTextFontSize: TextView? = null
    private var mFontSize: Int = 0
    private var mTextLineSpace: TextView? = null
    private var mLineSpace: Int = 0
    private var mTextPadding: TextView? = null
    private var mPadding: Int = 0
    private var mViewCVMSettings: View? = null
    private var mViewNoCVM: View? = null
    private var mTextPageUrl: TextView? = null
    private var mBtnFacebook: View? = null
    private var mBtnKakao: View? = null

    private var mSettingsMainLayout: ViewGroup? = null
    private var mSelectBgLayout: ViewGroup? = null
    private var mOnCloseClickListener: View.OnClickListener? = null
    private var mInAnimation: AnimationSet? = null
    private var mOutAnimation: AnimationSet? = null

    interface OnAlphaChangedListener {
        fun onAlphaChanged(alpha: Int)
    }

    interface OnFontSizeChangedListener {
        fun onFontSizeChanged(size: Int)
    }

    interface OnLineSpaceChangedListener {
        fun onLineSpaceChanged(space: Int)
    }

    interface OnPaddingChangedListener {
        fun onPaddingChanged(padding: Int)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.settings_view, this)
        mBtnColorBg = findViewById(R.id.btn_color_bg)
        mBtnColorFont = findViewById(R.id.btn_color_font)
        mTextFontSize = findViewById<View>(R.id.text_font_size) as TextView
        mTextLineSpace = findViewById<View>(R.id.text_line_space) as TextView
        mTextPadding = findViewById<View>(R.id.text_padding) as TextView
        mViewCVMSettings = findViewById(R.id.content_view_mode_settings)
        mViewNoCVM = findViewById(R.id.no_content_view_mode)
        mTextPageUrl = findViewById<View>(R.id.text_page_url) as TextView
        mSeekBar = findViewById<View>(R.id.seekbar_remocon_alpha) as SeekBar
        mSettingsMainLayout = findViewById<View>(R.id.settings_main_layout) as ViewGroup
        mBtnFacebook = findViewById(R.id.btn_facebook)
        mBtnKakao = findViewById(R.id.btn_kakao)

        mBtnFacebook!!.visibility = View.GONE
        mBtnKakao!!.visibility = View.GONE

        // 버그때문에 여기서 셋팅
        mSeekBar!!.progressDrawable = resources.getDrawable(R.drawable.progress)
        mSeekBar!!.thumb = resources.getDrawable(R.drawable.btn_bar_control)

        val listener = OnClickListener { v ->
            when (v.id) {
                R.id.btn_close_settings -> close()
                R.id.btn_color_bg, R.id.btn_color_bg_choose -> showSelectBgView(mOnBgColorChangedListener)
                R.id.btn_color_font, R.id.btn_color_font_choose -> showColorPickerView(mFontColor, mOnFontColor1ChangedListener)
                R.id.btn_plus_font_size -> setFontSize(mFontSize + 1)
                R.id.btn_minus_font_size -> setFontSize(mFontSize - 1)
                R.id.btn_plus_line_space -> setLineSpace(mLineSpace + 1)
                R.id.btn_minus_line_space -> setLineSpace(mLineSpace - 1)
                R.id.btn_plus_padding -> setContPadding(mPadding + 5)
                R.id.btn_minus_padding -> setContPadding(mPadding - 5)
            }
        }
        findViewById<View>(R.id.btn_close_settings).setOnClickListener(listener)
        mBtnColorBg!!.setOnClickListener(listener)
        mBtnColorFont!!.setOnClickListener(listener)
        findViewById<View>(R.id.btn_color_bg_choose).setOnClickListener(listener)
        findViewById<View>(R.id.btn_color_font_choose).setOnClickListener(listener)
        findViewById<View>(R.id.btn_plus_font_size).setOnClickListener(listener)
        findViewById<View>(R.id.btn_minus_font_size).setOnClickListener(listener)
        findViewById<View>(R.id.btn_plus_line_space).setOnClickListener(listener)
        findViewById<View>(R.id.btn_minus_line_space).setOnClickListener(listener)
        findViewById<View>(R.id.btn_plus_padding).setOnClickListener(listener)
        findViewById<View>(R.id.btn_minus_padding).setOnClickListener(listener)

        mSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setRemoconAlpha(mRemoconAlpha)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mRemoconAlpha = progress
                if (mOnAlphaChangedListener != null)
                    mOnAlphaChangedListener!!.onAlphaChanged(mRemoconAlpha)
            }
        })

        createAnimations()
    }

    fun open() {
        if (visibility != View.VISIBLE) {
            startAnimation(mInAnimation)
            visibility = View.VISIBLE
        }
    }

    fun close() {
        if (visibility != View.GONE) {
            startAnimation(mOutAnimation)
            visibility = View.GONE
        }

        if (mOnCloseClickListener != null)
            mOnCloseClickListener!!.onClick(null)
    }

    fun setOnCloseClickListener(listener: View.OnClickListener) {
        mOnCloseClickListener = listener
    }

    fun setFacebookClickListener(listener: View.OnClickListener) {
        mBtnFacebook!!.setOnClickListener(listener)
        mBtnFacebook!!.visibility = View.VISIBLE
    }

    fun setKakaoClickListener(listener: View.OnClickListener) {
        mBtnKakao!!.setOnClickListener(listener)
        mBtnKakao!!.visibility = View.VISIBLE
    }

    fun showColorPickerView(color: Int, listener: OnColorChangedListener?) {
        // lazy initiate
        if (mColorPickerLayout == null) {
            mColorPickerLayout = findViewById<View>(R.id.color_picker_layout) as ViewGroup
            val padding = mColorPickerLayout!!.paddingLeft * 2
            mColorPickerLayout!!.addView(ColorPickerView(mContext!!, mColorPickerLayout!!.width - padding, mColorPickerLayout!!.height - padding))
        }
        (mColorPickerLayout!!.getChildAt(0) as ColorPickerView).init(color, listener!!)
        mColorPickerLayout!!.visibility = View.VISIBLE
    }

    fun hideColorPickerView() {
        if (mColorPickerLayout != null)
            mColorPickerLayout!!.visibility = View.INVISIBLE
    }

    fun hideSelectBgView() {
        if (mSelectBgLayout != null)
            mSelectBgLayout!!.visibility = View.GONE
        mSettingsMainLayout!!.visibility = View.VISIBLE
    }

    fun setOnAlphaChangedListener(listener: OnAlphaChangedListener) {
        mOnAlphaChangedListener = listener
    }

    fun setRemoconAlpha(remoconAlpha: Int) {
        if (remoconAlpha < MIN_REMOCON_ALPHA)
            mRemoconAlpha = MIN_REMOCON_ALPHA
        else if (remoconAlpha > MAX_REMOCON_ALPHA)
            mRemoconAlpha = MAX_REMOCON_ALPHA
        else
            mRemoconAlpha = remoconAlpha

        mSeekBar!!.progress = mRemoconAlpha
        if (mOnAlphaChangedListener != null)
            mOnAlphaChangedListener!!.onAlphaChanged(mRemoconAlpha)
    }

    fun setOnColorChangedListener(bgColorListener: ColorPickerView.OnColorChangedListener, fontColor1Listener: ColorPickerView.OnColorChangedListener) {

        mOnBgColorChangedListener = bgColorListener
        mOnFontColor1ChangedListener = fontColor1Listener
    }

    fun setContBg(bg: Int) {
        if (SettingsView.sPatternMap.containsKey(bg)) {
            mBtnColorBg!!.setBackgroundResource(SettingsView.sPatternMap[bg]!!)
        } else {
            mBtnColorBg!!.setBackgroundColor(bg)
        }
        mBgColor = bg
    }

    fun setFontColor(color: Int) {
        mBtnColorFont!!.setBackgroundColor(color)
        mFontColor = color
    }

    fun setFontSize(size: Int) {
        if (mOnFontSizeChangedListener != null) {
            mOnFontSizeChangedListener!!.onFontSizeChanged(size)
        }
        mTextFontSize!!.text = "" + size
        mFontSize = size
    }

    fun setOnFontSizeChangedListener(listener: OnFontSizeChangedListener) {
        mOnFontSizeChangedListener = listener
    }

    fun setLineSpace(space: Int) {
        if (mOnLineSpaceChangedListener != null) {
            mOnLineSpaceChangedListener!!.onLineSpaceChanged(space)
        }
        mTextLineSpace!!.text = "" + space
        mLineSpace = space
    }

    fun setOnLineSpaceChangedListener(listener: OnLineSpaceChangedListener) {
        mOnLineSpaceChangedListener = listener
    }

    fun setContPadding(padding: Int) {
        if (mOnPaddingChangedListener != null) {
            mOnPaddingChangedListener!!.onPaddingChanged(padding)
        }
        mTextPadding!!.text = "" + padding
        mPadding = padding
    }

    fun setOnPaddingChangedListener(listener: OnPaddingChangedListener) {
        mOnPaddingChangedListener = listener
    }

    fun showCVMSettings(isShow: Boolean) {
        if (isShow) {
            mViewCVMSettings!!.visibility = View.VISIBLE
            mViewNoCVM!!.visibility = View.GONE
        } else {
            mViewCVMSettings!!.visibility = View.GONE
            mViewNoCVM!!.visibility = View.VISIBLE
        }
    }

    fun setPageUrl(url: String) {
        mTextPageUrl!!.text = url
    }

    private fun createAnimations() {
        if (mInAnimation == null) {
            mInAnimation = AnimationSet(false)
            mInAnimation!!.interpolator = AccelerateInterpolator()
            mInAnimation!!.addAnimation(TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f))
            mInAnimation!!.duration = ANIMATION_DURATION.toLong()
        }

        if (mOutAnimation == null) {
            mOutAnimation = AnimationSet(false)
            mOutAnimation!!.interpolator = AccelerateInterpolator()
            mOutAnimation!!.addAnimation(TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f))
            mOutAnimation!!.duration = ANIMATION_DURATION.toLong()
        }
    }


    fun showSelectBgView(listener: OnColorChangedListener?) {
        // lazy initiate
        if (mSelectBgLayout == null) {
            mSelectBgLayout = findViewById<View>(R.id.select_bg_layout) as ViewGroup
            val recommendBgGrid = findViewById<View>(R.id.recommend_bg_layout) as GridView
            recommendBgGrid.adapter = RecommendBgAdapter()
            recommendBgGrid.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                val bg = recommendBgGrid.adapter.getItemId(position).toInt()
                mBgColor = bg
                listener!!.colorChanged(ColorChange.CHANGE, bg)
            }
            findViewById<View>(R.id.btn_color_bg_2).setOnClickListener { showColorPickerView(mBgColor, mOnBgColorChangedListener) }

            val btnListener = OnClickListener { v ->
                when (v.id) {
                    R.id.btn_select_bg_ok -> listener!!.colorChanged(ColorChange.OK, mBgColor)
                    R.id.btn_select_bg_cancel -> listener!!.colorChanged(ColorChange.OK, MiscPref.getInstance(mContext!!).contBgColor)  // TODO: 리팩토링 필요
                }
            }
            findViewById<View>(R.id.btn_select_bg_ok).setOnClickListener(btnListener)
            findViewById<View>(R.id.btn_select_bg_cancel).setOnClickListener(btnListener)
        }
        val btnColorBg2 = findViewById<View>(R.id.btn_color_bg_2)
        if (sPatternMap.containsKey(mBgColor)) {
            btnColorBg2.setBackgroundResource(sPatternMap[mBgColor]!!)
        } else {
            btnColorBg2.setBackgroundColor(mBgColor)
        }
        mSelectBgLayout!!.visibility = View.VISIBLE
        mSettingsMainLayout!!.visibility = View.GONE
    }

    private inner class RecommendBgAdapter : BaseAdapter() {

        private val patternKeys: MutableList<Int>
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(mContext)
            patternKeys = ArrayList()
            val it = sPatternMap.keys.iterator()
            while (it.hasNext()) {
                patternKeys.add(it.next())
            }
        }

        override fun getCount(): Int {
            return patternKeys.size
        }

        override fun getItem(position: Int): Int? {
            return sPatternMap[getItemId(position).toInt()]
        }

        override fun getItemId(position: Int): Long {
            return patternKeys[position].toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val v = mInflater.inflate(R.layout.recommend_bg_item, null)
            (v.findViewById<View>(R.id.recommend_bg_image) as ImageView).setBackgroundResource(getItem(position)!!)
            return v
        }

    }

    companion object {

        private val MAX_REMOCON_ALPHA = 250
        private val MIN_REMOCON_ALPHA = 50

        private val ANIMATION_DURATION = 300
        val PATTERN_0 = 0x00000001
        val PATTERN_1 = 0x00000002
        val PATTERN_2 = 0x00000003
        val PATTERN_3 = 0x00000004
        val PATTERN_4 = 0x00000005
        val PATTERN_5 = 0x00000006
        val PATTERN_6 = 0x00000007
        val PATTERN_7 = 0x00000008
        val PATTERN_8 = 0x00000009
        val PATTERN_9 = 0x00000010
        val PATTERN_10 = 0x00000011
        val PATTERN_11 = 0x00000012
        val PATTERN_12 = 0x00000013
        val PATTERN_13 = 0x00000014
        val PATTERN_14 = 0x00000015
        val PATTERN_15 = 0x00000016
        val PATTERN_16 = 0x00000017
        val PATTERN_17 = 0x00000018
        val PATTERN_18 = 0x00000019
        val PATTERN_19 = 0x00000020
        var sPatternMap: MutableMap<Int, Int> = TreeMap()

        init {
            sPatternMap[PATTERN_0] = R.drawable.pattern0
            sPatternMap[PATTERN_1] = R.drawable.pattern1
            sPatternMap[PATTERN_2] = R.drawable.pattern2
            sPatternMap[PATTERN_3] = R.drawable.pattern3
            sPatternMap[PATTERN_4] = R.drawable.pattern4
            sPatternMap[PATTERN_5] = R.drawable.pattern5
            sPatternMap[PATTERN_6] = R.drawable.pattern_note1
            sPatternMap[PATTERN_7] = R.drawable.pattern_note2
            sPatternMap[PATTERN_8] = R.drawable.pattern_note3
            sPatternMap[PATTERN_9] = R.drawable.pattern_note4
            sPatternMap[PATTERN_10] = R.drawable.pattern_note5
            sPatternMap[PATTERN_11] = R.drawable.pattern_note6
            sPatternMap[PATTERN_12] = R.drawable.pattern_note7
            sPatternMap[PATTERN_13] = R.drawable.pattern_note8
            sPatternMap[PATTERN_14] = R.drawable.pattern_note9
            sPatternMap[PATTERN_15] = R.drawable.pattern_note10
            sPatternMap[PATTERN_16] = R.drawable.pattern_note11
            sPatternMap[PATTERN_17] = R.drawable.pattern_note12
            sPatternMap[PATTERN_18] = R.drawable.pattern_note13
            sPatternMap[PATTERN_19] = R.drawable.pattern_note14
        }

        var sRepeatMap: MutableMap<Int, Int> = TreeMap()

        init {
            sRepeatMap[PATTERN_0] = R.drawable.bg_content_repeat_0
            sRepeatMap[PATTERN_1] = R.drawable.bg_content_repeat_1
            sRepeatMap[PATTERN_2] = R.drawable.bg_content_repeat_2
            sRepeatMap[PATTERN_3] = R.drawable.bg_content_repeat_3
            sRepeatMap[PATTERN_4] = R.drawable.bg_content_repeat_4
            sRepeatMap[PATTERN_5] = R.drawable.bg_content_repeat_5
            sRepeatMap[PATTERN_6] = R.drawable.bg_content_note_repeat_1
            sRepeatMap[PATTERN_7] = R.drawable.bg_content_note_repeat_2
            sRepeatMap[PATTERN_8] = R.drawable.bg_content_note_repeat_3
            sRepeatMap[PATTERN_9] = R.drawable.bg_content_note_repeat_4
            sRepeatMap[PATTERN_10] = R.drawable.bg_content_note_repeat_5
            sRepeatMap[PATTERN_11] = R.drawable.bg_content_note_repeat_6
            sRepeatMap[PATTERN_12] = R.drawable.bg_content_note_repeat_7
            sRepeatMap[PATTERN_13] = R.drawable.bg_content_note_repeat_8
            sRepeatMap[PATTERN_14] = R.drawable.bg_content_note_repeat_9
            sRepeatMap[PATTERN_15] = R.drawable.bg_content_note_repeat_10
            sRepeatMap[PATTERN_16] = R.drawable.bg_content_note_repeat_11
            sRepeatMap[PATTERN_17] = R.drawable.bg_content_note_repeat_12
            sRepeatMap[PATTERN_18] = R.drawable.bg_content_note_repeat_13
            sRepeatMap[PATTERN_19] = R.drawable.bg_content_note_repeat_14
        }
    }

}
