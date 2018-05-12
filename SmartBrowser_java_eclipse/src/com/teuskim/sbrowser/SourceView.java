package com.teuskim.sbrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

public class SourceView extends ScrollView {
	
	private TextView mTextSource;

	public SourceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SourceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SourceView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.source_view, this);
		mTextSource = (TextView) findViewById(R.id.text_source);
	}
	
	public void setSource(String source){
		mTextSource.setText(source);
	}
	
	public String getSource(){
		return mTextSource.getText().toString();
	}
	
}
