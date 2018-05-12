package com.teuskim.sbrowser;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class HumanProgressBar extends RelativeLayout {
	
	private static final int MAX_PROGRESS = 100;
	private ImageView mPushman;
	private View mBtnCloseProgress;
	
	private int mDestProgress;
	private int mCurrProgress;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(mCurrProgress < mDestProgress){
				mCurrProgress++;
				LayoutParams lp = (LayoutParams) mPushman.getLayoutParams();
				lp.leftMargin = (int)((getWidth() - mPushman.getWidth()) * mCurrProgress / MAX_PROGRESS);
				mPushman.setLayoutParams(lp);
			}
			
			if(mCurrProgress < MAX_PROGRESS-5){
				mHandler.sendEmptyMessage(0);
			}
			else{
				hide();
			}
			
			if(mCurrProgress % 20 >= 10)
				mPushman.setImageResource(R.drawable.img_loading_pushman02);
			else
				mPushman.setImageResource(R.drawable.img_loading_pushman01);
			
		}
		
	};

	public HumanProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HumanProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HumanProgressBar(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.human_progress_bar, this);
		mPushman = (ImageView) findViewById(R.id.pushman);
		mBtnCloseProgress = findViewById(R.id.btn_close_progress);
	}
	
	public void setProgress(int progress){
		
		mDestProgress = progress;
	}
	
	public void setStopListener(OnTouchListener listener){
		mBtnCloseProgress.setOnTouchListener(listener);
	}
	
	public void show(){
		setVisibility(VISIBLE);
		mDestProgress = 0;
		mCurrProgress = 0;
		mHandler.sendEmptyMessage(0);
	}
	
	public void hide(){
		setVisibility(GONE);
	}
}
