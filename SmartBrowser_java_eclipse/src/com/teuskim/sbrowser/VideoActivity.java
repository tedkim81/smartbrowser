package com.teuskim.sbrowser;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	
	private VideoView mVideoView;
	private MediaController mCtrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.video);
		mVideoView = (VideoView) findViewById(R.id.video);
		mVideoView.setClickable(true);
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				mVideoView.stopPlayback();
				finish();
				
			}
		});
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				
				mCtrl = new MediaController(VideoActivity.this);
				mCtrl.setMediaPlayer(mVideoView);
				
				mVideoView.setMediaController(mCtrl);
			}
		});
		
		play(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		play(intent);
	}

	private void play(Intent i){
		
		Bundle extras = i.getExtras();
		String url = extras.getString("url");
		
		mVideoView.setVideoPath(url);
		mVideoView.requestFocus();
		mVideoView.start();
	}

}
