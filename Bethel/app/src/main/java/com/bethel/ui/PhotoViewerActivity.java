package com.bethel.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import com.bethel.R;
import com.bethel.utils.ProgressBarHandler;
import com.bethel.utils.TouchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


/**
 * Shows the photos in gallery mode.
 *
// */
public class PhotoViewerActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageview);

		final TouchImageView touchImageView=(TouchImageView)findViewById(R.id.imageview);
		if(getIntent().getExtras()!=null){

			 showProgress();
			Glide.with(PhotoViewerActivity.this)
					.load(getIntent().getExtras().getString("isImage"))
					.asBitmap()
					.into(new SimpleTarget<Bitmap>() {
						@Override
						public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
							touchImageView.setImageBitmap(resource);
							hideProgress();
						}
					});

		}else{
			touchImageView.setImageBitmap(AddReceiptActivity.mergedBitmap);
		}
	/*	byte[] byteArray = getIntent().getByteArrayExtra("image");
		Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	*/


	}
	private ProgressBarHandler progress;
	public void showProgress() {
		if(progress == null){
			progress = new ProgressBarHandler(this);
		}
		progress.show();
	}

	public void hideProgress() {
		progress.hide();
	}
}
