package com.example.jgreenb2.myselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jgreenb2 on 4/23/15.
 */
public class SelfieItem {

    private String mLabel = new String();
    private String mPhotoPath = new String();
    private boolean mIsChecked;

    static private final String THUMB_DIR = "thumbs";
    private final int QUALITY=75;

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void setmPhotoPath(String mPhotoPath) {
        this.mPhotoPath = mPhotoPath;
    }

    private Bitmap mThumb;

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean mIsChecked) {
        this.mIsChecked = mIsChecked;
    }

    public SelfieItem(String fileName, String photoPath, int thumbHeight, int thumbWidth) {
        // parse the fileName
        String[] labelComponents = fileName.split("_");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD HHmmss", Locale.US);
        Date date = sdf.parse(labelComponents[1]+" "+labelComponents[2], new ParsePosition(0));
        DateFormat readableDate = DateFormat.getDateTimeInstance();

        mLabel = readableDate.format(date);
        mPhotoPath = photoPath;

        Bitmap thumb = newThumb(thumbHeight, thumbWidth, photoPath);
        if (thumb != null) {
            mThumb = Bitmap.createBitmap(thumb);
        } else {
            mThumb = null;
        }
        mIsChecked = false;
    }

    public String getLabel() {
        return mLabel;
    }

    public Bitmap getThumb() {
        return mThumb;
    }

    private Bitmap newThumb(int targetH, int targetW, String src) {

        // construct the thumbNail path
        int i = src.lastIndexOf('/');
        String pathName = src.substring(0, i);
        String fileName = src.substring(i + 1);
        String thumbDirName = pathName+"/../"+ THUMB_DIR;

        // create thumbDir if it doesn't exist
        File thumbDir = new File(thumbDirName);
        if (!(thumbDir.exists() && thumbDir.isDirectory())) {
            thumbDir.mkdir();
        }

        // if the thumbnail already exists just read it in
        String thumbName = thumbDirName+"/"+fileName;
        File thumbNail = new File(thumbName);
        Bitmap thumb;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();


        if (thumbNail.exists()) {
            bmOptions.inJustDecodeBounds = false;
            thumb=BitmapFactory.decodeFile(thumbName, bmOptions);
            if (thumb==null) {
                // if the thumbNail is corrupt delete it and try to recreate it
                thumbNail.delete();
                return newThumb(targetH,targetW,src);
            }
        } else {
            // if the thumb doesn't exist we have to create it

            // Get the dimensions of the bitmap
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(src, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            thumb = BitmapFactory.decodeFile(src, bmOptions);
            if (thumb!=null) {
                // save thumb to the thumbNail directory

                try {
                    FileOutputStream fos = new FileOutputStream(thumbNail);
                    thumb.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // if the thumb can't be created -- perhaps because the image file
                // is corrupt, delete both the image file and thumb then return null
                thumbNail.delete();
                File srcFile = new File(src);
                srcFile.delete();
            }
        }
        return thumb;
    }
    // static method for removing all the selfies
    static public void removeSelfies(Context context) {

        // delete the image files
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String storageDirAbsolutePath = storageDir.getAbsolutePath();
        String thumbPath = storageDirAbsolutePath+"/../"+THUMB_DIR;
        File[] files = new File(storageDirAbsolutePath).listFiles();

        for (File f : files) {
            f.delete();
        }
        // now delete the thumbNails
        File thumbDir= new File(thumbPath);
        files = thumbDir.listFiles();
        for (File f : files) {
            f.delete();
        }
        thumbDir.delete();

    }
}
