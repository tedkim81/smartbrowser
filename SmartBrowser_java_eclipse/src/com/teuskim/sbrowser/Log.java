package com.teuskim.sbrowser;

public class Log {
	
	private static final boolean IS_LOGGING = false;

	public static void d(String tag, String msg){
		if(IS_LOGGING){
			android.util.Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg, Throwable tr){
		if(IS_LOGGING){
			android.util.Log.d(tag, msg, tr);
		}
	}

	public static void e(String tag, String msg){
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr){
			android.util.Log.e(tag, msg, tr);
	}

	public static void i(String tag, String msg){
		if(IS_LOGGING){
			android.util.Log.i(tag, msg);
		}
	}

	public static void i(String tag, String msg, Throwable tr){
		if(IS_LOGGING){
			android.util.Log.i(tag, msg, tr);
		}
	}

	public static void v(String tag, String msg){
		if(IS_LOGGING){
			android.util.Log.v(tag, msg);
		}
	}

	public static void v(String tag, String msg, Throwable tr){
		if(IS_LOGGING){
			android.util.Log.v(tag, msg, tr);
		}
	}

	public static void w(String tag, String msg){
		if(IS_LOGGING){
			android.util.Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable tr){
		if(IS_LOGGING){
			android.util.Log.w(tag, msg, tr);
		}
	}
	
}
