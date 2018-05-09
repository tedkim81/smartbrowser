package com.teuskim.sbrowser;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class RemoteImageView extends ImageView {
	
	Context mContext;
	Handler mHandler;
	
	final static ImageListener defaultListener = new ImageListener() {
		public void onImageLoaded(RemoteImageView im, Bitmap bm) {
			im.setImageBitmap(bm);
		}
	};

	ImageListener listener = null;
		
	public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_init(context);
	}

	public RemoteImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_init(context);
	}

	public RemoteImageView(Context context) {
		super(context);
		_init(context);
	}
	
	public void setImageListener(ImageListener listener) {
		this.listener = listener;
	}

	public void setImageFromURL(final String imageUrl) {
		
		new Thread(){
			
			public void run(){
				
				Downloader downloader = new Downloader(mContext);
				File saveImage = Downloader.getDefaultFile(MiscUtils.getImageCacheDirectory(mContext), imageUrl);
						
				if( downloader.get(imageUrl, saveImage) == true){
					
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inDensity = DisplayMetrics.DENSITY_HIGH;
					final Bitmap bm = BitmapFactory.decodeFile(saveImage.getAbsolutePath(), opts);
					if( bm == null){
						saveImage.delete();
					}
					if (RemoteImageView.this.getHandler() != null) {
						RemoteImageView.this.post(new ImageRunnable(bm));
					}else{
						mHandler.post(new ImageRunnable(bm));
					}
				}
			}
			
		}.start();
		
	}

	private void _init(Context context) {
		mContext = context;
		mHandler = new Handler();
	}

	// ---------- Image Load Listener ---------- //
	public static interface ImageListener {
		public void onImageLoaded(RemoteImageView im, Bitmap bm);
	}
	
	public class ImageRunnable implements Runnable{
		
		Bitmap bm;
		
		public ImageRunnable(Bitmap bm){
			this.bm = bm;
		}

		@Override
		public void run() {
			
			// call our listener
			if (RemoteImageView.this.listener != null) {
				RemoteImageView.this.listener.onImageLoaded(RemoteImageView.this, bm);
			}else {
				RemoteImageView.defaultListener.onImageLoaded(RemoteImageView.this, bm);
			}
			
		}
		
	}

}
