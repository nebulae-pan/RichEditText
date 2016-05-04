package com.pxh.richedittext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by pxh on 2016/4/13.
 * get bitmap displayed in TextView or EditText
 */
public class InsertBitmapCreator implements BitmapCreator
{
    private float maxWidth;
    private float maxHeight;

    public InsertBitmapCreator(int maxWidth, int maxHeight)
    {
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    @Override
    public Bitmap getBitmapByDiskPath(String path)
    {
        BitmapFactory.Options option = new BitmapFactory.Options();
        //just decodeBounds and resize the width and height of bitmap
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, option);
        int bmpWidth = option.outWidth;
        int bmpHeight = option.outHeight;
        float scale = 1;
        if (maxHeight < bmpHeight) {
            scale = bmpHeight / maxHeight;
        } else if (maxWidth < bmpWidth) {
            scale = bmpWidth / maxWidth;
        }
        option.outWidth = (int) (bmpWidth / scale);
        option.outHeight = (int) (bmpHeight / scale);
        option.inJustDecodeBounds = false;
        option.inSampleSize = (int) scale;

        Bitmap bmp = BitmapFactory.decodeFile(path, option);

        return getPlaceCenterBitmap(bmp);
    }

    @Override
    public Bitmap getBitmapByBitmap(Bitmap bitmap)
    {
        //scale bitmap
        Matrix matrix = new Matrix();
        float scale = 1;
        if (maxHeight < bitmap.getHeight()) {
            scale = maxHeight / bitmap.getHeight();

        } else if (maxWidth < bitmap.getWidth()) {
            scale = maxWidth / bitmap.getWidth();
        }
        matrix.postScale(scale, scale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


        return getPlaceCenterBitmap(bitmap);
    }

    private Bitmap getPlaceCenterBitmap(Bitmap bitmap)
    {
        Bitmap bgm = Bitmap.createBitmap((int) maxWidth, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        bgm.eraseColor(Color.argb(0, 0, 0, 0)); //transparent bitmap
        Canvas canvas = new Canvas(bgm);
        canvas.drawBitmap(bitmap, (maxWidth - bitmap.getWidth()) / 2, 0, null);//draw bitmap in centre horizontal

        bitmap.recycle();
        canvas.save();
        return bgm;
    }

    @Override
    public Bitmap getBitmapByString(String tagString, int textColor, int backgroundColor)
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
