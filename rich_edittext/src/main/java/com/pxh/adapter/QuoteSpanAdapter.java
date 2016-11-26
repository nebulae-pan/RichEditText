package com.pxh.adapter;

import android.text.style.LeadingMarginSpan;

import com.pxh.RichEditText;
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
