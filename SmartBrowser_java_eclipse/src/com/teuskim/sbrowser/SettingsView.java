package com.teuskim.sbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.teuskim.sbrowser.ColorPickerView.ColorChange;
import com.teuskim.sbrowser.ColorPickerView.OnColorChangedListener;

public class SettingsView extends LinearLayout {
	
	private static final int MAX_REMOCON_ALPHA = 250;
	private static final int MIN_REMOCON_ALPHA = 50;
	
	private Context mContext;
	private ViewGroup mColorPickerLayout;
	private ColorPickerView.OnColorChangedListener mOnBgColorChangedListener;
	private ColorPickerView.OnColorChangedListener mOnFontColor1ChangedListener;
	private OnAlphaChangedListener mOnAlphaChangedListener;  // remocon alpha
	private OnFontSizeChangedListener mOnFontSizeChangedListener;  // content font size
	private OnLineSpaceChangedListener mOnLineSpaceChangedListener;  // content line space
	private OnPaddingChangedListener mOnPaddingChangedListener;  // content area padding
	private int mRemoconAlpha;
	private SeekBar mSeekBar;
	private View mBtnColorBg;
	private View mBtnColorFont;
	private int mBgColor;
	private int mFontColor;
	private TextView mTextFontSize;
	private int mFontSize;
	private TextView mTextLineSpace;
	private int mLineSpace;
	private TextView mTextPadding;
	private int mPadding;
	private View mViewCVMSettings;
	private View mViewNoCVM;
	private TextView mTextPageUrl;
	private View mBtnFacebook;
	private View mBtnTwitter;
	private View mBtnKakao;
	private View mMypageBtnLayout;
	
	private ViewGroup mSettingsMainLayout;
	private ViewGroup mSelectBgLayout;
	private OnClickListener mOnCloseClickListener;
	
	private static final int ANIMATION_DURATION = 300;
	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;
	
	public interface OnAlphaChangedListener {
		public void onAlphaChanged(int alpha);
	}
	
	public interface OnFontSizeChangedListener {
		public void onFontSizeChanged(int size);
	}
	
	public interface OnLineSpaceChangedListener {
		public void onLineSpaceChanged(int space);
	}
	
	public interface OnPaddingChangedListener {
		public void onPaddingChanged(int padding);
	}

