package com.pxh.adapter;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.pxh.richedittext.RichEditText;
import com.pxh.richedittext.TextSpanStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by pxh on 2016/9/25.
 */
public abstract class SpanAdapter
{
    private static final String TAG = "SpanAdapter";

    RichEditText editor;

    public SpanAdapter(RichEditText editor)
    {
        this.editor = editor;
    }

    protected abstract void setSelectionText(boolean isEnable, int start, int end);

    public abstract boolean changeStatusBySelection(int start, int end);

    public abstract void changeSpanByTextChanged(int start, int lengthAfter);

    public abstract int getSpanStatusCode();


    public void enableSpan(boolean isEnable, TextSpanStatus state, int code)
    {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        if (end < start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        if (start < end)
            setSelectionText(isEnable, start, end);
        state.setTextSpanEnable(code,isEnable);
    }

    /**
     * when characters input , set the text's span by styleSpan.
     * Parameters start and lengthAfter must use the parameter of onTextChanged
     *
     * @param start       the start of character's input
     * @param lengthAfter the length of character's input
     */
    protected void setTextSpanByTextChanged(int style, int start, int lengthAfter)
    {
        if (start == 0) {
            //if start = 0, text must doesn't have spans, use new StyleSpan
            setSpan(new StyleSpan(style), start, start + lengthAfter);
        } else {
            //estimate the character what in front of input whether have span
            StyleSpan styleSpan = getStyleSpan(style, start - 1, start);
            if (styleSpan == null) {
                setSpan(new StyleSpan(style), start, start + lengthAfter);
            } else {
                //if have span , change the span's effect scope
                changeSpanEnd(styleSpan, lengthAfter,editor.getEditableText());
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
    protected void setTextSpanByTextChanged(Class<?> clazz, int start, int lengthAfter)
    {
        try {
            if (start == 0) {
                setSpan(clazz.newInstance(), start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1, start);
                if (preSpan == null) {
                    setSpan(clazz.newInstance(), start, start + lengthAfter);
                } else {
                    changeSpanEnd(preSpan, lengthAfter, editor.getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    protected void setTextSpanByTextChanged(Object span, int start, int lengthAfter)
    {
        if (start == 0) {
            setSpan(span, start, start + lengthAfter);
        } else {
            Object preSpan = getAssignSpan(span.getClass(), start - 1, start);
            if (preSpan == null) {
                setSpan(span, start, start + lengthAfter);
            } else {
                changeSpanEnd(preSpan, lengthAfter, editor.getEditableText());
            }
        }
    }

    /**
     * use reflection to change span effect scope
     *
     * @param span        span
     * @param lengthAfter the input character's increment
     * @param ss          use method EditText.getEditableText()
     */
    protected void changeSpanEnd(Object span, int lengthAfter, Editable ss)
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

    /**
     * estimate range whether in the span's start to end
     *
     * @param span  the span to estimate
     * @param start start of the range
     * @param end   end of the range
     * @return if in this bound return true else return false
     */
    protected boolean isRangeInSpan(Object span, int start, int end)
    {
        return editor.getEditableText().getSpanStart(span) <= start && editor.getEditableText().getSpanEnd(span) >= end;
    }

    /**
     * estimate span whether in the editText limit by parameters start and end
     *
     * @param span  the span to estimate
     * @param start start of the text
     * @param end   end of the text
     * @return if in this bound return true else return false
     */
    protected boolean isSpanInRange(Object span, int start, int end)
    {
        return editor.getEditableText().getSpanStart(span) >= start && editor.getEditableText().getSpanEnd(span) <= end;
    }

    /**
     * make the text which selected enable or disable span. if enable, it will merge same and adjacent spans
     * @param isValid whether enable
     * @param style style span
     * @param start span start
     * @param end span end
     */
    protected void setSelectionTextSpan(boolean isValid, int style, int start, int end)
    {
        //merge span
        if (isValid) {
            StyleSpan[] spans = getStyleSpans(style, start, end);
            for (StyleSpan span : spans) {
                if (isSpanInRange(span, start, end)) {
                    editor.getEditableText().removeSpan(span);
                }
            }
            int newStart = start;
            int newEnd = end;
            StyleSpan before = getStyleSpan(style,  start - 1, start);
            if (before != null) {
                newStart = editor.getEditableText().getSpanStart(before);
                editor.getEditableText().removeSpan(before);
            }
            StyleSpan after = getStyleSpan(style, end, end + 1);
            if (after != null) {
                newEnd = editor.getEditableText().getSpanEnd(after);
                editor.getEditableText().removeSpan(after);
            }
            setSpan(new StyleSpan(style), newStart, newEnd);
        } else { // spilt span
            StyleSpan span = getStyleSpan(style, start, end);
            int spanStart = editor.getEditableText().getSpanStart(span);
            int spanEnd = editor.getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(new StyleSpan(style), spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(new StyleSpan(style), end, spanEnd);
            }
            editor.getEditableText().removeSpan(span);
        }
    }

    protected void setSelectionTextSpan(boolean isValid, Object assignSpan, int start, int end)
    {
        if (isValid) {
            Object[] spans = getAssignSpans(assignSpan.getClass(), start, end);
            for (Object span : spans) {
                if (isSpanInRange(span, start, end)) {
                    editor.getEditableText().removeSpan(span);
                }
            }
            int newStart = start;
            int newEnd = end;
            Object before = getAssignSpan(assignSpan.getClass(), start - 1, start);
            if (before != null) {
                newStart = editor.getEditableText().getSpanStart(before);
                editor.getEditableText().removeSpan(before);
            }
            Object after = getAssignSpan(assignSpan.getClass(), end, end + 1);
            if (after != null) {
                newEnd = editor.getEditableText().getSpanEnd(after);
                editor.getEditableText().removeSpan(after);
            }
            setSpan(assignSpan, newStart, newEnd);
        } else { // spilt span
            Object span = getAssignSpan(assignSpan.getClass(), start, end);
            int spanStart = editor.getEditableText().getSpanStart(span);
            int spanEnd = editor.getEditableText().getSpanEnd(span);
            if (spanStart < start) {
                setSpan(assignSpan, spanStart, start);
            }
            if (spanEnd > end) {
                setSpan(assignSpan, end, spanEnd);
            }
            editor.getEditableText().removeSpan(span);
        }
    }

    /**
     * get current paragraph's start
     *
     * @param selectionStart selectionStart
     * @return paragraph's start position
     */
    protected int getParagraphStart(int selectionStart)
    {
        for (int i = selectionStart - 1; i > 0; i--) {
            if (editor.getEditableText().charAt(i) == '\n') {
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
    protected int getParagraphEnd(int selectionEnd)
    {
        for (int i = selectionEnd; i < editor.getEditableText().length() - 1; i++) {
            if (editor.getEditableText().charAt(i) == '\n') {
                return i;
            }
        }
        return editor.getEditableText().length();
    }

    /**
     * get styleSpan by specified style from the editable text
     *
     * @param style    the specified style
     * @param start    start of editable
     * @param end      end of editable
     * @return if there has a StyleSpan which style is specified in start to end,return it,or return null
     */
    protected StyleSpan getStyleSpan(int style, int start, int end)
    {
        StyleSpan[] spans = editor.getEditableText().getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                return span;
            }
        }
        return null;
    }

    protected StyleSpan[] getStyleSpans(int style, int start, int end)
    {
        StyleSpan[] spans = editor.getEditableText().getSpans(start, end, StyleSpan.class);
        ArrayList<StyleSpan> result = new ArrayList<>();
        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                result.add(span);
            }
        }
        return result.toArray(new StyleSpan[result.size()]);
    }

    protected <T> T[] getAssignSpans(Class<T> clazz, int start, int end)
    {
        return editor.getEditableText().getSpans(start, end, clazz);
    }

    protected Object getAssignSpan(Class<?> clazz, int start, int end)
    {
        Object[] spans = editor.getEditableText().getSpans(start, end, clazz);
        for (Object span : spans) {
            if (span.getClass().equals(clazz)) {
                return span;
            }
        }
        return null;
    }

    protected void setSpan(Object span, int start, int end)
    {
        editor.getEditableText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
