package com.teuskim.sbrowser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Handler
import android.text.*
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebView
import android.widget.*
import android.widget.TextView.BufferType
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ContentView : RelativeLayout {

    private var mLayoutScroll: ScrollView? = null
    private var mLayoutContent: View? = null
    private var mTextContent: TextView? = null
    private var mLayoutBottom: View? = null
    private var mBtnCloseLayoutBottom: Button? = null
    private var mLayoutWord: View? = null
    private var mBtnSearch: TextButton? = null
    private var mBtnTranslate: TextButton? = null
    private var mBtnCopy: TextButton? = null
    private var mLayoutWebViewSearch: View? = null
    private var mTextWebViewSearchTitle: TextView? = null
    private var mBtnCloseWebViewSearch: View? = null
    private var mWebView: SbWebView? = null
    private var mWebViewLoading: View? = null

    private var mSelectedX: Int = 0
    private var mSelectedY: Int = 0
    private var mSelectedWord: String? = null
    private var mSpannable: Spannable? = null
    private var mBackgroundColorSpan: BackgroundColorSpan? = null
    private var mDidSetBackground = false
    private var mContext: Context? = null

    private val mHandler = Handler()
    private val mImageMap = HashMap<String, Drawable>()

    var content: String
        get() = mTextContent!!.text.toString()
        set(content) {
            mTextContent!!.text = ""

            object : AsyncTask<String, Void, Spanned>() {

                private var mContent: String? = null

                override fun doInBackground(vararg params: String): Spanned {
                    mContent = params[0].replace("<img", ">img")
                            .replace("\\s".toRegex(), " ")
                            .replace("(<!--)[^>]*(-->)".toRegex(), "")
                            .replace("<script.*>.*</script>".toRegex(), "")
                            .replace("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>".toRegex(), "<br/>")
                            .replace("((\\s)*<br/>){6}".toRegex(), "<br/><br/>")
                            .replace("((\\s)*<br/>){5}".toRegex(), "<br/><br/>")
                            .replace("((\\s)*<br/>){4}".toRegex(), "<br/><br/>")
                            .replace("((\\s)*<br/>){3}".toRegex(), "<br/><br/>")
                            .replace(">img", "<img")
                    return Html.fromHtml(mContent)
                }

                override fun onPostExecute(result: Spanned) {
                    mTextContent!!.setText(result, BufferType.SPANNABLE)
                    object : AsyncTask<String, Void, Spanned>() {

                        override fun doInBackground(vararg params: String): Spanned {
                            val minSize = width / 2

                            return Html.fromHtml(mContent, Html.ImageGetter { source ->
                                val downloader = Downloader(mContext!!)
                                val imgFile = Downloader.getDefaultFile(MiscUtils.getImageCacheDirectory(mContext!!), source)
                                if (downloader.get(source, imgFile)) {
                                    val bm = BitmapFactory.decodeFile(imgFile.path)
                                    if (bm != null) {
                                        val img = BitmapDrawable(bm)
                                        val w: Int
                                        val h: Int
                                        if (img.intrinsicWidth < img.intrinsicHeight) {
                                            if (img.intrinsicWidth >= minSize)
                                                w = img.intrinsicWidth
                                            else
                                                w = minSize
                                            h = w * img.intrinsicHeight / img.intrinsicWidth
                                        } else {
                                            if (img.intrinsicHeight >= minSize)
                                                h = img.intrinsicHeight
                                            else
                                                h = minSize
                                            w = h * img.intrinsicWidth / img.intrinsicHeight
                                        }
                                        img.setBounds(0, 0, w, h)
                                        return@ImageGetter img
                                    }
                                }
                                null
                            }, null)
                        }

                        override fun onPostExecute(result: Spanned) {
                            mTextContent!!.setText(result, BufferType.SPANNABLE)

                            if (mDidSetBackground == false) {
                                val handler = Handler()
                                handler.postDelayed({ setBackground(MiscPref.getInstance(context).contBgColor) }, 100)
                            }
                        }

                    }.execute()
                }

            }.execute(content)
        }

    val isShowWebViewSearch: Boolean
        get() = mLayoutWebViewSearch!!.visibility == View.VISIBLE

    private val mTranslateRunnable = object : Runnable {

        override fun run() {
            if (mWebView!!.isPageFinished) {
                val selected = mSelectedWord!!.trim { it <= ' ' }.replace("\n", " ").replace("'", "\\'")
                val js = ("function fillSbData(){"
                        + "if(typeof document.getElementsByTagName('textarea')[0] != 'undefined'){"
                        + "document.getElementsByTagName('textarea')[0].value='" + selected + "';"
                        + "_e(null, 'translate+2');"
                        + "}else{"
                        + "setTimeout('fillSbData();',1000);"
                        + "}"
                        + "}"
                        + "fillSbData();")
                mWebView!!.injectJs(js)
            } else {
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    var scrollTop: Int
        get() = mLayoutScroll!!.scrollY
        set(scrollTop) = mLayoutScroll!!.scrollTo(0, scrollTop)

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        if (visibility == View.GONE) mImageMap.clear()
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
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.content_view, this)
        mLayoutScroll = findViewById<View>(R.id.layout_scroll) as ScrollView
        mLayoutContent = findViewById(R.id.layout_content_body)
        mTextContent = findViewById<View>(R.id.text_content) as TextView
        mLayoutBottom = findViewById(R.id.layout_bottom)
        mBtnCloseLayoutBottom = findViewById<View>(R.id.btn_close_layout_bottom) as Button
        mLayoutWord = findViewById(R.id.layout_word)
        TextButton.setTextColor(-0x1000000, -0x16b7e0)
        mBtnSearch = findViewById<View>(R.id.btn_search) as TextButton
        mBtnTranslate = findViewById<View>(R.id.btn_translate) as TextButton
        mBtnCopy = findViewById<View>(R.id.btn_copy) as TextButton
        mLayoutWebViewSearch = findViewById(R.id.layout_webview_search)
        mTextWebViewSearchTitle = findViewById<View>(R.id.text_webview_search_title) as TextView
        mBtnCloseWebViewSearch = findViewById(R.id.btn_close_webview_search)
        mWebView = findViewById<View>(R.id.webview_search) as SbWebView
        mWebViewLoading = findViewById(R.id.webview_search_loading)
        mWebView!!.setClients(SbWebView.SbWebChromeClient(context), object : SbWebView.SbWebViewClient(context, mWebView!!) {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mWebViewLoading!!.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mWebViewLoading!!.visibility = View.GONE
            }

        })
        mBackgroundColorSpan = BackgroundColorSpan(-0x7f00baf2)

        mTextContent!!.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> hideLayoutWord()
                MotionEvent.ACTION_UP -> showLayoutWord(event)
            }
            true
        }

        val clistener = OnClickListener { v ->
            when (v.id) {
                R.id.btn_search -> btnSearch()
                R.id.btn_translate -> btnTranslate()
                R.id.btn_copy -> btnCopy()
                R.id.btn_close_webview_search -> hideWebViewSearch()
                R.id.btn_close_layout_bottom -> hideLayoutBottom()
                R.id.btn_span_left -> btnSpanLeft()
                R.id.btn_span_below -> btnSpanBelow()
                R.id.btn_span_right -> btnSpanRight()
                R.id.btn_span_all -> btnSpanAll()
            }
        }
        mBtnSearch!!.setOnClickListener(clistener)
        mBtnTranslate!!.setOnClickListener(clistener)
        mBtnCopy!!.setOnClickListener(clistener)
        mBtnCloseWebViewSearch!!.setOnClickListener(clistener)
        mBtnCloseLayoutBottom!!.setOnClickListener(clistener)
        findViewById<View>(R.id.btn_span_left).setOnClickListener(clistener)
        findViewById<View>(R.id.btn_span_below).setOnClickListener(clistener)
        findViewById<View>(R.id.btn_span_right).setOnClickListener(clistener)
        findViewById<View>(R.id.btn_span_all).setOnClickListener(clistener)
    }

    fun setFontColor(color: Int) {
        mTextContent!!.setTextColor(color)
    }

    fun setFontSize(size: Int) {
        mTextContent!!.textSize = size.toFloat()
    }

    fun setLineSpace(space: Int) {
        mTextContent!!.setLineSpacing(space.toFloat(), 1f)
    }

    private fun showLayoutWord(e: MotionEvent) {
        val cont = mTextContent!!.text
        val layout = mTextContent!!.layout
        val line = layout.getLineForVertical(e.y.toInt())
        var offset = layout.getOffsetForHorizontal(line, e.x)
        while (offset >= 0) {
            if (isChar(cont[offset]) == false) break
            offset--
        }
        val start = ++offset
        while (offset < cont.length) {
            if (isChar(cont[offset]) == false) break
            offset++
        }
        val end = offset
        if (mSpannable != null)
            Selection.removeSelection(mSpannable)  // TODO: 기존 선택된 단어를 원복하려고 하는데.. 잘 안된다..
        mSpannable = cont as Spannable
        mSpannable!!.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        Selection.setSelection(mSpannable, start, end)

        mSelectedX = (mLayoutContent!!.paddingLeft + (layout.getPrimaryHorizontal(start) + layout.getPrimaryHorizontal(end)) / 2 - mLayoutWord!!.width / 2).toInt()
        mSelectedY = layout.getLineBaseline(line) - mLayoutScroll!!.scrollY + mLayoutContent!!.paddingTop
        if (mSelectedX > width - mLayoutWord!!.width - mLayoutContent!!.paddingLeft - mLayoutContent!!.paddingRight)
            mSelectedX = width - mLayoutWord!!.width - mLayoutContent!!.paddingLeft - mLayoutContent!!.paddingRight
        else if (mSelectedX < 0)
            mSelectedX = 0
        mSelectedWord = cont.subSequence(start, end).toString()
        if (TextUtils.isEmpty(mSelectedWord)) {
            mLayoutWord!!.visibility = View.GONE
        } else {
            val lp = mLayoutWord!!.layoutParams as RelativeLayout.LayoutParams
            lp.setMargins(mSelectedX, mSelectedY, 0, 0)
            mLayoutWord!!.layoutParams = lp
            mLayoutWord!!.visibility = View.VISIBLE
        }
    }

    private fun isChar(ch: Char): Boolean {
        return ch.toInt() >= 97 && ch.toInt() <= 122 || ch.toInt() >= 65 && ch.toInt() <= 90 || ch.toInt() >= 0x3131 && ch.toInt() <= 0xd7a3
    }

    fun hideLayoutWord() {
        mLayoutWord!!.visibility = View.INVISIBLE
    }

    fun hideWebViewSearch() {
        mLayoutWebViewSearch!!.visibility = View.GONE
    }

    fun hideLayoutBottom() {
        mLayoutBottom!!.visibility = View.GONE
    }

    private fun btnSearch() {
        var url: String? = null
        try {
            url = "http://www.google.com/search?q=" + URLEncoder.encode(mSelectedWord, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            return
        }

        mWebView!!.loadUrl(url)
        mTextWebViewSearchTitle!!.text = resources.getString(R.string.search) + " > " + mSelectedWord
        mLayoutWebViewSearch!!.visibility = View.VISIBLE

        hideLayoutWord()
    }

    private fun btnTranslate() {
        /* TODO : 검색 api 를 사용할 수가 없어서 번역 페이지로 이동하도록 수정. mLayoutBottom을 다른걸로 활용할지 확인하고 없다면 삭제하자.
		mTextTranslate.setText("google\n구글. 구글로 검색하다.");
		mLayoutBottom.setVisibility(View.VISIBLE);
		*/

        /* TODO: q 가 적용이 제대로 되지 않는다. 추후 될때가 되면 아래 코드를 살리자.
		String url = null;
		try {
			url = "http://translate.google.com/m/translate?q="+URLEncoder.encode(mSelectedWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		*/
        val url = "http://translate.google.com/m/translate"

        mWebView!!.loadUrl(url)
        mTextWebViewSearchTitle!!.text = resources.getString(R.string.translate) + " > " + mSelectedWord
        mLayoutWebViewSearch!!.visibility = View.VISIBLE

        hideLayoutWord()

        // TODO: q를 넘김에도 번역페이지에 결과가 나오지 않는 문제가 있다. 그래서 javascript로 해당 내용이 출력되도록 임의 조치한다.
        mHandler.postDelayed(mTranslateRunnable, 1000)
    }

    private fun btnCopy() {
        val cm = mContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.text = mSelectedWord
        Toast.makeText(mContext, R.string.toast_copy_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun btnSpanLeft() {
        val cont = mTextContent!!.text
        val start = mSpannable!!.getSpanStart(mBackgroundColorSpan)
        var end = mSpannable!!.getSpanEnd(mBackgroundColorSpan)
        while (end < cont.length) {
            if (isChar(cont[end]) == true) break
            end--
        }
        while (end < cont.length) {
            if (isChar(cont[end]) == false) break
            end--
        }
        if (start > end) {
            return
        }
        mSpannable!!.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        mSelectedWord = cont.subSequence(start, end).toString()
    }

    private fun btnSpanBelow() {
        try {
            val cont = mTextContent!!.text
            val start = mSpannable!!.getSpanStart(mBackgroundColorSpan)
            var end = mSpannable!!.getSpanEnd(mBackgroundColorSpan)
            val layout = mTextContent!!.layout
            val line = layout.getLineForOffset(end)
            end = layout.getLineEnd(line + 1)
            mSpannable!!.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            mSelectedWord = cont.subSequence(start, end).toString()
        } catch (e: Exception) {
        }

    }

    private fun btnSpanRight() {
        val cont = mTextContent!!.text
        val start = mSpannable!!.getSpanStart(mBackgroundColorSpan)
        var end = mSpannable!!.getSpanEnd(mBackgroundColorSpan)
        while (end < cont.length) {
            if (isChar(cont[end]) == true) break
            end++
        }
        while (end < cont.length) {
            if (isChar(cont[end]) == false) break
            end++
        }
        mSpannable!!.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        mSelectedWord = cont.subSequence(start, end).toString()
    }

    private fun btnSpanAll() {
        val cont = mTextContent!!.text
        val start = 0
        val end = cont.length
        mSpannable!!.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        mSelectedWord = cont.subSequence(start, end).toString()
    }

    fun setBackground(bg: Int) {
        mDidSetBackground = true
        if (SettingsView.sRepeatMap.containsKey(bg)) {
            mLayoutContent!!.setBackgroundResource(SettingsView.sRepeatMap[bg]!!)
        } else {
            mLayoutContent!!.setBackgroundColor(bg)
        }
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        if (mLayoutContent != null)
            mLayoutContent!!.setPadding(left, top, right, bottom)
        else
            super.setPadding(left, top, right, bottom)
    }

    companion object {

        fun adjustPref(context: Context, contentView: ContentView) {
            val pref = MiscPref.getInstance(context)
            contentView.setFontColor(pref.contFontColor)
            contentView.setFontSize(pref.contFontSize)
            contentView.setLineSpace(pref.contLineSpace)
            val padding = pref.contPadding
            contentView.setPadding(padding, padding, padding, padding)
            // background는 text 셋팅후에 셋팅
        }
    }

}
