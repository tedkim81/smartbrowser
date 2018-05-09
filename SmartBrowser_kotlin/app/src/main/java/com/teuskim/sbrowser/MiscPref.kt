package com.teuskim.sbrowser

import twitter4j.auth.AccessToken
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

class MiscPref private constructor(context: Context) {

    private val mPref: SharedPreferences
    private val mEditor: SharedPreferences.Editor

    var mypage: String?
        get() = mPref.getString(KEY_MYPAGE, null)
        set(mypage) {
            mEditor.putString(KEY_MYPAGE, mypage)
            mEditor.commit()
        }

    var remoconLeft: Int
        get() = mPref.getInt(KEY_REMOCON_LEFT, Integer.MIN_VALUE)
        set(left) {
            mEditor.putInt(KEY_REMOCON_LEFT, left)
            mEditor.commit()
        }

    var remoconBottom: Int
        get() = mPref.getInt(KEY_REMOCON_BOTTOM, Integer.MIN_VALUE)
        set(bottom) {
            mEditor.putInt(KEY_REMOCON_BOTTOM, bottom)
            mEditor.commit()
        }

    var remoconAlpha: Int
        get() = mPref.getInt(KEY_REMOCON_ALPHA, INIT_REMOCON_ALPHA)
        set(alpha) {
            mEditor.putInt(KEY_REMOCON_ALPHA, alpha)
            mEditor.commit()
        }

    var contBgColor: Int
        get() = mPref.getInt(KEY_CONT_BG_COLOR, INIT_CONT_BG_COLOR)
        set(color) {
            mEditor.putInt(KEY_CONT_BG_COLOR, color)
            mEditor.commit()
        }

    var contFontColor: Int
        get() = mPref.getInt(KEY_CONT_FONT_COLOR, INIT_CONT_FONT_COLOR)
        set(color) {
            mEditor.putInt(KEY_CONT_FONT_COLOR, color)
            mEditor.commit()
        }

    var contFontSize: Int
        get() = mPref.getInt(KEY_CONT_FONT_SIZE, INIT_CONT_FONT_SIZE)
        set(size) {
            mEditor.putInt(KEY_CONT_FONT_SIZE, size)
            mEditor.commit()
        }

    var contLineSpace: Int
        get() = mPref.getInt(KEY_CONT_LINE_SPACE, INIT_CONT_LINE_SPACE)
        set(space) {
            mEditor.putInt(KEY_CONT_LINE_SPACE, space)
            mEditor.commit()
        }

    var contPadding: Int
        get() = mPref.getInt(KEY_CONT_PADDING, INIT_CONT_PADDING)
        set(padding) {
            mEditor.putInt(KEY_CONT_PADDING, padding)
            mEditor.commit()
        }

    var lastUrl: String?
        get() = mPref.getString(KEY_LAST_URL, null)
        set(url) {
            mEditor.putString(KEY_LAST_URL, url)
            mEditor.commit()
        }

    val facebookId: String?
        get() = mPref.getString("facebook_usrid", null)

    val fbToken: String?
        get() = mPref.getString("fb_token", null)

    val twitterId: String?
        get() = mPref.getString("twitter_usrid", null)

    val twitterToken: String?
        get() = mPref.getString("twitter_token", null)

    val twitterTokenSecret: String?
        get() = mPref.getString("twitter_token_secret", null)

    init {
        mPref = context.getSharedPreferences("miscpref", 0)
        mEditor = mPref.edit()
    }

    /*
	 * SNS 관련 부분은 일단 코딩후 다시 다듬어야 한다. 시간이 부족해서..
	 */

    fun setFacebookId(facebookId: String): Boolean {
        mEditor.putString("facebook_usrid", facebookId)
        return mEditor.commit()
    }

    private fun checkNull(str: String?): String? {
        return if (str != null && str == "null" == false)
            str
        else
            null
    }

    fun setFbToken(patamFbToken: String): Boolean {
        mEditor.putString("fb_token", checkNull(patamFbToken))

        return mEditor.commit()
    }

    fun saveTwitter(usrid: String, passwd: String, token: AccessToken) {

        if (TextUtils.isEmpty(usrid) || TextUtils.isEmpty(passwd)) {
            mEditor.remove("twitter_usrid")
            mEditor.remove("twitter_passwd")
            mEditor.remove("twitter_token")
            mEditor.remove("twitter_token_secret")
        } else {
            mEditor.putString("twitter_usrid", usrid)
            mEditor.putString("twitter_passwd", passwd)
            mEditor.putString("twitter_token", token.token)
            mEditor.putString("twitter_token_secret", token.tokenSecret)
        }

        mEditor.commit()
    }

    companion object {

        private var sInstance: MiscPref? = null

        private val KEY_MYPAGE = "mypage"
        private val KEY_REMOCON_LEFT = "remoconleft"
        private val KEY_REMOCON_BOTTOM = "remoconbottom"
        private val KEY_REMOCON_ALPHA = "remoconalpha"
        private val KEY_CONT_BG_COLOR = "contbgcolor"
        private val KEY_CONT_FONT_COLOR = "contfontcolor1"
        private val KEY_CONT_FONT_SIZE = "contfontsize"
        private val KEY_CONT_LINE_SPACE = "contlinespace"
        private val KEY_CONT_PADDING = "contpadding"
        private val KEY_LAST_URL = "lasturl"

        val INIT_REMOCON_ALPHA = 130
        val INIT_CONT_BG_COLOR = SettingsView.PATTERN_0
        val INIT_CONT_FONT_COLOR = -0x1000000
        val INIT_CONT_FONT_SIZE = 20
        val INIT_CONT_LINE_SPACE = 5
        val INIT_CONT_PADDING = 20

        @Synchronized
        fun getInstance(context: Context): MiscPref {
            if (sInstance == null) {
                sInstance = MiscPref(context)
            }
            return sInstance as MiscPref
        }
    }
}
