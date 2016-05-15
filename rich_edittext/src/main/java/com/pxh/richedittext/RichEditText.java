package com.pxh.richedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class RichEditText extends AppCompatEditText
{
    private Context context;

    private BitmapCreator bitmapCreator;

    StyleSpan styleSpan = null;

    boolean isBold = false;

    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(final Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                DisplayMetrics metric = new DisplayMetrics();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metric);
                int maxWidth = RichEditText.this.getMeasuredWidth() - 2;
                int maxHeight = metric.heightPixels;
                bitmapCreator = new InsertBitmapCreator(maxWidth, maxHeight);
            }
        });
    }

    public void insertImage(Uri uri)
    {
        String path = UriUtils.getValidPath(context, uri);
        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);

        SpannableString ss = new SpannableString(path);

        //construct a drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(context.getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, path, DynamicDrawableSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();

        getEditableText().insert(start, ss);//insert imageSpan
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

    public void insertUrl(String describe, String url)
    {
        SpannableString ss = new SpannableString(describe);
        ss.setSpan(new URLSpan(url), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(getSelectionStart(), ss);
    }

    public void setBold(boolean isValid)
    {
        isBold = isValid;
//        if (isBold) {
//            int startSelection = getSelectionStart();
//            int endSelection = getSelectionEnd();
//            if (startSelection > endSelection) {
//                startSelection = getSelectionEnd();
//                endSelection = getSelectionStart();
//            }
//            getEditableText().setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startSelection, endSelection, 0);
//            setSelection(startSelection, endSelection);
//        } else {
//            int startSelection = getSelectionStart();
//            int endSelection = getSelectionEnd();
//            if (startSelection > endSelection) {
//                startSelection = getSelectionEnd();
//                endSelection = getSelectionStart();
//            }
//            Spannable str = getText();
//            StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);
//            for (int i = 0; i < ss.length; i++) {
//                if (ss[i].getStyle() == android.graphics.Typeface.BOLD) {
//                    str.removeSpan(ss[i]);
//                }
//                if (ss[i].getStyle() == android.graphics.Typeface.ITALIC) {
//                    str.removeSpan(ss[i]);
//                }
//            }
//        }
    }

    public void setHtml(final String html)
    {
        Html.ImageGetter imgGetter = new RichEditorImageGetter(this);
        setText(Html.fromHtml(html, imgGetter, null));
    }

    public String getHtml()
    {
        return Html.toHtml(getText());
    }

//    private static class PassThrough extends StyleSpan
//    {
//        StyleSpan mSpan;
//
//        public PassThrough(StyleSpan styleSpan)
//        {
//            super(styleSpan.getStyle());
//            mSpan = styleSpan;
//        }
//
//        public PassThrough(Parcel src)
//        {
//            super(src);
//        }
//
//        @Override
//        public void updateDrawState(TextPaint ds)
//        {
//            ds.setARGB(255, 255, 255, 255);
//            mSpan.updateDrawState(ds);
//        }
//    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
        setTextSpan(start, lengthBefore, lengthAfter);
    }

    private void setTextSpan(int start, int lengthBefore, int lengthAfter)
    {
        if (isBold) {
            if (styleSpan == null) {
                styleSpan = new StyleSpan(Typeface.BOLD);
                getEditableText().setSpan(styleSpan, start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ChangeEnd(styleSpan, getEditableText(), lengthAfter);

        }
    }

    private void ChangeEnd(StyleSpan styleSpan, Editable ss, int lengthAfter)
    {
        try {
//            Log.v("start", ss.getSpanStart(styleSpan) + ":" + ss.getSpanEnd(styleSpan));
            Class<?> classType = ss.getClass();
//            Field starts = classType.getDeclaredField("mSpanStarts");
//
//            starts.setAccessible(true);
//            int[] mSpanStarts = (int[]) starts.get(ss);
            Field count = classType.getDeclaredField("mSpanCount");
            Field spans = classType.getDeclaredField("mSpans");
            Field ends = classType.getDeclaredField("mSpanEnds");

            count.setAccessible(true);
            spans.setAccessible(true);
            ends.setAccessible(true);

            int mSpanCount = (int) count.get(ss);
            Object[] mSpans = (Object[]) spans.get(ss);
            int[] mSpanEnds = (int[]) ends.get(ss);


            for (int i = mSpanCount - 1; i >= 0; i--) {
                if (mSpans[i] == styleSpan) {
                    mSpanEnds[i] += lengthAfter;
                }
            }

            ends.set(ss,mSpanEnds);


//            Field field1 = classType.getDeclaredField("mGapStart");
//            Field field2 = classType.getDeclaredField("mGapLength");
//            field1.setAccessible(true);
//            field2.setAccessible(true);
//            int mGapStart = (int) field1.get(ss);
//            Log.v("GapStart", String.valueOf(mGapStart));
//            int mGapLength = (int) field2.get(ss);
//            Log.v("GapLength", String.valueOf(mGapLength));
//

//            Log.v("getGet", String.valueOf(getSpanStart(styleSpan, mSpanCount, mSpans, mSpanStarts, mGapStart,
// mGapLength)));

//            Field mIndexOfSpan = classType.getDeclaredField("mLowWaterMark");
//            mIndexOfSpan.setAccessible(true);
//            IdentityHashMap<Object, Integer> index = (IdentityHashMap<Object, Integer>) mIndexOfSpan.get(ss);
//            Log.v("index", String.valueOf(index.get(styleSpan)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public int getSpanStart(Object what, int mSpanCount, Object[] mSpans, int[] mSpanStarts, int mGapStart, int
//            mGapLength)
//    {
//        int count = mSpanCount;
//        Object[] spans = mSpans;
//
//        for (int i = count - 1; i >= 0; i--) {
//            if (spans[i] == what) {
//                int where = mSpanStarts[i];
//                Log.v("where", "i:" + i + "," + where);
//                if (where > mGapStart)
//                    where -= mGapLength;
//
//                return where;
//            }
//        }
//        return -1;
//    }

    protected StyleSpan getSpan(int style, StyleSpan[] spans)
    {
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                Log.v("hasSpan", style + "true");
                return span;
            }
        }
        Log.v("hasSpan", style + "false");
        return null;
    }
}
