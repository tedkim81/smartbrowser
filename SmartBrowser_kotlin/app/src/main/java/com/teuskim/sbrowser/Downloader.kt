package com.teuskim.sbrowser

import android.content.Context
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * HTTP로 파일을 다운로드 한다.
 */
class Downloader(context: Context) {

    private var mListener: DownloadListener? = null

    private var mIsStop = false
    private var mTTL = (86400000 * 7).toLong() // 기본 7일 , 0이면 무제한

    init {
        HttpManager.init()
    }

    fun setDownloadListener(listener: DownloadListener) {
        mListener = listener
    }

    /**
     * Cache 유효시간을 설정한다.
     * @param msec
     */
    fun setTTL(msec: Long) {
        mTTL = msec
    }

    /**
     * Cache 된 파일이 있다면 Cache사용 없다면 HTTP통신을 통해서 받아온다.
     */
    operator fun get(strUrl: String, file: File): Boolean {
        return download(strUrl, file, true)
    }

    operator fun get(request: HttpUriRequest, file: File): Boolean {
        return download(request, file, true)
    }


    /**
     * Cache 된 파일을 사용하지 않고 무조건 HTTP통신으로 가져오기
     */
    fun getForce(strUrl: String, file: File): Boolean {
        return download(strUrl, file, false)
    }

    fun getForce(request: HttpUriRequest, file: File): Boolean {
        return download(request, file, false)
    }

    fun stop() {
        mIsStop = true
    }


    private fun download(strUrl: String?, downFile: File, isUseCache: Boolean): Boolean {

        if (strUrl == null)
            return false

        val get = HttpGet(strUrl)
        return download(get, downFile, isUseCache)
    }


    private fun download(request: HttpUriRequest?, downFile: File, isUseCache: Boolean): Boolean {

        mIsStop = false

        if (request == null)
            return false

        val dir = downFile.parentFile
        if (dir.exists() == false) {
            MiscUtils.makeDirs(dir)
        }

        /**
         * Cache 데이터를 사용하고 이미 다운받은 파일이 있다면 그냥 리턴한다.
         */
        val cacheExists = downFile.exists()

        if (isUseCache == true && cacheExists && (mTTL <= 0 || Date().time - downFile.lastModified() < mTTL)) {
            Log.i(TAG, "캐싱된 파일을 재사용 - " + downFile.absolutePath)
            return true
        }

        var entity: HttpEntity? = null
        var result = false

        try {

            val response = HttpManager.execute(request)

            if (response.statusLine.statusCode == HttpStatus.SC_OK) {

                entity = response.entity

                var `in`: InputStream? = null
                var out: FileOutputStream? = null

                var contentLength: Long = 0
                var current: Long = 0
                var read = 0
                val b = ByteArray(BUFFER_SIZE)

                try {

                    `in` = entity!!.content
                    contentLength = entity.contentLength

                    //받다가 중간에 끊어지면 이미 받아놓은 cache파일이 못쓰게 되므로 tmp파일에 작성하고 바꿔치기 한다.
                    val tmpFile = File(downFile.absolutePath + ".tmp")
                    out = FileOutputStream(tmpFile)

                    read = `in`.read(b)
                    while (read != -1) {

                        if (mIsStop) { //받고 있는 중에 중단 한다.

                            `in`.close()
                            out.close()
                            tmpFile.delete()

                            return false
                        }

                        out.write(b, 0, read)

                        current += read.toLong()
                        if (mListener != null) {
                            mListener!!.onProgress(current, contentLength)
                        }

                        read = `in`.read(b)
                    }

                    out.flush()

                    if (downFile.exists())
                        downFile.delete()

                    tmpFile.renameTo(downFile)
                    result = true

                } catch (e: IOException) {

                    Log.e(TAG, "Could not save file from " + request.uri.toString(), e)

                } finally {
                    IOUtils.closeStream(`in`)
                    IOUtils.closeStream(out)
                }

            }

        } catch (e: IOException) {

            Log.e(TAG, "Could not load file from " + request.uri.toString(), e)

        } finally {

            if (entity != null) {
                try {
                    entity.consumeContent()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not save file from " + request.uri.toString(), e)
                }

            }

        }

        // cache 사용이라면 다운로드에 실패했지만 이전파일이 있다면 이전파일을 사용.
        return if (result == false && isUseCache == true && cacheExists == true) {
            true
        } else result

    }


    /**
     * 다운로드 진행상태가 필요할때 사용할 Interface
     * @author jonathan
     */
    interface DownloadListener {
        fun onProgress(current: Long, total: Long)
    }

    companion object {

        private val TAG = "Downloader"

        private val BUFFER_SIZE = 8 * 1024

        fun getDefaultFile(dir: File, strUrl: String): File {
            return File(dir, "file_" + MD5.encode(strUrl))
        }
    }
}
