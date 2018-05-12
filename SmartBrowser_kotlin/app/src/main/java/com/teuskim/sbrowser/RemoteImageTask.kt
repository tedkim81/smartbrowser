package com.teuskim.sbrowser

import java.io.File

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask

class RemoteImageTask(context: Context) : AsyncTask<String, Int, Bitmap>() {

    private val mDownloader: Downloader
    private val mSaveDir: File
    private var mOutSize = 500
    private var onPostExecuteListener: IResultListener? = null

    init {
        mSaveDir = MiscUtils.getImageCacheDirectory(context)
        mDownloader = Downloader(context)
    }

    fun setOnPostExecuteListener(onPostExecuteListener: IResultListener) {
        this.onPostExecuteListener = onPostExecuteListener
    }

    override fun onPostExecute(bm: Bitmap) {
        if (onPostExecuteListener != null) onPostExecuteListener!!.onResult(bm)
    }

    override fun doInBackground(vararg params: String): Bitmap? {

        val strUrl = params[0] ?: return null

        val saveImage = Downloader.getDefaultFile(mSaveDir, strUrl)
        if (mDownloader.get(strUrl, saveImage)) {

            val options = BitmapFactory.Options()
            options.inSampleSize = getSampleSize(saveImage.absolutePath)

            val bm = BitmapFactory.decodeFile(saveImage.absolutePath, options)

            if (bm == null) { //받은 파일이 잘못되어 있다면 파일을 지워버린다.
                saveImage.delete()
            } else
                return bm
        }

        return null
    }

    fun setOutSize(size: Int) {
        mOutSize = size
    }

    private fun getSize(path: String): IntArray {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        return intArrayOf(options.outWidth, options.outHeight)

    }

    fun getSampleSize(path: String): Int {

        val sizeArray = getSize(path)

        val w = sizeArray[0]
        val h = sizeArray[1]

        val l = (if (w > h) w else h).toDouble()


        return if (l <= mOutSize) {
            1
        } else Math.ceil(l / mOutSize.toDouble()).toInt()

    }

}
