package com.teuskim.sbrowser

import java.io.Closeable
import java.io.FileOutputStream
import java.io.IOException

object IOUtils {

    private val TAG = "IOUtils"

    val IO_BUFFER_SIZE = 4 * 1024

    fun closeStream(stream: Closeable?) {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close stream", e)
            }

        }
    }

    /**
     *
     * @param stream
     * @return
     */
    fun sync(stream: FileOutputStream?): Boolean {
        try {
            stream?.fd?.sync()
            return true
        } catch (e: IOException) {
        }

        return false
    }
}
