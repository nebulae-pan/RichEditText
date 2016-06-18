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
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

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

    /**
     * enable bold span
     * @param isValid if enable is true
     */
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
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextItalic(isValid, start, end);
        state.enableItalic(isValid);
    }

    public void enableUnderLine(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextUnderLine(isValid, start, end);
        state.enableUnderLine(isValid);
    }

    public void enableStrikethrough(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextStrikeThrough(isValid, start, end);
        state.enableStrikethrough(isValid);
    }

    public void enableQuote(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextStrikeThrough(isValid, start, end);
        state.enableStrikethrough(isValid);
        setQuote();
    }

    public void enableBullet(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < end)
            setSelectionTextStrikeThrough(isValid, start, end);
        state.enableStrikethrough(isValid);
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
        if (state == null) {
            return;
        }
        if (state.isBoldEnable()) {
            setTextSpan(Typeface.BOLD, start, lengthAfter);
        }
        if (state.isItalicEnable()) {
            setTextSpan(Typeface.ITALIC, start, lengthAfter);
        }
        if (state.isUnderLineEnable()) {
            setTextSpan(new UnderlineSpan(), start, lengthAfter);
        }
        if (state.isStrikethroughEnable()) {
            setTextSpan(new StrikethroughSpan(), start, lengthAfter);
        }
    }

    /**
     * when characters input , set the text's span by characterStyle and ParagraphStyle.
     * Parameters start and lengthAfter must use the parameter of onTextChanged
     *
     * @param start       the start of character's input
     * @param lengthAfter the length of character's input
     */
    private void setTextSpan(int style, int start, int lengthAfter)
    {
        if (start == 0) {
            //if start = 0, text must doesn't have spans, use new StyleSpan
            setSpan(new StyleSpan(style), start, start + lengthAfter);
        } else {
            //estimate the character what in front of input whether have span
            StyleSpan boldSpan = getStyleSpan(style, getEditableText(), start - 1, start);
            if (boldSpan == null) {
                setSpan(new StyleSpan(style), start, start + lengthAfter);
            } else {
                //if have span , change the span's effect scope
                changeCharacterStyleEnd(boldSpan, lengthAfter, getEditableText());
            }
        }
    }

    private void setTextSpan(CharacterStyle span, int start, int lengthAfter)
    {
        if (start == 0) {
            setSpan(span, start, start + lengthAfter);
        } else {
            CharacterStyle characterStyleSpan = getCharacterStyleSpan(span.getClass(), start - 1, start);
            if (characterStyleSpan == null) {
                setSpan(span, start, start + lengthAfter);
            } else {
                changeCharacterStyleEnd(characterStyleSpan, lengthAfter, getEditableText());
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
        StyleSpan[] spans = getEditableText().getSpans(start - 1, start, StyleSpan.class);
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
            if (isRangeInSpan(span, start, end)) {
                if (span.getStyle() == Typeface.BOLD) {
                    state.enableBold(true);
                } else {
                    state.enableItalic(true);
                }
            }
        }
        UnderlineSpan[] underLineSpans = getEditableText().getSpans(start, start, UnderlineSpan.class);
        if (underLineSpans.length != 0 && isRangeInSpan(underLineSpans[0], start, end)) {
            state.enableUnderLine(true);
        }
        StrikethroughSpan[] strikethroughSpan = getEditableText().getSpans(start, start, StrikethroughSpan.class);
        if (strikethroughSpan.length != 0 && isRangeInSpan(strikethroughSpan[0], start, end)) {
            state.enableStrikethrough(true);
        }
    }

    /**
     * estimate range whether in the span's start to end
     *
     * @param span  the span to estimate
     * @param start start of the range
     * @param end   end of the range
     * @return if in this bound return true else return false
     */
    private boolean isRangeInSpan(Object span, int start, int end)
    {
        return getEditableText().getSpanStart(span) <= start && getEditableText().getSpanEnd(span) >= end;
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
        return getEditableText().getSpanStart(span) >= start && getEditableText().getSpanEnd(span) <= end;
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
        setSelectionTextSpan(isBold, Typeface.BOLD, start, end);
    }

    private void setSelectionTextItalic(boolean isItalic, int start, int end)
    {
        setSelectionTextSpan(isItalic, Typeface.ITALIC, start, end);
    }

    private void setSelectionTextUnderLine(boolean isItalic, int start, int end)
    {
        setSelectionTextSpan(isItalic, new UnderlineSpan(), start, end);
    }

    private void setSelectionTextStrikeThrough(boolean isItalic, int start, int end)
    {
        setSelectionTextSpan(isItalic, new StrikethroughSpan(), start, end);
    }

    private void setSelectionTextSpan(boolean isValid, int style, int start, int end)
    {
        //merge span
        if (isValid) {
            StyleSpan[] spans = getStyleSpans(style, start, end);
            for (StyleSpan span : spans) {
                if (isSpanInRange(span, start, end)) {
                    getEditableText().removeSpan(span);
                }
            }
            int newStart = start;
            int newEnd = end;
            StyleSpan before = getStyleSpan(style, getEditableText(), start - 1, start);
            if (before != null) {
                newStart = getEditableText().getSpanStart(before);
                getEditableText().removeSpan(before);
            }
            StyleSpan after = getStyleSpan(style, getEditableText(), end, end + 1);
            if (after != null) {
                newEnd = getEditableText().getSpanEnd(after);
                getEditableText().removeSpan(after);
            }
            setSpan(new StyleSpan(style), newStart, newEnd);
        } else { // spilt span
            StyleSpan span = getStyleSpan(style, getEditableText(), start, end);
            int spanStart = getEditableText().getSpanStart(span);
            int spanEnd = getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(new StyleSpan(style), spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(new StyleSpan(style), end, spanEnd);
            }
            getEditableText().removeSpan(span);
        }
    }

    private void setSelectionTextSpan(boolean isBold, CharacterStyle cSpan, int start, int end)
    {
        //merge span
        if (isBold) {
            CharacterStyle[] spans = getCharacterStyles(cSpan.getClass(), start, end);
            for (CharacterStyle span : spans) {
                if (isSpanInRange(span, start, end)) {
                    getEditableText().removeSpan(span);
                }
            }
            int newStart = start;
            int newEnd = end;
            CharacterStyle before = getCharacterStyleSpan(cSpan.getClass(), start - 1, start);
            if (before != null) {
                newStart = getEditableText().getSpanStart(before);
                getEditableText().removeSpan(before);
            }
            CharacterStyle after = getCharacterStyleSpan(cSpan.getClass(), end, end + 1);
            if (after != null) {
                newEnd = getEditableText().getSpanEnd(after);
                getEditableText().removeSpan(after);
            }
            setSpan(cSpan, newStart, newEnd);
        } else { // spilt span
            CharacterStyle span = getCharacterStyleSpan(cSpan.getClass(), start, end);
            int spanStart = getEditableText().getSpanStart(span);
            int spanEnd = getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(cSpan, spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(cSpan, end, spanEnd);
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

    protected CharacterStyle getCharacterStyleSpan(Class<? extends CharacterStyle> clazz, int
            start, int end)
    {
        CharacterStyle[] spans = getEditableText().getSpans(start, end, CharacterStyle.class);
        for (CharacterStyle span : spans) {
            if (span.getClass().equals(clazz)) {
                return span;
            }
        }
        return null;
    }

    protected CharacterStyle[] getCharacterStyles(Class<? extends CharacterStyle> clazz, int start, int end)
    {
        CharacterStyle[] spans = getEditableText().getSpans(start, end, CharacterStyle.class);
        ArrayList<CharacterStyle> result = new ArrayList<>();
        for (CharacterStyle span : spans) {
            if (span.getClass().equals(clazz)) {
                result.add(span);
            }
        }
        return result.toArray(new CharacterStyle[result.size()]);
    }

    private void setSpan(Object span, int start, int end)
    {
        getEditableText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
