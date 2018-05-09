package com.teuskim.sbrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;

/**
 * 
 * HTTP로 파일을 다운로드 한다.
 * 
 * @author jonathan
 *
 */
public class Downloader {

	private static final String TAG = "Downloader";
	
	private DownloadListener mListener;
	
	private boolean mIsStop = false;
	private long mTTL = 86400000 * 7; // 기본 7일 , 0이면 무제한

	private static final int BUFFER_SIZE = 8 * 1024;
	
	public Downloader(Context context){
		HttpManager.init();
	}
	
	public void setDownloadListener(DownloadListener listener){
		mListener = listener;
	}
	
	/**
	 * Cache 유효시간을 설정한다.
	 * @param msec
	 */
	public void setTTL(long msec){
		mTTL = msec;
	}
	
	/**
	 * Cache 된 파일이 있다면 Cache사용 없다면 HTTP통신을 통해서 받아온다.
	 */
	public boolean get(String strUrl, File file){
		return download(strUrl, file, true);
	}
	
	public boolean get(HttpUriRequest request, File file){
		return download(request, file, true);
	}
	
	
	/**
	 * Cache 된 파일을 사용하지 않고 무조건 HTTP통신으로 가져오기
	 */
	public boolean getForce(String strUrl, File file){
		return download(strUrl, file, false);
	}
	
	public boolean getForce(HttpUriRequest request, File file){
		return download(request, file, false);
	}
	
	public static File getDefaultFile(File dir, String strUrl){
		return new File(dir,  URLEncoder.encode(strUrl).replace(".", "_"));
	}
	
	public void stop(){
		mIsStop = true;
	}
	
	
	private boolean download(String strUrl, File downFile, boolean isUseCache){
		
		if(strUrl == null)
			return false;
		
		final HttpGet get = new HttpGet(strUrl);
		return download(get, downFile, isUseCache);
	}
	
	
	private boolean download(HttpUriRequest request, File downFile, boolean isUseCache){
		
		mIsStop = false;
		
		if(request == null)
			return false;
		
		File dir = downFile.getParentFile();
		if( dir.exists() == false ){
			MiscUtils.makeDirs(dir);
		}
		
		/**
		 * Cache 데이터를 사용하고 이미 다운받은 파일이 있다면 그냥 리턴한다.
		 */
		boolean cacheExists = downFile.exists();

		if(isUseCache == true && cacheExists && ( mTTL <= 0 || ( ( new Date().getTime() -  downFile.lastModified()) < mTTL ) )){
			Log.i(TAG, "캐싱된 파일을 재사용 - " + downFile.getAbsolutePath());
			return true;
		}
		
		HttpEntity entity = null;
		boolean result = false;
		
		try {
			
			final HttpResponse response = HttpManager.execute(request);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			
				entity = response.getEntity();

				InputStream in = null;
				FileOutputStream  out = null;

				long contentLength = 0;
				long current = 0;
				int read = 0;
				byte[] b = new byte[BUFFER_SIZE];
				
				try {

					in = entity.getContent();
					contentLength = entity.getContentLength();
					
					//받다가 중간에 끊어지면 이미 받아놓은 cache파일이 못쓰게 되므로 tmp파일에 작성하고 바꿔치기 한다.
					File tmpFile = new File(downFile.getAbsolutePath() + ".tmp");
					out = new FileOutputStream(tmpFile);
					
			        while ((read = in.read(b)) != -1) {
			        	
			        	if(mIsStop){ //받고 있는 중에 중단 한다.
			        	
			        		in.close();
			        		out.close();
			        		tmpFile.delete();
			        		
			        		return false;
			        	}
			        	
			        	out.write(b, 0, read);

			        	current += read; 
			        	if(mListener != null){
			        		mListener.onProgress(current, contentLength);
			            }
			        }
			        
					out.flush();
					
					if(downFile.exists())
						downFile.delete();
					
					tmpFile.renameTo(downFile);
					result = true;
					
				} catch (IOException e) {
					
					Log.e(TAG, "Could not save file from " + request.getURI().toString() , e );
					
				} finally {
					IOUtils.closeStream(in);
					IOUtils.closeStream(out);
				}
				
			}
			
		} catch (IOException e) {
			
			Log.e(TAG, "Could not load file from " + request.getURI().toString() , e);
			
		} finally {
			
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
					Log.e(TAG, "Could not save file from " + request.getURI().toString(), e );
				}
			}
			
		}
		
		// cache 사용이라면 다운로드에 실패했지만 이전파일이 있다면 이전파일을 사용.
		if( result == false && isUseCache == true && cacheExists == true){
			return true;
		}
		
		return result;
	}
	

	
	
	/**
	 * 다운로드 진행상태가 필요할때 사용할 Interface
	 * @author jonathan
	 *
	 */
	public static interface DownloadListener{
		public void onProgress(long current, long total);
	}
}
