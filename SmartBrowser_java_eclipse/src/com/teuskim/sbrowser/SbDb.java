package com.teuskim.sbrowser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SbDb {
	
	private static final String TAG = "SbDb";
	private static final String DATABASE_NAME = "sbrowser.db";
	
	private static final int DATABASE_VERSION = 1;
	private static SbDb sInstance;
	//private Context mContext;
	
	private SQLiteDatabase mDb;
	
	private SbDb(Context context){
		//mContext = context;
	}
	
	public static SbDb getInstance(Context context){
		
		if(sInstance != null){
			return sInstance;
		}
		
		sInstance = new SbDb(context);
		if( sInstance.open(context) ){
			return sInstance;
		}else{
			return null;
		}
		
	}
	
	private boolean open(Context context){
		DbOpenHelper dbHelper;
    	dbHelper = new DbOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    	mDb = dbHelper.getWritableDatabase();
    	return (mDb == null) ? false : true;
	}
	
	public void close(){
		mDb.close();
	}
	
	public boolean insertFavoriteUrl(String url, String title, byte[] thumb){
		
		ContentValues values = new ContentValues();
		values.put(Favorite.URL, url);
		values.put(Favorite.TITLE, title);
		values.put(Favorite.TYPE, Favorite.TYPE_URL);
		values.put(Favorite.THUMB, thumb);
		values.put(Favorite.CRT_DT, new Date().getTime());
		values.put(Favorite.ORDER_NUM, getFavoriteNextOrderNum());
		
		long rowID = mDb.insert(Favorite.TABLE_NAME, null, values);
		if(rowID <= 0){
			throw new SQLException("Failed to insert row into " + Favorite.TABLE_NAME);
		}
		return true;
	}
	
	public boolean insertFavoritePart(String url, String title, String indexSet, int width, int height, byte[] thumb){
		
		ContentValues values = new ContentValues();
		values.put(Favorite.URL, url);
		values.put(Favorite.TITLE, title);
		values.put(Favorite.TYPE, Favorite.TYPE_PART);
		values.put(Favorite.INDEX_SET, indexSet);
		values.put(Favorite.PART_WIDTH, width);
		values.put(Favorite.PART_HEIGHT, height);
		values.put(Favorite.THUMB, thumb);
		values.put(Favorite.CRT_DT, new Date().getTime());
		values.put(Favorite.ORDER_NUM, getFavoriteNextOrderNum());
		
		long rowID = mDb.insert(Favorite.TABLE_NAME, null, values);
		if(rowID <= 0){
			throw new SQLException("Failed to insert row into " + Favorite.TABLE_NAME);
		}
		return true;
	}
	
	public int getFavoriteNextOrderNum(){
		String query = "select max("+Favorite.ORDER_NUM+") from "+Favorite.TABLE_NAME;
		Cursor cursor = mDb.rawQuery(query, null);
		if(cursor.getCount() > 0 && cursor.moveToFirst()){
			return cursor.getInt(0)+10000;
		}
		return 10000;
	}
	
	public boolean updateFavoriteOrderNum(int id, int orderNum){
		ContentValues values = new ContentValues();
		values.put(Favorite.ORDER_NUM, orderNum);
		return (mDb.update(Favorite.TABLE_NAME, values, Favorite._ID+"=?", new String[]{""+id}) > 0);
	}
	
	public boolean deleteFavorite(int id){
		return (mDb.delete(Favorite.TABLE_NAME, Favorite._ID+"=?", new String[]{""+id}) > 0);
	}
	
	public List<Favorite> getFavoriteList(){
		List<Favorite> list = new ArrayList<Favorite>();
		Cursor cursor = mDb.query(Favorite.TABLE_NAME
								, new String[]{Favorite._ID, Favorite.URL, Favorite.TITLE, Favorite.TYPE, Favorite.THUMB, Favorite.INDEX_SET, Favorite.PART_WIDTH, Favorite.PART_HEIGHT, Favorite.CRT_DT, Favorite.ORDER_NUM}
								, null, null, null, null, Favorite.ORDER_NUM);
		if(cursor.moveToFirst()){
			do{
				Favorite favorite = getFavorite(cursor);
				list.add(favorite);
			}while(cursor.moveToNext());
		}
		return list;
	}
	
	private Favorite getFavorite(Cursor cursor){
		Favorite favorite = new Favorite();
		favorite.mId = cursor.getInt(0);
		favorite.mUrl = cursor.getString(1);
		favorite.mTitle = cursor.getString(2);
		favorite.mType = cursor.getInt(3);
		favorite.mThumb = cursor.getBlob(4);
		favorite.mIndexSet = cursor.getString(5);
		favorite.mPartWidth = cursor.getInt(6);
		favorite.mPartHeight = cursor.getInt(7);
		favorite.mCrtDt = cursor.getString(8);
		favorite.mOrderNum = cursor.getInt(9);
		return favorite;
	}
	
	public Favorite getFavorite(int id){
		Favorite favorite = null;
		try{
			Cursor cursor = mDb.query(Favorite.TABLE_NAME
							, new String[]{Favorite._ID, Favorite.URL, Favorite.TITLE, Favorite.TYPE, Favorite.THUMB, Favorite.INDEX_SET, Favorite.PART_WIDTH, Favorite.PART_HEIGHT, Favorite.CRT_DT, Favorite.ORDER_NUM}
							, Favorite._ID+"=?"
							, new String[]{""+id}
							, null, null, null);
			
			if(cursor.moveToFirst()){
				favorite = getFavorite(cursor);
			}
			cursor.close();
		}catch(Exception e){}
		
		return favorite;
	}
	
	public boolean insertSavedCont(String title, String html, byte[] thumb){
		
		ContentValues values = new ContentValues();
		values.put(SavedCont.TITLE, title);
		values.put(SavedCont.HTML, html);
		values.put(SavedCont.SIZE, html.getBytes().length);
		values.put(SavedCont.THUMB, thumb);
		values.put(SavedCont.CRT_DT, new Date().getTime());
		values.put(SavedCont.ORDER_NUM, getSavedContNextOrderNum());
		
		long rowID = mDb.insert(SavedCont.TABLE_NAME, null, values);
		if(rowID <= 0){
			throw new SQLException("Failed to insert row into " + SavedCont.TABLE_NAME);
		}
		return true;
	}
	
	private int getSavedContNextOrderNum(){
		String query = "select max("+SavedCont.ORDER_NUM+") from "+SavedCont.TABLE_NAME;
		Cursor cursor = mDb.rawQuery(query, null);
		if(cursor.getCount() > 0 && cursor.moveToFirst()){
			return cursor.getInt(0)+10000;
		}
		return 10000;
	}
	
	public boolean updateSavedContOrderNum(int id, int orderNum){
		ContentValues values = new ContentValues();
		values.put(SavedCont.ORDER_NUM, orderNum);
		return (mDb.update(SavedCont.TABLE_NAME, values, SavedCont._ID+"=?", new String[]{""+id}) > 0);
	}
	
	public boolean deleteSavedCont(int id){
		return (mDb.delete(SavedCont.TABLE_NAME, SavedCont._ID+"=?", new String[]{""+id}) > 0);
	}
	
	public List<SavedCont> getSavedContList(){
		List<SavedCont> list = new ArrayList<SavedCont>();
		Cursor cursor = mDb.query(SavedCont.TABLE_NAME
								, new String[]{SavedCont._ID, SavedCont.TITLE, SavedCont.SIZE, SavedCont.THUMB, SavedCont.CRT_DT, SavedCont.ORDER_NUM}
								, null, null, null, null, SavedCont.ORDER_NUM);
		if(cursor.moveToFirst()){
			do{
				SavedCont savedCont = new SavedCont();
				savedCont.mId = cursor.getInt(0);
				savedCont.mTitle = cursor.getString(1);
				savedCont.mSize = cursor.getInt(2);
				savedCont.mThumb = cursor.getBlob(3);
				savedCont.mCrtDt = cursor.getString(4);
				savedCont.mOrderNum = cursor.getInt(5);
				list.add(savedCont);
			}while(cursor.moveToNext());
		}
		return list;
	}
	
	public SavedCont getSavedCont(int id){
		SavedCont saved = null;
		try{
			Cursor cursor = mDb.query(SavedCont.TABLE_NAME
					, new String[]{SavedCont._ID, SavedCont.TITLE, SavedCont.HTML, SavedCont.SIZE, SavedCont.THUMB, SavedCont.CRT_DT, SavedCont.ORDER_NUM}
							, SavedCont._ID+"=?"
							, new String[]{""+id}
							, null, null, null);
			
			if(cursor.moveToFirst()){
				saved = new SavedCont();
				saved.mId = cursor.getInt(0);
				saved.mTitle = cursor.getString(1);
				saved.mHtml = cursor.getString(2);
				saved.mSize = cursor.getInt(3);
				saved.mThumb = cursor.getBlob(4);
				saved.mCrtDt = cursor.getString(5);
				saved.mOrderNum = cursor.getInt(6);
			}
			cursor.close();
		}catch(Exception e){}
		
		return saved;
	}
	
	private boolean insertOrUpdateInputHistory(String input, InputType inputType, String title, String directYN){
		
		InputHistory ih = getInputHistory(input);
		if(ih != null){  // update
			ContentValues values = new ContentValues();
			long currtime = new Date().getTime();
			values.put(InputHistory.UPD_DT, currtime);
			values.put(InputHistory.USE_CNT, ih.mUseCnt + 1);
			
			int rowCnt = mDb.update(InputHistory.TABLE_NAME, values, InputHistory._ID+"=?", new String[]{ ""+ih.mId });
			if(rowCnt <= 0){
				Log.e(TAG, "Failed to update row into " + InputHistory.TABLE_NAME);
				return false;
			}
		}
		else{  // insert
			ContentValues values = new ContentValues();
			values.put(InputHistory.INPUT, input);
			values.put(InputHistory.INPUT_TYPE, inputType.toString());
			values.put(InputHistory.TITLE, title);
			values.put(InputHistory.DIRECT_YN, directYN);
			long currtime = new Date().getTime();
			values.put(InputHistory.CRT_DT, currtime);
			values.put(InputHistory.UPD_DT, currtime);
			values.put(InputHistory.USE_CNT, 1);
			values.put(InputHistory.DATA, HangulUtil.separateJaso(input));
			
			long rowID = mDb.insert(InputHistory.TABLE_NAME, null, values);
			if(rowID <= 0){
				Log.e(TAG, "Failed to insert row into " + InputHistory.TABLE_NAME);
				return false;
			}
		}
		return true;
	}
	
	public boolean insertOrUpdateInputUrl(String url, String title, String directYN){
		return insertOrUpdateInputHistory(url, InputType.URL, title, directYN);
	}
	
	public boolean insertOrUpdateInputWord(String word, String title, String directYN){
		return insertOrUpdateInputHistory(word, InputType.WORD, title, directYN);
	}
	
	public InputHistory getInputHistory(String input){
		InputHistory ih = null;
		try{
			Cursor cursor = mDb.query(InputHistory.TABLE_NAME
					, new String[]{InputHistory._ID, InputHistory.INPUT, InputHistory.INPUT_TYPE, InputHistory.TITLE, InputHistory.DIRECT_YN, InputHistory.CRT_DT, InputHistory.UPD_DT, InputHistory.USE_CNT}
					, InputHistory.INPUT+"=?"
					, new String[]{ input }
					, null, null, null);
			if(cursor.getCount() > 0 && cursor.moveToFirst()){
				ih = new InputHistory();
				ih.mId = cursor.getInt(0);
				ih.mInput = cursor.getString(1);
				ih.mInputType = InputType.valueOf(cursor.getString(2));
				ih.mTitle = cursor.getString(3);
				ih.mDirectYN = cursor.getString(4);
				ih.mCrtDt = cursor.getString(5);
				ih.mUpdDt = cursor.getString(6);
				ih.mUseCnt = cursor.getInt(7);
			}
			cursor.close();
		}catch(Exception e){}
		
		return ih;
	}
	
	public Cursor getInputHistoryList(String preword){
		if(preword == null)
			preword = "";
		Cursor cursor = mDb.query(InputHistory.TABLE_NAME
				, new String[]{InputHistory._ID, InputHistory.INPUT, InputHistory.INPUT_TYPE, InputHistory.TITLE, InputHistory.DIRECT_YN, InputHistory.CRT_DT, InputHistory.UPD_DT, InputHistory.USE_CNT}
						, InputHistory.DATA+" like ?"
						, new String[]{ preword+"_%" }
						, null, null
						, InputHistory.UPD_DT+" desc");
		return cursor;
	}
	
	public InputHistory getInputHistory(Cursor cursor){
		InputHistory ih = new InputHistory();
		ih.mId = cursor.getInt(0);
		ih.mInput = cursor.getString(1);
		ih.mInputType = InputType.valueOf(cursor.getString(2));
		ih.mTitle = cursor.getString(3);
		ih.mDirectYN = cursor.getString(4);
		ih.mCrtDt = cursor.getString(5);
		ih.mUpdDt = cursor.getString(6);
		ih.mUseCnt = cursor.getInt(7);
		return ih;
	}
	
	public boolean deleteInputHistory(int id){
		int rowCnt = mDb.delete(InputHistory.TABLE_NAME
								, InputHistory._ID+"=?"
								, new String[]{ ""+id });
		return (rowCnt > 0);
	}

	
	/**
	 * db open helper 
	 */
	public static class DbOpenHelper extends SQLiteOpenHelper{
		
		public DbOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		public DbOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db){
			
			Favorite.onCreate(db);
			SavedCont.onCreate(db);
			InputHistory.onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 버전 업그레이드 할때 필요한 동작은 여기에 추가.
		}
	}
	
	
	/**
	 * Favorite 테이블 구조.
	 */
	public static class Favorite{
		
		public static final String TABLE_NAME = "favorite";
		public static final String _ID = "_id";
		public static final String URL = "url";
		public static final String TITLE = "title";
		public static final String TYPE = "type";
		public static final String THUMB = "thumb";
		public static final String INDEX_SET = "index_set";
		public static final String PART_WIDTH = "part_width";
		public static final String PART_HEIGHT = "part_height";
		public static final String CRT_DT = "crt_dt";
		public static final String ORDER_NUM = "order_num";
		
		public static final int TYPE_URL = 0;
		public static final int TYPE_PART = 1;
		
		public int mId;
		public String mUrl;
		public String mTitle;
		public int mType;
		public byte[] mThumb;
		public String mIndexSet;
		public int mPartWidth;
		public int mPartHeight;
		public String mCrtDt;
		public int mOrderNum;
		
		public static final String CREATE = 
			"CREATE TABLE " + TABLE_NAME +"( "
			+ _ID + " INTEGER primary key autoincrement, "
			+ URL + " TEXT, "
			+ TITLE + " TEXT, "
			+ TYPE + " INTEGER, "
			+ THUMB + " BLOB, "
			+ INDEX_SET + " TEXT, "
			+ PART_WIDTH + " INTEGER, "
			+ PART_HEIGHT + " INTEGER, "
			+ CRT_DT + " TEXT, "
			+ ORDER_NUM + " INTEGER"
			+ ");";
		
		public static void onCreate(SQLiteDatabase db){
			db.execSQL(CREATE);
		}
	}

	/**
	 * SavedCont 테이블 구조
	 */
	public static class SavedCont{
		
		public static final String TABLE_NAME = "saved_cont";
		public static final String _ID = "_id";
		public static final String TITLE = "title";
		public static final String HTML = "html";
		public static final String SIZE = "size";
		public static final String THUMB = "thumb";
		public static final String CRT_DT = "crt_dt";
		public static final String ORDER_NUM = "order_num";
		
		public int mId;
		public String mTitle;
		public String mHtml;
		public int mSize;
		public byte[] mThumb;
		public String mCrtDt;
		public int mOrderNum;
		
		public static final String CREATE = 
			"CREATE TABLE " + TABLE_NAME +"( "
			+ _ID + " INTEGER primary key autoincrement, "
			+ TITLE + " TEXT, "
			+ HTML + " TEXT, "
			+ SIZE + " INTEGER, "
			+ THUMB + " BLOB, "
			+ CRT_DT + " TEXT, "
			+ ORDER_NUM + " INTEGER"
			+ ");";
		
		public static void onCreate(SQLiteDatabase db){
			db.execSQL(CREATE);
		}
	}
	
	/**
	 * InputHistory 의 input_type
	 */
	public static enum InputType {
		URL, WORD
	}
	
	/**
	 * InputHistory 테이블 구조
	 */
	public static class InputHistory{
		
		public static final String TABLE_NAME = "input_history";
		public static final String _ID = "_id";
		public static final String INPUT = "input";
		public static final String INPUT_TYPE = "input_type";
		public static final String TITLE = "title";
		public static final String DIRECT_YN = "direct_yn";
		public static final String CRT_DT = "crt_dt";
		public static final String UPD_DT = "upd_dt";
		public static final String USE_CNT = "use_cnt";
		public static final String DATA = "data";
		
		public int mId;
		public String mInput;
		public InputType mInputType;
		public String mTitle;
		public String mDirectYN;
		public String mCrtDt;
		public String mUpdDt;
		public int mUseCnt;
		
		public static final String CREATE = 
			"CREATE TABLE " + TABLE_NAME +"( "
			+ _ID + " INTEGER primary key autoincrement, "
			+ INPUT + " TEXT, "
			+ INPUT_TYPE + " TEXT, "
			+ TITLE + " TEXT, "
			+ DIRECT_YN + " TEXT, "
			+ CRT_DT + " TEXT, "
			+ UPD_DT + " TEXT, "
			+ USE_CNT + " INTEGER, "
			+ DATA + " TEXT"
			+ ");";
		
		public static void onCreate(SQLiteDatabase db){
			db.execSQL(CREATE);
		}
	}
		
}
