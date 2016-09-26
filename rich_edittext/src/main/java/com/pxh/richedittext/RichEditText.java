package com.pxh.richedittext;

import android.content.Context;
import android.graphics.Bitmap;
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
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.pxh.adapter.BoldSpanAdapter;
import com.pxh.adapter.ItalicSpanAdapter;
import com.pxh.adapter.SpanAdapter;
import com.pxh.adapter.StrikethroughSpanAdapter;
import com.pxh.adapter.UnderLineSpanAdapter;
import com.pxh.richparser.RichHtml;
import com.pxh.span.RichBulletSpan;
import com.pxh.span.RichQuoteSpan;

import java.lang.reflect.Field;
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

    TextSpanStatus state = new TextSpanStatus();

    /**
     * leave room for the extension
     */
    SparseArray<SpanAdapter> adapters = new SparseArray<>(10);

    HashMap<Class<?>, ReplaceInfo> replaceMap = new HashMap<>();

    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(final Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initSpanAdapterArray();

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

    private void initSpanAdapterArray()
    {
        addSpanAdapterInternal(new BoldSpanAdapter(this));
        addSpanAdapterInternal(new ItalicSpanAdapter(this));
        addSpanAdapterInternal(new UnderLineSpanAdapter(this));
        addSpanAdapterInternal(new StrikethroughSpanAdapter(this));
    }

    private void addSpanAdapterInternal(SpanAdapter adapter)
    {
        adapters.put(adapter.getSpanStatusCode(), adapter);
    }

    public void addSpanAdapter(SpanAdapter adapter)
    {
        if (adapters.get(adapter.getSpanStatusCode()) == null) {
            throw new IllegalArgumentException(TAG + ":adapter code already exist");
        }
        int code = adapter.getSpanStatusCode();
        if ((code & (code - 1)) != 0) {
            throw new IllegalArgumentException(TAG + ":status code must be the integer power of 2");
        }
        addSpanAdapterInternal(adapter);
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
        enableSpan(isValid, TextSpans.Bold);
    }

    /**
     * enable italic span
     *
     * @param isValid if enable is true
     */
    public void enableItalic(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Italic);
    }

    /**
     * enable UnderLine
     *
     * @param isValid if enable is true
     */
    public void enableUnderLine(boolean isValid)
    {
        enableSpan(isValid, TextSpans.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Strikethrough);
    }

    /**
     * enable the span which is assigned by code, use for custom span
     *
     * @param isValid whether enable
     * @param code    span code, must be integer power of 2
     */
    public void enableSpan(boolean isValid, int code)
    {
        adapters.get(code).enableSpan(isValid, state, code);
    }

    public void enableQuote(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end < start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end) {
            setSelectionTextQuote(isValid, start, end);
//        } else {// start == end
//            if (isValid) {
//                int quoteStart = getParagraphStart(start);
//                int quoteEnd = getParagraphEnd(start);
//                //if there is just a single line,insert a replacement span
//                if (quoteStart == start &&
//                        (getEditableText().length() == quoteStart ||
//                                getEditableText().charAt(quoteStart) == '\n')) {
//                    insertReplacementSpan(RichQuoteSpan.class, start);
//                } else {
//                    //else set whole paragraph by quote span
//                    setSpan(new RichQuoteSpan(this), quoteStart, quoteEnd);
//                }
//            } else {
//                if (start == replaceMap.get(RichQuoteSpan.class).position) {
//                    removeReplacementSpan(RichQuoteSpan.class, start);
//                } else {
//                    Object richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start, start);
//                    getEditableText().removeSpan(richQuoteSpan);
//                }
//            }
        }
        state.enableQuote(isValid);
    }

    public void enableBullet(boolean isValid)
    {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (end < start) {
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
                    insertReplacementSpan(RichBulletSpan.class, start);
                } else {
                    //else set whole paragraph by quote span
                    setSpan(new RichBulletSpan(), bulletStart, bulletEnd);
                }
            } else {
                if (start == replaceMap.get(RichBulletSpan.class).position) {
                    removeReplacementSpan(RichBulletSpan.class, start);
                } else {
                    Object richBulletSpan = getAssignSpan(RichBulletSpan.class, start, start);
                    getEditableText().removeSpan(richBulletSpan);
                }
            }
        }
        state.enableBullet(isValid);
    }

    public boolean isTextSpanEnable(int textSpan)
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

    public void setHtml(String html)
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
//        Log.d(TAG, "onTextChanged() called with: " + "text = [" + text + "], start = [" + start + "], lengthBefore = " +
//                "[" + lengthBefore + "], lengthAfter = [" + lengthAfter + "]");
//        if (text.charAt(start + lengthAfter - 1) == '\n') {
//            Log.v("tag", text.getClass().toString());
//            Field field = null;
//            try {
//                field = text.getClass().getDeclaredField("mText");
//                field.setAccessible(true);
//                char[] mText = (char[]) field.get(text);
//                if (mText.length > text.length() + 1) {
//                    mText[text.length()] = '1';
//                }
//                field.set(text, mText);
//                Log.v("tag", mText.length + "" + new String(mText));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//        Log.v("line", getLayout().getLineForOffset(getSelectionStart()) + "");
//        Log.v("line", String.valueOf(getLayout() instanceof DynamicLayout));

        for (int i = 0; i < adapters.size(); i++) {
            int key = adapters.keyAt(i);
            if (state.isTextSpanEnable(key)) {
                adapters.get(key).changeSpanByTextChanged(start, lengthAfter);
            }
        }
        if (state.isQuoteEnable()) {
            //onEnabledInput(RichQuoteSpan.class, text, start, lengthAfter);
            setTextSpan(new RichQuoteSpan(this), start, lengthAfter);
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
                    insertReplacementSpan(clazz, start + 1);
                } else {
                    replaceMap.get(clazz).isEnterOnce = false;
                    removeReplacementSpan(clazz, start);
                    return;
                }
            } else {
                replaceMap.get(clazz).isEnterOnce = false;
            }
            if (start == replaceMap.get(clazz).position) {
                if (clazz.equals(RichBulletSpan.class)) {
                    setTextSpan(clazz, start, lengthAfter);
                } else {
                    setTextSpanBySpanBeforeReplacement(clazz, start, lengthAfter, 7);
                }
                removeReplacementSpan(clazz, start);
            } else {
                setTextSpan(clazz, start, lengthAfter);
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

    private void setTextSpan(Object span, int start, int lengthAfter)
    {
        if (start == 0) {
            setSpan(span, start, start + lengthAfter);
        } else {
            Object preSpan = getAssignSpan(span.getClass(), start - 1, start);
            if (preSpan == null) {
                setSpan(span, start, start + lengthAfter);
            } else {
                changeStyleEnd(preSpan, lengthAfter, getEditableText());
            }
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
        for (int i = 0; i < adapters.size(); i++) {
            int key = adapters.keyAt(i);
            state.setTextSpanEnable(key, adapters.get(key).changeStatusBySelection(start, end));
        }
        QuoteSpan[] quoteSpans = getEditableText().getSpans(start - 1, start, QuoteSpan.class);
        if (quoteSpans.length != 0 && isRangeInSpan(quoteSpans[0], start, end)) {
            state.enableQuote(true);
        }
//        ReplacementSpan[] replacementSpan = getEditableText().getSpans(start - 1, start, RichQuoteSpan
//                .ReplaceQuoteSpan.class);
//        if (replacementSpan.length != 0 && isRangeInSpan(replacementSpan[0], start, end)) {
//            state.enableQuote(true);
//        }
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

    private void setSelectionTextQuote(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new RichQuoteSpan(this), start, end);
    }

    private void setSelectionTextBullet(boolean isValid, int start, int end)
    {
        setSelectionTextSpan(isValid, new BulletSpan(), start, end);
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
            replaceMap.get(clazz).isNeedSet = false;
            String replacementString = "Replace";
            getEditableText().insert(start, replacementString);
            setSpan(replaceMap.get(clazz).clazz.newInstance(), start, start + replacementString.length());
            replaceMap.get(clazz).position = start + replacementString.length();
            // save paragraph parameters
            replaceMap.get(clazz).isNeedSet = true;
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    private void removeReplacementSpan(Class<?> clazz, int start)
    {
        Object replacementSpan = getAssignSpan(replaceMap.get(clazz).clazz, start, start);
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

    @Override
    final public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_ENTER && getEditableText().charAt(getEditableText().length() - 1) == '\n') {
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    public interface TextSpanChangeListener
    {
        /**
         * called when current text span changed
         *
         * @param type    span type
         * @param isValid is span Valid
         */
        void OnTextSpanChanged(int type, boolean isValid);
    }
}