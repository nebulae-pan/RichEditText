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
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.pxh.richparser.RichHtml;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pxh on 2016/4/13.
 * RichEditText extend AppCompatEditText
 */
public class RichEditText extends AppCompatEditText
{
    private static final String TAG = "RichEditText";

    /**
     * use bitmap creator get a bitmap
     */
    private BitmapCreator bitmapCreator;

    TextSpanState state = new TextSpanState();

    HashMap<Class<?>, ReplaceInfo> replaceMap = new HashMap<>();

    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(final Context context, AttributeSet attrs)
    {
        super(context, attrs);

        replaceMap.put(RichQuoteSpan.class, new ReplaceInfo(RichQuoteSpan.ReplaceQuoteSpan.class));
        replaceMap.put(RichBulletSpan.class, new ReplaceInfo(RichBulletSpan.ReplaceBulletSpan.class));

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

    /**
     * use local uri to insert a image
     *
     * @param uri image uri
     */
    public void insertImage(Uri uri)
    {
        String path = UriUtils.getValidPath(getContext(), uri);
        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);

        SpannableString ss = new SpannableString(path);

        //construct a Drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(getContext().getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, path, DynamicDrawableSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();

        getEditableText().insert(start, ss);//insert the imageSpan
        setSelection(start + ss.length());  //set selection start position
    }

    /**
     * insert a hyperlink and display by describe
     *
     * @param describe hyperlink display
     * @param url      url
     */
    public void insertUrl(String describe, String url)
    {
        SpannableString ss = new SpannableString(describe);
        ss.setSpan(new URLSpan(url), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(getSelectionStart(), ss);
    }

    /**
     * enable bold span
     *
     * @param isValid if enable is true
     */
    public void enableBold(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end)
            setSelectionTextBold(isValid, start, end);
        state.enableBold(isValid);
    }

