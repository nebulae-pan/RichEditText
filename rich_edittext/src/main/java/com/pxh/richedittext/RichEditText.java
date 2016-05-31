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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

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
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextBold(isValid, start, end);
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
        if (selStart == selEnd) {
            changeSpanStateBySelection(selStart);
        } else {
            changeSpanStateBySelection(selStart, selEnd);
        }
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
        if (state.isBoldEnable()) {
            if (start == 0) {
                //if start = 0, text must doesn't have spans, use new StyleSpan
                setSpan(new StyleSpan(Typeface.BOLD), start, start + lengthAfter);
            } else {
                //estimate the character what in front of input whether have span
                StyleSpan boldSpan = getStyleSpan(Typeface.BOLD, getEditableText(), start - 1, start);
                if (boldSpan == null) {
                    setSpan(new StyleSpan(Typeface.BOLD), start, start + lengthAfter);
                } else {
                    //if have span , change the span's effect scope
                    changeCharacterStyleEnd(boldSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isItalicEnable()) {
            if (start == 0) {
                setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter);
            } else {
                StyleSpan italicSpan = getStyleSpan(Typeface.ITALIC, getEditableText(), start - 1, start);
                if (italicSpan == null) {
                    setSpan(new StyleSpan(Typeface.ITALIC), start, start + lengthAfter);
                } else {
                    changeCharacterStyleEnd(italicSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isUnderLineEnable()) {
            if (start == 0) {
                setSpan(new UnderlineSpan(), start, start + lengthAfter);
            } else {
                CharacterStyle underLineSpan = getCharacterStyleSpan(UnderlineSpan.class, getEditableText(), start - 1,
                        start);
                if (underLineSpan == null) {
                    setSpan(new UnderlineSpan(), start, start + lengthAfter);
                } else {
                    changeCharacterStyleEnd(underLineSpan, lengthAfter, getEditableText());
                }
            }
        }
        if (state.isStrikethroughEnable()) {
            if (start == 0) {
                setSpan(new StrikethroughSpan(), start, start + lengthAfter);
            } else {
                CharacterStyle strikeThroughSpan = getCharacterStyleSpan(StrikethroughSpan.class, getEditableText(),
                        start - 1, start);
                if (strikeThroughSpan == null) {
                    setSpan(new StrikethroughSpan(), start, start + lengthAfter);
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
        state.clearSelection();
        StyleSpan[] spans = getEditableText().getSpans(start - 1, start , StyleSpan.class);
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

    private void changeSpanStateBySelection(int start, int end)
    {
        state.clearSelection();
        StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            if (isSpanInRange(span, start, end)) {
                if (span.getStyle() == Typeface.BOLD) {
                    state.enableBold(true);
                } else {
                    state.enableItalic(true);
                }
            }
        }
        UnderlineSpan[] underLineSpans = getEditableText().getSpans(start, start, UnderlineSpan.class);
        if (underLineSpans.length != 0 && isSpanInRange(underLineSpans[0], start, end)) {
            state.enableUnderLine(true);
        }
        StrikethroughSpan[] strikethroughSpan = getEditableText().getSpans(start, start, StrikethroughSpan.class);
        if (strikethroughSpan.length != 0 && isSpanInRange(strikethroughSpan[0], start, end)) {
            state.enableStrikethrough(true);
        }
    }

    /**
     * estimate span whether in the editText limit by parameters start and end
     *
     * @param span  the span to estimate
     * @param start start of the text
     * @param end   end of the text
     * @return if in this bound return true else return false
     */
    private boolean isSpanInRange(Object span, int start, int end)
    {
        return getEditableText().getSpanStart(span) <= start && getEditableText().getSpanEnd(span) >= end;
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

    private void setSelectionTextBold(boolean isBold, int start, int end)
    {
        //merge span
        if (isBold) {
            StyleSpan[] spans = getStyleSpans(Typeface.BOLD, start, end);
            for (StyleSpan span : spans) {
                getEditableText().removeSpan(span);
            }
            int newStart = start;
            int newEnd = end;
            StyleSpan before = getStyleSpan(Typeface.BOLD, getEditableText(), start - 1, start);
            if (before != null) {
                newStart = getEditableText().getSpanStart(before);
                getEditableText().removeSpan(before);
            }
            StyleSpan after = getStyleSpan(Typeface.BOLD, getEditableText(), end, end + 1);
            if (after != null) {
                newEnd = getEditableText().getSpanEnd(after);
                getEditableText().removeSpan(after);
            }
            setSpan(new StyleSpan(Typeface.BOLD), newStart, newEnd);
        } else { // spilt span
            StyleSpan span = getStyleSpan(Typeface.BOLD, getEditableText(), start, end);
            int spanStart = getEditableText().getSpanStart(span);
            int spanEnd = getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(new StyleSpan(Typeface.BOLD), spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(new StyleSpan(Typeface.BOLD), end, spanEnd);
            }
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
        setSpan(new QuoteSpan(), getSelectionStart(), getSelectionEnd());
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
        setSpan(new BulletSpan(), getSelectionStart(), getSelectionEnd());
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

    protected StyleSpan[] getStyleSpans(int style, int start, int end)
    {
        StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
        ArrayList<StyleSpan> result = new ArrayList<>();
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                result.add(span);
            }
        }
        return result.toArray(new StyleSpan[result.size()]);
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

    private void setSpan(Object span, int start, int end)
    {
        getEditableText().setSpan(span,start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
