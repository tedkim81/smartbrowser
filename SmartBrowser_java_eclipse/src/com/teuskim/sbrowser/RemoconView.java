package com.teuskim.sbrowser;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RemoconView extends RelativeLayout {
	
	private View mRemoconView;
	private View mIcDrag;
	private View mRemoconBody;
	private View mRemoconBottom;
	private ImageView mBtnInputBack;
	private ImageView mBtnInputFoward;
	
	private boolean mIsRemoconBodyTouchable;
	private int mRemoconAlpha;
	private int mMinLeft;
	private int mMaxLeft;
	private int mMinBottom;
	private int mMaxBottom;
	private int mWindowWidth;
	private int mWindowHeight;
	
	private MiscPref mPref;
	
	private static final int ANIMATION_DURATION = 100;
	private AnimationSet mShowAnimation;
	private AnimationSet mHideAnimation;
	private Handler mShowShadowHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mRemoconView.setBackgroundResource(R.drawable.shadow_remote);
		}
		
	};

	public RemoconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RemoconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RemoconView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.remocon, this);
		
		mPref = MiscPref.getInstance(context);
		mRemoconAlpha = mPref.getRemoconAlpha();
		
		mIsRemoconBodyTouchable = false;
		mMinLeft = Integer.MIN_VALUE;
		mMaxLeft = Integer.MAX_VALUE;
		mMinBottom = Integer.MIN_VALUE;
		mMaxBottom = Integer.MAX_VALUE;
		
		mRemoconView = findViewById(R.id.remocon_layout);
		mRemoconBody = findViewById(R.id.remocon_body);
		mBtnInputBack = (ImageView) findViewById(R.id.btn_input_back);
		mBtnInputFoward = (ImageView) findViewById(R.id.btn_input_foward);
		mIcDrag = findViewById(R.id.ic_drag);
		mIcDrag.setOnTouchListener(new OnTouchListener() {
			
			private int mTouchedX = Integer.MIN_VALUE;
			private int mTouchedY = Integer.MIN_VALUE;
			private int mTouchedLeft;
			private int mTouchedBottom;
			private boolean mDidShow;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mWindowWidth == 0 || mWindowHeight == 0)
					return false;
				
				int x = (int)event.getX();
				int y = (int)event.getY();
				if(mTouchedX == Integer.MIN_VALUE) mTouchedX = x;
				if(mTouchedY == Integer.MIN_VALUE) mTouchedY = x;
				
				int left = mRemoconView.getLeft() + x - mTouchedX;
				int bottom = mWindowHeight - mRemoconView.getTop() - mRemoconView.getHeight() - y + mTouchedY;
				
				int action = event.getAction();
				switch(action){
				case MotionEvent.ACTION_DOWN:
					mTouchedX = x;
					mTouchedY = y;
					mTouchedLeft = left;
					mTouchedBottom = bottom;
					if(mRemoconBody.getVisibility() == View.VISIBLE){
						mDidShow = true;
					}
					else{
						mRemoconBody.startAnimation(mShowAnimation);
						showRemocon();
						mDidShow = false;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					setRemoconPosition(left, bottom);
					break;
				case MotionEvent.ACTION_UP:
					// 마지막 위치 저장
					mPref.setRemoconLeft(left);
					mPref.setRemoconBottom(bottom);
					
					// 클릭이었다면(별로 움직이지 않았다면) 리모콘 toggle
					if(Math.abs(left-mTouchedLeft)+Math.abs(bottom-mTouchedBottom) < 30){
						if(mDidShow){
							mRemoconBody.startAnimation(mHideAnimation);
							hideRemocon();
						}
						else{
							showRemocon();
						}
					}
					break;
				}
				return true;
			}
		});
		
		mRemoconView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mIsRemoconBodyTouchable;
			}
		});
		
		createAnimations();
	}
	
	public void setBottomView(View v){
		mRemoconBottom = v;
		((ViewGroup)mRemoconBody).addView(v);
	}
	
	private void setBoundary(){
		mMinLeft = 0;
		mMinBottom = 0;
		mMaxLeft = (int)(mWindowWidth - (mRemoconBody.getWidth()*0.6));
		mMaxBottom = mWindowHeight - mRemoconBody.getHeight();
	}
	
	public void setWindowSize(int width, int height){
		mWindowWidth = width;
		mWindowHeight = height;
		
		setBoundary();
	}
	
	public void initAlphaAndPosition(){
		if(mRemoconBody.getVisibility() == View.VISIBLE)
			setRemoconAlpha(255, mIcDrag);
		else
			setRemoconAlpha(mRemoconAlpha, mIcDrag);
		
		int left = mPref.getRemoconLeft();
		int bottom = mPref.getRemoconBottom();
		setRemoconPosition(left, bottom);
	}
	
	protected void showRemocon(){
		mRemoconBody.setVisibility(View.VISIBLE);
		mShowShadowHandler.sendEmptyMessageDelayed(0, ANIMATION_DURATION);
		setRemoconAlpha(255, mIcDrag);
		mIsRemoconBodyTouchable = true;
	}
	
	protected void hideRemocon(){
		mRemoconBody.setVisibility(View.INVISIBLE);
		mRemoconView.setBackgroundColor(Color.TRANSPARENT);
		setRemoconAlpha(mRemoconAlpha, mIcDrag);
		mIsRemoconBodyTouchable = false;
	}
	
	private void setRemoconPosition(int left, int bottom){
		if(left == Integer.MIN_VALUE) left = 50;
		else if(left < mMinLeft) left = mMinLeft;
		else if(left > mMaxLeft) left = mMaxLeft;
		
		if(bottom == Integer.MIN_VALUE) bottom = 50;
		else if(bottom < mMinBottom) bottom = mMinBottom;
		else if(bottom > mMaxBottom) bottom = mMaxBottom;
		
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRemoconView.getLayoutParams();
		lp.setMargins(left, 0, 0, bottom);
		mRemoconView.setLayoutParams(lp);
		mRemoconView.invalidate();
	}
	
	public void setRemoconAlpha(int alpha){
		mRemoconAlpha = alpha;
		invalidate();
		mPref.setRemoconAlpha(alpha);
	}
	
	private boolean setRemoconAlpha(int alpha, View view)
	{
	    if (view instanceof ViewGroup){
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
	        	setRemoconAlpha(alpha, ((ViewGroup) view).getChildAt(i));
	            if (((ViewGroup) view).getBackground() != null) ((ViewGroup) view).getBackground().setAlpha(alpha);
	        }
	    }
	    else{
	    	if (view instanceof ImageView)
		        if (((ImageView) view).getDrawable() != null) ((ImageView) view).getDrawable().setAlpha(alpha);
		    else if (view instanceof TextView)
		        ((TextView) view).setTextColor(((TextView) view).getTextColors().withAlpha(alpha));
	    	
	    	if(view.getBackground() != null) view.getBackground().setAlpha(alpha);
	    }
	    return true;
	}
	
	public void setOnBackClickListener(OnClickListener listener){
		if(listener != null){
			mBtnInputBack.setImageResource(R.drawable.btn_input_back);
			mBtnInputBack.setClickable(true);
			mBtnInputBack.setOnClickListener(listener);
		}
		else{
			mBtnInputBack.setImageResource(R.drawable.btn_input_back_disable);
			mBtnInputBack.setClickable(false);
		}
	}
	
	public void setOnFowardClickListener(OnClickListener listener){
		if(listener != null){
			mBtnInputFoward.setImageResource(R.drawable.btn_input_foward);
			mBtnInputFoward.setClickable(true);
			mBtnInputFoward.setOnClickListener(listener);
		}
		else{
			mBtnInputFoward.setImageResource(R.drawable.btn_input_foward_disable);
			mBtnInputFoward.setClickable(false);
		}
	}
	
	public int getRemoconWidth(){
		return mRemoconBody.getWidth();
	}
	
	private void createAnimations() {
		if (mShowAnimation == null) {
			mShowAnimation = new AnimationSet(false);
			mShowAnimation.setInterpolator(new AccelerateInterpolator());
			mShowAnimation.addAnimation(new ScaleAnimation(0.2f, 1.0f, 0.5f, 1.0f
					, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0));
			mShowAnimation.setDuration(ANIMATION_DURATION);
		}

		if (mHideAnimation == null) {
			mHideAnimation = new AnimationSet(false);
			mHideAnimation.setInterpolator(new AccelerateInterpolator());
			mHideAnimation.addAnimation(new ScaleAnimation(1.0f, 0.2f, 1.0f, 0.5f
					, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0));
			mHideAnimation.setDuration(ANIMATION_DURATION);
		}
	}

}