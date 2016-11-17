package com.pxh.adapter;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
import com.pxh.span.HolderSpan;
import com.pxh.span.RichQuoteSpan;

/**
 * Created by pxh on 2016/9/30.
 * QuoteSpan在当前行没有文字时也能够显示，主要用于充填能被QuoteSpan影响却不显示的空行
 * 无字时插入，换行时插入。一旦当前行有新的输入，则去掉占位符，当前行文字全部被取消，则插入占位符
 * 连续两次回车取消QuoteSpan影响
 */
public class QuoteSpanAdapter extends ParagraphAdapter {
//    RichQuoteSpan quoteSpan;
//
//    boolean flag = true;
//    boolean debug = false;
//
//    public QuoteSpanAdapter(RichEditText editor)
//    {
//        super(editor);
//    }
//
//    @Override
//    public void enableSpan(boolean isEnable, TextSpanStatus state, int code)
//    {
//        int start = editor.getSelectionStart();
//        int end = editor.getSelectionEnd();
//        if (end < start) {
//            start = start ^ end;
//            end = start ^ end;
//            start = start ^ end;
//        }
//        if (start < end) {
////            setSelectionTextQuote(isValid, start, end);
//        } else {
//            if (isEnable) {
//                int quoteStart = getParagraphStart(start);
//                int quoteEnd = getParagraphEnd(start);
//                //if there is just a single line,insert a replacement span
//                if (quoteStart == start &&
//                        (getEditableText().length() == quoteStart ||
//                                getEditableText().charAt(quoteStart) == '\n')) {
//                    insertHolderSpan(start);
//                } else {
//                    //else set whole paragraph by quote span
//                    setSpan(getDrawSpan(), quoteStart, quoteEnd);
//                    quoteSpan = null;
//                }
//            } else {
//                if (!removeHolderSpan(start)) {
//                    Object richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start, start);
//                    getEditableText().removeSpan(richQuoteSpan);
//                }
//            }
//        }
//        state.enableQuote(isEnable);
//    }
//
//    protected void insertHolderSpan(int start)
//    {
//        getEditableText().insert(start, "|");
//        getEditableText().setSpan(new HolderSpan(getDrawSpan()), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//    }
//
//    protected boolean removeHolderSpan(int start)
//    {
//        if (isHasHolderSpan(start)) {
//            Object replacementSpan = getAssignSpan(HolderSpan.class, start, start);
//            getEditableText().removeSpan(replacementSpan);
//            getEditableText().delete(start - 1, start);
//            return true;
//        }
//        return false;
//    }
//
//    protected LeadingMarginSpan getDrawSpan()
//    {
//        if (quoteSpan == null) {
//            quoteSpan = new RichQuoteSpan();
//        }
//        return quoteSpan;
//    }
//
//    @Override
//    public boolean changeStatusBySelection(int start, int end)
//    {
//        if (!flag) return false;
//        QuoteSpan quoteSpan = getAssignSpan(RichQuoteSpan.class, start - 1, start);
//        return quoteSpan != null && isRangeInSpan(quoteSpan, start, end) || isHasHolderSpan(start);
//    }
//
//    private boolean isHasHolderSpan(int start)
//    {
//        HolderSpan[] holderSpans = getAssignSpans(HolderSpan.class, start - 1, start);
//        for (HolderSpan span : holderSpans) {
//            if (span.getInnerSpan() instanceof RichQuoteSpan) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void changeSpanByTextChanged(int start,int lengthBefore, int lengthAfter)
//    {
//        //when the span ahead of input is HolderSpan, remove it, then decrease start
//        if (removeHolderSpan(start)) {
//            start--;
//        }
//        setTextSpanByTextChanged(RichQuoteSpan.class, start, lengthAfter);
//        //when input last character is CRLF, insert a HolderSpan
//        if (getEditableText().charAt(start + lengthAfter - 1) == '\n') {
//            insertHolderSpan(start);
//        }
//    }

    private boolean changeReplacement = false;
    private LeadingMarginSpan leadingMarginSpan;
    private int spanDeletedIndex = -1;

