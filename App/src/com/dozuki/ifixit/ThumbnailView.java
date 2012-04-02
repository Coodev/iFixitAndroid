package com.dozuki.ifixit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ifixit.android.imagemanager.ImageManager;

public class ThumbnailView extends LinearLayout {

   private ArrayList<ImageView> mThumbs;
   private ImageView mMainImage;
   private ImageManager mImageManager;
   private Context mContext;
   private String mCurrentURL;
   private ImageSizes mImageSizes;

   public ThumbnailView(Context context) {
      super(context);
      init(context);
   }

   public ThumbnailView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }

   private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.thumbnail_list, this, true);

      mThumbs = new ArrayList<ImageView>();

      mThumbs.add((ImageView)findViewById(R.id.thumbnail_1));
      mThumbs.add((ImageView)findViewById(R.id.thumbnail_2));
      mThumbs.add((ImageView)findViewById(R.id.thumbnail_3));
   }

   public void setImageSizes(ImageSizes imageSizes) {
      mImageSizes = imageSizes;
   }

   public void setThumbs(ArrayList<StepImage> images,
    ImageManager imageManager, Context context) {
      if (images.size() <= 1) {
         setVisibility(INVISIBLE);
      }

      mImageManager = imageManager;
      mContext = context;

      if (!images.isEmpty()) {
         for (int thumbId = 0; thumbId < images.size(); thumbId++) {
            ImageView thumb = mThumbs.get(thumbId);
            thumb.setVisibility(VISIBLE);
            thumb.setTag(images.get(thumbId).mText);

            thumb.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  setCurrentThumb((String)v.getTag());
               }
            });

            mImageManager.displayImage(images.get(thumbId).mText +
             mImageSizes.getThumb(), (Activity)mContext, thumb);
         }
      }
   }

   public void setCurrentThumb(String url) {
      mCurrentURL = url;
      mImageManager.displayImage(mCurrentURL + mImageSizes.getMain(),
       (Activity)mContext, mMainImage);
      mMainImage.setTag(url);
   }

   public void setMainImage(ImageView mainImg) {
      mMainImage = mainImg;
   }

   public void setContext(Context context) {
      mContext = context;
   }

   public void setImageManager(ImageManager imageManager) {
      mImageManager = imageManager;
   }

   public ArrayList<ImageView> getThumbViews() {
      return mThumbs;
   }

   public String getCurrentURL() {
      return mCurrentURL;
   }
}
