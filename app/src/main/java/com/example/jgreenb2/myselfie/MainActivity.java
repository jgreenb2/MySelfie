package com.example.jgreenb2.myselfie;
/*
    5/7/15 -- adding contextual action bar
 */
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;


public class MainActivity extends ActionBarActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static String mCurrentPhotoPath;
    static String mCurrentPhotoLabel;


    static SelfieListAdapter mSelfieAdapter;
    static private ListView mListView;
    static private AlarmReceiver mAlarmReceiver;

    static private Context mContext;
    static final String TAG="Selfie_app";

    static private int mThumbHeight, mThumbWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        mSelfieAdapter = new SelfieListAdapter(MainActivity.this);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mSelfieAdapter);

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
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    mSelfieAdapter.addItemToSelectionSet(position);
                } else {
                    mSelfieAdapter.removeItemFromSelectionSet(position);
                }
                mode.setTitle(Integer.toString(mSelfieAdapter.getNumberOfCheckedPositions()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.cab_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        Toast.makeText(mContext,"deleting selection",Toast.LENGTH_LONG).show();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // uncheck all
                mSelfieAdapter.removeAllFromSelectionSet();
            }
        });

        Resources resources = getResources();

        mThumbHeight = (int) resources.getDimension(R.dimen.thumb_height);
        mThumbWidth = (int) resources.getDimension(R.dimen.thumb_width);

        mAlarmReceiver = new AlarmReceiver(MainActivity.this);

        mAlarmReceiver.setSelfieAlarm();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_selfie) {
            mCurrentPhotoLabel = createImageFileName();
            Log.i(TAG, "label=|" + mCurrentPhotoLabel + "|");
            dispatchTakePictureIntent(mCurrentPhotoLabel);
            return true;
        } else if (id == R.id.delete_selfies) {
            SelfieItem.removeSelfies(mContext);
            mSelfieAdapter.clear();
            Toast.makeText(getApplicationContext(), "Selfie's Removed!",
                    Toast.LENGTH_LONG).show();
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
                        mThumbHeight, mThumbWidth);
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
                                                      mThumbHeight,mThumbWidth);
                if (newSelfie.getThumb() != null) {
                    mSelfieAdapter.add(newSelfie);
                }
            }
        }
    }

    // turns out we don't actually need this function but keep it here for
    // future reference
    View getViewFromPosition(ListView listView,int position) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int viewPosition = position - firstVisiblePosition;
        return listView.getChildAt(viewPosition);

    }
}
