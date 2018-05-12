package com.teuskim.sbrowser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView

class VideoActivity : Activity() {

    private var mVideoView: VideoView? = null
    private var mCtrl: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.video)
        mVideoView = findViewById<VideoView>(R.id.video)
        mVideoView!!.isClickable = true
        mVideoView!!.setOnCompletionListener {
            mVideoView!!.stopPlayback()
            finish()
        }
        mVideoView!!.setOnPreparedListener {
            mCtrl = MediaController(this@VideoActivity)
            mCtrl!!.setMediaPlayer(mVideoView)

            mVideoView!!.setMediaController(mCtrl)
        }

        play(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        play(intent)
    }

    private fun play(i: Intent) {

        val extras = i.extras
        val url = extras!!.getString("url")

        mVideoView!!.setVideoPath(url)
        mVideoView!!.requestFocus()
        mVideoView!!.start()
    }

}
