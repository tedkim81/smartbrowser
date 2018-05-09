package com.teuskim.sbrowser

object Log {

    private val IS_LOGGING = true

    fun d(tag: String, msg: String) {
        if (IS_LOGGING) {
            android.util.Log.d(tag, msg)
        }
    }

    fun d(tag: String, msg: String, tr: Throwable) {
        if (IS_LOGGING) {
            android.util.Log.d(tag, msg, tr)
        }
    }

    fun e(tag: String, msg: String) {
        android.util.Log.e(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        android.util.Log.e(tag, msg, tr)
    }

    fun i(tag: String, msg: String) {
        if (IS_LOGGING) {
            android.util.Log.i(tag, msg)
        }
    }

    fun i(tag: String, msg: String, tr: Throwable) {
        if (IS_LOGGING) {
            android.util.Log.i(tag, msg, tr)
        }
    }

    fun v(tag: String, msg: String) {
        if (IS_LOGGING) {
            android.util.Log.v(tag, msg)
        }
    }

    fun v(tag: String, msg: String, tr: Throwable) {
        if (IS_LOGGING) {
            android.util.Log.v(tag, msg, tr)
        }
    }

    fun w(tag: String, msg: String) {
        if (IS_LOGGING) {
            android.util.Log.w(tag, msg)
        }
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        if (IS_LOGGING) {
            android.util.Log.w(tag, msg, tr)
        }
    }

}
