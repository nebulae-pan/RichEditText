package com.pxh.richedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class RichEditText extends AppCompatEditText
{
    private Context context;

    private BitmapCreator bitmapCreator;

    boolean isBold = false;
    boolean isItalic = false;
    boolean isUnderLine = false;

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
        setSelection(start + ss.length());// set selection start position
    }

    public void insertUrl(String describe, String url)
    {
        SpannableString ss = new SpannableString(describe);
        ss.setSpan(new URLSpan(url), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(getSelectionStart(), ss);
    }

    public void enableBold(boolean isValid)
    {
        isBold = isValid;
    }

    public void enableItalic(boolean isValid)
    {
        isItalic = isValid;
    }

    public void enableUnderLine(boolean isValid)
    {
        isUnderLine = isValid;
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

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selStart, selEnd);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
        setTextSpan(start, lengthAfter);
    }

    private void setTextSpan(int start, int lengthAfter)
    {
        if (isBold) {
            if (start == 0) {
                //if start=0, text must doesn't have spans, use new StyleSpan
                getEditableText().setSpan(new StyleSpan(Typeface.BOLD), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                //estimate the character what in front of input whether have span
                StyleSpan boldSpan = getStyleSpan(Typeface.BOLD, getEditableText(), start - 1, start);
                if (boldSpan == null) {
                    getEditableText().setSpan(new StyleSpan(Typeface.BOLD), start, start + lengthAfter, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    //if have span , change the span's effect scope
                    changeStyleSpanEnd(Typeface.BOLD, start, lengthAfter, getEditableText());
                }
            }
        }
        if (isItalic) {
            if (start == 0) {
                getEditableText().setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                StyleSpan boldSpan = getStyleSpan(Typeface.ITALIC, getEditableText(), start - 1, start);
                if (boldSpan == null) {
                    getEditableText().setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    changeStyleSpanEnd(Typeface.ITALIC, start, lengthAfter, getEditableText());
                }
            }
        }
        if (isUnderLine) {
            if (start == 0) {
                getEditableText().setSpan(new UnderlineSpan(), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
//                UnderlineSpan boldSpan = getCharacterStyleSpan(Typeface.ITALIC, getEditableText(), start - 1, start);
//                if (boldSpan == null) {
//                    getEditableText().setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter, Spanned
//                            .SPAN_EXCLUSIVE_EXCLUSIVE);
//                } else {
//                    changeStyleSpanEnd(Typeface.ITALIC, start, lengthAfter, getEditableText());
//                }
            }
        }
    }


    private void changeStyleSpanEnd(int style, int start, int lengthAfter, Editable ss)
    {
        changeCharacterStyleEnd(getStyleSpan(style, ss, start, lengthAfter),lengthAfter, ss);
    }

    /**
     * use reflection to change span effect scope
     *
     * @param span        the span what extend CharacterStyle
     * @param lengthAfter the input character's increment
     * @param ss          use method EditText.getEditableText()
     */
    private void changeCharacterStyleEnd(CharacterStyle span,int lengthAfter, Editable ss)
    {
        if (lengthAfter == 0)
            return;
        try {
            Class<?> classType = ss.getClass();

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
                if (mSpans[i] == span) {
                    mSpanEnds[i] += lengthAfter;
                    break;
                }
            }
            ends.set(ss, mSpanEnds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected StyleSpan getStyleSpan(int style, Editable editable, int start, int end)
    {
        StyleSpan[] spans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                return span;
            }
        }
        return null;
    }

    protected CharacterStyle getCharacterStyleSpan(Class<? extends CharacterStyle> clazz, Editable editable, int
            start, int end)
    {
        CharacterStyle[] spans = editable.getSpans(start, end, CharacterStyle.class);
        for (CharacterStyle span : spans) {
            if (span.getClass().equals(clazz)) {
                return span;
            }
        }
        return null;
    }
}
