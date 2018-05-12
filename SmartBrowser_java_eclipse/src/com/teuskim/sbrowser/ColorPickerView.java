package com.teuskim.sbrowser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {

	private Paint mPaint;
	private float mCurrentHue = 0;
	private int mCurrentX = 0, mCurrentY = 0;
	private int mCurrentColor, mDefaultColor;   // mDefaultColor 는 기존 색상으로 취소시 적용된다.
	private final int[] mHueBarColors = new int[258];
	private int[] mMainColors = new int[65536];
	private OnColorChangedListener mListener;
	private int mWidth,mHeight;
	
	public interface OnColorChangedListener {
        void colorChanged(ColorChange colorChange, int color);
    }
	
	public enum ColorChange {
		CHANGE, OK, CANCEL
	}
	
	public ColorPickerView(Context c, int width, int height) {
		super(c);
		
		mWidth = width;
		mHeight = height;
	}
	
	public void init(int color, OnColorChangedListener l) {
		mDefaultColor = color;
		mListener = l;

		// Get the current hue from the current color and update the main color field
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		mCurrentHue = hsv[0];
		updateMainColors();

		mCurrentColor = color;

		// Initialize the colors of the hue slider bar
		int index = 0;
		for (float i=0; i<256; i += 256/42) // Red (#f00) to pink (#f0f)
		{
			mHueBarColors[index] = Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Pink (#f0f) to blue (#00f)
		{
			mHueBarColors[index] = Color.rgb(255-(int) i, 0, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Blue (#00f) to light blue (#0ff)
		{
			mHueBarColors[index] = Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Light blue (#0ff) to green (#0f0)
		{
			mHueBarColors[index] = Color.rgb(0, 255, 255-(int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Green (#0f0) to yellow (#ff0)
		{
			mHueBarColors[index] = Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i=0; i<256; i += 256/42) // Yellow (#ff0) to red (#f00)
		{
			mHueBarColors[index] = Color.rgb(255, 255-(int) i, 0);
			index++;
		}

		// Initializes the Paint that will draw the View
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(mWidth/20);
	}
	
	private int getCurrentMainColor()
	{
		int translatedHue = 255-(int)(mCurrentHue*255/360);
		int index = 0;
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255, 0, (int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255-(int) i, 0, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(0, (int) i, 255);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(0, 255, 255-(int) i);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb((int) i, 255, 0);
			index++;
		}
		for (float i=0; i<256; i += 256/42)
		{
			if (index == translatedHue)
				return Color.rgb(255, 255-(int) i, 0);
			index++;
		}
		return Color.RED;
	}
	
	private void updateMainColors()
	{
		int mainColor = getCurrentMainColor();
		int index = 0;
		int[] topColors = new int[256];
		for (int y=0; y<256; y++)
		{
			for (int x=0; x<256; x++)
			{
				if (y == 0)
				{
					mMainColors[index] = Color.rgb(255-(255-Color.red(mainColor))*x/255, 255-(255-Color.green(mainColor))*x/255, 255-(255-Color.blue(mainColor))*x/255);
					topColors[x] = mMainColors[index];
				}
				else
					mMainColors[index] = Color.rgb((255-y)*Color.red(topColors[x])/255, (255-y)*Color.green(topColors[x])/255, (255-y)*Color.blue(topColors[x])/255);
				index++;
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int translatedHue = 255-(int)(mCurrentHue*255/360);
		// Display all the colors of the hue bar with lines
		float thick = mWidth / 256f;
		for (int x=0; x<256; x++)
		{
			// If this is not the current selected hue, display the actual color
			if (translatedHue != x)
			{
				mPaint.setColor(mHueBarColors[x]);
				mPaint.setStrokeWidth(thick);
			}
			else // else display a slightly larger black line
			{
				mPaint.setColor(Color.BLACK);
				mPaint.setStrokeWidth(thick*3);
			}
			canvas.drawLine((x*thick), 0, (x*thick), (float)(mHeight*0.15), mPaint);
		}

		// Display the main field colors using LinearGradient
		for (int x=0; x<256; x++)
		{
			int[] colors = new int[2];
			colors[0] = mMainColors[x];
			colors[1] = Color.BLACK;
			Shader shader = new LinearGradient(0, (float)(mHeight*0.16), 0, (float)(mHeight*0.85), colors, null, Shader.TileMode.REPEAT);
			mPaint.setShader(shader);
			canvas.drawLine((x*thick), (float)(mHeight*0.16), (x*thick), (float)(mHeight*0.85), mPaint);
		}
		mPaint.setShader(null);

		// Display the circle around the currently selected color in the main field
		if (mCurrentX != 0 && mCurrentY != 0)
		{
			mPaint.setStyle(Paint.Style.STROKE);
			if (isDark(mCurrentColor))
				mPaint.setColor(Color.WHITE);
			else
				mPaint.setColor(Color.BLACK);
			canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
		}

		// Draw a 'button' with the currently selected color
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mCurrentColor);
		canvas.drawRect((float)(mWidth*0.01), (float)(mHeight*0.86), (float)(mWidth*0.49), (float)(mHeight*0.99), mPaint);

		// Set the text color according to the brightness of the color
		if (isDark(mCurrentColor))
			mPaint.setColor(Color.WHITE);
		else
			mPaint.setColor(Color.BLACK);
		canvas.drawText(getResources().getString(R.string.confirm), (float)(mWidth*0.25), (float)(mHeight*0.94), mPaint);

		// Draw a 'button' with the default color
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mDefaultColor);
		canvas.drawRect((float)(mWidth*0.51), (float)(mHeight*0.86), (float)(mWidth*0.99), (float)(mHeight*0.99), mPaint);

		// Set the text color according to the brightness of the color
		if (isDark(mDefaultColor))
			mPaint.setColor(Color.WHITE);
		else
			mPaint.setColor(Color.BLACK);
		canvas.drawText(getResources().getString(R.string.cancel), (float)(mWidth*0.75), (float)(mHeight*0.94), mPaint);
	}
	
	private boolean isDark(int color){
		return (Color.red(color)+Color.green(color)+Color.blue(color) < 384);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
		float x = event.getX();
		float y = event.getY();

		// If the touch event is located in the hue bar
		if (x > 10 && x < mWidth && y > 0 && y < (float)(mHeight*0.15))
		{
			// Update the main field colors
			float thick = mWidth / 256f;
			mCurrentHue = (255-(x/thick))*360/255;
			updateMainColors();

			// Update the current selected color
			int transX = (int)((mCurrentX-10)*256/(mWidth-10));
			int transY = (int)((mCurrentY-(mHeight*0.16))*256/(mHeight*0.69));
			int index = 256*transY+transX;
			if (index > 0 && index < mMainColors.length)
				mCurrentColor = mMainColors[index];

			// Force the redraw of the dialog
			invalidate();
		}

		// If the touch event is located in the main field
		if (x > 10 && x < mWidth && y > (float)(mHeight*0.16) && y < (float)(mHeight*0.85))
		{
			mCurrentX = (int) x;
			mCurrentY = (int) y;
			int transX = (int)((mCurrentX-10)*256/(mWidth-10));
			int transY = (int)((mCurrentY-(mHeight*0.16))*256/(mHeight*0.69));
			int index = 256*transY+transX;
			if (index > 0 && index < mMainColors.length)
			{
				// Update the current color
				mCurrentColor = mMainColors[index];
				// Force the redraw of the dialog
				invalidate();
			}
		}

		if (x > (float)(mWidth*0.04) && x < (float)(mWidth*0.48) && y > (float)(mHeight*0.85) && y < mHeight)
			mListener.colorChanged(ColorChange.OK, mCurrentColor);
		else if (x > (float)(mWidth*0.52) && x < (float)(mWidth*0.96) && y > (float)(mHeight*0.85) && y < mHeight)
			mListener.colorChanged(ColorChange.CANCEL, mDefaultColor);
		else
			mListener.colorChanged(ColorChange.CHANGE, mCurrentColor);

		return true;
	}
	
}
