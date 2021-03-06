package com.example.jgreenb2.myselfie;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.animation.Animation;

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

    private String mLabel;
    private String mPhotoPath;
    private String mThumbPath;
    private boolean mIsChecked;
    private Bitmap mThumb;

    static public final String THUMB_DIR = "thumbs";
    private final int QUALITY=75;

    public SelfieItem(String fileName, String photoPath, int thumbHeight, int thumbWidth,
                      SharedPreferences labelFile) {
        String storedLabel = labelFile.getString(fileName,"");
        if (storedLabel == "") {
            setLabel(formatFileToLabel(fileName),labelFile);
        } else {
            mLabel = storedLabel;
        }

        mPhotoPath = photoPath;

        Bitmap thumb = newThumb(thumbHeight, thumbWidth, photoPath);
        if (thumb != null) {
            mThumb = Bitmap.createBitmap(thumb);
        } else {
            mThumb = null;
        }
        mIsChecked = false;
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public String getThumbPath() {
        return mThumbPath;
    }

    public void setLabel(String label, SharedPreferences labelFile) {
        String fileName = getFileName();
        mLabel = label;
        SharedPreferences.Editor editor =  labelFile.edit();
        editor.putString(fileName,mLabel);
        editor.commit();
        mLabel = label;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean mIsChecked) {
        this.mIsChecked = mIsChecked;
    }

    static public String formatFileToLabel(String fileName) {
        String[] labelComponents = fileName.split("_");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD HHmmss", Locale.US);
        Date date = sdf.parse(labelComponents[1] + " " + labelComponents[2], new ParsePosition(0));
        DateFormat readableDate = DateFormat.getDateTimeInstance();

        return readableDate.format(date);
    }

    public String getLabel() {
        return mLabel;
    }

    public String getFileName() {
        return mPhotoPath.substring(mPhotoPath.lastIndexOf('/')+1);
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
        String thumbPath = thumbDirName+"/"+fileName;
        mThumbPath = thumbPath;
        File thumbNail = new File(thumbPath);
        Bitmap thumb;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();


        if (thumbNail.exists()) {
            bmOptions.inJustDecodeBounds = false;
            thumb=BitmapFactory.decodeFile(thumbPath, bmOptions);
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
}
