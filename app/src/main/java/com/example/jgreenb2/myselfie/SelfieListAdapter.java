package com.example.jgreenb2.myselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private Bitmap mCheckMark;

    public SelfieListAdapter(Context context) {
        mContext = context;
        mCheckMark = BitmapFactory.decodeResource(context.getResources(), R.drawable.checkmark);
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

            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        holder.dateView.setText(selfieItem.getLabel());
        if (isPositionChecked(position)) {
            holder.imageView.setImageBitmap(mCheckMark);
        } else {
            holder.imageView.setImageBitmap(selfieItem.getThumb());
        }
        holder.imageView.invalidate();
        return row;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public TextView dateView;
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
