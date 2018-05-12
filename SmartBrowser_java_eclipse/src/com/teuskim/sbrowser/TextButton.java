package com.teuskim.sbrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class TextButton extends TextView {
	
	private static int sNormal = 0xff000000;
	private static int sPressed = 0xff000000;

	public TextButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextButton(Context context) {
		super(context);
	}
	
	public static void setTextColor(int normal, int pressed){
		sNormal = normal;
		sPressed = pressed;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			setTextColor(sPressed);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			setTextColor(sNormal);
			break;
		}
		return super.onTouchEvent(event);
	}
	
}
