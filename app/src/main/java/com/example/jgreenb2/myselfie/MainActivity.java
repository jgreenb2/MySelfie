package com.example.jgreenb2.myselfie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static String mCurrentPhotoPath;
    static String mCurrentPhotoLabel;
    static String mStorageDirectory;

    static SelfieListAdapter mSelfieAdapter;
    static private ListView mListView;

    static private AlarmManager mAlarmManager;
    static private Intent mNotificationReceiverIntent;
    static private PendingIntent mNotificationReceiverPendingIntent;

    private static final long INITIAL_ALARM_DELAY = 10 * 1000L;
    private static final long SELFIE_INTERVAL = 2 * 60 * 1000L;

    static private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        mSelfieAdapter = new SelfieListAdapter(MainActivity.this);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mSelfieAdapter);

        // set up an onClickListener
        mListView.setClickable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelfieItem selfieItem = (SelfieItem) mSelfieAdapter.getItem(position);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file:" + selfieItem.getmPhotoPath()),"image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setText("Please install a photo viewer!");
                    toast.show();
                }
            }
        } );
        // set up the annoying alarm
        // Get the AlarmManager Service
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmReceiver
        mNotificationReceiverIntent = new Intent(MainActivity.this,
                AlarmReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, mNotificationReceiverIntent, 0);

        setSelfieAlarm();
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
            dispatchTakePictureIntent(mCurrentPhotoLabel);
            return true;
        } else if (id == R.id.delete_selfies) {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String storageDirAbsolutePath = storageDir.getAbsolutePath();
            File[] files = new File(storageDirAbsolutePath).listFiles();
            for (File f : files) {
                f.delete();
            }
            mSelfieAdapter.clear();
            Toast.makeText(getApplicationContext(), "Selfie's Removed!",
                    Toast.LENGTH_LONG).show();
        } else if (id == R.id.cancel_alarm) {
            cancelSelfieAlarm();
            Toast.makeText(getApplicationContext(), "Alarms Cancelled!",
                    Toast.LENGTH_LONG).show();
        } else if (id == R.id.resume_alarm) {
            setSelfieAlarm();
            Toast.makeText(getApplicationContext(), "Alarms Resumed!",
                    Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Resources resources = getResources();

            int height = (int) resources.getDimension(R.dimen.thumb_height);
            int width = (int) resources.getDimension(R.dimen.thumb_width);
            Bitmap imageBitmap = getPic(height,width,mCurrentPhotoPath);

            SelfieItem newSelfie = new SelfieItem(mCurrentPhotoLabel, mCurrentPhotoPath,
                                                          imageBitmap);
            mSelfieAdapter.add(newSelfie);
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
        mStorageDirectory = storageDir.getAbsolutePath();
        File image = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap getPic(int targetH, int targetW, String src) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(src, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(src, bmOptions);
        return bitmap;
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

            Resources resources = getResources();
            int height = (int) resources.getDimension(R.dimen.thumb_height);
            int width = (int) resources.getDimension(R.dimen.thumb_width);
            for (File f : files) {
                String fName = f.getName();
                Bitmap imageBitmap = getPic(height,width,f.getAbsolutePath());
                if (null != imageBitmap) {
                    SelfieItem newSelfie = new SelfieItem(fName, f.getAbsolutePath(),
                            imageBitmap);
                    mSelfieAdapter.add(newSelfie);
                }
            }
        }
    }

    public void setSelfieAlarm() {
        // Set repeating alarm
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
                SELFIE_INTERVAL,
                mNotificationReceiverPendingIntent);
    }

    public void cancelSelfieAlarm() {
        mAlarmManager.cancel(mNotificationReceiverPendingIntent);
    }
}
