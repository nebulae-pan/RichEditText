package com.pxh.adapter;

import android.text.style.LeadingMarginSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
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
    public int getSpanStatusCode() {
        return TextSpans.Quote;
    }

    @Override
    protected Class<? extends LeadingMarginSpan> getSpanClass() {
        return RichQuoteSpan.class;
    }
}
