package com.example.jgreenb2.myselfie;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;


/**
 * Created by jgreenb2 on 5/16/15.
 */
public class ContextualActionBar  {

    static final int FLIP_DURATION = 400;

    private ListView mListView;
    private SelfieListAdapter mSelfieAdapter;
    private Context mContext;

    public ContextualActionBar(final Activity activity, ListView listView) {
        mContext = activity.getApplicationContext();
        mListView = listView;
        mSelfieAdapter = (SelfieListAdapter) mListView.getAdapter();



        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            BroadcastReceiver receiveDeleteEvents;

            @Override
            public void onItemCheckedStateChanged(final ActionMode mode, final int position, long id, final boolean checked) {
                final View itemView = getViewFromPosition(mListView, position);
                final View rootView = itemView.findViewById(R.id.thumbNailRoot);
                ImageView thumbView = (ImageView) rootView.findViewById(R.id.thumbNail);
                ImageView checkView = (ImageView) rootView.findViewById(R.id.checkMark);

                // create an animation
                FlipAnimation flipAnimation = new FlipAnimation(thumbView, checkView);
                flipAnimation.setDuration(FLIP_DURATION);
                flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        rootView.setHasTransientState(true);
                        mListView.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rootView.setHasTransientState(false);
                        if (checked) {
                            mSelfieAdapter.addItemToSelectionSet(position);
                        } else {
                            mSelfieAdapter.removeItemFromSelectionSet(position);
                        }
                        mode.setTitle(Integer.toString(mSelfieAdapter.getNumberOfCheckedPositions()));
                        mListView.setEnabled(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                if (!checked) flipAnimation.reverse();
                rootView.startAnimation(flipAnimation);
            }

            @Override
            public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.cab_menu, menu);

                receiveDeleteEvents = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mSelfieAdapter.removeSelectedSelfies();
                        mode.finish();
                    }
                };

                LocalBroadcastManager.getInstance(mContext).registerReceiver(this.receiveDeleteEvents,
                        new IntentFilter("delete-selected-selfies-event"));
                return true;
            }

            @Override
            public boolean onPrepareActionMode(final ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                ConfirmDeleteDialog mDialog = new ConfirmDeleteDialog();
                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        Bundle dialogParam = new Bundle();
                        dialogParam.putInt("nSelected", mSelfieAdapter.getNumberOfCheckedPositions());
                        mDialog.setArguments(dialogParam);
                        mDialog.show(activity.getFragmentManager(), "confirmDeleteDialog");
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // uncheck all
                mSelfieAdapter.removeAllFromSelectionSet();
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiveDeleteEvents);
            }
        });
    }


    View getViewFromPosition(ListView listView,int position) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int viewPosition = position - firstVisiblePosition;
        return listView.getChildAt(viewPosition);

    }
}
