package com.example.jgreenb2.myselfie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    public String getmPhotoPath() {
        return mPhotoPath;
    }

    public void setmPhotoPath(String mPhotoPath) {
        this.mPhotoPath = mPhotoPath;
    }

    private Bitmap mThumb;

    public SelfieItem(String fileName, String photoPath, int thumbHeight, int thumbWidth) {
        // parse the fileName
        String[] labelComponents = fileName.split("_");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD HHmmss", Locale.US);
        Date date = sdf.parse(labelComponents[1]+" "+labelComponents[2], new ParsePosition(0));
        DateFormat readableDate = DateFormat.getDateTimeInstance();

        mLabel = readableDate.format(date);
        mPhotoPath = photoPath;

        Bitmap thumb = getPic(thumbHeight,thumbWidth,photoPath);
        mThumb = Bitmap.createBitmap(thumb);
    }

    public String getmLabel() {
        return mLabel;
    }

    public void setmLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public Bitmap getmThumb() {
        return mThumb;
    }

    public void setmThumb(Bitmap mThumb) {
        this.mThumb = mThumb;
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
}
