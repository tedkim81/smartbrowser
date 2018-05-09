package com.teuskim.sbrowser

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import com.teuskim.sbrowser.MoveableListView.DragListener
import com.teuskim.sbrowser.MoveableListView.DropListener
import com.teuskim.sbrowser.SbDb.Favorite
import com.teuskim.sbrowser.SbDb.SavedCont
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import java.util.*

class MainActivity : BaseRemoconActivity(), DragListener, DropListener {
    private var mMainMode = MAIN_MODE_HOME

    private var mIconDefault: ImageView? = null
    private var mTitleMain1: TextView? = null
    private var mTitleMain2: TextView? = null
    private var mListViewHome: MoveableListView? = null
    private var mListViewSaved: MoveableListView? = null
    private var mBtnSettings: RemoconMenuView? = null
    private var mBtnMainMode: RemoconMenuView? = null
    private var mLogo: View? = null

    private var mListFavorite: List<Favorite>? = null
    private var mAdapterHome: HomeAdapter? = null

    private var mListSavedCont: List<SavedCont>? = null
    private var mAdapterSaved: SavedAdapter? = null

    private var mInflater: LayoutInflater? = null
    private var mDb: SbDb? = null
    private var mIsDnd = false
    private var mPref: MiscPref? = null

    protected override// 최초 의도는 스마트 브라우저의 홈화면을 웹으로도 구현하여, 자신이 꾸며놓은 것을 친구에게 공유할 수 있도록 하고자 했고,
            // 구글 앱엔진을 이용해 appspot.com으로 공유하도록 개발도 했었으나, 개발 후 너무 오랜 시간이 지나 원복하는데에 상당한 시간이
            // 걸릴 것으로 예상되어 일단 홈화면에서의 공유기능은 spec out 함.
    val shareUrl: String?
        get() = null

    private val deviceId: String
        get() = Secure.getString(contentResolver, Secure.ANDROID_ID)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        mInflater = LayoutInflater.from(this)
        mDb = SbDb.getInstance(applicationContext)
        mPref = MiscPref.getInstance(applicationContext)
        HttpManager.init()

        findViews()

        mListFavorite = mDb!!.favoriteList
        mAdapterHome = HomeAdapter()
        mListViewHome!!.adapter = mAdapterHome
        if (mListFavorite!!.size > 0) {
            mListViewHome!!.visibility = View.VISIBLE
            mIconDefault!!.visibility = View.GONE
        }

        mListSavedCont = mDb!!.savedContList
        mAdapterSaved = SavedAdapter()
        mListViewSaved!!.adapter = mAdapterSaved

