package com.pxh.adapter;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.pxh.RichEditText;
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

    Field count;
    Field spans;
    Field ends;

    public SpanAdapter(RichEditText editor)
    {
        this.editor = editor;
    }

    protected abstract void setSelectionText(boolean isEnable, int start, int end);

    public abstract boolean changeStatusBySelectionChanged(int start, int end);

    public abstract void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter);

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
        state.setTextSpanEnable(code, isEnable);
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
        Object span;
        if (start == 0) {
            //if start = 0, text must doesn't have spans, use new StyleSpan
            span = new StyleSpan(style);
            setSpan(span, start, start + lengthAfter);
        } else {
            //estimate the character what in front of input whether have span
            StyleSpan styleSpan = getStyleSpan(style, start - 1, start);
            if (styleSpan == null) {
                span = new StyleSpan(style);
                setSpan(span, start, start + lengthAfter);
            } else {
                if (start < editor.getEditableText().getSpanEnd(styleSpan)) {
                    return;
                }
                //if have span , change the span's effect scope
                span = styleSpan;
                changeSpanEnd(styleSpan, lengthAfter, editor.getEditableText());
            }
        }
        //merge two span
        Object nextSpan = getAssignSpan(span.getClass(), start + lengthAfter, start + lengthAfter + 1);
        if (nextSpan != null) {
            int nextEnd = getEditableText().getSpanEnd(nextSpan);
            getEditableText().removeSpan(nextSpan);
            int curStart = getEditableText().getSpanStart(span);
            getEditableText().removeSpan(span);
            getEditableText().setSpan(span, curStart, nextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        Object span = null;
        try {
            if (start == 0) {
                span = clazz.newInstance();
                setSpan(span, start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1, start);
                if (preSpan == null) {
                    span = clazz.newInstance();
                    setSpan(span, start, start + lengthAfter);
                } else {
                    if (start < editor.getEditableText().getSpanEnd(preSpan)) {
                        return;
                    }
                    span = preSpan;
                    changeSpanEnd(preSpan, lengthAfter, editor.getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
        //merge two span
        if (span == null) return;
        Object nextSpan = getAssignSpan(span.getClass(), start + lengthAfter, start + lengthAfter + 1);
        if (nextSpan != span) {
            int nextEnd = getEditableText().getSpanEnd(nextSpan);
            if (nextEnd == -1) {
                return;
            }
            getEditableText().removeSpan(nextSpan);
            int curStart = getEditableText().getSpanStart(span);
            if (curStart == -1) {
                return;
            }
            getEditableText().removeSpan(span);
            getEditableText().setSpan(span, curStart, nextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.v("tag", getEditableText().getSpanStart(span) + ":" + getEditableText().getSpanEnd(span));
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
                if (start < editor.getEditableText().getSpanEnd(preSpan)) {
                    return;
                }
                span = preSpan;
                changeSpanEnd(preSpan, lengthAfter, editor.getEditableText());
            }
        }
        //merge two span
        Object nextSpan = getAssignSpan(span.getClass(), start + lengthAfter, start + lengthAfter + 1);
        if (nextSpan != null) {
            int nextEnd = getEditableText().getSpanEnd(nextSpan);
            getEditableText().removeSpan(nextSpan);
            int curStart = getEditableText().getSpanStart(span);
            getEditableText().removeSpan(span);
            getEditableText().setSpan(span, curStart, nextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            if (count == null) {
                Class<?> classType = ss.getClass();
                count = classType.getDeclaredField("mSpanCount");
                spans = classType.getDeclaredField("mSpans");
                ends = classType.getDeclaredField("mSpanEnds");
                count.setAccessible(true);
                spans.setAccessible(true);
                ends.setAccessible(true);
            }
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
        int sstart = getEditableText().getSpanStart(span);
        int send = getEditableText().getSpanEnd(span);
        boolean result = editor.getEditableText().getSpanStart(span) <= start && editor.getEditableText().getSpanEnd(span) >= end;
        return result;
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
     *
     * @param isValid whether enable
     * @param style   style span
     * @param start   span start
     * @param end     span end
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
            StyleSpan before = getStyleSpan(style, start - 1, start);
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

    protected Editable getEditableText()
    {
        return editor.getEditableText();
    }

    /**
     * get styleSpan by specified style from the editable text
     *
     * @param style the specified style
     * @param start start of editable
     * @param end   end of editable
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

    protected <T> T getAssignSpan(Class<T> clazz, int start, int end)
    {
        T[] spans = editor.getEditableText().getSpans(start, end, clazz);
        for (T span : spans) {
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