    public QuoteSpanAdapter(RichEditText editor) {
        super(editor);
    }

    @Override
    public void enableSpan(boolean isEnable, TextSpanStatus state, int code) {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        if (end < start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        state.enableQuote(isEnable);
        if (start < end) {
        } else {
            if (isEnable) {
                int quoteStart = getParagraphStart(start);
                int quoteEnd = getParagraphEnd(start);
                //if there is just a single line,insert a replacement span
                if (quoteStart == start &&
                        (getEditableText().length() == quoteStart ||
                                getEditableText().charAt(quoteStart) == '\n')) {
                    if (leadingMarginSpan == null) {
                        leadingMarginSpan = new RichQuoteSpan();
                    }
                    insertReplacement(quoteStart);
                } else {
                    //else set whole paragraph by quote span
                    setSelectionTextSpan(true, new RichQuoteSpan(), quoteStart, quoteEnd);
                }
            } else {
                RichQuoteSpan span = getAssignSpan(RichQuoteSpan.class, start, end);
                getEditableText().removeSpan(span);
                removeReplacementIfExist(start);
//                getEditableText().delete(start, start + 1);
            }
        }
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end) {

    }

    @Override
    public boolean changeStatusBySelectionChanged(int start, int end) {
        HolderSpan[] holderSpans = getAssignSpans(HolderSpan.class, start - 1, start);
        for (HolderSpan span : holderSpans) {
            if (span.getInnerSpan() instanceof RichQuoteSpan) {
                return true;
            }
        }
        RichQuoteSpan[] quoteSpans = getEditableText().getSpans(start - 1, start, RichQuoteSpan.class);
        return (quoteSpans.length != 0 && isRangeInSpan(quoteSpans[0], start, end));
    }

    private boolean isInHolderMode(int start) {
        if (start >= getEditableText().length()) {
            return false;
        }
        if (getEditableText().charAt(start) != '\n') {
            return false;
        }
        RichQuoteSpan[] quoteSpans = getEditableText().getSpans(start, start + 1, RichQuoteSpan.class);
        if (quoteSpans.length > 0) {
            int len = getEditableText().getSpanEnd(quoteSpans[0]) - getEditableText().getSpanStart(quoteSpans[0]);
            if (len == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        Log.v("tag", lengthBefore + ":" + lengthAfter);
        if (lengthBefore > lengthAfter) {//when text delete
            RichQuoteSpan richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start + lengthBefore - 1, start + lengthBefore + 1);
            if (spanDeletedIndex >= 0) {
                insertReplacement(spanDeletedIndex);
                spanDeletedIndex = -1;
            }
            if (richQuoteSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richQuoteSpan);
            int sEnd = getEditableText().getSpanEnd(richQuoteSpan);
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
            if (start + lengthAfter >= 1
                    && getEditableText().charAt(start + lengthAfter - 1) == '\n'
                    && editor.getSelectionStart() == getEditableText().length()
                    && !changeReplacement) {
                //while '\n' input and there only this '\n' at tail of text, add another '\n' after text to show quote effect
                editor.setEnableStatusChangeBySelection(false);
                getEditableText().append("\n");
                editor.setSelection(start + lengthAfter);
                editor.setEnableStatusChangeBySelection(true);
                lengthAfter++;
            }
        }

        setTextSpanByTextChanged(RichQuoteSpan.class, start, lengthAfter);
    }

    @Override
    public void changeSpanBeforeTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        if (lengthBefore > lengthAfter) {
            RichQuoteSpan richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start + lengthBefore - 1, start + lengthBefore + 1);
            if (richQuoteSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richQuoteSpan);
            Log.d("tag", "changeSpanBeforeTextChanged() called with: start = [" + start + "], lengthBefore = [" + lengthBefore + "], lengthAfter = [" + lengthAfter + "]" + sStart);
            if (sStart == start && lengthAfter == 0) {
                spanDeletedIndex = start;
            }
        }
    }

    @Override
    public int getSpanStatusCode() {
        return TextSpans.Quote;
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
