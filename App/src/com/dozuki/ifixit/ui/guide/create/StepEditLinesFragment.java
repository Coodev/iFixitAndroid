package com.dozuki.ifixit.ui.guide.create;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.ui.guide.create.BulletReorderFragment.BulletRearrangeListener;
import com.dozuki.ifixit.ui.guide.create.ChooseBulletDialog.BulletDialogListener;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.*;

import java.util.ArrayList;

public class StepEditLinesFragment extends Fragment implements BulletDialogListener, BulletRearrangeListener {
   private static final int BULLET_LIMIT = 8;
   private static final int BULLET_INDENT = 25;
   private static final String STEP_LIST_KEY = "STEP_LIST_KEY";
   private static final String SHOWING_BULLET_FRAG = "SHOWING_BULLET_FRAG";
   private static final String BULLET_FRAG_ID = "BULLET_FRAG_ID";
   private static final String SHOWING_REORDER_FRAG = "SHOWING_REORDER_FRAG";
   private static final int NONE = -1;
   private static final int INDENT_LIMIT = 2;
   private static final String REORDER_FRAG_ID = "REORDER_FRAG_ID";
   private LinearLayout mBulletContainer;
   private Button mNewBulletButton;
   private ArrayList<StepLine> mLines = new ArrayList<StepLine>();
   private ChooseBulletDialog mChooseBulletDialog;
   private boolean mShowingChooseBulletDialog;
   private boolean mReorderModeActive;
   private BulletReorderFragment mReorderFragment;
   private boolean mConfirmDelete;

   private static String NO_TITLE = "";
   private static final String TITLE_KEY = "TITLE_KEY";
   private static final String STEP_NUM_KEY = null;

