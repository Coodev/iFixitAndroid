package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.StepEmbedFragment;
import com.dozuki.ifixit.ui.guide.StepVideoFragment;

public class GuideStepViewFragment extends BaseFragment {

   private static final String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   private static final int MEDIA_CONTAINER = R.id.guide_step_media;
   private static final String STEP_IMAGE_FRAGMENT_TAG = "STEP_IMAGE_FRAGMENT_TAG";
   private static final String STEP_VIDEO_FRAGMENT_TAG = "STEP_VIDEO_FRAGMENT_TAG";
   private static final String STEP_EMBED_FRAGMENT_TAG = "STEP_EMBED_FRAGMENT_TAG";

   private static final String VIDEO_TYPE = "video";
   private static final String IMAGE_TYPE = "image";
   private static final String EMBED_TYPE = "embed";

   private GuideStep mStep;

   private StepLinesFragment mLinesFrag;
   private StepVideoFragment mVideoFrag;
   private StepEmbedFragment mEmbedFrag;
   private StepImageFragment mImageFrag;

   public GuideStepViewFragment() { }

   public GuideStepViewFragment(GuideStep step) {
      mStep = step;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_step, container, false);

      if (savedInstanceState != null) {
         mStep = (GuideStep) savedInstanceState.getSerializable(GUIDE_STEP_KEY);
         String stepType = mStep.type();

         if (stepType.equals(VIDEO_TYPE)) {
            mVideoFrag = (StepVideoFragment) getChildFragmentManager().findFragmentByTag(STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            mEmbedFrag = (StepEmbedFragment) getChildFragmentManager().findFragmentByTag(STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            mImageFrag = (StepImageFragment) getChildFragmentManager().findFragmentByTag(STEP_IMAGE_FRAGMENT_TAG);
         }

         mLinesFrag = (StepLinesFragment) getChildFragmentManager().findFragmentById(R.id.guide_step_content);

      } else {
         String stepType = mStep.type();
         mLinesFrag = new StepLinesFragment();
         mLinesFrag.setRetainInstance(true);
         Bundle linesArgs = new Bundle();

         linesArgs.putSerializable(StepLinesFragment.GUIDE_STEP, mStep);

         mLinesFrag.setArguments(linesArgs);

         FragmentTransaction ft = getChildFragmentManager()
          .beginTransaction()
          .add(R.id.guide_step_content, mLinesFrag);

         if (stepType.equals(VIDEO_TYPE)) {
            Bundle videoArgs = new Bundle();

            videoArgs.putSerializable(StepVideoFragment.GUIDE_VIDEO_KEY, mStep.getVideo());
            mVideoFrag = new StepVideoFragment();
            mVideoFrag.setArguments(videoArgs);

            ft.add(MEDIA_CONTAINER, mVideoFrag, STEP_VIDEO_FRAGMENT_TAG);
         } else if (stepType.equals(EMBED_TYPE)) {
            mEmbedFrag = StepEmbedFragment.newInstance(mStep.getEmbed());
            ft.add(MEDIA_CONTAINER, mEmbedFrag, STEP_EMBED_FRAGMENT_TAG);
         } else if (stepType.equals(IMAGE_TYPE)) {
            mImageFrag = new StepImageFragment(mStep.getImages());
            ft.add(MEDIA_CONTAINER, mImageFrag, STEP_IMAGE_FRAGMENT_TAG);
         }

         ft.commit();
      }

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(GUIDE_STEP_KEY, mStep);
   }
}
