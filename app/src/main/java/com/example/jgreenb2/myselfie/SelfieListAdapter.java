package com.example.jgreenb2.myselfie;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by jgreenb2 on 4/23/15.
 */
public class SelfieListAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<SelfieItem> mItems = new ArrayList<>();
    private int mContextPos;
    private ListView mListView;
    private BroadcastReceiver mReceiveRenameEvents;
    private SharedPreferences mSharedPreferences;

    public SelfieListAdapter(Context context, ListView listView) {
        mContext = context;
        mListView=listView;
        mSharedPreferences = ((Activity) mContext).getPreferences(Context.MODE_PRIVATE);
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
            holder.labelView = (TextView) row.findViewById(R.id.photoLabel);
            holder.editLabelView = (EditText) row.findViewById(R.id.editPhotoLabel);
            holder.imageView = (ImageView) row.findViewById(R.id.thumbNail);
            holder.checkView = (ImageView) row.findViewById(R.id.checkMark);
            holder.contextView = (ImageView) row.findViewById(R.id.contextMenu);

            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        holder.labelView.setText(selfieItem.getLabel());
        holder.imageView.setImageBitmap(selfieItem.getThumb());

        if (isPositionChecked(position)) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
        }



        holder.contextView.setTag(position);
        holder.contextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextPos = (int) v.getTag();
                Activity activity = (Activity) mContext;
                activity.registerForContextMenu(v);
                activity.openContextMenu(v);
                activity.unregisterForContextMenu(v);
            }
        });
        return row;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public ImageView checkView;
        public TextView labelView;
        public EditText editLabelView;
        public ImageView contextView;


    }

    public int getContextPos() {
        return mContextPos;
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
        // now remove any stored labels
        String fileName = selfieItem.getFileName();
        if (mSharedPreferences.getString(fileName,"") != "") {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(fileName);
            editor.commit();
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
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    static View getViewFromPosition(ListView listView,int position) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int viewPosition = position - firstVisiblePosition;
        return listView.getChildAt(viewPosition);

    }

    public void switchToEditView(final int pos) {
        View rootView = getViewFromPosition(mListView, pos);
        final TextView  labelView = (TextView) rootView.findViewById(R.id.photoLabel);
        final EditText editView = (EditText) rootView.findViewById(R.id.editPhotoLabel);

        labelView.setVisibility(View.GONE);

        editView.setText(labelView.getText());
        editView.setVisibility(View.VISIBLE);

        editView.selectAll();
        if (editView.requestFocus()) {
            final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, 0);

            editView.setOnEditorActionListener(
                    new EditText.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == R.id.my_DONE_ID ||
                                (actionId == EditorInfo.IME_NULL &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                                        event.getAction()==KeyEvent.ACTION_DOWN)) {
                                    // the user is done typing.
                                    //imm.toggleSoftInput(0,0);
                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                    editView.clearFocus();

                                    mReceiveRenameEvents = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            if (intent.getBooleanExtra("ExecuteRename",false)) {
                                                String newLabel = editView.getText().toString();
                                                labelView.setText(newLabel);
                                                SelfieItem selfieItem = (SelfieItem) getItem(pos);
                                                selfieItem.setLabel(newLabel);
                                                String fileName = selfieItem.getFileName();
                                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                                editor.putString(fileName,newLabel);
                                                editor.commit();
                                                Log.i(MainActivity.TAG, "rename");
                                            } else {
                                                editView.setText(labelView.getText());
                                                Log.i(MainActivity.TAG, "don't rename");
                                            }
                                            switchToLabelView(pos);
                                            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiveRenameEvents);
                                        }
                                    };
                                    LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiveRenameEvents,
                                            new IntentFilter("rename-selected-selfies-event"));

                                    ConfirmRenameDialog dialog = new ConfirmRenameDialog();
                                    Bundle dialogParam = new Bundle();
                                    String fromStr = labelView.getText().toString();
                                    String toStr = v.getText().toString();
                                    dialogParam.putString("from",fromStr);
                                    dialogParam.putString("to",toStr);
                                    dialog.setArguments(dialogParam);
                                    dialog.show(((Activity) mContext).getFragmentManager(), "confirmRenameDialog");
                                    return true; // consume.
                            }
                            return false; // pass on to other listeners.
                        }
                    });
        }
        notifyDataSetChanged();
    }

    public void switchToLabelView(int pos) {
        View rootView = getViewFromPosition(mListView,pos);
        View labelView = rootView.findViewById(R.id.photoLabel);
        View editView = rootView.findViewById(R.id.editPhotoLabel);

        labelView.setVisibility(View.VISIBLE);
        editView.setVisibility(View.GONE);
        notifyDataSetChanged();
    }
}
