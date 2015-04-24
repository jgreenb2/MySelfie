package com.example.jgreenb2.myselfie;

import android.graphics.Bitmap;

/**
 * Created by jgreenb2 on 4/23/15.
 */
public class SelfieItem {

    private String mFilename = new String();
    private String mPhotoPath = new String();

    public String getmPhotoPath() {
        return mPhotoPath;
    }

    public void setmPhotoPath(String mPhotoPath) {
        this.mPhotoPath = mPhotoPath;
    }

    private Bitmap mThumb;

    public SelfieItem(String mFilename, String mPhotoPath, Bitmap mThumb) {
        this.mFilename = mFilename;
        this.mPhotoPath = mPhotoPath;
        this.mThumb = Bitmap.createBitmap(mThumb);
    }

    public String getmFilename() {
        return mFilename;
    }

    public void setmFilename(String mFilename) {
        this.mFilename = mFilename;
    }

    public Bitmap getmThumb() {
        return mThumb;
    }

    public void setmThumb(Bitmap mThumb) {
        this.mThumb = mThumb;
    }


}
