package com.pxh.richedittext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

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
    public Bitmap getBitmapByTag(String tagString, int textColor, int backgroundColor)
    {
        Paint p = new Paint();
        p.setTextSize(35);
        p.setColor(textColor);

        int width = (int) (getFontLength(p, tagString) + 40);
        int height = 60;
        Bitmap bgm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bgm);

        Paint paint = new Paint();
        paint.setColor(backgroundColor);
        paint.setAntiAlias(true);
        float roundPx = 30;
        float roundPy = 50;
        Rect rect = new Rect(8, 5, bgm.getWidth() - 8, bgm.getHeight() - 5);
        RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        float tX = (width - getFontLength(p, tagString)) / 2;
        float tY = (height - getFontHeight(p)) / 2 + getFontLeading(p);
        canvas.drawText(tagString, tX, tY, p);
        canvas.save();
        return bgm;
    }

    /**
     * @return 返回指定笔和指定字符串的长度
     */
    public static float getFontLength(Paint paint, String str)
    {
        return paint.measureText(str);
    }

    /**
     * @return 返回指定笔的文字高度
     */
    public static float getFontHeight(Paint paint)
    {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * @return 返回指定笔离文字顶部的基准距离
     */
    public static float getFontLeading(Paint paint)
    {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }
}
