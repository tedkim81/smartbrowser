package com.teuskim.sbrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MoveableListView extends ListView {
	
	private Context mContext;
	private ImageView mDragView;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWindowParams;
	private int mDragPos;	  // 드래그 아이템의 위치
	private int mFirstDragPos; // 드래그 아이템의 원래 위치
	private int mDragPoint;
	private int mCoordOffset;  // 스크린에서의 위치와 뷰내에서의 위치의 차이
	private DragListener mDragListener;
	private DropListener mDropListener;
	private int mUpperBound;
	private int mLowerBound;
	private int mHeight;
	private Rect mTempRect = new Rect();
	private Bitmap mDragBitmap;
	private final int mTouchSlop;
	private int mItemHeightNormal;
	private int mItemHeightExpanded;
	private int mDndViewId;
	private int mDragImageX = 0;
	private Bitmap mBitmap;
	private int mTouchedY;
	private int mCurrentY;
	private int mForcedItemHeight;
	private int mIconWidth;
	private Handler mHandler = new Handler();
	private Runnable mScrollRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(doScroll())
				scroll(mCurrentY);
		}
	};
	
	public MoveableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}
	
	public void setIconWidth(int width){
		mIconWidth = width;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if ((mDragListener != null || mDropListener != null) && ev.getX() < mIconWidth) {
			switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN:
					
					int x = (int) ev.getX();
					mTouchedY = (int) ev.getY();
					int itemnum = pointToPosition(x, mTouchedY);
					if (itemnum == AdapterView.INVALID_POSITION) {
						break;
					}
					
					View item = (View) getChildAt(itemnum - getFirstVisiblePosition()); // 드래그 아이템
					
					if(mForcedItemHeight > 0)
						mItemHeightNormal = mForcedItemHeight;
					else
						mItemHeightNormal = item.getHeight(); // 아이템의 높이
					
					mItemHeightExpanded = mItemHeightNormal + item.getHeight(); // 아이템이 드래그 할때 벌어질 높이
					
					mDragPoint = mTouchedY - item.getTop();
					mCoordOffset = ((int)ev.getRawY()) - mTouchedY;
					
					View dragger = item.findViewById(mDndViewId); // 드래그 이벤트를 할 아이템내에서의 뷰
					if(dragger == null)
						dragger = item;
					Rect r = mTempRect;
					dragger.getDrawingRect(r);
					
					if (x < r.right * 2) {
						item.setDrawingCacheEnabled(true);
						
						// 드래그 하는 아이템의 이미지 캡쳐
						if(mBitmap != null)
							mBitmap.recycle();
						mBitmap = Bitmap.createBitmap(item.getDrawingCache());
						mDragPos = itemnum;
						mFirstDragPos = mDragPos;
						mHeight = getHeight();
						
						// 스크롤링을 위한 값 획득
						int touchSlop = mTouchSlop;
						mUpperBound = Math.min(mTouchedY - touchSlop, mItemHeightNormal);
						mLowerBound = Math.max(mTouchedY + touchSlop, mHeight - mItemHeightNormal);
						return false;
					}
					mDragView = null;
					break;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	public void setForcedHeights(int height){
		mForcedItemHeight = height;
	}
	
	public void startDragging(){
		startDragging(mBitmap, mTouchedY);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		if ((mDragListener != null || mDropListener != null) && mDragView != null && ev.getX() < mIconWidth) {
			int action = ev.getAction(); 
			switch (action) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					
					Rect r = mTempRect;
					mDragView.getDrawingRect(r);
					stopDragging();
					if (mDropListener != null && mDragPos >= 0 && mDragPos < getCount()) {
						mDropListener.drop(mFirstDragPos, mDragPos);
					}
					unExpandViews();
					
					mCurrentY = mHeight / 2;
					
					break;
					
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					
					int x = (int) ev.getX();
					int y = (int) ev.getY();
					dragView(x, y);
					int itemnum = getItemForPosition(y);
					if (itemnum >= 0) {
						
						scroll(y);
						
						if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
							if (mDragListener != null) {
								mDragListener.drag(mDragPos, itemnum);
							}
							mDragPos = itemnum;
							doExpansion(); // 처음 드래그한 아이템과 다른 위치에 있을 경우 펼쳐지게 한다.
						}						
					}
					break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	private int getItemForPosition(int y) {
		int pos;
		int adjustedy1 = y - mDragPoint + 10;
		int adjustedy2 = y - mDragPoint + (mItemHeightNormal - 10);
		int pos1 = pointToPosition(1, adjustedy1);
		int pos2 = pointToPosition(1, adjustedy2);
		
		if(pos1 == AdapterView.INVALID_POSITION || pos2 == AdapterView.INVALID_POSITION)
			return AdapterView.INVALID_POSITION;
		
		if(pos1 < mFirstDragPos){  // 본래 위치보다 위에서 드래그 할때는 아이템의 하단을 기준으로 현재 포지션을 구하고,
			pos = pos2;
		}
		else{  // 본래 위치보다 아래에서 드래그 할때는 아이템의 상단을 기준으로 현재 포지션을 구한다.
			pos = pos1;
		}
		
		return pos;
	}
	
	private void scroll(int y){
		mCurrentY = y;
		mHandler.removeCallbacks(mScrollRunnable);
		mHandler.post(mScrollRunnable);
	}
	
	private boolean doScroll(){
		
		int ref = pointToPosition(1, mHeight / 2);
		if (ref == AdapterView.INVALID_POSITION) {
			ref = pointToPosition(1, mHeight / 2 + getDividerHeight() + 64);
		}		
		View v = getChildAt(ref - getFirstVisiblePosition());
		
		if(v != null){
			int pos = v.getTop();
			if(mCurrentY < mUpperBound){
				setSelectionFromTop(ref, pos + 10);
				return true;
			}
			else if(mCurrentY > mLowerBound){
				setSelectionFromTop(ref, pos - 10);
				return true;
			}
		}
		return false;
	}

	private void doExpansion() {
		int relativePosition = mDragPos - getFirstVisiblePosition();  // 현재 드래그 되고 있는 점의 현재화면에서의 상대적 포지션
		View first = getChildAt(mFirstDragPos - getFirstVisiblePosition());  // 드래그하기 위해 선택한 뷰
		int startIndex = 0;
		if(getFirstVisiblePosition() < getHeaderViewsCount())
			startIndex = getHeaderViewsCount() - getFirstVisiblePosition();

		for (int i = startIndex;; i++) {
			LinearLayout vv = (LinearLayout) getChildAt(i);
			if (vv == null) {
				break;
			}
			int height = mItemHeightNormal;
			int visibility = View.VISIBLE;
			
			if(vv.equals(first)){  // 선택뷰인 경우, 
				if(i == relativePosition){  // 현재 드래그된 포지션과 같으면 영역은 차지하되 보이지 않게 하고,
					visibility = View.INVISIBLE;
				}
				else{  // 현재 드래그된 포지션과 다르면 높이를 줄여서 보이지 않게 한다.
					height = 5;
				}				
			}
			else if(i == relativePosition){  // 선택뷰가 아닌 나머지 중에 현재 드래그된 포지션과 같은 뷰는 두배로 늘린다.
				height = mItemHeightExpanded;
			}			
			
			ViewGroup.LayoutParams params = vv.getLayoutParams();
			params.height = height;
			vv.setLayoutParams(params);
			if(mFirstDragPos > mDragPos){  // 본래 위치보다 위로 드래그 할때는 아이템의 상단을 넓히고,
				vv.setGravity(Gravity.BOTTOM);
				vv.setPadding(0, 0, 0, 4);
			}
			else{  // 본래 위치보다 아래로 드래그 할때는 아이템의 하단을 넓히고,
				vv.setGravity(Gravity.TOP);
			}			
			vv.setVisibility(visibility);
		}
	}
	
	
	private void unExpandViews() {
		for (int i = getHeaderViewsCount();; i++) {
			View v = getChildAt(i);
			if (v == null) {
				layoutChildren();
				v = getChildAt(i);
				if (v == null) {
					break;
				}
			}
			ViewGroup.LayoutParams params = v.getLayoutParams();
			params.height = mItemHeightNormal;
			v.setLayoutParams(params);
			v.setVisibility(View.VISIBLE);
		}
	}
	
	// 드래그 시작
	private void startDragging(Bitmap bm, int y) {
		stopDragging();

		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP;
		mWindowParams.x = mDragImageX;
		mWindowParams.y = y - mDragPoint + mCoordOffset;

		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;
		
		ImageView v = new ImageView(mContext);
		int backGroundColor = Color.parseColor("#ffffffff");
		v.setBackgroundColor(backGroundColor);
		v.setImageBitmap(bm);
		mDragBitmap = bm;

		mWindowManager = (WindowManager)mContext.getSystemService("window");
		mWindowManager.addView(v, mWindowParams);
		mDragView = v;
	}
	
	// 드래그를 위해 만들어 준 뷰의 이동
	private void dragView(int x, int y) {
		mWindowParams.y = y - mDragPoint + mCoordOffset;
		mWindowManager.updateViewLayout(mDragView, mWindowParams);
	}
	
	// 드래그 종료 처리
	private void stopDragging() {
		if (mDragView != null) {
			WindowManager wm = (WindowManager)mContext.getSystemService("window");
			wm.removeView(mDragView);
			mDragView.setImageDrawable(null);
			mDragView = null;
		}
		if (mDragBitmap != null) {
			mDragBitmap.recycle();
			mDragBitmap = null;
		}
	}
	
	/**
	 * 드래그 이벤트 리스너 등록
	 * @param l 드래그 이벤트 리스너
	 */
	public void setDragListener(DragListener l) {
		mDragListener = l;
	}
	
	/**
	 * 드랍 이벤트 리스너 등록
	 * @param l 드랍 이벤트 리스너
	 */
	public void setDropListener(DropListener l) {
		mDropListener = l;
	}
	
	/**
	 * 리스트 아이템에 있는 뷰들 중 드래그 드랍 이벤트를 발생시킬 뷰의 아이 
	 * @param id 드래그 드랍 이벤트를 발생시킬 뷰의 아이디
	 */
	public void setDndView(int id){
		mDndViewId = id;
	}
	
	/**
	 * 드래그시 표시되는 뷰의 스크린에서의 left padding
	 * @param x 스크린에서의 left padding, 설정안하면 0
	 */
	public void setDragImageX(int x){
		mDragImageX = x;
	}
	
	public interface DragListener {
		void drag(int from, int to);
	}
	public interface DropListener {
		void drop(int from, int to);
	}
}
