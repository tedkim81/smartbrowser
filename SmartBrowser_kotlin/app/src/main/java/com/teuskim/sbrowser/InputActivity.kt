package com.teuskim.sbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.widget.SimpleCursorAdapter
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener

class InputActivity : Activity() {

    private var mInputQuery: EditText? = null
    private var mInputHistoryListView: ListView? = null
    private var mNoListView: TextView? = null

    private var mImm: InputMethodManager? = null
    private var mAdapter: InputHistoryAdapter? = null
    private var mDb: SbDb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input)

        mImm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mDb = SbDb.getInstance(applicationContext)

        findViews()
        loadList()
    }

    protected fun findViews() {
        mInputQuery = findViewById<View>(R.id.input_query) as EditText
        mInputHistoryListView = findViewById<View>(R.id.input_history) as ListView
        mNoListView = findViewById<View>(R.id.no_input_history) as TextView
        findViewById<View>(R.id.btn_go).setOnClickListener { goWeb(mInputQuery!!.text.toString()) }

        val urlWordBar = findViewById<View>(R.id.url_word_bar) as LinearLayout
        val urlWords = arrayOf("http://", "www.", "m.", ".com", ".", "/", "?", "&", "=")
        for (i in urlWords.indices) {
            val word = urlWords[i]
            val btn = Button(applicationContext)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            btn.layoutParams = lp
            btn.text = word
            btn.textSize = 12f
            btn.setPadding(0, 0, 0, 0)
            btn.setOnClickListener {
                val edit = mInputQuery!!.editableText
                edit.append(word)
            }
            urlWordBar.addView(btn)
        }

        mInputHistoryListView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val input = view.tag as String
            goWeb(input)
        }

        mInputQuery!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                goWeb(v.text.toString())
                return@OnEditorActionListener true
            }
            false
        })

        mInputQuery!!.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                loadList()
            }
        })
    }

    private fun goWeb(query: String) {
        if (TextUtils.isEmpty(query)) {
            finish()
        } else {
            val i = Intent(applicationContext, WebActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val url: String
            if (query.contains("://") || query.startsWith("sms:") || query.startsWith("tel:") || query.startsWith("mailto:")) {
                url = query
                saveInputHistory(true, url)
            } else if (query.contains(".")) {
                url = "http://" + query
                saveInputHistory(true, url)
            } else {
                url = "http://www.google.com/search?q=" + query
                saveInputHistory(false, query)
            }
            i.putExtra("url", url)
            startActivity(i)
        }
    }

    private fun saveInputHistory(isUrl: Boolean, input: String) {
        var input = input
        try {
            input = input.trim { it <= ' ' }
            if (isUrl) {
                SbDb.getInstance(applicationContext)!!.insertOrUpdateInputUrl(input, null, "Y")
            } else {
                SbDb.getInstance(applicationContext)!!.insertOrUpdateInputWord(input, null, "Y")
            }
        } catch (e: Exception) {
        }

    }

    private fun loadList() {
        val preword = mInputQuery!!.text.toString()
        val cursor = SbDb.getInstance(applicationContext)!!.getInputHistoryList(preword)
        if (cursor.count > 0)
            mNoListView!!.visibility = View.GONE
        else
            mNoListView!!.visibility = View.VISIBLE

        if (mAdapter == null) {
            mAdapter = InputHistoryAdapter(applicationContext, cursor)
            mInputHistoryListView!!.adapter = mAdapter
        } else {
            mAdapter!!.changeCursor(cursor)
        }
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onPause() {
        mImm!!.hideSoftInputFromWindow(mInputQuery!!.windowToken, 0)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mAdapter!!.cursor.close()
        finish()
    }

    private inner class InputHistoryAdapter(context: Context, c: Cursor) : SimpleCursorAdapter(context, 0, c, arrayOf(), intArrayOf(), 0) {

        private val mInflater: LayoutInflater
        private val mDeleteListener = OnClickListener { v ->
            val id = v.tag as Int
            mDb!!.deleteInputHistory(id)
            loadList()
        }

        init {
            mInflater = LayoutInflater.from(context)
        }

        override fun bindView(v: View, context: Context?, cursor: Cursor) {
            val inputHistory = mDb!!.getInputHistory(cursor)
            (v.findViewById<View>(R.id.url) as TextView).text = inputHistory.mInput

            val btnDelete = v.findViewById<View>(R.id.btn_delete)
            btnDelete.tag = inputHistory.mId
            btnDelete.setOnClickListener(mDeleteListener)

            v.tag = inputHistory.mInput
        }

        override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
            return mInflater.inflate(R.layout.input_history_item, null)
        }

    }

}
