package com.teuskim.sbrowser;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class RemoteImageTask extends AsyncTask<String, Integer, Bitmap> {

	private Downloader mDownloader;
	private File mSaveDir;
	private int mOutSize = 500;
	private IResultListener onPostExecuteListener;
	
	public RemoteImageTask(Context context) {
		mSaveDir = MiscUtils.getImageCacheDirectory(context);
		mDownloader = new Downloader(context);
	}
	
	public void setOnPostExecuteListener(IResultListener onPostExecuteListener){
		this.onPostExecuteListener = onPostExecuteListener;
	}

	@Override
	protected void onPostExecute(Bitmap bm) {
		if(onPostExecuteListener != null) onPostExecuteListener.onResult(bm);
	}

	@Override
	protected Bitmap doInBackground(String... params) {

		String strUrl = params[0];
		if(strUrl == null)
			return null;
		
		File saveImage = Downloader.getDefaultFile(mSaveDir, strUrl);
		if( mDownloader.get(strUrl, saveImage) ){
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = getSampleSize(saveImage.getAbsolutePath());
			
			Bitmap bm = BitmapFactory.decodeFile(saveImage.getAbsolutePath(), options);
			
			if(bm == null){ //받은 파일이 잘못되어 있다면 파일을 지워버린다.
				saveImage.delete();
			}
			else return bm;
		}
		
		return null;
	}
	
	public void setOutSize(int size){
		mOutSize = size;
	}
	
	private int[] getSize(String path){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		return new int[]{options.outWidth, options.outHeight};
		
	}
	
	public int getSampleSize(String path){
		
		int[] sizeArray = getSize(path);
		
		int w = sizeArray[0];
		int h = sizeArray[1];
		
		double l = ( w > h ) ? w : h;
		
		
		if(l <= mOutSize ){
			return 1;
		}
		
		int sampleSize  = (int) Math.ceil( l / (double)mOutSize) ;
		return sampleSize;
	}

}
