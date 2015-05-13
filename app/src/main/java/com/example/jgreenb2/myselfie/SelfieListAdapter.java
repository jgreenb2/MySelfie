package com.example.jgreenb2.myselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by jgreenb2 on 4/23/15.
 */
public class SelfieListAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<SelfieItem> mItems = new ArrayList<>();

    public SelfieListAdapter(Context context) {
        mContext = context;
    }

    // Clears the list adapter of all items.

    public void clear() {

        mItems.clear();
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {

        return mItems.size();

    }

    @Override
    public Object getItem(int pos) {

        return mItems.get(pos);

    }

    // Get the ID for the SelfieItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {

        return pos;

    }

    // Add a SelfieItem to the adapter
    // Notify observers that the data set has changed

    public void add(SelfieItem item) {

        mItems.add(item);
        notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View row;
        ViewHolder holder;

        final SelfieItem selfieItem = (SelfieItem) getItem(position);
        if (selfieItem.getAnimationTransitionState()== SelfieItem.transitionState.INPROGRESS) {
            Log.i(MainActivity.TAG, "getView while animation INPROGRESS! pos=" + position);
        }

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.photo_entry, null);

            holder = new ViewHolder();
            holder.dateView = (TextView) row.findViewById(R.id.photoDate);
            holder.imageView = (ImageView) row.findViewById(R.id.thumbNail);
<<<<<<< HEAD
            holder.checkMarkView = (ImageView) row.findViewById(R.id.checkMark);
            holder.thumbRoot = (View) row.findViewById(R.id.thumbNailRoot);
=======
            holder.checkView = (ImageView) row.findViewById(R.id.checkMark);
>>>>>>> a19077e... animation now works (whew!)

            row.setTag(holder);
            Log.i(MainActivity.TAG,"new view, pos="+position+" checked="+selfieItem.isChecked()
                                  +" label="+selfieItem.getLabel());
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
            Log.i(MainActivity.TAG, "reusing view, pos="+position+" checked="+selfieItem.isChecked()
                                   +" label="+selfieItem.getLabel());
        }

        holder.dateView.setText(selfieItem.getLabel());
        holder.imageView.setImageBitmap(selfieItem.getThumb());
<<<<<<< HEAD


        if (selfieItem.getAnimationTransitionState()== SelfieItem.transitionState.SCHEDULED) {
            selfieItem.setAnimation(new FlipAnimation(holder.imageView,holder.checkMarkView));
            selfieItem.getAnimation().setTag(selfieItem);
            selfieItem.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
//                    FlipAnimation flipAnimation = (FlipAnimation) animation;
//                    SelfieItem curSelfieItem = (SelfieItem) flipAnimation.getTag();
//                    curSelfieItem.setAnimationTransitionState(SelfieItem.transitionState.INPROGRESS);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    FlipAnimation flipAnimation = (FlipAnimation) animation;
                    SelfieItem curSelfieItem = (SelfieItem) flipAnimation.getTag();
                    curSelfieItem.setAnimationTransitionState(SelfieItem.transitionState.UNSCHEDULED);
                    curSelfieItem.setChecked(!curSelfieItem.isChecked());

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            selfieItem.setAnimationTransitionState(SelfieItem.transitionState.INPROGRESS);
            if (selfieItem.isChecked()) {
                selfieItem.getAnimation().reverse();
                holder.thumbRoot.startAnimation(selfieItem.getAnimation());
            } else {
                holder.thumbRoot.startAnimation(selfieItem.getAnimation());
            }
        } else if (selfieItem.getAnimationTransitionState()== SelfieItem.transitionState.UNSCHEDULED) {
            if (selfieItem.isChecked()) {
                holder.imageView.setVisibility(View.GONE);
                holder.checkMarkView.setVisibility(View.VISIBLE);
            } else {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.checkMarkView.setVisibility(View.GONE);
            }
        }

=======
        if (isPositionChecked(position)) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
        }
>>>>>>> a19077e... animation now works (whew!)
        return row;
    }

    private static class ViewHolder {
        public ImageView imageView;
<<<<<<< HEAD
        public ImageView checkMarkView;
=======
        public ImageView checkView;
>>>>>>> a19077e... animation now works (whew!)
        public TextView dateView;
        public View thumbRoot;
        public FlipAnimation flipAnimation;
    }

    public void addItemToSelectionSet(int position) {
        ((SelfieItem) getItem(position)).setChecked(true);
        notifyDataSetChanged();
    }

    public int getNumberOfCheckedPositions() {
        int nCheck=0;
        for (int i=0;i<getCount();i++) {
            if (((SelfieItem) getItem(i)).isChecked()) nCheck++;
        }
        return nCheck;
    }

    public boolean isPositionChecked(int position) {
        Boolean result = ((SelfieItem) getItem(position)).isChecked();
        return result;
    }

    public void removeItemFromSelectionSet(int position) {
        ((SelfieItem) getItem(position)).setChecked(false);
        notifyDataSetChanged();
    }

    public void removeAllFromSelectionSet() {
        for (int i=0;i<getCount();i++) {
            ((SelfieItem) getItem(i)).setChecked(false);
        }
        notifyDataSetChanged();
    }

    public void requestCheckmarkThumbTransition(int position) {
        SelfieItem selfieItem = (SelfieItem) getItem(position);
        selfieItem.setAnimationTransitionState(SelfieItem.transitionState.SCHEDULED);
        notifyDataSetChanged();
    }

    public void logSelfies() {
        SelfieItem selfieItem;
        for (int i=0;i<getCount();i++) {
            selfieItem = (SelfieItem) getItem(i);
            Log.i(MainActivity.TAG, "reusing view, index="+i+" checked="+selfieItem.isChecked()
                    +" label="+selfieItem.getLabel());
        }
    }
}
