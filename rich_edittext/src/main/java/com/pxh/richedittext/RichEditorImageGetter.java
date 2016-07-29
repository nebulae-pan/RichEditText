package com.pxh.richedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;

/**
 * Created by pxh on 2016/4/29.
 * RichEditor ImageGetter can loadImage for TextView when it use method:setHtml()
 */
public class RichEditorImageGetter implements Html.ImageGetter
{
    EditText editor;
    int width;

    public RichEditorImageGetter(EditText editor)
    {
        WeakReference<EditText> reference = new WeakReference<>(editor);
        this.editor = reference.get();
        width = editor.getMeasuredWidth();
    }

    @Override
    public Drawable getDrawable(String source)
    {
        final RichEditorDrawable drawable = new RichEditorDrawable();
        ImageLoader.getInstance().loadImage(source,
                new SimpleImageLoadingListener()
                {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                    {
                        DisplayMetrics metric = new DisplayMetrics();
                        WindowManager wm = (WindowManager) editor.getContext().getSystemService(Context.WINDOW_SERVICE);
                        wm.getDefaultDisplay().getMetrics(metric);
                        int maxWidth = width;
                        int maxHeight = metric.heightPixels;
                        BitmapCreator bitmapCreator = new InsertBitmapCreator(maxWidth, maxHeight);
                        drawable.bitmap = bitmapCreator.getBitmapByBitmap(loadedImage);
                        drawable.setBounds(0, 0, drawable.bitmap.getWidth(), drawable.bitmap.getHeight());
                        editor.invalidate();
                        editor.setText(editor.getText());
                    }
                });
//        BitmapDrawable bd = (BitmapDrawable) editor.getContext().getResources().getDrawable(R.drawable.holder);
        BitmapDrawable bd = (BitmapDrawable) ContextCompat.getDrawable(editor.getContext(), R.drawable.holder);
        Bitmap holder = bd.getBitmap();
        float scaleWidth = ((float) width) / holder.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        holder = Bitmap.createBitmap(holder, 0, 0, holder.getWidth(), holder.getHeight(), matrix, true);
        drawable.bitmap = holder;
        drawable.setBounds(0, 0, holder.getWidth(), holder.getHeight());
        return drawable;
    }
}
