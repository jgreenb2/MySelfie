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
import android.widget.Toast;

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

    /* selection state methods

        the selection state is set to true when a list item is 'checked'
        during a contextual action bar multiple selection mode.

        The user can select an arbitrary number of non-contiguous
        positions for future deletion. Therefore we have to have methods
        to track, set and clear the selection set.

    */

    // set the selection state for the item at position
    public void addItemToSelectionSet(int position) {
        ((SelfieItem) getItem(position)).setChecked(true);
        notifyDataSetChanged();
    }

    // return the number of selected positions
    public int getNumberOfCheckedPositions() {
        int nCheck=0;
        for (int i=0;i<getCount();i++) {
            if (((SelfieItem) getItem(i)).isChecked()) nCheck++;
        }
        return nCheck;
    }

    // return true when position is in the selected state
    public boolean isPositionChecked(int position) {
        Boolean result = ((SelfieItem) getItem(position)).isChecked();
        return result;
    }

    // return the selection set as an ArrayList
    public ArrayList<Integer> getSelectionSet() {
       ArrayList<Integer> checkedPositions =  new ArrayList<>();
        for (int i=0;i<getCount();i++) {
            SelfieItem selfieItem = (SelfieItem) getItem(i);
            if (selfieItem.isChecked()) checkedPositions.add(i);
        }
        return checkedPositions;
    }

    // unset the selection state of the item at position
    public void removeItemFromSelectionSet(int position) {
        ((SelfieItem) getItem(position)).setChecked(false);
        notifyDataSetChanged();
    }

    // unset the selection state for all items
    public void removeAllFromSelectionSet() {
        for (int i=0;i<getCount();i++) {
            ((SelfieItem) getItem(i)).setChecked(false);
        }
        notifyDataSetChanged();
    }

    public void addAllToSelectionSet() {
        for (int i=0;i<getCount();i++) {
            SelfieItem selfieItem = (SelfieItem) getItem(i);
            if (!selfieItem.isChecked()) selfieItem.setChecked(true);
        }
    }

    /* deletion methods

        Items can be deleted one at a time, all at once or in selected
        groups. These methods deal with deleting the actual files as well
        as the ListAdapter entries

     */
    // delete the photo and thumbNail at position
    public void deleteSelfie(int position) {
        SelfieItem selfieItem = (SelfieItem) getItem(position);
        String photoPath = selfieItem.getPhotoPath();
        File photoFile = new File(photoPath);
        if (!photoFile.delete()) {
            Log.i(MainActivity.TAG,"error deleting photo |"+photoPath+"|");
        }
        String thumbPath = selfieItem.getThumbPath();
        File thumbFile = new File(thumbPath);
        if (!thumbFile.delete()) {
            Log.i(MainActivity.TAG,"error deleting thumbNail |"+thumbPath+"|");
        }
    }

    // This method deletes all the selfie photos, thumbNails and
    // adapter list entries associated with the currently selected positions
    public void removeSelectedSelfies() {
        ArrayList<Integer> selectedPositions = getSelectionSet();
        for (int i = 0; i < selectedPositions.size(); i++) {
            int position = selectedPositions.get(i);
            deleteSelfie(position); // remove the photos and thumbs
        }
        removeItem(selectedPositions);  // remove the adapter entries
        String notifyText = String.format("%d items deleted", selectedPositions.size());
        Toast.makeText(mContext, notifyText, Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    // delete all selfies, thumbNails and adapter list entries
    public void removeAllSelfies() {
        addAllToSelectionSet();
        removeSelectedSelfies();
    }
}
