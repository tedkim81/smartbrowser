package com.teuskim.sbrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RemoconMenuView extends LinearLayout {
	
	private ImageView mImage;
	private TextView mTitle;

	public RemoconMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RemoconMenuView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.remocon_menu_view, this);
		mImage = (ImageView) findViewById(R.id.menu_image);
		mTitle = (TextView) findViewById(R.id.menu_title);
	}
	
	public void setIconAndTitle(int imageResId, String title){
		mImage.setImageResource(imageResId);
		mTitle.setText(title);
	}
	
	public void setTitle(String title){
		mTitle.setText(title);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			mImage.setPressed(true);
			mTitle.setTextColor(0xffe94820);
			break;
		case MotionEvent.ACTION_UP:
			mImage.setPressed(false);
			mTitle.setTextColor(0xff606261);
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		
		if(selected){
			mImage.setPressed(true);
			mTitle.setTextColor(0xffe94820);
		}
		else{
			mImage.setPressed(false);
			mTitle.setTextColor(0xff606261);
		}
	}
	
}
