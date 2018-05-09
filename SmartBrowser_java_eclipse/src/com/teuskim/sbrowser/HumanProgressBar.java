package com.teuskim.sbrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class HumanProgressBar extends RelativeLayout {
	
	private static final int MAX_PROGRESS = 100;
	private ImageView mPushman;
	private View mBtnCloseProgress;
	private boolean mIsHurry;

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
		LayoutParams lp = (LayoutParams) mPushman.getLayoutParams();
		lp.leftMargin = (int)((getWidth() - mPushman.getWidth()) * progress / MAX_PROGRESS);
		mPushman.setLayoutParams(lp);
		
		if(mIsHurry)
			mPushman.setImageResource(R.drawable.img_loading_pushman02);
		else
			mPushman.setImageResource(R.drawable.img_loading_pushman01);
		mIsHurry = !mIsHurry;
	}
	
	public void setStopListener(OnTouchListener listener){
		mBtnCloseProgress.setOnTouchListener(listener);
	}
}
