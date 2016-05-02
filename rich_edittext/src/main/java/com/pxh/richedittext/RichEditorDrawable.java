package com.pxh.richedittext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by pxh on 2016/4/30.
 * async load drawable
 */
@SuppressWarnings("deprecation")
public class RichEditorDrawable extends BitmapDrawable
{
    public Bitmap bitmap = null;
    Paint paint = new Paint();
    private final Rect rect;

    public RichEditorDrawable(int width)
    {
        rect = new Rect(0, 0, width, 500);
        paint.setColor(0xff000100);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas)
    {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, getPaint());
        }
    }
}
