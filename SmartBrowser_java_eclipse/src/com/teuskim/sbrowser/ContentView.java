package com.teuskim.sbrowser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.os.Handler;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class ContentView extends RelativeLayout {
	
	private ScrollView mLayoutScroll;
	private View mLayoutContent;
	private TextView mTextContent;
	private View mLayoutBottom;
	private Button mBtnCloseLayoutBottom;
	private View mLayoutWord;
	private TextButton mBtnSearch;
	private TextButton mBtnTranslate;
	private View mLayoutWebViewSearch;
	private TextView mTextWebViewSearchTitle;
	private View mBtnCloseWebViewSearch;
	private SbWebView mWebView;
	
	private int mSelectedX;
	private int mSelectedY;
	private String mSelectedWord;
	private Spannable mSpannable;
	private BackgroundColorSpan mBackgroundColorSpan;
	
	private Handler mHandler = new Handler();

	public ContentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ContentView(Context context) {
		super(context);
		init(context);
	}
	
	public static void adjustPref(Context context, ContentView contentView){
		MiscPref pref = MiscPref.getInstance(context);
		contentView.setBackground(pref.getContBgColor());
		contentView.setFontColor(pref.getContFontColor());
		contentView.setFontSize(pref.getContFontSize());
		contentView.setLineSpace(pref.getContLineSpace());
		int padding = pref.getContPadding();
		contentView.setPadding(padding, padding, padding, padding);
	}

	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.content_view, this);
		mLayoutScroll = (ScrollView) findViewById(R.id.layout_scroll);
		mLayoutContent = findViewById(R.id.layout_content_body);
		mTextContent = (TextView) findViewById(R.id.text_content);
		mLayoutBottom = findViewById(R.id.layout_bottom);
		mBtnCloseLayoutBottom = (Button) findViewById(R.id.btn_close_layout_bottom);
		mLayoutWord = findViewById(R.id.layout_word);
		TextButton.setTextColor(0xff000000, 0xffe94820);
		mBtnSearch = (TextButton) findViewById(R.id.btn_search);
		mBtnTranslate = (TextButton) findViewById(R.id.btn_translate);
		mLayoutWebViewSearch = findViewById(R.id.layout_webview_search);
		mTextWebViewSearchTitle = (TextView) findViewById(R.id.text_webview_search_title);
		mBtnCloseWebViewSearch = findViewById(R.id.btn_close_webview_search);
		mWebView = (SbWebView) findViewById(R.id.webview_search);
		mWebView.setDefaultClients();
		mBackgroundColorSpan = new BackgroundColorSpan(0x80ff450e);
		
		mTextContent.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch(action){
				case MotionEvent.ACTION_DOWN:
					hideLayoutWord();
					break;
				case MotionEvent.ACTION_UP:
					showLayoutWord(event);
					break;
				}
				return true;
			}
		});
		
		OnClickListener clistener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.btn_search:
					btnSearch();
					break;
				case R.id.btn_translate:
					btnTranslate();
					break;
				case R.id.btn_close_webview_search:
					hideWebViewSearch();
					break;
				case R.id.btn_close_layout_bottom:
					hideLayoutBottom();
					break;
				case R.id.btn_span_left:
					btnSpanLeft();
					break;
				case R.id.btn_span_below:
					btnSpanBelow();
					break;
				case R.id.btn_span_right:
					btnSpanRight();
					break;
				case R.id.btn_span_all:
					btnSpanAll();
					break;
				}
			}
		};
		mBtnSearch.setOnClickListener(clistener);
		mBtnTranslate.setOnClickListener(clistener);
		mBtnCloseWebViewSearch.setOnClickListener(clistener);
		mBtnCloseLayoutBottom.setOnClickListener(clistener);
		findViewById(R.id.btn_span_left).setOnClickListener(clistener);
		findViewById(R.id.btn_span_below).setOnClickListener(clistener);
		findViewById(R.id.btn_span_right).setOnClickListener(clistener);
		findViewById(R.id.btn_span_all).setOnClickListener(clistener);
	}
	
	public void setContent(String content){
		mTextContent.setText(content, BufferType.SPANNABLE);
	}
	
	public String getContent(){
		return mTextContent.getText().toString();
	}
	
	public void setFontColor(int color){
		mTextContent.setTextColor(color);
	}
	
	public void setFontSize(int size){
		mTextContent.setTextSize(size);
	}
	
	public void setLineSpace(int space){
		mTextContent.setLineSpacing(space, 1);
	}
	
	private void showLayoutWord(MotionEvent e){
		CharSequence cont = mTextContent.getText();
		Layout layout = mTextContent.getLayout();
		int line = layout.getLineForVertical((int)e.getY());
		int offset = layout.getOffsetForHorizontal(line, e.getX());
		for(; offset>=0; offset--){
			if(isChar(cont.charAt(offset)) == false) break;
		}
		int start = ++offset;
		for(; offset<cont.length(); offset++){
			if(isChar(cont.charAt(offset)) == false) break;
		}
		int end = offset;
		if(mSpannable != null)
			Selection.removeSelection(mSpannable);  // TODO: 기존 선택된 단어를 원복하려고 하는데.. 잘 안된다..
		mSpannable = (Spannable)cont;
		mSpannable.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		Selection.setSelection(mSpannable, start, end);
		
		mSelectedX = (int)(mLayoutContent.getPaddingLeft() + ((layout.getPrimaryHorizontal(start)+layout.getPrimaryHorizontal(end))/2) - (mLayoutWord.getWidth()/2));
		mSelectedY = layout.getLineBaseline(line)-mLayoutScroll.getScrollY()+mLayoutContent.getPaddingTop();
		if(mSelectedX > getWidth()-mLayoutWord.getWidth()-mLayoutContent.getPaddingLeft()-mLayoutContent.getPaddingRight())
			mSelectedX = getWidth()-mLayoutWord.getWidth()-mLayoutContent.getPaddingLeft()-mLayoutContent.getPaddingRight();
		else if(mSelectedX < 0)
			mSelectedX = 0;
		mSelectedWord = cont.subSequence(start, end).toString();
				
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLayoutWord.getLayoutParams();
		lp.setMargins(mSelectedX, mSelectedY, 0, 0);
		mLayoutWord.setLayoutParams(lp);
		mLayoutWord.setVisibility(View.VISIBLE);
	}
	
	private boolean isChar(char ch){
		return ((ch >= 97 && ch <= 122) || (ch >= 65 && ch <= 90) || (ch >= 0x3131 && ch <= 0xd7a3));
	}
	
	public void hideLayoutWord(){
		mLayoutWord.setVisibility(View.INVISIBLE);
	}
	
	public void hideWebViewSearch(){
		mLayoutWebViewSearch.setVisibility(View.GONE);
	}
	
	public boolean isShowWebViewSearch(){
		return (mLayoutWebViewSearch.getVisibility() == View.VISIBLE);
	}
	
	public void hideLayoutBottom(){
		mLayoutBottom.setVisibility(View.GONE);
	}
	
	private void btnSearch(){
		String url = null;
		try {
			url = "http://www.google.com/search?q="+URLEncoder.encode(mSelectedWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		
		mWebView.loadUrl(url);
		mTextWebViewSearchTitle.setText(getResources().getString(R.string.search)+" > "+mSelectedWord);
		mLayoutWebViewSearch.setVisibility(View.VISIBLE);
		
		hideLayoutWord();
	}
	
	private void btnTranslate(){
		/* TODO : 검색 api 를 사용할 수가 없어서 번역 페이지로 이동하도록 수정. mLayoutBottom을 다른걸로 활용할지 확인하고 없다면 삭제하자.
		mTextTranslate.setText("google\n구글. 구글로 검색하다.");
		mLayoutBottom.setVisibility(View.VISIBLE);
		*/
		
		/* TODO: q 가 적용이 제대로 되지 않는다. 추후 될때가 되면 아래 코드를 살리자.
		String url = null;
		try {
			url = "http://translate.google.com/m/translate?q="+URLEncoder.encode(mSelectedWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		*/
		String url = "http://translate.google.com/m/translate";
		
		mWebView.loadUrl(url);
		mTextWebViewSearchTitle.setText(getResources().getString(R.string.translate)+" > "+mSelectedWord);
		mLayoutWebViewSearch.setVisibility(View.VISIBLE);
		
		hideLayoutWord();
		
		// TODO: q를 넘김에도 번역페이지에 결과가 나오지 않는 문제가 있다. 그래서 javascript로 해당 내용이 출력되도록 임의 조치한다.
		mHandler.postDelayed(mTranslateRunnable, 1000);
	}
	
	private Runnable mTranslateRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(mWebView.isPageFinished()){
				String selected = mSelectedWord.trim().replace("\n", " ").replace("'","\\'");
				String js = "function fillSbData(){"
								+"if(typeof document.getElementsByTagName('textarea')[0] != 'undefined'){"
									+"document.getElementsByTagName('textarea')[0].value='"+selected+"';"
									+"_e(null, 'translate+2');"
								+"}else{"
									+"setTimeout('fillSbData();',1000);"
								+"}"
							+"}"
							+"fillSbData();";
				mWebView.injectJs(js);
			}
			else{
				mHandler.postDelayed(mTranslateRunnable, 1000);
			}
		}
	};
	
	private void btnSpanLeft(){
		CharSequence cont = mTextContent.getText();
		int start = mSpannable.getSpanStart(mBackgroundColorSpan);
		int end = mSpannable.getSpanEnd(mBackgroundColorSpan);
		for(; end<cont.length(); end--){
			if(isChar(cont.charAt(end)) == true) break;
		}
		for(; end<cont.length(); end--){
			if(isChar(cont.charAt(end)) == false) break;
		}
		if(start > end){
			return;
		}
		mSpannable.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mSelectedWord = cont.subSequence(start, end).toString();
	}
	
	private void btnSpanBelow(){
		CharSequence cont = mTextContent.getText();
		int start = mSpannable.getSpanStart(mBackgroundColorSpan);
		int end = mSpannable.getSpanEnd(mBackgroundColorSpan);
		Layout layout = mTextContent.getLayout();
		int line = layout.getLineForOffset(end);
		end = layout.getLineEnd(line+1);
		mSpannable.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mSelectedWord = cont.subSequence(start, end).toString();
	}
	
	private void btnSpanRight(){
		CharSequence cont = mTextContent.getText();
		int start = mSpannable.getSpanStart(mBackgroundColorSpan);
		int end = mSpannable.getSpanEnd(mBackgroundColorSpan);
		for(; end<cont.length(); end++){
			if(isChar(cont.charAt(end)) == true) break;
		}
		for(; end<cont.length(); end++){
			if(isChar(cont.charAt(end)) == false) break;
		}
		mSpannable.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mSelectedWord = cont.subSequence(start, end).toString();
	}
	
	private void btnSpanAll(){
		CharSequence cont = mTextContent.getText();
		int start = 0;
		int end = cont.length();
		mSpannable.setSpan(mBackgroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mSelectedWord = cont.subSequence(start, end).toString();
	}
	
	public void setBackground(int bg){
		if(SettingsView.sRepeatMap.containsKey(bg)){
			mLayoutContent.setBackgroundResource(SettingsView.sRepeatMap.get(bg));
		}
		else{
			mLayoutContent.setBackgroundColor(bg);
		}
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		if(mLayoutContent != null)
			mLayoutContent.setPadding(left, top, right, bottom);
		else
			super.setPadding(left, top, right, bottom);
	}
	
}
