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
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


import java.lang.reflect.Field;

public class RichEditText extends AppCompatEditText
{
    private Context context;

    /**
     * use bitmap creator get a bitmap
     */
    private BitmapCreator bitmapCreator;

    TextSpanState state = new TextSpanState();

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

        //construct a Drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(context.getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, path, DynamicDrawableSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();

        getEditableText().insert(start, ss);//insert the imageSpan
        setSelection(start + ss.length());  //set selection start position
    }

    public void insertUrl(String describe, String url)
    {
        SpannableString ss = new SpannableString(describe);
        ss.setSpan(new URLSpan(url), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(getSelectionStart(), ss);
    }

    public void enableBold(boolean isValid)
    {
        if (getSelectionStart() < getSelectionEnd())
            setSelectionTextBold(isValid);
        else
            state.enableBold(isValid);
    }

    public void enableItalic(boolean isValid)
    {
        state.enableItalic(isValid);
    }

    public void enableUnderLine(boolean isValid)
    {
        state.enableUnderLine(isValid);
    }

    public void enableStrikethrough(boolean isValid)
    {
        state.enableStrikethrough(isValid);
    }

    public void enableQuote(boolean isValid)
    {
        setQuote();
    }

    public void enableBullet(boolean isValid)
    {
        setBullet();
    }

    public boolean isBoldEnable()
    {
        return state.isBoldEnable();
    }

    public boolean isUnderLineEnable()
    {
        return state.isUnderLineEnable();
    }

    public boolean isItalicEnable()
    {
        return state.isItalicEnable();
    }

    public boolean isStrikethroughEnable()
    {
        return state.isStrikethroughEnable();
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

    public void setSpanChangeListener(TextSpanChangeListener listener)
    {
        state.setSpanChangeListener(listener);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selStart, selEnd);
        if (state == null) {
            return;
        }
        Log.v("before", String.valueOf(state.isUnderLineEnable()));

        changeSpanStateBySelection(selStart);
        Log.v("after", String.valueOf(state.isUnderLineEnable()));


    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
        setTextSpan(start, lengthAfter);
    }

    /**
     * when characters input , set the text's span by characterStyle and ParagraphStyle.
     * Parameters start and lengthAfter must use the parameter of onTextChanged
     *
     * @param start       the start of character's input
     * @param lengthAfter the length of character's input
     */
    private void setTextSpan(int start, int lengthAfter)
    {
        if (state == null) {
            return;
        }
        Log.v("underline", String.valueOf(state.isUnderLineEnable()));
        if (state.isBoldEnable()) {
            if (start == 0) {
                //if start = 0, text must doesn't have spans, use new StyleSpan
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
                    changeCharacterStyleEnd(boldSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isItalicEnable()) {
            if (start == 0) {
                getEditableText().setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                StyleSpan italicSpan = getStyleSpan(Typeface.ITALIC, getEditableText(), start - 1, start);
                if (italicSpan == null) {
                    getEditableText().setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    changeCharacterStyleEnd(italicSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isUnderLineEnable()) {
            if (start == 0) {
                getEditableText().setSpan(new UnderlineSpan(), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                CharacterStyle underLineSpan = getCharacterStyleSpan(UnderlineSpan.class, getEditableText(), start - 1,
                        start);
                if (underLineSpan == null) {
                    getEditableText().setSpan(new UnderlineSpan(), start, start + lengthAfter, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    changeCharacterStyleEnd(underLineSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isStrikethroughEnable()) {
            if (start == 0) {
                getEditableText().setSpan(new StrikethroughSpan(), start, start + lengthAfter, Spanned
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                CharacterStyle strikeThroughSpan = getCharacterStyleSpan(StrikethroughSpan.class, getEditableText(),
                        start - 1, start);
                if (strikeThroughSpan == null) {
                    getEditableText().setSpan(new StrikethroughSpan(), start, start + lengthAfter, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    changeCharacterStyleEnd(strikeThroughSpan, lengthAfter, getEditableText());
                }
            }
        }
    }

    /**
     * change Span's state when selection change
     *
     * @param start the selection start after change
     */
    private void changeSpanStateBySelection(int start)
    {
        if (state == null) {
            return;
        }
        state.clearSelection();
        StyleSpan[] spans = getEditableText().getSpans(start, start, StyleSpan.class);
        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.BOLD) {
                state.enableBold(true);
            } else {
                state.enableItalic(true);
            }
        }
        UnderlineSpan[] underLineSpans = getEditableText().getSpans(start, start, UnderlineSpan.class);
        if (underLineSpans.length != 0) {
            state.enableUnderLine(true);
        }
        StrikethroughSpan[] strikethroughSpan = getEditableText().getSpans(start, start, StrikethroughSpan.class);
        if (strikethroughSpan.length != 0) {
            state.enableStrikethrough(true);
        }
    }


    /**
     * use reflection to change span effect scope
     *
     * @param span        the span what is extend CharacterStyle
     * @param lengthAfter the input character's increment
     * @param ss          use method EditText.getEditableText()
     */
    private void changeCharacterStyleEnd(CharacterStyle span, int lengthAfter, Editable ss)
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

    private void setSelectionTextBold(boolean isBold)
    {
        if (isBold) {
            getEditableText().setSpan(new StyleSpan(Typeface.BOLD), getSelectionStart(), getSelectionEnd(), Spanned
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else
        {
            StyleSpan span = getStyleSpan(Typeface.BOLD, getEditableText(), getSelectionStart(), getSelectionEnd());
            getEditableText().removeSpan(span);
        }
    }

    private void setQuote()
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        for (int i = 0; i < getLineCount(); i++) {
            int lineStart = getLayout().getLineStart(i);
            if (lineStart == start) {
                break;
            }
            if ((lineStart > start) || (lineStart < start && i == (getLineCount() - 1))) {
                getEditableText().insert(start, "\n");
                setSelection(start + 1, end + 1);
                break;
            }
        }
        getEditableText().setSpan(new QuoteSpan(), getSelectionStart(), getSelectionEnd(), Spanned
                .SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setBullet()
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        for (int i = 0; i < getLineCount(); i++) {
            int lineStart = getLayout().getLineStart(i);
            if (lineStart == start) {
                break;
            }
            if ((lineStart > start) || (lineStart < start && i == (getLineCount() - 1))) {
                getEditableText().insert(start, "\n");
                setSelection(start + 1, end + 1);
                break;
            }
        }
        getEditableText().setSpan(new BulletSpan(), getSelectionStart(), getSelectionEnd(), Spanned
                .SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * get styleSpan by specified style from the editable text
     *
     * @param style    the specified style
     * @param editable the editable text
     * @param start    start of editable
     * @param end      end of editable
     * @return if there has a StyleSpan which style is specified in start to end,return it,or return null
     */
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


    public interface TextSpanChangeListener
    {
        /**
         * called when current text span changed
         *
         * @param type    span type
         * @param isValid is span Valid
         */
        void OnTextSpanChanged(TextSpanState.TextSpan type, boolean isValid);
    }
}