        setSettingsPageUrl(getString(R.string.share_home_text))
        setBtnGoText(getString(R.string.google))
    }

    protected fun findViews() {
        mIconDefault = findViewById<View>(R.id.icon_default) as ImageView
        mTitleMain1 = findViewById<View>(R.id.title_main_1) as TextView
        mTitleMain2 = findViewById<View>(R.id.title_main_2) as TextView
        mListViewHome = findViewById<View>(R.id.list_home) as MoveableListView
        mListViewSaved = findViewById<View>(R.id.list_saved) as MoveableListView
        mLogo = findViewById(R.id.logo)

        val remoconBottom = mInflater!!.inflate(R.layout.remocon_bottom_main, null)
        setRemoconBottomView(remoconBottom)

        mBtnSettings = remoconBottom.findViewById<View>(R.id.btn_settings) as RemoconMenuView
        mBtnMainMode = remoconBottom.findViewById<View>(R.id.btn_main_mode) as RemoconMenuView

        mBtnSettings!!.setIconAndTitle(R.drawable.ic_menu_settings, getString(R.string.settings_long))
        mBtnMainMode!!.setIconAndTitle(R.drawable.ic_menu_save, getString(R.string.saved_cont))

        val remoconMenuListener = OnClickListener { v ->
            when (v.id) {
                R.id.btn_settings -> btnSettings()
                R.id.btn_main_mode -> btnMainMode()
                R.id.btn_instructions -> {
                    val i = Intent(applicationContext, InstructionsActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(i)
                }
            }
        }
        mBtnSettings!!.setOnClickListener(remoconMenuListener)
        mBtnMainMode!!.setOnClickListener(remoconMenuListener)
        findViewById<View>(R.id.btn_instructions).setOnClickListener(remoconMenuListener)

        mListViewHome!!.setForcedHeights(getPixelFromDip(43))
        mListViewHome!!.setDragListener(this)
        mListViewHome!!.setDropListener(this)
        mListViewSaved!!.setDragListener(this)
        mListViewSaved!!.setDropListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            // 리모콘 바운더리를 지정한다.
            setBoundary()

            // TODO: 개연성 없지만 일단 급하니까 임시로..
            mListViewHome!!.setIconWidth(mLogo!!.width)
            mListViewSaved!!.setIconWidth(mLogo!!.width)
        }
    }

    private fun refreshList() {
        mListFavorite = mDb!!.favoriteList
        mListSavedCont = mDb!!.savedContList

        if (mListViewSaved!!.visibility == View.VISIBLE) {
            mAdapterSaved!!.notifyDataSetChanged()
        } else {
            mAdapterHome!!.notifyDataSetChanged()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("prev_url")) {
                val prevUrl = extras.getString("prev_url")
                setOnBackClickListener(object: OnClickListener {
                    override fun onClick(p0: View?) {
                        val i = Intent(applicationContext, WebActivity::class.java)
                        i.putExtra("url", prevUrl)
                        startActivity(i)
                    }
                })
            } else {
                setOnBackClickListener(null)
            }

            if (extras.containsKey("next_url")) {
                val nextUrl = extras.getString("next_url")
                setOnFowardClickListener(object: OnClickListener {
                    override fun onClick(p0: View?) {
                        val i = Intent(applicationContext, WebActivity::class.java)
                        i.putExtra("url", nextUrl)
                        startActivity(i)
                    }
                })
            } else {
                setOnFowardClickListener(null)
            }
        } else {
            val lastUrl = mPref!!.lastUrl
            if (lastUrl != null) {
                setOnFowardClickListener(object: OnClickListener {
                    override fun onClick(p0: View?) {
                        val i = Intent(applicationContext, WebActivity::class.java)
                        i.putExtra("url", lastUrl)
                        startActivity(i)
                    }
                })
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (mMainMode == MAIN_MODE_SAVED)
            setMainModeSaved()
        else
            setMainModeHome()

        refreshList()
    }

    private fun btnDeleteFavorite(id: Int) {
        AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.confirm) { dialog, which ->
                    Log.i(TAG, "delete favorite id: " + id)
                    mDb!!.deleteFavorite(id)
                    mListFavorite = mDb!!.favoriteList
                    mAdapterHome!!.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun btnDeleteSaved(id: Int) {
        AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.confirm) { dialog, which ->
                    Log.i(TAG, "delete saved cont id: " + id)
                    mDb!!.deleteSavedCont(id)
                    mListSavedCont = mDb!!.savedContList
                    mAdapterSaved!!.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun btnGo() {
        hideSettingsView()

        val i = Intent(applicationContext, WebActivity::class.java)
        i.putExtra("url", "http://www.google.com")
        startActivity(i)
    }

    override fun btnSettings() {
        super.btnSettings()

        if (isShowSettingsView) {
            mBtnSettings!!.isSelected = true
        } else {
            mBtnSettings!!.isSelected = false
        }
    }

    protected fun btnMainMode() {
        if (mMainMode == MAIN_MODE_HOME) {
            setMainModeSaved()
        } else {
            setMainModeHome()
        }
    }

    private fun setMainModeHome() {
        mTitleMain1!!.setText(R.string.app_name_1)
        mTitleMain2!!.setText(R.string.app_name_2)
        mTitleMain1!!.visibility = View.VISIBLE
        mTitleMain2!!.visibility = View.VISIBLE
        mListViewHome!!.visibility = View.VISIBLE
        mListViewSaved!!.visibility = View.GONE
        if (mListViewHome!!.count > 0)
            mIconDefault!!.visibility = View.GONE
        else
            mIconDefault!!.visibility = View.VISIBLE

        mBtnMainMode!!.setIconAndTitle(R.drawable.ic_menu_save, getString(R.string.saved_cont))
        mMainMode = MAIN_MODE_HOME
    }

    private fun setMainModeSaved() {
        mTitleMain1!!.setText(R.string.saved_cont)
        mTitleMain1!!.visibility = View.VISIBLE
        mTitleMain2!!.visibility = View.GONE
        mListViewHome!!.visibility = View.GONE
        mListViewSaved!!.visibility = View.VISIBLE
        if (mListViewSaved!!.count > 0)
            mIconDefault!!.visibility = View.GONE
        else
            mIconDefault!!.visibility = View.VISIBLE

        mBtnMainMode!!.setIconAndTitle(R.drawable.ic_menu_home, getString(R.string.home))
        mMainMode = MAIN_MODE_SAVED
        refreshList()
    }

    override fun initSettingsView(settingsView: SettingsView?) {
        super.initSettingsView(settingsView)

        showCVMSettings(false)
        settingsView!!.setOnCloseClickListener(object: OnClickListener {
            override fun onClick(p0: View?) {
                mBtnSettings!!.isSelected = false
            }
        })
    }

    private fun getDateFromTime(time: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return cal.get(Calendar.YEAR).toString() + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH)
    }

    override fun drag(from: Int, to: Int) {
        if (!mIsDnd)
            mIsDnd = true
    }

    override fun drop(from: Int, to: Int) {
        if (mIsDnd) {
            if (from == to) {
                if (mListViewHome!!.visibility == View.VISIBLE)
                    mAdapterHome!!.notifyDataSetChanged()
                else if (mListViewSaved!!.visibility == View.VISIBLE)
                    mAdapterSaved!!.notifyDataSetChanged()
                return
            }
            if (mListViewHome!!.visibility == View.VISIBLE) {
                val betweenOrder: Int
                val id = mListFavorite!![from].mId
                if (from < to) {
                    val targetOrder = mListFavorite!![to].mOrderNum
                    if (mListFavorite!!.size > to + 1) {
                        betweenOrder = (targetOrder + mListFavorite!![to + 1].mOrderNum) / 2
                    } else {
                        betweenOrder = targetOrder + 10000
                    }
                } else {
                    val targetOrder = mListFavorite!![to].mOrderNum
                    if (to > 0) {
                        betweenOrder = (targetOrder + mListFavorite!![to - 1].mOrderNum) / 2
                    } else {
                        betweenOrder = targetOrder / 2
                    }
                }
                mDb!!.updateFavoriteOrderNum(id, betweenOrder)
                mListFavorite = mDb!!.favoriteList
                mAdapterHome!!.notifyDataSetChanged()
            } else if (mListViewSaved!!.visibility == View.VISIBLE) {
                val betweenOrder: Int
                val id = mListSavedCont!![from].mId
                if (from < to) {
                    val targetOrder = mListSavedCont!![to].mOrderNum
                    if (mListSavedCont!!.size > to + 1) {
                        betweenOrder = (targetOrder + mListSavedCont!![to + 1].mOrderNum) / 2
                    } else {
                        betweenOrder = targetOrder + 10000
                    }
                } else {
                    val targetOrder = mListSavedCont!![to].mOrderNum
                    if (to > 0) {
                        betweenOrder = (targetOrder + mListSavedCont!![to - 1].mOrderNum) / 2
                    } else {
                        betweenOrder = targetOrder / 2
                    }
                }
                mDb!!.updateSavedContOrderNum(id, betweenOrder)
                mListSavedCont = mDb!!.savedContList
                mAdapterSaved!!.notifyDataSetChanged()
            }

            mIsDnd = false
        }
    }

    private inner class HomeAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return mListFavorite!!.size
        }

        override fun getItem(position: Int): Favorite {
            return mListFavorite!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val favorite = getItem(position)
            if (favorite.mType == SbDb.Favorite.TYPE_URL) {
                val v = mInflater!!.inflate(R.layout.favorite_url_item, null)
                v.findViewById<View>(R.id.btn_article).setOnClickListener { goWeb(favorite.mUrl) }

                val faviconView = v.findViewById<View>(R.id.favicon) as ImageView
                if (favorite.mThumb != null)
                    faviconView.setImageBitmap(BitmapFactory.decodeByteArray(favorite.mThumb, 0, favorite.mThumb!!.size))

                val btnChangeOrder = v.findViewById<View>(R.id.btn_change_order) as ImageView
                btnChangeOrder.setOnTouchListener { v, event ->
                    mListViewHome!!.startDragging()
                    false
                }

                val favoriteTitle = v.findViewById<View>(R.id.favorite_url_title) as TextView
                favoriteTitle.text = favorite.mTitle

                (v.findViewById<View>(R.id.btn_delete_favorite) as Button).setOnClickListener { btnDeleteFavorite(favorite.mId) }
                return v
            } else {
                val v = FavoritePartView(this@MainActivity)

                val faviconView = v.findViewById<View>(R.id.favicon) as ImageView
                if (favorite.mThumb != null)
                    faviconView.setImageBitmap(BitmapFactory.decodeByteArray(favorite.mThumb, 0, favorite.mThumb!!.size))

                val btnChangeOrder = v.findViewById<View>(R.id.btn_change_order) as ImageView
                btnChangeOrder.setOnTouchListener { v, event ->
                    mListViewHome!!.startDragging()
                    false
                }

                v.layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
                v.setTitle(favorite.mTitle!!)
                v.setOnClickListener { goWeb(favorite.mUrl) }
                v.setWebViewLayoutParams(RelativeLayout.LayoutParams(favorite.mPartWidth, favorite.mPartHeight))
                val wv = v.webView
                wv!!.loadUrl(favorite.mUrl!!)
                v.setOnLoadRunnable(object: Runnable {
                    override fun run() {
                        wv.injectRedo(favorite.mIndexSet!!)
                    }
                })
                val btnDelete = v.findViewById<View>(R.id.btn_delete_favorite) as Button
                btnDelete.setOnClickListener { btnDeleteFavorite(favorite.mId) }
                return v
            }

        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()

            if (count > 0)
                mIconDefault!!.visibility = View.GONE
            else
                mIconDefault!!.visibility = View.VISIBLE
        }

        private fun goWeb(url: String?) {
            val i = Intent(applicationContext, WebActivity::class.java)
            i.putExtra("url", url)
            startActivity(i)
        }

    }

    private inner class SavedAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return mListSavedCont!!.size
        }

        override fun getItem(position: Int): SavedCont {
            return mListSavedCont!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val saved = getItem(position)

            val v = mInflater!!.inflate(R.layout.saved_cont_item, null)
            v.findViewById<View>(R.id.btn_article).setOnClickListener {
                val i = Intent(applicationContext, WebActivity::class.java)
                i.putExtra("saved_cont_id", saved.mId)
                startActivity(i)
            }

            val faviconView = v.findViewById<View>(R.id.favicon) as ImageView
            if (saved.mThumb != null)
                faviconView.setImageBitmap(BitmapFactory.decodeByteArray(saved.mThumb, 0, saved.mThumb!!.size))

            val btnChangeOrder = v.findViewById<View>(R.id.btn_change_order) as ImageView
            btnChangeOrder.setOnTouchListener { v, event ->
                mListViewSaved!!.startDragging()
                false
            }

            val savedContTitle = v.findViewById<View>(R.id.saved_cont_title) as TextView
            val dateStr = getDateFromTime(java.lang.Long.valueOf(saved.mCrtDt)!!)
            savedContTitle.text = saved.mTitle + " (" + dateStr + ")"

            (v.findViewById<View>(R.id.btn_delete_saved) as Button).setOnClickListener { btnDeleteSaved(saved.mId) }
            return v
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()

            if (count > 0)
                mIconDefault!!.visibility = View.GONE
            else
                mIconDefault!!.visibility = View.VISIBLE
        }

    }

    private fun callApi(url: String, params: String): String? {
        Log.e(TAG, "callApi : $url $params")
        try {
            val post = HttpPost(url)
            post.setHeader("Content-type", "application/x-www-form-urlencoded")
            post.entity = StringEntity(params, "UTF-8")
            val result = HttpManager.execute(post, BasicResponseHandler()) as String
            Log.e(TAG, "result : " + result)
            return result
        } catch (e: Exception) {
            Log.e(TAG, url, e)
        }

        return null
    }

    companion object {

        private val TAG = "MainActivity"

        private val MAIN_MODE_HOME = 0
        private val MAIN_MODE_SAVED = 1
    }
}