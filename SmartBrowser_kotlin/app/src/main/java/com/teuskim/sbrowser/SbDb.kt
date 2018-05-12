package com.teuskim.sbrowser

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class SbDb private constructor(context: Context)//mContext = context;
{
    //private Context mContext;

    private var mDb: SQLiteDatabase? = null

    val favoriteNextOrderNum: Int
        get() {
            val query = "select max(" + Favorite.ORDER_NUM + ") from " + Favorite.TABLE_NAME
            val cursor = mDb!!.rawQuery(query, null)
            return if (cursor.count > 0 && cursor.moveToFirst()) {
                cursor.getInt(0) + 10000
            } else 10000
        }

    val favoriteList: List<Favorite>
        get() {
            val list = ArrayList<Favorite>()
            val cursor = mDb!!.query(Favorite.TABLE_NAME, arrayOf(Favorite._ID, Favorite.URL, Favorite.TITLE, Favorite.TYPE, Favorite.THUMB, Favorite.INDEX_SET, Favorite.PART_WIDTH, Favorite.PART_HEIGHT, Favorite.CRT_DT, Favorite.ORDER_NUM, Favorite.COOKIE), null, null, null, null, Favorite.ORDER_NUM)
            if (cursor.moveToFirst()) {
                do {
                    val favorite = getFavorite(cursor)
                    list.add(favorite)
                } while (cursor.moveToNext())
            }
            return list
        }

    private val savedContNextOrderNum: Int
        get() {
            val query = "select max(" + SavedCont.ORDER_NUM + ") from " + SavedCont.TABLE_NAME
            val cursor = mDb!!.rawQuery(query, null)
            return if (cursor.count > 0 && cursor.moveToFirst()) {
                cursor.getInt(0) + 10000
            } else 10000
        }

    val savedContList: List<SavedCont>
        get() {
            val list = ArrayList<SavedCont>()
            val cursor = mDb!!.query(SavedCont.TABLE_NAME, arrayOf(SavedCont._ID, SavedCont.TITLE, SavedCont.FILENAME, SavedCont.SIZE, SavedCont.THUMB, SavedCont.CRT_DT, SavedCont.ORDER_NUM), null, null, null, null, SavedCont.ORDER_NUM)
            if (cursor.moveToFirst()) {
                do {
                    val savedCont = SavedCont()
                    savedCont.mId = cursor.getInt(0)
                    savedCont.mTitle = cursor.getString(1)
                    savedCont.mFilename = cursor.getString(2)
                    savedCont.mSize = cursor.getInt(3)
                    savedCont.mThumb = cursor.getBlob(4)
                    savedCont.mCrtDt = cursor.getString(5)
                    savedCont.mOrderNum = cursor.getInt(6)
                    list.add(savedCont)
                } while (cursor.moveToNext())
            }
            return list
        }

    private fun open(context: Context): Boolean {
        val dbHelper: DbOpenHelper
        dbHelper = DbOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
        mDb = dbHelper.writableDatabase
        return if (mDb == null) false else true
    }

    fun close() {
        mDb!!.close()
    }

    fun insertFavoriteUrl(url: String, title: String, thumb: ByteArray): Boolean {

        val values = ContentValues()
        values.put(Favorite.URL, url)
        values.put(Favorite.TITLE, title)
        values.put(Favorite.TYPE, Favorite.TYPE_URL)
        values.put(Favorite.THUMB, thumb)
        values.put(Favorite.CRT_DT, Date().time)
        values.put(Favorite.ORDER_NUM, favoriteNextOrderNum)

        val rowID = mDb!!.insert(Favorite.TABLE_NAME, null, values)
        if (rowID <= 0) {
            throw SQLException("Failed to insert row into " + Favorite.TABLE_NAME)
        }
        return true
    }

    fun insertFavoritePart(url: String, title: String, indexSet: String, width: Int, height: Int, thumb: ByteArray, cookie: String): Boolean {

        val values = ContentValues()
        values.put(Favorite.URL, url)
        values.put(Favorite.TITLE, title)
        values.put(Favorite.TYPE, Favorite.TYPE_PART)
        values.put(Favorite.INDEX_SET, indexSet)
        values.put(Favorite.PART_WIDTH, width)
        values.put(Favorite.PART_HEIGHT, height)
        values.put(Favorite.THUMB, thumb)
        values.put(Favorite.COOKIE, cookie)
        values.put(Favorite.CRT_DT, Date().time)
        values.put(Favorite.ORDER_NUM, favoriteNextOrderNum)

        val rowID = mDb!!.insert(Favorite.TABLE_NAME, null, values)
        if (rowID <= 0) {
            throw SQLException("Failed to insert row into " + Favorite.TABLE_NAME)
        }
        return true
    }

    fun updateFavoriteOrderNum(id: Int, orderNum: Int): Boolean {
        val values = ContentValues()
        values.put(Favorite.ORDER_NUM, orderNum)
        return mDb!!.update(Favorite.TABLE_NAME, values, Favorite._ID + "=?", arrayOf("" + id)) > 0
    }

    fun deleteFavorite(id: Int): Boolean {
        return mDb!!.delete(Favorite.TABLE_NAME, Favorite._ID + "=?", arrayOf("" + id)) > 0
    }

    private fun getFavorite(cursor: Cursor): Favorite {
        val favorite = Favorite()
        favorite.mId = cursor.getInt(0)
        favorite.mUrl = cursor.getString(1)
        favorite.mTitle = cursor.getString(2)
        favorite.mType = cursor.getInt(3)
        favorite.mThumb = cursor.getBlob(4)
        favorite.mIndexSet = cursor.getString(5)
        favorite.mPartWidth = cursor.getInt(6)
        favorite.mPartHeight = cursor.getInt(7)
        favorite.mCrtDt = cursor.getString(8)
        favorite.mOrderNum = cursor.getInt(9)
        favorite.mCookie = cursor.getString(10)
        return favorite
    }

    fun getFavorite(id: Int): Favorite? {
        var favorite: Favorite? = null
        try {
            val cursor = mDb!!.query(Favorite.TABLE_NAME, arrayOf(Favorite._ID, Favorite.URL, Favorite.TITLE, Favorite.TYPE, Favorite.THUMB, Favorite.INDEX_SET, Favorite.PART_WIDTH, Favorite.PART_HEIGHT, Favorite.CRT_DT, Favorite.ORDER_NUM, Favorite.COOKIE), Favorite._ID + "=?", arrayOf("" + id), null, null, null)

            if (cursor.moveToFirst()) {
                favorite = getFavorite(cursor)
            }
            cursor.close()
        } catch (e: Exception) {
        }

        return favorite
    }

    fun insertSavedCont(title: String, filename: String, thumb: ByteArray): Boolean {

        val values = ContentValues()
        values.put(SavedCont.TITLE, title)
        values.put(SavedCont.FILENAME, filename)
        values.put(SavedCont.SIZE, 0)
        values.put(SavedCont.THUMB, thumb)
        values.put(SavedCont.CRT_DT, Date().time)
        values.put(SavedCont.ORDER_NUM, savedContNextOrderNum)

        val rowID = mDb!!.insert(SavedCont.TABLE_NAME, null, values)
        if (rowID <= 0) {
            throw SQLException("Failed to insert row into " + SavedCont.TABLE_NAME)
        }
        return true
    }

    fun updateSavedContOrderNum(id: Int, orderNum: Int): Boolean {
        val values = ContentValues()
        values.put(SavedCont.ORDER_NUM, orderNum)
        return mDb!!.update(SavedCont.TABLE_NAME, values, SavedCont._ID + "=?", arrayOf("" + id)) > 0
    }

    fun deleteSavedCont(id: Int): Boolean {
        return mDb!!.delete(SavedCont.TABLE_NAME, SavedCont._ID + "=?", arrayOf("" + id)) > 0
    }

    fun getSavedCont(id: Int): SavedCont? {
        var saved: SavedCont? = null
        try {
            val cursor = mDb!!.query(SavedCont.TABLE_NAME, arrayOf(SavedCont._ID, SavedCont.TITLE, SavedCont.FILENAME, SavedCont.SIZE, SavedCont.THUMB, SavedCont.CRT_DT, SavedCont.ORDER_NUM), SavedCont._ID + "=?", arrayOf("" + id), null, null, null)

            if (cursor.moveToFirst()) {
                saved = SavedCont()
                saved.mId = cursor.getInt(0)
                saved.mTitle = cursor.getString(1)
                saved.mFilename = cursor.getString(2)
                saved.mSize = cursor.getInt(3)
                saved.mThumb = cursor.getBlob(4)
                saved.mCrtDt = cursor.getString(5)
                saved.mOrderNum = cursor.getInt(6)
            }
            cursor.close()
        } catch (e: Exception) {
        }

        return saved
    }

    private fun insertOrUpdateInputHistory(input: String, inputType: InputType, title: String?, directYN: String): Boolean {

        val ih = getInputHistory(input)
        if (ih != null) {  // update
            val values = ContentValues()
            val currtime = Date().time
            values.put(InputHistory.UPD_DT, currtime)
            values.put(InputHistory.USE_CNT, ih.mUseCnt + 1)

            val rowCnt = mDb!!.update(InputHistory.TABLE_NAME, values, InputHistory._ID + "=?", arrayOf("" + ih.mId))
            if (rowCnt <= 0) {
                Log.e(TAG, "Failed to update row into " + InputHistory.TABLE_NAME)
                return false
            }
        } else {  // insert
            val values = ContentValues()
            values.put(InputHistory.INPUT, input)
            values.put(InputHistory.INPUT_TYPE, inputType.toString())
            values.put(InputHistory.TITLE, title)
            values.put(InputHistory.DIRECT_YN, directYN)
            val currtime = Date().time
            values.put(InputHistory.CRT_DT, currtime)
            values.put(InputHistory.UPD_DT, currtime)
            values.put(InputHistory.USE_CNT, 1)
            values.put(InputHistory.DATA, HangulUtil.separateJaso(input))

            val rowID = mDb!!.insert(InputHistory.TABLE_NAME, null, values)
            if (rowID <= 0) {
                Log.e(TAG, "Failed to insert row into " + InputHistory.TABLE_NAME)
                return false
            }
        }
        return true
    }

    fun insertOrUpdateInputUrl(url: String, title: String?, directYN: String): Boolean {
        return insertOrUpdateInputHistory(url, InputType.URL, title, directYN)
    }

    fun insertOrUpdateInputWord(word: String, title: String?, directYN: String): Boolean {
        return insertOrUpdateInputHistory(word, InputType.WORD, title, directYN)
    }

    fun getInputHistory(input: String): InputHistory? {
        var ih: InputHistory? = null
        try {
            val cursor = mDb!!.query(InputHistory.TABLE_NAME, arrayOf(InputHistory._ID, InputHistory.INPUT, InputHistory.INPUT_TYPE, InputHistory.TITLE, InputHistory.DIRECT_YN, InputHistory.CRT_DT, InputHistory.UPD_DT, InputHistory.USE_CNT), InputHistory.INPUT + "=?", arrayOf(input), null, null, null)
            if (cursor.count > 0 && cursor.moveToFirst()) {
                ih = InputHistory()
                ih.mId = cursor.getInt(0)
                ih.mInput = cursor.getString(1)
                ih.mInputType = InputType.valueOf(cursor.getString(2))
                ih.mTitle = cursor.getString(3)
                ih.mDirectYN = cursor.getString(4)
                ih.mCrtDt = cursor.getString(5)
                ih.mUpdDt = cursor.getString(6)
                ih.mUseCnt = cursor.getInt(7)
            }
            cursor.close()
        } catch (e: Exception) {
        }

        return ih
    }

    fun getInputHistoryList(preword: String?): Cursor {
        var preword = preword
        if (preword == null)
            preword = ""
        return mDb!!.query(InputHistory.TABLE_NAME, arrayOf(InputHistory._ID, InputHistory.INPUT, InputHistory.INPUT_TYPE, InputHistory.TITLE, InputHistory.DIRECT_YN, InputHistory.CRT_DT, InputHistory.UPD_DT, InputHistory.USE_CNT), InputHistory.DATA + " like ?", arrayOf(preword + "_%"), null, null, InputHistory.UPD_DT + " desc")
    }

    fun getInputHistory(cursor: Cursor): InputHistory {
        val ih = InputHistory()
        ih.mId = cursor.getInt(0)
        ih.mInput = cursor.getString(1)
        ih.mInputType = InputType.valueOf(cursor.getString(2))
        ih.mTitle = cursor.getString(3)
        ih.mDirectYN = cursor.getString(4)
        ih.mCrtDt = cursor.getString(5)
        ih.mUpdDt = cursor.getString(6)
        ih.mUseCnt = cursor.getInt(7)
        return ih
    }

    fun deleteInputHistory(id: Int): Boolean {
        val rowCnt = mDb!!.delete(InputHistory.TABLE_NAME, InputHistory._ID + "=?", arrayOf("" + id))
        return rowCnt > 0
    }


    /**
     * db open helper
     */
    class DbOpenHelper : SQLiteOpenHelper {

        constructor(context: Context) : super(context, DATABASE_NAME, null, DATABASE_VERSION) {}

        constructor(context: Context, name: String, factory: CursorFactory?, version: Int) : super(context, name, factory, version) {}

        override fun onCreate(db: SQLiteDatabase) {

            Favorite.onCreate(db)
            SavedCont.onCreate(db)
            InputHistory.onCreate(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // 버전 업그레이드 할때 필요한 동작은 여기에 추가.
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE " + Favorite.TABLE_NAME + " ADD COLUMN " + Favorite.COOKIE + " TEXT")
            }
        }
    }


    /**
     * Favorite 테이블 구조.
     */
    class Favorite {

        var mId: Int = 0
        var mUrl: String? = null
        var mTitle: String? = null
        var mType: Int = 0
        var mThumb: ByteArray? = null
        var mIndexSet: String? = null
        var mPartWidth: Int = 0
        var mPartHeight: Int = 0
        var mCrtDt: String? = null
        var mOrderNum: Int = 0
        var mCookie: String? = null

        companion object {

            val TABLE_NAME = "favorite"
            val _ID = "_id"
            val URL = "url"
            val TITLE = "title"
            val TYPE = "type"
            val THUMB = "thumb"
            val INDEX_SET = "index_set"
            val PART_WIDTH = "part_width"
            val PART_HEIGHT = "part_height"
            val CRT_DT = "crt_dt"
            val ORDER_NUM = "order_num"
            val COOKIE = "cookie"

            val TYPE_URL = 0
            val TYPE_PART = 1

            val CREATE = (
                    "CREATE TABLE " + TABLE_NAME + "( "
                            + _ID + " INTEGER primary key autoincrement, "
                            + URL + " TEXT, "
                            + TITLE + " TEXT, "
                            + TYPE + " INTEGER, "
                            + THUMB + " BLOB, "
                            + INDEX_SET + " TEXT, "
                            + PART_WIDTH + " INTEGER, "
                            + PART_HEIGHT + " INTEGER, "
                            + CRT_DT + " TEXT, "
                            + ORDER_NUM + " INTEGER, "
                            + COOKIE + " TEXT"
                            + ");")

            fun onCreate(db: SQLiteDatabase) {
                db.execSQL(CREATE)
            }
        }
    }

    /**
     * SavedCont 테이블 구조
     */
    class SavedCont {

        var mId: Int = 0
        var mTitle: String? = null
        var mFilename: String? = null
        var mSize: Int = 0
        var mThumb: ByteArray? = null
        var mCrtDt: String? = null
        var mOrderNum: Int = 0

        companion object {

            val TABLE_NAME = "saved_cont"
            val _ID = "_id"
            val TITLE = "title"
            val FILENAME = "html"  // 실제로는 파일명으로 사용. 최초 설계시 실수
            val SIZE = "size"  // 실제로는 사용하지 않는다.
            val THUMB = "thumb"
            val CRT_DT = "crt_dt"
            val ORDER_NUM = "order_num"

            val CREATE = (
                    "CREATE TABLE " + TABLE_NAME + "( "
                            + _ID + " INTEGER primary key autoincrement, "
                            + TITLE + " TEXT, "
                            + FILENAME + " TEXT, "
                            + SIZE + " INTEGER, "
                            + THUMB + " BLOB, "
                            + CRT_DT + " TEXT, "
                            + ORDER_NUM + " INTEGER"
                            + ");")

            fun onCreate(db: SQLiteDatabase) {
                db.execSQL(CREATE)
            }
        }
    }

    /**
     * InputHistory 의 input_type
     */
    enum class InputType {
        URL, WORD
    }

    /**
     * InputHistory 테이블 구조
     */
    class InputHistory {

        var mId: Int = 0
        var mInput: String? = null
        var mInputType: InputType? = null
        var mTitle: String? = null
        var mDirectYN: String? = null
        var mCrtDt: String? = null
        var mUpdDt: String? = null
        var mUseCnt: Int = 0

        companion object {

            val TABLE_NAME = "input_history"
            val _ID = "_id"
            val INPUT = "input"
            val INPUT_TYPE = "input_type"
            val TITLE = "title"
            val DIRECT_YN = "direct_yn"
            val CRT_DT = "crt_dt"
            val UPD_DT = "upd_dt"
            val USE_CNT = "use_cnt"
            val DATA = "data"

            val CREATE = (
                    "CREATE TABLE " + TABLE_NAME + "( "
                            + _ID + " INTEGER primary key autoincrement, "
                            + INPUT + " TEXT, "
                            + INPUT_TYPE + " TEXT, "
                            + TITLE + " TEXT, "
                            + DIRECT_YN + " TEXT, "
                            + CRT_DT + " TEXT, "
                            + UPD_DT + " TEXT, "
                            + USE_CNT + " INTEGER, "
                            + DATA + " TEXT"
                            + ");")

            fun onCreate(db: SQLiteDatabase) {
                db.execSQL(CREATE)
            }
        }
    }

    companion object {

        private val TAG = "SbDb"
        private val DATABASE_NAME = "sbrowser.db"

        private val DATABASE_VERSION = 2
        private var sInstance: SbDb? = null

        fun getInstance(context: Context): SbDb? {

            if (sInstance != null) {
                return sInstance
            }

            sInstance = SbDb(context)
            return if (sInstance!!.open(context)) {
                sInstance
            } else {
                null
            }

        }
    }

}
