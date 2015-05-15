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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

    public void removeItem(int pos) {
        mItems.remove(pos);
        notifyDataSetChanged();
    }

    // It's not safe to call removeItem(int) from a loop -- this will generate an
    // exception because the list.remove() isn't iterator-safe. To handle multiple
    // deletions we pass in an ArrayList of positions to be deleted and iterate
    // over mItems.
    //
    // However, this is trickier than it should be because of the way java deals with
    // lists and iterators.  We need to scan backward through the list so that
    // the index positions are stable.
    //
    public void removeItem(ArrayList<Integer> list) {
        Collections.sort(list,Collections.reverseOrder());
        ListIterator<SelfieItem> j = mItems.listIterator(mItems.size());
        for (int i : list) {
            while (j.previousIndex()+1 > i)j.previous();
            j.remove();
        }
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

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.photo_entry, null);

            holder = new ViewHolder();
            holder.dateView = (TextView) row.findViewById(R.id.photoDate);
            holder.imageView = (ImageView) row.findViewById(R.id.thumbNail);
            holder.checkView = (ImageView) row.findViewById(R.id.checkMark);


            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        holder.dateView.setText(selfieItem.getLabel());
        holder.imageView.setImageBitmap(selfieItem.getThumb());

        if (isPositionChecked(position)) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
        }

        return row;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public ImageView checkView;
        public TextView dateView;

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

    public ArrayList<Integer> getSelectionSet() {
       ArrayList<Integer> checkedPositions =  new ArrayList<>();
        for (int i=0;i<getCount();i++) {
            SelfieItem selfieItem = (SelfieItem) getItem(i);
            if (selfieItem.isChecked()) checkedPositions.add(i);
        }
        return checkedPositions;
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

    public void deleteSelfie(int position) {
        SelfieItem selfieItem = (SelfieItem) getItem(position);
        String photoPath = selfieItem.getPhotoPath();
        File photoFile = new File(photoPath);
        photoFile.delete();
        int lastSlash = photoPath.lastIndexOf('/');
        String thumbName = photoPath.substring(lastSlash+1);
        String photoDir = photoPath.substring(0,lastSlash-1);
        String thumbPath = photoDir+"/../"+SelfieItem.THUMB_DIR+"/"+thumbName;
        File thumbFile = new File(thumbPath);
        thumbFile.delete();
    }
}
