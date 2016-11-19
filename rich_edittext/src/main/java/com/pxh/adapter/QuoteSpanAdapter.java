package com.pxh.adapter;

import android.text.Spanned;
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

    public QuoteSpanAdapter(RichEditText editor) {
        super(editor);
        leadingMarginSpan = new RichQuoteSpan();
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
            int quoteStart = getParagraphStart(start);
            int quoteEnd = getParagraphEnd(end);
            if (isEnable) {
                setSelectionTextSpan(true, new RichQuoteSpan(), quoteStart, quoteEnd);
            } else {
                removeSpan(RichQuoteSpan.class, quoteStart, quoteEnd);
            }
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

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
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
            RichQuoteSpan richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start - 1, start);
            int sEnd = getEditableText().getSpanEnd(richQuoteSpan);
            Log.v("tag", sEnd + ":");
            if (start + lengthAfter >= 1
                    && getEditableText().charAt(start + lengthAfter - 1) == '\n'
                    && editor.getSelectionStart() == sEnd + lengthAfter
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

        if (start > 0 && lengthAfter == 1
                && getEditableText().charAt(start) == '\n'
                && getEditableText().charAt(start - 1) == '\n') {
            RichQuoteSpan richQuoteSpan = getAssignSpan(RichQuoteSpan.class, start - 1, start);
            int sStart = getEditableText().getSpanStart(richQuoteSpan);
            getEditableText().removeSpan(richQuoteSpan);
            getEditableText().setSpan(richQuoteSpan, sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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
            if (sStart == start && lengthAfter == 0) {
                spanDeletedIndex = start;
            }
        }
    }

    @Override
    public int getSpanStatusCode() {
        return TextSpans.Quote;
    }
}
