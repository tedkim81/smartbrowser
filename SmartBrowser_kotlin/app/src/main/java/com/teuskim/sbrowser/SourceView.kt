package com.teuskim.sbrowser

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import android.widget.TextView

class SourceView : ScrollView {

    private var mTextSource: TextView? = null

    var source: String
        get() = mTextSource!!.text.toString()
        set(source) {
            mTextSource!!.text = source
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
        LayoutInflater.from(context).inflate(R.layout.source_view, this)
        mTextSource = findViewById<TextView>(R.id.text_source)
    }

}
