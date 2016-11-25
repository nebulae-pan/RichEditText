package com.pxh.adapter;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

import com.pxh.RichEditText;
import com.pxh.span.HolderSpan;
import com.pxh.span.RichQuoteSpan;

/**
 * Created by pxh on 2016/9/30.
 */
abstract public class ParagraphAdapter extends SpanAdapter {
    private static final String TAG = "ParagraphAdapter";

    protected boolean changeReplacement = false;
    protected LeadingMarginSpan leadingMarginSpan;
    /**
     * span index
     */
    protected int spanDeletedIndex = -1;


    public ParagraphAdapter(RichEditText editor) {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end) {

    }



    @Override
    public boolean changeStatusBySelectionChanged(int start, int end) {
        HolderSpan[] holderSpans = getAssignSpans(HolderSpan.class, start - 1, start);
        for (HolderSpan span : holderSpans) {
            if (span.getInnerSpan().getClass().equals(getSpanClass())) {
                return true;
            }
        }
        LeadingMarginSpan[] spans = getEditableText().getSpans(start - 1, start, getSpanClass());
        return (spans.length != 0 && isRangeInSpan(spans[0], start, end));
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter){
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        if (lengthBefore > lengthAfter) {//when text delete
            LeadingMarginSpan richSpan = getAssignSpan(getSpanClass(), start + lengthBefore - 1, start + lengthBefore + 1);
            if (spanDeletedIndex >= 0) {
                insertReplacement(spanDeletedIndex);
                spanDeletedIndex = -1;
            }
            if (richSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richSpan);
            int sEnd = getEditableText().getSpanEnd(richSpan);
            if (sEnd - sStart == 1) {
                if (getEditableText().subSequence(sStart, sEnd).toString().equals("\n")) {
                    changeReplacement = true;
                    getEditableText().delete(sStart, sEnd);
                    changeReplacement = false;
                }
            }
        } else {
            //text insert or replace old text
            if (removeReplacementIfExist(start)) {
                start--;
            }
            LeadingMarginSpan richSpan = getAssignSpan(getSpanClass(), start - 1, start);
            int sEnd = getEditableText().getSpanEnd(richSpan);
            if (start + lengthAfter >= 1
                    && getEditableText().charAt(start + lengthAfter - 1) == '\n'
                    && editor.getSelectionStart() == sEnd + lengthAfter
                    && !changeReplacement) {
                if (!(getEditableText().length() > sEnd + lengthAfter + 1
                        && getEditableText().charAt(sEnd + lengthAfter + 1) == '\n')) {
                    //while '\n' input and there only this '\n' at tail of text, add another '\n' after text to show quote effect
                    editor.setEnableStatusChangeBySelection(false);
                    getEditableText().insert(sEnd + lengthAfter, "\n");
                    editor.setSelection(start + lengthAfter);
                    editor.setEnableStatusChangeBySelection(true);
                }
                lengthAfter++;
            }
        }

        setTextSpanByTextChanged(getSpanClass(), start, lengthAfter);

        if (start > 0 && lengthAfter == 1
                && getEditableText().charAt(start) == '\n'
                && getEditableText().charAt(start - 1) == '\n') {
            LeadingMarginSpan richSpan = getAssignSpan(getSpanClass(), start - 1, start);
            int sStart = getEditableText().getSpanStart(richSpan);
            getEditableText().removeSpan(richSpan);
            getEditableText().setSpan(richSpan, sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

    }

    @Override
    public void changeSpanBeforeTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        if (lengthBefore > lengthAfter) {
            LeadingMarginSpan richSpan = getAssignSpan(getSpanClass() , start + lengthBefore - 1, start + lengthBefore + 1);
            if (richSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richSpan);
            if (sStart == start && lengthAfter == 0) {
                spanDeletedIndex = start;
            }
        }
    }

    @Override
    abstract public int getSpanStatusCode();

    abstract protected Class<? extends LeadingMarginSpan> getSpanClass();

    /**
     * get current paragraph's start
     *
     * @param selectionStart selectionStart
     * @return paragraph's start position
     */
    protected int getParagraphStart(int selectionStart) {
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
    protected int getParagraphEnd(int selectionEnd) {
        for (int i = selectionEnd; i < editor.getEditableText().length() - 1; i++) {
            if (editor.getEditableText().charAt(i) == '\n') {
                return i;
            }
        }
        return editor.getEditableText().length();
    }

    protected void setTextSpanBySpanBeforeReplacement(Class<?> clazz, int start, int lengthAfter, int replacementLength) {
        try {
            if (start == 0) {
                setSpan(clazz.newInstance(), start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1 - replacementLength, start - replacementLength);
                if (preSpan == null) {
                    setSpan(clazz.newInstance(), start, start + lengthAfter);
                } else {
                    changeSpanEnd(preSpan, lengthAfter + replacementLength, getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

    protected void insertReplacement(int start) {
        changeReplacement = true;
        HolderSpan holderSpan = new HolderSpan(leadingMarginSpan);
        SpannableStringBuilder sb = new SpannableStringBuilder("|");
        sb.setSpan(holderSpan, start, start + sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(start, sb);
        changeReplacement = false;

    }

    /**
     * this method will change text length
     *
     * @param end replacement's end
     * @return if true, text length sub 1,else text length not change
     */
    protected boolean removeReplacementIfExist(int end) {
        HolderSpan[] holderSpans = getAssignSpans(HolderSpan.class, end - 1, end);
        for (HolderSpan holderSpan : holderSpans) {
            if (holderSpan.getInnerSpan() instanceof RichQuoteSpan) {
                changeReplacement = true;
                getEditableText().removeSpan(holderSpan);
                getEditableText().delete(end - 1, end);
                changeReplacement = false;
                return true;
            }
        }
        return false;
    }
}