	public SettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SettingsView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context){
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.settings_view, this);
		mBtnColorBg = findViewById(R.id.btn_color_bg);
		mBtnColorFont = findViewById(R.id.btn_color_font);
		mTextFontSize = (TextView) findViewById(R.id.text_font_size);
		mTextLineSpace = (TextView) findViewById(R.id.text_line_space);
		mTextPadding = (TextView) findViewById(R.id.text_padding);
		mViewCVMSettings = findViewById(R.id.content_view_mode_settings);
		mViewNoCVM = findViewById(R.id.no_content_view_mode);
		mTextPageUrl = (TextView) findViewById(R.id.text_page_url);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar_remocon_alpha);
		mSettingsMainLayout = (ViewGroup) findViewById(R.id.settings_main_layout);
		mBtnFacebook = findViewById(R.id.btn_facebook);
		mBtnTwitter = findViewById(R.id.btn_twitter);
		mBtnKakao = findViewById(R.id.btn_kakao);
		mMypageBtnLayout = findViewById(R.id.mypage_btn_layout);

		mBtnFacebook.setVisibility(GONE);
		mBtnTwitter.setVisibility(GONE);
		mBtnKakao.setVisibility(GONE);
		
		// 버그때문에 여기서 셋팅
		mSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
		mSeekBar.setThumb(getResources().getDrawable(R.drawable.btn_bar_control));
		
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.btn_close_settings:
					close();
					break;
				case R.id.btn_color_bg:
				case R.id.btn_color_bg_choose:	
					showSelectBgView(mOnBgColorChangedListener);
					break;
				case R.id.btn_color_font:
				case R.id.btn_color_font_choose:
					showColorPickerView(mFontColor, mOnFontColor1ChangedListener);
					break;
				case R.id.btn_plus_font_size:
					setFontSize(mFontSize+1);
					break;
				case R.id.btn_minus_font_size:
					setFontSize(mFontSize-1);
					break;
				case R.id.btn_plus_line_space:
					setLineSpace(mLineSpace+1);
					break;
				case R.id.btn_minus_line_space:
					setLineSpace(mLineSpace-1);
					break;
				case R.id.btn_plus_padding:
					setContPadding(mPadding+5);
					break;
				case R.id.btn_minus_padding:
					setContPadding(mPadding-5);
					break;
				}
			}
		};
		findViewById(R.id.btn_close_settings).setOnClickListener(listener);
		mBtnColorBg.setOnClickListener(listener);
		mBtnColorFont.setOnClickListener(listener);
		findViewById(R.id.btn_color_bg_choose).setOnClickListener(listener);
		findViewById(R.id.btn_color_font_choose).setOnClickListener(listener);
		findViewById(R.id.btn_plus_font_size).setOnClickListener(listener);
		findViewById(R.id.btn_minus_font_size).setOnClickListener(listener);
		findViewById(R.id.btn_plus_line_space).setOnClickListener(listener);
		findViewById(R.id.btn_minus_line_space).setOnClickListener(listener);
		findViewById(R.id.btn_plus_padding).setOnClickListener(listener);
		findViewById(R.id.btn_minus_padding).setOnClickListener(listener);
		
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setRemoconAlpha(mRemoconAlpha);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mRemoconAlpha = progress;
				if(mOnAlphaChangedListener != null)
					mOnAlphaChangedListener.onAlphaChanged(mRemoconAlpha);
			}
		});
		
		createAnimations();
	}
	
	public void open(){
		if(getVisibility() != VISIBLE){
			startAnimation(mInAnimation);
			setVisibility(VISIBLE);
		}
	}
	
	public void close(){
		if(getVisibility() != GONE){
			startAnimation(mOutAnimation);
			setVisibility(GONE);
		}
		
		if(mOnCloseClickListener != null)
			mOnCloseClickListener.onClick(null);
	}
	
	public void setOnCloseClickListener(OnClickListener listener){
		mOnCloseClickListener = listener;
	}
	
	public void setFacebookClickListener(OnClickListener listener){
		mBtnFacebook.setOnClickListener(listener);
		mBtnFacebook.setVisibility(VISIBLE);
	}
	
	public void setTwitterClickListener(OnClickListener listener){
		mBtnTwitter.setOnClickListener(listener);
		mBtnTwitter.setVisibility(VISIBLE);
	}
	
	public void setKakaoClickListener(OnClickListener listener){
		mBtnKakao.setOnClickListener(listener);
		mBtnKakao.setVisibility(VISIBLE);
	}
	
	public void showColorPickerView(int color, OnColorChangedListener listener){
		// lazy initiate
		if(mColorPickerLayout == null){
			mColorPickerLayout = (ViewGroup) findViewById(R.id.color_picker_layout);
			int padding = mColorPickerLayout.getPaddingLeft() * 2;
			mColorPickerLayout.addView(new ColorPickerView(mContext, mColorPickerLayout.getWidth()-padding, mColorPickerLayout.getHeight()-padding));
		}
		((ColorPickerView)mColorPickerLayout.getChildAt(0)).init(color, listener);
		mColorPickerLayout.setVisibility(VISIBLE);
	}
	
	public void hideColorPickerView(){
		if(mColorPickerLayout != null)
			mColorPickerLayout.setVisibility(INVISIBLE);
	}
	
	public void hideSelectBgView(){
		if(mSelectBgLayout != null)
			mSelectBgLayout.setVisibility(GONE);
		mSettingsMainLayout.setVisibility(VISIBLE);
	}
	
	public void setOnAlphaChangedListener(OnAlphaChangedListener listener){
		mOnAlphaChangedListener = listener;
	}
	
	public void setRemoconAlpha(int remoconAlpha){
		if(remoconAlpha < MIN_REMOCON_ALPHA)
			mRemoconAlpha = MIN_REMOCON_ALPHA;
		else if(remoconAlpha > MAX_REMOCON_ALPHA)
			mRemoconAlpha = MAX_REMOCON_ALPHA;
		else
			mRemoconAlpha = remoconAlpha;
		
		mSeekBar.setProgress(mRemoconAlpha);
		if(mOnAlphaChangedListener != null)
			mOnAlphaChangedListener.onAlphaChanged(mRemoconAlpha);
	}
	
	public void setOnColorChangedListener(ColorPickerView.OnColorChangedListener bgColorListener
			, ColorPickerView.OnColorChangedListener fontColor1Listener){
		
		mOnBgColorChangedListener = bgColorListener;
		mOnFontColor1ChangedListener = fontColor1Listener;
	}
	
	public void setContBg(int bg){
		if(SettingsView.sPatternMap.containsKey(bg)){
			mBtnColorBg.setBackgroundResource(SettingsView.sPatternMap.get(bg));
		}
		else{
			mBtnColorBg.setBackgroundColor(bg);
		}
		mBgColor = bg;
	}
	
	public void setFontColor(int color){
		mBtnColorFont.setBackgroundColor(color);
		mFontColor = color;
	}
	
	public void setFontSize(int size){
		if(mOnFontSizeChangedListener != null){
			mOnFontSizeChangedListener.onFontSizeChanged(size);
		}
		mTextFontSize.setText(""+size);
		mFontSize = size;
	}
	
	public void setOnFontSizeChangedListener(OnFontSizeChangedListener listener){
		mOnFontSizeChangedListener = listener;
	}
	
	public void setLineSpace(int space){
		if(mOnLineSpaceChangedListener != null){
			mOnLineSpaceChangedListener.onLineSpaceChanged(space);
		}
		mTextLineSpace.setText(""+space);
		mLineSpace = space;
	}
	
	public void setOnLineSpaceChangedListener(OnLineSpaceChangedListener listener){
		mOnLineSpaceChangedListener = listener;
	}
	
	public void setContPadding(int padding){
		if(mOnPaddingChangedListener != null){
			mOnPaddingChangedListener.onPaddingChanged(padding);
		}
		mTextPadding.setText(""+padding);
		mPadding = padding;
	}
	
	public void setOnPaddingChangedListener(OnPaddingChangedListener listener){
		mOnPaddingChangedListener = listener;
	}
	
	public void showCVMSettings(boolean isShow){
		if(isShow){
			mViewCVMSettings.setVisibility(VISIBLE);
			mViewNoCVM.setVisibility(GONE);
		}
		else{
			mViewCVMSettings.setVisibility(GONE);
			mViewNoCVM.setVisibility(VISIBLE);
		}
	}
	
	public void setPageUrl(String url){
		mTextPageUrl.setText(url);
	}
	
	public void showMypageBtnLayout(OnClickListener syncListener, OnClickListener initListener, OnClickListener goListener){
		mMypageBtnLayout.setVisibility(VISIBLE);
		mMypageBtnLayout.findViewById(R.id.btn_mypage_sync).setOnClickListener(syncListener);
		mMypageBtnLayout.findViewById(R.id.btn_mypage_init).setOnClickListener(initListener);
		mMypageBtnLayout.findViewById(R.id.btn_mypage_go).setOnClickListener(goListener);
	}
	
	private void createAnimations() {
		if (mInAnimation == null) {
			mInAnimation = new AnimationSet(false);
			mInAnimation.setInterpolator(new AccelerateInterpolator());
			mInAnimation.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
					Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f));
			mInAnimation.setDuration(ANIMATION_DURATION);
		}

		if (mOutAnimation == null) {
			mOutAnimation = new AnimationSet(false);
			mOutAnimation.setInterpolator(new AccelerateInterpolator());
			mOutAnimation.addAnimation(new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
					Animation.ABSOLUTE, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f));
			mOutAnimation.setDuration(ANIMATION_DURATION);
		}
	}
	
	
	public void showSelectBgView(final OnColorChangedListener listener){
		// lazy initiate
		if(mSelectBgLayout == null){
			mSelectBgLayout = (ViewGroup) findViewById(R.id.select_bg_layout);
			final GridView recommendBgGrid = (GridView) findViewById(R.id.recommend_bg_layout);
			recommendBgGrid.setAdapter(new RecommendBgAdapter());
			recommendBgGrid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int bg = (int)recommendBgGrid.getAdapter().getItemId(position);
					mBgColor = bg;
					listener.colorChanged(ColorChange.CHANGE, bg);
				}
				
			});
			findViewById(R.id.btn_color_bg_2).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showColorPickerView(mBgColor, mOnBgColorChangedListener);
				}
			});
			
			OnClickListener btnListener = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					switch(v.getId()){
					case R.id.btn_select_bg_ok:
						listener.colorChanged(ColorChange.OK, mBgColor);
						break;
					case R.id.btn_select_bg_cancel:
						listener.colorChanged(ColorChange.OK, MiscPref.getInstance(mContext).getContBgColor());  // TODO: 리팩토링 필요
						break;
					}
				}
			};
			findViewById(R.id.btn_select_bg_ok).setOnClickListener(btnListener);
			findViewById(R.id.btn_select_bg_cancel).setOnClickListener(btnListener);
		}
		View btnColorBg2 = findViewById(R.id.btn_color_bg_2);
		if(sPatternMap.containsKey(mBgColor)){
			btnColorBg2.setBackgroundResource(sPatternMap.get(mBgColor));
		}
		else{
			btnColorBg2.setBackgroundColor(mBgColor);
		}
		mSelectBgLayout.setVisibility(VISIBLE);
		mSettingsMainLayout.setVisibility(GONE);
	}
	public static final int PATTERN_0 = 0x00000001;
	public static final int PATTERN_1 = 0x00000002;
	public static final int PATTERN_2 = 0x00000003;
	public static final int PATTERN_3 = 0x00000004;
	public static final int PATTERN_4 = 0x00000005;
	public static final int PATTERN_5 = 0x00000006;
	public static final int PATTERN_6 = 0x00000007;
	public static final int PATTERN_7 = 0x00000008;
	public static final int PATTERN_8 = 0x00000009;
	public static final int PATTERN_9 = 0x00000010;
	public static final int PATTERN_10 = 0x00000011;
	public static final int PATTERN_11 = 0x00000012;
	public static final int PATTERN_12 = 0x00000013;
	public static final int PATTERN_13 = 0x00000014;
	public static final int PATTERN_14 = 0x00000015;
	public static final int PATTERN_15 = 0x00000016;
	public static final int PATTERN_16 = 0x00000017;
	public static final int PATTERN_17 = 0x00000018;
	public static final int PATTERN_18 = 0x00000019;
	public static final int PATTERN_19 = 0x00000020;
	public static Map<Integer, Integer> sPatternMap = new TreeMap<Integer, Integer>();
	static {
		sPatternMap.put(PATTERN_0, R.drawable.pattern0);
		sPatternMap.put(PATTERN_1, R.drawable.pattern1);
		sPatternMap.put(PATTERN_2, R.drawable.pattern2);
		sPatternMap.put(PATTERN_3, R.drawable.pattern3);
		sPatternMap.put(PATTERN_4, R.drawable.pattern4);
		sPatternMap.put(PATTERN_5, R.drawable.pattern5);
		sPatternMap.put(PATTERN_6, R.drawable.pattern_note1);
		sPatternMap.put(PATTERN_7, R.drawable.pattern_note2);
		sPatternMap.put(PATTERN_8, R.drawable.pattern_note3);
		sPatternMap.put(PATTERN_9, R.drawable.pattern_note4);
		sPatternMap.put(PATTERN_10, R.drawable.pattern_note5);
		sPatternMap.put(PATTERN_11, R.drawable.pattern_note6);
		sPatternMap.put(PATTERN_12, R.drawable.pattern_note7);
		sPatternMap.put(PATTERN_13, R.drawable.pattern_note8);
		sPatternMap.put(PATTERN_14, R.drawable.pattern_note9);
		sPatternMap.put(PATTERN_15, R.drawable.pattern_note10);
		sPatternMap.put(PATTERN_16, R.drawable.pattern_note11);
		sPatternMap.put(PATTERN_17, R.drawable.pattern_note12);
		sPatternMap.put(PATTERN_18, R.drawable.pattern_note13);
		sPatternMap.put(PATTERN_19, R.drawable.pattern_note14);
	}
	public static Map<Integer, Integer> sRepeatMap = new TreeMap<Integer, Integer>();
	static {
		sRepeatMap.put(PATTERN_0, R.drawable.bg_content_repeat_0);
		sRepeatMap.put(PATTERN_1, R.drawable.bg_content_repeat_1);
		sRepeatMap.put(PATTERN_2, R.drawable.bg_content_repeat_2);
		sRepeatMap.put(PATTERN_3, R.drawable.bg_content_repeat_3);
		sRepeatMap.put(PATTERN_4, R.drawable.bg_content_repeat_4);
		sRepeatMap.put(PATTERN_5, R.drawable.bg_content_repeat_5);
		sRepeatMap.put(PATTERN_6, R.drawable.bg_content_note_repeat_1);
		sRepeatMap.put(PATTERN_7, R.drawable.bg_content_note_repeat_2);
		sRepeatMap.put(PATTERN_8, R.drawable.bg_content_note_repeat_3);
		sRepeatMap.put(PATTERN_9, R.drawable.bg_content_note_repeat_4);
		sRepeatMap.put(PATTERN_10, R.drawable.bg_content_note_repeat_5);
		sRepeatMap.put(PATTERN_11, R.drawable.bg_content_note_repeat_6);
		sRepeatMap.put(PATTERN_12, R.drawable.bg_content_note_repeat_7);
		sRepeatMap.put(PATTERN_13, R.drawable.bg_content_note_repeat_8);
		sRepeatMap.put(PATTERN_14, R.drawable.bg_content_note_repeat_9);
		sRepeatMap.put(PATTERN_15, R.drawable.bg_content_note_repeat_10);
		sRepeatMap.put(PATTERN_16, R.drawable.bg_content_note_repeat_11);
		sRepeatMap.put(PATTERN_17, R.drawable.bg_content_note_repeat_12);
		sRepeatMap.put(PATTERN_18, R.drawable.bg_content_note_repeat_13);
		sRepeatMap.put(PATTERN_19, R.drawable.bg_content_note_repeat_14);
	}
	private class RecommendBgAdapter extends BaseAdapter {
		
		private List<Integer> patternKeys;
		private LayoutInflater mInflater;
		
		public RecommendBgAdapter(){
			mInflater = LayoutInflater.from(mContext);
			patternKeys= new ArrayList<Integer>();
			Iterator<Integer> it = sPatternMap.keySet().iterator();
			while(it.hasNext()){
				patternKeys.add(it.next());
			}
		}

		@Override
		public int getCount() {
			return patternKeys.size();
		}

		@Override
		public Integer getItem(int position) {
			return sPatternMap.get((int)getItemId(position));
		}

		@Override
		public long getItemId(int position) {
			return patternKeys.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.recommend_bg_item, null);
			((ImageView) v.findViewById(R.id.recommend_bg_image)).setBackgroundResource(getItem(position));
			return v;
		}
		
	}

}
