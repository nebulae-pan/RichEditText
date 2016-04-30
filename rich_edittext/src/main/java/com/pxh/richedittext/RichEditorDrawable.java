package com.pxh.richedittext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by pxh on 2016/4/30.
 * async load drawable
 */
@SuppressWarnings("deprecation")
public class RichEditorDrawable extends BitmapDrawable
{
    public Bitmap bitmap = getBitmap();
    @Override
    public void draw(Canvas canvas)
    {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, getPaint());
        }
    }


}
