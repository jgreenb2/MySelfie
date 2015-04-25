package com.example.jgreenb2.myselfie;

import android.graphics.Bitmap;

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

    public SelfieItem(String mLabel, String mPhotoPath, Bitmap mThumb) {
        this.mLabel = mLabel;
        this.mPhotoPath = mPhotoPath;
        this.mThumb = Bitmap.createBitmap(mThumb);
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


}