    /**
     * enable italic span
     *
     * @param isValid if enable is true
     */
    public void enableItalic(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end)
            setSelectionTextItalic(isValid, start, end);
        state.enableItalic(isValid);
    }

    /**
     * enable UnderLine
     *
     * @param isValid if enable is true
     */
    public void enableUnderLine(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end)
            setSelectionTextUnderLine(isValid, start, end);
        state.enableUnderLine(isValid);
    }

    public void enableStrikethrough(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end)
            setSelectionTextStrikeThrough(isValid, start, end);
        state.enableStrikethrough(isValid);
    }

    public void enableQuote(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end) {
            setSelectionTextQuote(isValid, start, end);
        } else {// start == end
            if (isValid) {
                int quoteStart = getParagraphStart(start);
                int quoteEnd = getParagraphEnd(start);
                //if there is just a single line,insert a replacement span
                if (quoteStart == start &&
                        (getEditableText().length() == quoteStart ||
                                getEditableText().charAt(quoteStart) == '\n')) {
                    insertReplacementSpan(RichQuoteSpan.ReplaceQuoteSpan.class, start);
                } else {
                    //else set whole paragraph by quote span
                    setSpan(new RichQuoteSpan(), quoteStart, quoteEnd);
                }
            } else {
                if (start == replaceMap.get(RichQuoteSpan.class).position) {
                    removeReplacementSpan(RichQuoteSpan.ReplaceQuoteSpan.class, start);
                } else {
                    Object richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start, start);
                    getEditableText().removeSpan(richQuoteSpan);
                }
            }
        }
        state.enableQuote(isValid);
    }

    public void enableBullet(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end > start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end) {
            setSelectionTextBullet(isValid, start, end);
        } else {// start == end
            if (isValid) {
                int bulletStart = getParagraphStart(start);
                int bulletEnd = getParagraphEnd(start);
                //if there is just a single line,insert a replacement span
                if (bulletStart == start &&
                        (getEditableText().length() == bulletStart ||
                                getEditableText().charAt(bulletStart) == '\n')) {
                    insertReplacementSpan(RichBulletSpan.ReplaceBulletSpan.class, start);
                } else {
                    //else set whole paragraph by quote span
                    setSpan(new RichBulletSpan(), bulletStart, bulletEnd);
                }
            } else {
                if (start == replaceMap.get(RichBulletSpan.class).position) {
                    removeReplacementSpan(RichBulletSpan.ReplaceBulletSpan.class, start);
                } else {
                    Object richBulletSpan = getAssignSpan(RichBulletSpan.class, start, start);
                    getEditableText().removeSpan(richBulletSpan);
                }
            }
        }
        state.enableBullet(isValid);
    }

    public boolean isTextSpanEnable(TextSpanState.TextSpan textSpan)
    {
        return state.isTextSpanEnable(textSpan);
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

    public boolean isQuoteEnable()
    {
        return state.isQuoteEnable();
    }

    public boolean isBulletEnable()
    {
        return state.isBulletEnable();
    }

    public void setHtml(final String html)
    {
        Html.ImageGetter imgGetter = new RichEditorImageGetter(this);
        setText(RichHtml.fromHtml(html, imgGetter, null));
    }

    public String getHtml()
    {
        return RichHtml.toHtml(getText());
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
        changeSpanStateBySelection(selStart, selEnd);
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
            setTextSpan(UnderlineSpan.class, start, lengthAfter);
        }
        if (state.isStrikethroughEnable()) {
            setTextSpan(StrikethroughSpan.class, start, lengthAfter);
        }
        if (state.isQuoteEnable()) {
            onEnabledInput(RichQuoteSpan.class, text, start, lengthAfter);
//            setTextSpan(RichQuoteSpan.class, start, lengthAfter);
        }
        if (state.isBulletEnable()) {
            onEnabledInput(RichBulletSpan.class, text, start, lengthAfter);
        }
    }

    private void onEnabledInput(Class<?> clazz, CharSequence text, int start, int lengthAfter)
    {
        if (replaceMap.get(clazz).isNeedSet) {
            if (lengthAfter == 1 && text.charAt(start) == '\n') {
                if (!replaceMap.get(clazz).isEnterOnce) {
                    replaceMap.get(clazz).isEnterOnce = true;
                    insertReplacementSpan(replaceMap.get(clazz).clazz, start + 1);
                } else {
                    replaceMap.get(clazz).isEnterOnce = false;
                    removeReplacementSpan(replaceMap.get(clazz).clazz, start);
                    return;
                }
            } else {
                replaceMap.get(clazz).isEnterOnce = false;
            }
            if (start == replaceMap.get(clazz).position) {
                setTextSpanBySpanBeforeReplacement(clazz, start, lengthAfter, 7);
                removeReplacementSpan(replaceMap.get(clazz).clazz, start);
            } else {
                setTextSpan(clazz, start, lengthAfter);
            }
        }
    }

    /**
     * when characters input , set the text's span by styleSpan.
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
            StyleSpan styleSpan = getStyleSpan(style, getEditableText(), start - 1, start);
            if (styleSpan == null) {
                setSpan(new StyleSpan(style), start, start + lengthAfter);
            } else {
                //if have span , change the span's effect scope
                changeStyleEnd(styleSpan, lengthAfter, getEditableText());
            }
        }
    }

    /**
     * when characters input , set the text's span by characterStyle and ParagraphStyle.
     * Parameters start and lengthAfter must use the parameter of onTextChanged
     *
     * @param start       the start of character's input
     * @param lengthAfter the length of character's input
     */
    private void setTextSpan(Class<?> clazz, int start, int lengthAfter)
    {
        try {
            if (start == 0) {
                setSpan(clazz.newInstance(), start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1, start);
                if (preSpan == null) {
                    setSpan(clazz.newInstance(), start, start + lengthAfter);
                } else {
                    changeStyleEnd(preSpan, lengthAfter, getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    private void setTextSpanBySpanBeforeReplacement(Class<?> clazz, int start, int lengthAfter, int replacementLength)
    {
        try {
            if (start == 0) {
                setSpan(clazz.newInstance(), start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1 - replacementLength, start - replacementLength);
                if (preSpan == null) {
                    setSpan(clazz.newInstance(), start, start + lengthAfter);
                } else {
                    changeStyleEnd(preSpan, lengthAfter + replacementLength, getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    /**
     * change Span's state when selection change
     *
     * @param start the selection start after change
     */
    private void changeSpanStateBySelection(int start, int end)
    {
        state.clearSelection();
        StyleSpan[] spans;
        if (start != end) {
            spans = getEditableText().getSpans(start, end, StyleSpan.class);
        } else {
            spans = getEditableText().getSpans(start - 1, start, StyleSpan.class);
        }
        for (StyleSpan span : spans) {
            if (isRangeInSpan(span, start, end)) {
                if (span.getStyle() == Typeface.BOLD) {
                    state.enableBold(true);
                } else {
                    state.enableItalic(true);
                }
            }
        }
        UnderlineSpan[] underLineSpans = getEditableText().getSpans(start - 1, start, UnderlineSpan.class);
        if (underLineSpans.length != 0 && isRangeInSpan(underLineSpans[0], start, end)) {
            state.enableUnderLine(true);
        }
        StrikethroughSpan[] strikethroughSpans = getEditableText().getSpans(start - 1, start, StrikethroughSpan.class);
        if (strikethroughSpans.length != 0 && isRangeInSpan(strikethroughSpans[0], start, end)) {
            state.enableStrikethrough(true);
        }
        QuoteSpan[] quoteSpans = getEditableText().getSpans(start - 1, start, QuoteSpan.class);
        if (quoteSpans.length != 0 && isRangeInSpan(quoteSpans[0], start, end)) {
            state.enableQuote(true);
        }
        ReplacementSpan[] replacementSpan = getEditableText().getSpans(start - 1, start, RichQuoteSpan
                .ReplaceQuoteSpan.class);
        if (replacementSpan.length != 0 && isRangeInSpan(replacementSpan[0], start, end)) {
            state.enableQuote(true);
        }
        BulletSpan[] bulletSpans = getEditableText().getSpans(start - 1, start, BulletSpan.class);
        if (bulletSpans.length != 0 && isRangeInSpan(bulletSpans[0], start, end)) {
            state.enableBullet(true);
        }
        ReplacementSpan[] replaceBulletSpans = getEditableText().getSpans(start - 1, start, RichBulletSpan
                .ReplaceBulletSpan.class);
        if (replaceBulletSpans.length != 0 && isRangeInSpan(replaceBulletSpans[0], start, end)) {
            state.enableBullet(true);
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
     * @param span        span
     * @param lengthAfter the input character's increment
     * @param ss          use method EditText.getEditableText()
     */
    private void changeStyleEnd(Object span, int lengthAfter, Editable ss)
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

    private void setSelectionTextUnderLine(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new UnderlineSpan(), start, end);
    }

    private void setSelectionTextStrikeThrough(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new StrikethroughSpan(), start, end);
    }

    private void setSelectionTextQuote(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new RichQuoteSpan(), start, end);
    }

    private void setSelectionTextBullet(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new BulletSpan(), start, end);
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

    private void setSelectionTextSpan(boolean isValid, Object assignSpan, int start, int end)
    {
        if (isValid) {
            Object[] spans = getAssignSpans(assignSpan.getClass(), start, end);
            for (Object span : spans) {
                if (isSpanInRange(span, start, end)) {
                    getEditableText().removeSpan(span);
                }
            }
            int newStart = start;
            int newEnd = end;
            Object before = getAssignSpan(assignSpan.getClass(), start - 1, start);
            if (before != null) {
                newStart = getEditableText().getSpanStart(before);
                getEditableText().removeSpan(before);
            }
            Object after = getAssignSpan(assignSpan.getClass(), end, end + 1);
            if (after != null) {
                newEnd = getEditableText().getSpanEnd(after);
                getEditableText().removeSpan(after);
            }
            setSpan(assignSpan, newStart, newEnd);
        } else { // spilt span
            Object span = getAssignSpan(assignSpan.getClass(), start, end);
            int spanStart = getEditableText().getSpanStart(span);
            int spanEnd = getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(assignSpan, spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(assignSpan, end, spanEnd);
            }
            getEditableText().removeSpan(span);
        }
    }

    /**
     * get current paragraph's start
     *
     * @param selectionStart selectionStart
     * @return paragraph's start position
     */
    private int getParagraphStart(int selectionStart)
    {
        for (int i = selectionStart - 1; i > 0; i--) {
            if (getEditableText().charAt(i) == '\n') {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * exclude \n
     *
     * @param selectionEnd selectionEnd
     * @return paragraph's end position
     */
    private int getParagraphEnd(int selectionEnd)
    {
        for (int i = selectionEnd; i < getEditableText().length() - 1; i++) {
            if (getEditableText().charAt(i) == '\n') {
                return i;
            }
        }
        return getEditableText().length();
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

    protected Object[] getAssignSpans(Class<?> clazz, int start, int end)
    {
        return getEditableText().getSpans(start, end, clazz);
    }

    protected Object getAssignSpan(Class<?> clazz, int start, int end)
    {
        Object[] spans = getEditableText().getSpans(start, end, clazz);
        for (Object span : spans) {
            if (span.getClass().equals(clazz)) {
                return span;
            }
        }
        return null;
    }

    private void setSpan(Object span, int start, int end)
    {
        getEditableText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void insertReplacementSpan(Class<?> clazz, int start)
    {
        try {
            replaceMap.get(clazz).isNeedSet = true;
            String quoteReplacement = "Replace";
            getEditableText().insert(start, quoteReplacement);
            setSpan(clazz.newInstance(), start, start + quoteReplacement.length());
            replaceMap.get(clazz).position = start + quoteReplacement.length();
            // save paragraph parameters
            replaceMap.get(clazz).isNeedSet = true;
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    private void removeReplacementSpan(Class<?> clazz, int start)
    {
        Object replacementSpan = getAssignSpan(clazz, start, start);
        getEditableText().removeSpan(replacementSpan);
        getEditableText().delete(start - 7, start);
        replaceMap.get(clazz).position = -1;
    }

    static class ReplaceInfo
    {
        public ReplaceInfo(Class<?> clazz)
        {
            this.clazz = clazz;
        }

        int position = -1;
        boolean isNeedSet = true;
        boolean isEnterOnce = false;
        Class<?> clazz;
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