   // title
   private String mTitle = NO_TITLE;
   private int mStepNum = 0;
   private EditText mStepTitle;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   /**
    * unchecked: we know what we are getting from the from the STEP_LIST_KEY *
    */
   @SuppressWarnings("unchecked")
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.guide_create_step_edit_lines, container, false);

      mStepTitle = (EditText) v.findViewById(R.id.step_edit_title_text);
      mNewBulletButton = (Button) v.findViewById(R.id.add_new_bullet_button);
      mBulletContainer = (LinearLayout) v.findViewById(R.id.edit_step_bullet_container);

      mStepTitle.addTextChangedListener(new TextWatcher() {
         @Override
         public void afterTextChanged(Editable s) {
            if (mTitle.equals(s.toString())) {
               return;
            }
            mTitle = s.toString();
            setGuideDirty();
         }

         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) { }

      });

      if (mTitle.length() > 0) {
         mStepTitle.setText(mTitle);
      }

      if (savedInstanceState != null) {
         mTitle = savedInstanceState.getString(TITLE_KEY);
         mStepNum = savedInstanceState.getInt(STEP_NUM_KEY);

         mLines = (ArrayList<StepLine>) savedInstanceState.getSerializable(STEP_LIST_KEY);
         mChooseBulletDialog =
          (ChooseBulletDialog) getSupportFragmentManager().getFragment(savedInstanceState, BULLET_FRAG_ID);
         mReorderFragment =
          (BulletReorderFragment) getSupportFragmentManager().getFragment(savedInstanceState,
           REORDER_FRAG_ID);

         mShowingChooseBulletDialog = savedInstanceState.getBoolean(SHOWING_BULLET_FRAG, false);
         if (mChooseBulletDialog != null && mShowingChooseBulletDialog) {
            mChooseBulletDialog.setTargetFragment(this, 0);
         }
         mReorderModeActive = savedInstanceState.getBoolean(SHOWING_REORDER_FRAG, false);
         if (mReorderFragment != null && mReorderModeActive) {
            mReorderFragment.setBulletRearrangeListener(this);
         }
      }

      mNewBulletButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mLines.add(new StepLine());
            View view = getView(mLines.get(mLines.size() - 1), mLines.size() - 1);
            mBulletContainer.addView(view, mLines.size() - 1);

            mNewBulletButton.setVisibility(View.GONE);

            view.findViewById(mLines.size() - 1).requestFocus();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(
             view.findViewById(mLines.size() - 1), InputMethodManager.SHOW_FORCED);
         }
      });
      initilizeBulletContainer();

      return v;
   }

   public void setStepTitle(String title) {
      mTitle = title;
      if (mStepTitle != null && title.length() > 0) {
         mStepTitle.setText(mTitle);
      }
   }

   public void setStepNumber(int num) {
      mStepNum = num;
   }

   public String getTitle() {
      return mTitle;
   }

   @Override
   public void onResume() {
      super.onResume();
   }

   public void setSteps(ArrayList<StepLine> lines) {
      mLines.addAll(lines);
   }

   private void initilizeBulletContainer() {

      if (mLines.size() == 0 || (mLines.get(mLines.size() - 1).getTextRaw().length() != 0 && mLines.size() !=
       BULLET_LIMIT)) {
         mNewBulletButton.setVisibility(View.VISIBLE);
      } else {
         mNewBulletButton.setVisibility(View.GONE);
      }

      for (int i = 0; i < mLines.size(); i++) {
         mBulletContainer.addView(getView(mLines.get(i), i), i);
      }
   }

   public View getView(final StepLine line, int index) {
      LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View v = vi.inflate(R.layout.guide_create_step_edit_list_item, null);
      FrameLayout iconFrame = (FrameLayout) v.findViewById(R.id.guide_step_item_frame);

      iconFrame.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            mChooseBulletDialog = new ChooseBulletDialog();
            mChooseBulletDialog.setTargetFragment(StepEditLinesFragment.this, 0);
            mChooseBulletDialog.setStepIndex(mLines.indexOf(line));
            mChooseBulletDialog.show(fm, "fragment_choose_bullet");
            restrictDialogOptions(mChooseBulletDialog, line);
            mShowingChooseBulletDialog = true;
         }
      });

      LayoutParams params = (LayoutParams) iconFrame.getLayoutParams();

      params.setMargins(BULLET_INDENT * line.getLevel(), 0, 0, 0);
      iconFrame.setLayoutParams(params);
      final EditText text = (EditText) v.findViewById(R.id.step_title_textview);
      text.setText(line.getTextRaw());
      text.setId(index);
      text.addTextChangedListener(new TextWatcher() {
         @Override
         public void afterTextChanged(Editable s) {
            if (text.getText().toString().equals(line.getTextRaw())) {
               return;
            }
            mLines.get(mLines.indexOf(line)).setTextRaw(text.getText().toString());

            if (text.getText().length() == 0 && mLines.indexOf(line) == mLines.size() - 1) {
               mNewBulletButton.setVisibility(View.GONE);
            } else if (text.getText().length() != 0 && mLines.size() != BULLET_LIMIT
             && mLines.indexOf(line) == mLines.size() - 1) {
               mNewBulletButton.setVisibility(View.VISIBLE);
            }

            setGuideDirty();
         }

         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {}

      });

      // Override ENTER key on hardware keyboards to prevent newlines in steps
      text.setOnKeyListener(new View.OnKeyListener() {
         @Override
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) return true;
            return false;
         }
      });

      ImageView icon = (ImageView) v.findViewById(R.id.guide_step_item_thumbnail);
      icon.setImageResource(getBulletResource(line.getColor()));

      return v;
   }

   protected void restrictDialogOptions(ChooseBulletDialog dialog, StepLine line) {
      if (!canIndent(line)) {
         dialog.disableIndent();
      }

      if (!canUnIndent(line)) {
         dialog.disableUnIndent();
      }

      if (!canDelete(line)) {
         dialog.disableDelete();
      }

      if (!canRearrange(line)) {
         dialog.disableRearrange();
      }
   }

   private boolean canRearrange(StepLine line) {
      return (mLines.size() > 1);
   }

   private boolean canDelete(StepLine line) {
      return (mLines.size() > 1);
   }

   private boolean canUnIndent(StepLine line) {
      return line.getLevel() != 0;
   }

   private boolean canIndent(StepLine line) {
      if (!(line.getLevel() < 2)) {
         return false;
      }

      if (mLines.indexOf(line) == 0) {
         return false;
      }

      if (line.getLevel() == 0) {
         return true;
      }

      if (line.getLevel() == 1) {
         if (mLines.get(mLines.indexOf(line) - 1).getLevel() >= 1) {
            return true;
         }
      }

      return false;
   }

   public int getBulletResource(String color) {
      int iconRes;

      if (color.equals("black")) {
         iconRes = R.drawable.ic_dialog_bullet_black;
      } else if (color.equals("orange")) {
         iconRes = R.drawable.ic_dialog_bullet_orange;
      } else if (color.equals("blue")) {
         iconRes = R.drawable.ic_dialog_bullet_blue;
      } else if (color.equals("violet")) {
         iconRes = R.drawable.ic_dialog_bullet_pink;
      } else if (color.equals("red")) {
         iconRes = R.drawable.ic_dialog_bullet_red;
      } else if (color.equals("green")) {
         iconRes = R.drawable.ic_dialog_bullet_green;
      } else if (color.equals("white")) {
         iconRes = R.drawable.bullet_white;
      } else if (color.equals("yellow")) {
         iconRes = R.drawable.ic_dialog_bullet_yellow;
      } else if (color.equals("icon_reminder")) {
         iconRes = R.drawable.ic_dialog_bullet_reminder_dark;
      } else if (color.equals("icon_caution")) {
         iconRes = R.drawable.ic_dialog_bullet_caution;
      } else if (color.equals("icon_note")) {
         iconRes = R.drawable.ic_dialog_bullet_note_dark;
      } else {
         iconRes = R.drawable.ic_dialog_bullet_black;
      }
      return iconRes;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(STEP_LIST_KEY, mLines);

      savedInstanceState.putString(TITLE_KEY, mTitle);
      savedInstanceState.putInt(STEP_NUM_KEY, mStepNum);

      if (mChooseBulletDialog != null && mShowingChooseBulletDialog) {
         getSupportFragmentManager().putFragment(savedInstanceState, BULLET_FRAG_ID, mChooseBulletDialog);
         savedInstanceState.putBoolean(SHOWING_BULLET_FRAG, mShowingChooseBulletDialog);
      }
      savedInstanceState.putBoolean(SHOWING_REORDER_FRAG, mReorderModeActive);

      if (mReorderFragment != null && mReorderModeActive) {
         getSupportFragmentManager().putFragment(savedInstanceState, REORDER_FRAG_ID, mReorderFragment);
         savedInstanceState.putBoolean(SHOWING_REORDER_FRAG, mReorderModeActive);
      }

   }

   @Override
   public void onFinishBulletDialog(int index, String color) {
      mShowingChooseBulletDialog = false;
      StepLine curStep = mLines.get(index);

      if (color.equals("action_indent")) {
         if (curStep.getLevel() == INDENT_LIMIT) {
            Toast.makeText((getActivity()), R.string.indent_limit_above, Toast.LENGTH_SHORT).show();
            return;
         }
         indentBullet(index);
         setGuideDirty();
      } else if (color.equals("action_unindent")) {
         if (curStep.getLevel() == 0) {
            Toast.makeText((getActivity()), R.string.indent_limit_below, Toast.LENGTH_SHORT).show();
            return;
         }
         unIndentBullet(index);
         setGuideDirty();
      } else if (color.equals("action_reorder")) {
         launchBulletReorder();
      } else if (color.equals("action_delete")) {
         createDeleteDialog(getActivity(), index).show();
      } else if (color.equals("action_cancel")) {
         return;
      } else {
         curStep.setColor(color);
         mBulletContainer.removeViewAt(index);
         mBulletContainer.addView(getView(mLines.get(index), index), index);
         setGuideDirty();
      }
   }

   private void unIndentBullet(int index) {
      StepLine curStep = mLines.get(index);
      if (curStep.getLevel() == 0) {
         return;
      }
      for (int i = index + 1; i < mLines.size(); i++) {
         if (mLines.get(i).getLevel() == (curStep.getLevel() + 1)) {
            unIndentBullet(i);
         } else if (mLines.get(i).getLevel() <= (curStep.getLevel())) {
            break;
         }
      }
      curStep.setLevel(curStep.getLevel() - 1);
      mBulletContainer.removeViewAt(index);
      mBulletContainer.addView(getView(mLines.get(index), index), index);
   }

   private void indentBullet(int index) {
      StepLine curStep = mLines.get(index);
      if (curStep.getLevel() == INDENT_LIMIT) {
         return;
      }
      for (int i = index + 1; i < mLines.size(); i++) {
         if (mLines.get(i).getLevel() == (curStep.getLevel() + 1)) {
            indentBullet(i);
         } else if (mLines.get(i).getLevel() <= (curStep.getLevel())) {
            break;
         }
      }
      curStep.setLevel(curStep.getLevel() + 1);
      mBulletContainer.removeViewAt(index);
      mBulletContainer.addView(getView(mLines.get(index), index), index);
   }

   private boolean isIndentionStateValid() {
      if (mLines.get(0).getLevel() != 0) {
         return false;
      }
      for (int i = 1; i < mLines.size(); i++) {
         if (mLines.get(i).getLevel() > mLines.get(i - 1).getLevel() + 1) {
            return false;
         }
      }
      return true;
   }

   private void fixIndentionState() {
      mLines.get(0).setLevel(0);

      for (int i = 1; i < mLines.size(); i++) {
         if (mLines.get(i).getLevel() > mLines.get(i - 1).getLevel() + 1) {
            mLines.get(i).setLevel(mLines.get(i - 1).getLevel() + 1);
         }
      }
      removeBullets();
      initilizeBulletContainer();
   }

   public ArrayList<StepLine> getLines() {
      return mLines;
   }

   private void launchBulletReorder() {
      FragmentManager fm = getActivity().getSupportFragmentManager();
      mReorderFragment = new BulletReorderFragment();
      mReorderFragment.setLines(mLines);
      mReorderFragment.setBulletRearrangeListener(this);
      mReorderFragment.show(fm, "fragment_reorder_bullet");
      mReorderModeActive = true;
   }

   @Override
   public void onReorderComplete(boolean canceled, ArrayList<StepLine> list) {
      mReorderModeActive = false;
      if (!canceled) {
         mLines.clear();
         mLines.addAll(list);
         removeBullets();
         initilizeBulletContainer();
         if (!isIndentionStateValid()) {
            fixIndentionState();
         }
         // ((GuideStepChangedListener) getActivity()).enableSave();
         setGuideDirty();
      }
   }

   public void setGuideDirty() {
      ((StepEditFragment.GuideStepChangedListener) getActivity()).onGuideStepChanged();
   }

   public boolean isReorderModeActive() {
      return mReorderModeActive;
   }

   public void removeBullets() {
      mBulletContainer.removeViewsInLayout(0, mLines.size());
   }

   public AlertDialog createDeleteDialog(final Context context, final int index) {
      mConfirmDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(context.getString(R.string.step_delete_dialog_title))
       .setMessage(context.getString(R.string.step_delete_dialog_body))
       .setPositiveButton(context.getString(R.string.logout_confirm), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
             mConfirmDelete = false;
             removeBullet(index);
             if (!isIndentionStateValid()) {
                fixIndentionState();
             }
             setGuideDirty();
             dialog.dismiss();
          }
       }).setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            mConfirmDelete = false;
            dialog.dismiss();
         }
      });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mConfirmDelete = false;
         }
      });

      return dialog;
   }

   private void removeBullet(int index) {
      mLines.remove(index);
      mBulletContainer.removeViewAt(index);
      mNewBulletButton.setVisibility(View.VISIBLE);
   }
}
