package com.pxh.richedittext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

/**
 * Created by pxh on 2016/4/13.
 */
public class InsertBitmapCreator implements BitmapCreator
{
    private int screenWidth;

    public InsertBitmapCreator(int screenWidth)
    {
        this.screenWidth = screenWidth;
    }

    @Override
    public Bitmap getBitmapByDiskPath(String path)
    {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, option);
        int bmpWidth = option.outWidth;
        int bmpHeight = option.outHeight;
        if (200 < bmpHeight) {
            int scale = bmpHeight / (200);
            option.outWidth = bmpWidth / scale;
            option.outHeight = 200;
            option.inSampleSize = scale;
        } else if (screenWidth < bmpWidth) {
            float scale = bmpWidth / (float) screenWidth;
            option.outWidth = screenWidth;
            option.outHeight = (int) (bmpHeight / scale);
            option.inSampleSize = (int) scale;
        }

        Bitmap bgm = Bitmap.createBitmap(screenWidth, option.outHeight, Bitmap.Config.ARGB_8888);
        bgm.eraseColor(Color.argb(0, 0, 0, 0)); // 透明位图
        Canvas canvas = new Canvas(bgm);
        option.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, option);
        canvas.drawBitmap(bmp, (screenWidth - option.outWidth) / 2, 0l, null);
        bmp.recycle();
        canvas.save();
        return bgm;
    }

    @Override
    public Bitmap getBitmapByTag(String tagString)
    {
        return null;
    }
}
