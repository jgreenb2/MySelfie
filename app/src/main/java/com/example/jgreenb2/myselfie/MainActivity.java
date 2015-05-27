package com.example.jgreenb2.myselfie;
/*
    5/7/15 -- adding contextual action bar
 */
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    static String mCurrentPhotoPath;
    static String mCurrentPhotoLabel;


    static SelfieListAdapter mSelfieAdapter;
    static private ListView mListView;
    static private AlarmReceiver mAlarmReceiver;
    BroadcastReceiver mReceiveDeleteEvents;
    static private Context mContext;

    static final String TAG="Selfie_app";

    static private int mThumbHeight, mThumbWidth;
    static private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mListView = (ListView) findViewById(R.id.listView);
        mSelfieAdapter = new SelfieListAdapter(MainActivity.this,mListView);

        mListView.setAdapter(mSelfieAdapter);
        mListView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);

        // set up an onClickListener
        // a short click just opens a viewer for the image
        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelfieItem selfieItem = (SelfieItem) mSelfieAdapter.getItem(position);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file:" + selfieItem.getPhotoPath()), "image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please install a photo viewer!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // setup the Contextual Action Bar to allow multiple objects to
        // be selected and deleted
        new ContextualActionBar(this,mListView);

        Resources resources = getResources();

        mThumbHeight = (int) resources.getDimension(R.dimen.thumb_height);
        mThumbWidth = (int) resources.getDimension(R.dimen.thumb_width);

        mAlarmReceiver = new AlarmReceiver(MainActivity.this);

        mAlarmReceiver.setSelfieAlarm();

        // use a key-value pref file to associate selfie filenames with
        // display labels

        mSharedPref = getPreferences(Context.MODE_PRIVATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_selfie) {
            mCurrentPhotoLabel = createImageFileName();
            Log.i(TAG, "label=|" + mCurrentPhotoLabel + "|");
            dispatchTakePictureIntent(mCurrentPhotoLabel);
            return true;
        } else if (id == R.id.delete_selfies) {
            mSelfieAdapter.addAllToSelectionSet();
            requestSelfieDeletions();
        } else if (id == R.id.cancel_alarm) {
            mAlarmReceiver.cancelSelfieAlarm();
            Toast.makeText(getApplicationContext(), "Alarms Cancelled!",
                    Toast.LENGTH_LONG).show();
        } else if (id == R.id.resume_alarm) {
            mAlarmReceiver.setSelfieAlarm();
            Toast.makeText(getApplicationContext(), "Alarms Resumed!",
                    Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                SelfieItem newSelfie = new SelfieItem(mCurrentPhotoLabel, mCurrentPhotoPath,
                        mThumbHeight, mThumbWidth,mSharedPref);
                if (newSelfie.getThumb() != null) {
                    mSelfieAdapter.add(newSelfie);
                    // restart the alarms
                    if (mAlarmReceiver.areAlarmsEnabled()) {
                        mAlarmReceiver.setSelfieAlarm();
                    }
                }
            } else {
                // remove the file
                File staleFile = new File(mCurrentPhotoPath);
                staleFile.delete();
            }
        }
    }

    // create the floating context menu in each list item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        SelfieItem item = (SelfieItem) mSelfieAdapter.getItem((int) v.getTag());
        menu.setHeaderTitle(item.getLabel());
    }

    // handle floating context menu events
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int id = item.getItemId();
        int pos = mSelfieAdapter.getContextPos();
        SelfieItem selfieItem = (SelfieItem) mSelfieAdapter.getItem(pos);
        switch (id) {
            case R.id.delete_single_selfie:
                mSelfieAdapter.addItemToSelectionSet(pos);
                requestSelfieDeletions();
                break;

            case R.id.rename_selfie:
                mSelfieAdapter.switchToEditView(pos);
                break;

            case R.id.email_selfie:
                File attachment = new File(selfieItem.getPhotoPath());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Want to see my selfies?");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + JpegContentProvider.AUTHORITY + File.separator +
                                selfieItem.getFileName()));
                // intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

                startActivity(intent);
                //Toast.makeText(mContext,"email item "+mSelfieAdapter.getContextPos(),Toast.LENGTH_LONG).show();
                break;

            case R.id.reset_self_name:
                String label = SelfieItem.formatFileToLabel(selfieItem.getFileName());
                selfieItem.setLabel(label, mSharedPref);
                mSelfieAdapter.notifyDataSetChanged();
                break;

            default:
                return false;

        }
        return true;
    }

    private void requestSelfieDeletions() {
        mReceiveDeleteEvents = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("ExecuteDelete",false)) {
                    mSelfieAdapter.removeSelectedSelfies();
                } else {
                    mSelfieAdapter.removeAllFromSelectionSet();
                }
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiveDeleteEvents);
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiveDeleteEvents,
                new IntentFilter("delete-selected-selfies-event"));

        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle dialogParam = new Bundle();
        dialogParam.putInt("nSelected", mSelfieAdapter.getNumberOfCheckedPositions());
        dialog.setArguments(dialogParam);
        dialog.show(getFragmentManager(), "confirmDeleteDialog");
    }

    private void dispatchTakePictureIntent(String fileName) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(fileName);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast errMsg = Toast.makeText(getApplicationContext(), "Error creating image",
                                              Toast.LENGTH_SHORT);
                errMsg.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                // stop the alarms and restart after the Selfie has been taken
                mAlarmReceiver.cancelSelfieAlarm();
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private String createImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + "_";
    }

    private File createImageFile(String fileName) throws IOException {
        // Create an image file name

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onResume() {
        super.onResume();

        // load the pictures if need be
        if (mSelfieAdapter.getCount()==0) {
            // find the directory name
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String storageDirAbsolutePath = storageDir.getAbsolutePath();
            File[] files = new File(storageDirAbsolutePath).listFiles();

            for (File f : files) {
                String fName = f.getName();

                SelfieItem newSelfie = new SelfieItem(fName, f.getAbsolutePath(),
                                                      mThumbHeight,mThumbWidth,mSharedPref);
                if (newSelfie.getThumb() != null) {
                    mSelfieAdapter.add(newSelfie);
                }
            }
        }
    }
}
