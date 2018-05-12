package com.teuskim.sbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class InputActivity extends Activity {
	
	private EditText mInputQuery;
	private ListView mInputHistoryListView;
	private TextView mNoListView;
	
	private InputMethodManager mImm;
	private InputHistoryAdapter mAdapter;
	private SbDb mDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input);
		
		mImm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		mDb = SbDb.getInstance(getApplicationContext());
		
		findViews();
		loadList();
	}
	
	protected void findViews(){
		mInputQuery = (EditText)findViewById(R.id.input_query);
		mInputHistoryListView = (ListView)findViewById(R.id.input_history);
		mNoListView = (TextView)findViewById(R.id.no_input_history);
		findViewById(R.id.btn_go).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goWeb(mInputQuery.getText().toString());
			}
		});
		
		LinearLayout urlWordBar = (LinearLayout) findViewById(R.id.url_word_bar);
		String[] urlWords = new String[]{"http://","www","com",".","/","?","&","="};
		for(int i=0; i<urlWords.length; i++){
			final String word = urlWords[i];
			Button btn = new Button(getApplicationContext());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			btn.setLayoutParams(lp);
			btn.setText(word);
			btn.setTextSize(12);
			btn.setTextColor(0xff606261);
			btn.setBackgroundResource(R.drawable.btn_settings_default);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Editable edit = mInputQuery.getEditableText();
					edit.append(word);
				}
			});
			urlWordBar.addView(btn);
		}
		
		mInputHistoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String input = (String) view.getTag();
				goWeb(input);
			}
			
		});
		
		mInputQuery.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_GO
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
					
					goWeb(v.getText().toString());
					return true;
				}
				return false;
			}
		});
		
		mInputQuery.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				loadList();
			}
		});
	}
	
	private void goWeb(String query){
		if(TextUtils.isEmpty(query)){
			finish();
		}
		else{
			Intent i = new Intent(getApplicationContext(), WebActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			String url;
			if(query.contains("://") || query.startsWith("sms:") || query.startsWith("tel:") || query.startsWith("mailto:")){
				url = query;
				saveInputHistory(true, url);
			}
			else if(query.contains(".")){
				url = "http://"+query;
				saveInputHistory(true, url);
			}
			else{
				url = "http://www.google.com/search?q="+query;
				saveInputHistory(false, query);
			}
			i.putExtra("url", url);
			startActivity(i);
		}
	}
	
	private void saveInputHistory(boolean isUrl, String input){
		try{
			input = input.trim();
			if(isUrl){
				SbDb.getInstance(getApplicationContext()).insertOrUpdateInputUrl(input, null, "Y");
			}
			else{
				SbDb.getInstance(getApplicationContext()).insertOrUpdateInputWord(input, null, "Y");
			}
		}catch(Exception e){}
	}
	
	private void loadList(){
		String preword = mInputQuery.getText().toString();
		Cursor cursor = SbDb.getInstance(getApplicationContext()).getInputHistoryList(preword);
		if(cursor.getCount() > 0)
			mNoListView.setVisibility(View.GONE);
		else
			mNoListView.setVisibility(View.VISIBLE);
		
		if(mAdapter == null){
			mAdapter = new InputHistoryAdapter(getApplicationContext(), cursor);
			mInputHistoryListView.setAdapter(mAdapter);
		}
		else{
			mAdapter.changeCursor(cursor);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		mImm.hideSoftInputFromWindow(mInputQuery.getWindowToken(), 0);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mAdapter.getCursor().close();
		finish();
	}
	
	private class InputHistoryAdapter extends SimpleCursorAdapter {
		
		private LayoutInflater mInflater;
		private OnClickListener mDeleteListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int id = (Integer) v.getTag();
				mDb.deleteInputHistory(id);
				loadList();
			}
		};
		
		public InputHistoryAdapter(Context context, Cursor c) {
			super(context, 0, c, new String[]{}, new int[]{}, 0);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			SbDb.InputHistory inputHistory = mDb.getInputHistory(cursor);
			((TextView)v.findViewById(R.id.url)).setText(inputHistory.mInput);
			
			View btnDelete = v.findViewById(R.id.btn_delete);
			btnDelete.setTag(inputHistory.mId);
			btnDelete.setOnClickListener(mDeleteListener);
			
			v.setTag(inputHistory.mInput);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.input_history_item, null);
			return v;
		}
		
	}
	
}
