package com.teuskim.sbrowser

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener

class InstructionsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions)
        findViews()
    }

    private fun findViews() {
        findViewById<View>(R.id.btn_close_instructions).setOnClickListener { finish() }
    }

}
