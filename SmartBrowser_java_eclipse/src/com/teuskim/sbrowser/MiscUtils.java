package com.teuskim.sbrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

public class MiscUtils {

	public static boolean makeDirs(File dir){
		
		if( dir.exists() == false){
			return dir.mkdirs();
		}
		
		return true;
		
	}
	
	public static boolean makeDirs(String path){
		
		File f = new File(path);
		if( f.exists() == false){
			return f.mkdirs();
		}
		
		return true;
		
	}

	
	public static String getRawResourceText(Context context , int rid){
	
		Resources resource = context.getResources();
		BufferedReader reader = new BufferedReader( new InputStreamReader( resource.openRawResource(rid)));
		StringBuilder builder =  new StringBuilder();
		String line;
		try {
			
			while( ( line = reader.readLine()) != null ){
				builder.append(line);
			}
			return builder.toString();
			
		} catch (IOException e) {

		} 
		return null;
	}
	
	/**
	 * cache dir
	 * @param context
	 * @return
	 */
	public static File getImageCacheDirectory(Context context){
		
		File cacheDir;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			cacheDir = new File( Environment.getExternalStorageDirectory(),  "/sbrowser/cache" );
		}else{
			cacheDir = new File(context.getCacheDir(), "/sbrowser/cache");
		}
		return cacheDir;
	}
	
	/**
	 * 지정된 디렉토리 안에 내용 전부 지우기.
	 * @param f
	 */
	public static void deleteFile(File f){
		
		if(f.exists() == false)
			return;
		
		File[] list = f.listFiles();
		for( int i = 0 ; i < list.length; i++){

			if(list[i].isDirectory()){
				deleteFile(list[i]);
			}

			list[i].delete();
		}
	}
	
	
	/**
	 * email 형식 체크
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email){
		String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(email);
		if( !m.matches() ) {
			return false; 
		}
		return true;
	}
	
}
