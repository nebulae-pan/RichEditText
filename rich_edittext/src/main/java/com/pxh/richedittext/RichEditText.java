package com.pxh.richedittext;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.WindowManager;

public class RichEditText extends AppCompatEditText
{
    private Context context;

    private BitmapCreator bitmapCreator;

    SparseArray<ImageSite> imgArray = new SparseArray<>();
    private int screenWidth;

    /**
     * Description:用于存储插入editText的图片信息:起止位置，路径信息 <br/>
     * <p/>
     * CodeTime:2015年9月20日下午7:31:39
     *
     * @author pxh
     */
    public static class ImageSite
    {
        public int start;
        public int end;
        public String path;

        public ImageSite(int start, int end, String path)
        {
            this.start = start;
            this.end = end;
            this.path = path;
        }

        @Override
        public String toString()
        {
            return "ImageSite{" +
                    "start=" + start +
                    ", end=" + end +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        screenWidth = (int) ((metric.widthPixels) - getPaddingRight() - getPaddingLeft() / 1.1);
    }

    public void insertImage(Uri uri)
    {
        if (bitmapCreator == null) {
            bitmapCreator = new InsertBitmapCreator(screenWidth);
        }
        String path = UriUtils.getValidPath(context, uri);
        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);

        SpannableString ss = new SpannableString(path);

        ImageSpan span = new ImageSpan(context, bitmap);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();
        imgArray.put(start + ss.length(), new ImageSite(start, start + ss.length(), ss.toString()));
        Editable et = getEditableText();// 先获取EditText中的内容
        et.insert(start, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

    public void insertImage(String s, int start, int end, Bitmap bitmap)
    {
        SpannableString ss = new SpannableString(s);

        ImageSpan span = new ImageSpan(context, bitmap);
        ss.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        imgArray.put(end, new ImageSite(start, end, ss.toString()));
        Editable et = getEditableText();// 先获取EditText中的内容
        et.replace(start, end, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

    public void insertImage(String s, Bitmap bitmap)
    {
        SpannableString ss = new SpannableString(s);

        ImageSpan span = new ImageSpan(context, bitmap);
        ss.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = getSelectionStart();
        imgArray.put(start + ss.length(), new ImageSite(start, start + ss.length(), ss.toString()));
        Editable et = getEditableText();// 先获取EditText中的内容
        et.insert(start, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

    public boolean deleteImg()
    {
        int pos = this.getSelectionStart();
        ImageSite info = imgArray.get(pos);
        if (info == null)
            return false;
        Editable et = this.getText();
        et.delete(info.start, info.end);
        this.setText(et);
        this.setSelection(info.start);
        return true;
    }

//    /**
//     * if use this method , it will occur a bug that can't delete a inserted image with press delete key only once
//     */
//    @Override
//    public void setOnKeyListener(OnKeyListener l)
//    {
//        super.setOnKeyListener(l);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() != KeyEvent.ACTION_UP) {
            if (!deleteImg()) {
                return super.dispatchKeyEvent(event);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setBitmapCreator(BitmapCreator bitmapCreator)
    {
        this.bitmapCreator = bitmapCreator;
    }

    public String getContent()
    {
        return this.getText().toString();
    }

    public SparseArray<ImageSite> getImageSite()
    {
        return imgArray;
    }
}
