package com.example.jgreenb2.myselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private SparseBooleanArray mSelection = new SparseBooleanArray();

    public SelfieListAdapter(Context context) {
        mContext = context;
    }

    // Clears the list adapter of all items.

    public void clear() {

        mItems.clear();
        notifyDataSetChanged();

    }

    // Returns the number of ToDoItems

    @Override
    public int getCount() {

        return mItems.size();

    }

    // Retrieve the number of ToDoItems

    @Override
    public Object getItem(int pos) {

        return mItems.get(pos);

    }

    // Get the ID for the ToDoItem
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

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.photo_entry, null);

            holder = new ViewHolder();
            holder.dateView = (TextView) row.findViewById(R.id.photoDate);
            holder.imageView = (ImageView) row.findViewById(R.id.thumbNail);
            holder.checkMarkView = (ImageView) row.findViewById(R.id.checkMark);
            holder.thumbRoot = (View) row.findViewById(R.id.thumbNailRoot);
            //holder.flipAnimation = new FlipAnimation(holder.imageView, holder.checkMarkView);

            row.setTag(holder);
            Log.i(MainActivity.TAG,"new view, pos="+position);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
            Log.i(MainActivity.TAG, "reusing view, pos="+position);
        }

        holder.dateView.setText(selfieItem.getLabel());
        holder.imageView.setImageBitmap(selfieItem.getThumb());

        FlipAnimation flipAnimation = new FlipAnimation(holder.imageView, holder.checkMarkView);
        if (isPositionChecked(position) && holder.imageView.getVisibility()==View.VISIBLE) {
            holder.thumbRoot.startAnimation(flipAnimation);
        } else if (!isPositionChecked(position) && holder.imageView.getVisibility()==View.GONE) {
            flipAnimation.reverse();
            holder.thumbRoot.startAnimation(flipAnimation);
        }

        return row;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public ImageView checkMarkView;
        public TextView dateView;
        public View thumbRoot;
        public FlipAnimation flipAnimation;
    }

    public void addItemToSelectionSet(int position) {
        mSelection.put(position, true);
        notifyDataSetChanged();
    }

    public int getNumberOfCheckedPositions() {
        int nCheck=0;
        for (int i=0;i<mSelection.size();i++) {
            if (mSelection.valueAt(i)) nCheck++;
        }
        return nCheck;
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result;
    }

    public void removeItemFromSelectionSet(int position) {
        mSelection.delete(position);
        notifyDataSetChanged();
    }

    public void removeAllFromSelectionSet() {
        mSelection.clear();
        notifyDataSetChanged();
    }
}
