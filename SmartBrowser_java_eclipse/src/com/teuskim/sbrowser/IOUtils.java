package com.teuskim.sbrowser;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

public final class IOUtils {

	private static final String TAG = "IOUtils";
    
	public static final int IO_BUFFER_SIZE = 4 * 1024;

    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close stream", e);
            }
        }
    }
    
    /**
     * 
     * @param stream
     * @return
     */
    public static boolean sync(FileOutputStream stream) {
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }
}
