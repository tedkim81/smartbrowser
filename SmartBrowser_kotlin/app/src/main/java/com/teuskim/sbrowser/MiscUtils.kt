package com.teuskim.sbrowser

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern

import android.content.Context
import android.os.Environment

object MiscUtils {

    fun makeDirs(dir: File): Boolean {

        return if (dir.exists() == false) {
            dir.mkdirs()
        } else true

    }

    fun makeDirs(path: String): Boolean {

        val f = File(path)
        return if (f.exists() == false) {
            f.mkdirs()
        } else true

    }


    fun getRawResourceText(context: Context, rid: Int): String? {

        val resource = context.resources
        val reader = BufferedReader(InputStreamReader(resource.openRawResource(rid)))
        val builder = StringBuilder()
        var line: String
        try {
            line = reader.readLine()
            while (line != null) {
                builder.append(line)
                line = reader.readLine()
            }
            return builder.toString()

        } catch (e: IOException) {

        }

        return null
    }

    /**
     * cache dir
     * @param context
     * @return
     */
    fun getImageCacheDirectory(context: Context): File {

        val cacheDir: File
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheDir = File(Environment.getExternalStorageDirectory(), "/sbrowser/cache")
        } else {
            cacheDir = File(context.cacheDir, "/sbrowser/cache")
        }
        return cacheDir
    }

    /**
     * 지정된 디렉토리 안에 내용 전부 지우기.
     * @param f
     */
    fun deleteFile(f: File) {

        if (f.exists() == false)
            return

        val list = f.listFiles()
        for (i in list.indices) {

            if (list[i].isDirectory) {
                deleteFile(list[i])
            }

            list[i].delete()
        }
    }


    /**
     * email 형식 체크
     * @param email
     * @return
     */
    fun isValidEmail(email: String): Boolean {
        val regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$"
        val p = Pattern.compile(regex)
        val m = p.matcher(email)
        return if (!m.matches()) {
            false
        } else true
    }

}
