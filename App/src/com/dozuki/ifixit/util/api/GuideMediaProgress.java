package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.util.ImageSizes;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores progress information about syncing guide media.
 */
public class GuideMediaProgress {
   public ApiEvent.ViewGuide mGuideEvent;
   public Guide mGuide;
   public Set<String> mMissingMedia;
   public int mTotalMedia;
   public int mMediaProgress;
   private boolean mAddAllMedia;

   public GuideMediaProgress(ApiEvent.ViewGuide guideEvent) {
      this(guideEvent.getResult(), false);

      mGuideEvent = guideEvent;
   }

   public GuideMediaProgress(Guide guide, boolean addAllMedia) {
      mGuide = guide;
      mMissingMedia = new HashSet<String>();
      mTotalMedia = 0;
      mAddAllMedia = addAllMedia;

      Image introImage = mGuide.getIntroImage();
      if (introImage.isValid()) {
         addMediaIfMissing(introImage.getPath(ImageSizes.guideList));
      }

      for (GuideStep step : mGuide.getSteps()) {
         for (Image image : step.getImages()) {
            addMediaIfMissing(image.getPath(ImageSizes.stepThumb));
            addMediaIfMissing(image.getPath(ImageSizes.stepMain));
            addMediaIfMissing(image.getPath(ImageSizes.stepFull));
         }

         if (step.hasVideo()) {
            Video video = step.getVideo();
            addMediaIfMissing(video.getThumbnail().getPath(ImageSizes.stepMain));
            // TODO: I don't think that the order of the encodings is reliable so
            // we should pick one that we like and use that.
            addMediaIfMissing(video.getEncodings().get(0).getURL());
         }
      }

      mMediaProgress = mTotalMedia - mMissingMedia.size();
   }

   public GuideMediaProgress(Guide guide, int totalMedia, int mediaProgress) {
      mGuide = guide;
      mTotalMedia = totalMedia;
      mMediaProgress = mediaProgress;
   }

   private void addMediaIfMissing(String imageUrl) {
      if (mMissingMedia.contains(imageUrl)) {
         // Don't acknowledge duplicates in the total.
         return;
      }

      mTotalMedia++;

      if (mAddAllMedia ||
       !new File(ApiSyncAdapter.getOfflineMediaPath(imageUrl)).exists()) {
         mMissingMedia.add(imageUrl);
      }
   }

   public boolean isComplete() {
      return mTotalMedia == mMediaProgress;
   }
}
