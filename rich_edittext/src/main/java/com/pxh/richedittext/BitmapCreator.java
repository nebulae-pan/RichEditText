package com.pxh.richedittext;

import android.graphics.Bitmap;

/**
 * Created by pxh on 2016/4/13.
 */
public interface BitmapCreator
{
    Bitmap getBitmapByDiskPath(String path);

    Bitmap getBitmapByString(String tagString, int textColor, int backgroundColor);
}
