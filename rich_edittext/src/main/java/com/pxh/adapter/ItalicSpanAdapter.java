package com.pxh.adapter;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpans;

/**
 * Created by pxh on 2016/9/25.
 */
public class ItalicSpanAdapter extends SpanAdapter
{

    public ItalicSpanAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {
        setSelectionTextSpan(isEnable, Typeface.ITALIC, start, end);

    }

    @Override
    public boolean changeStatusBySelectionChanged(int start, int end)
    {
        StyleSpan styleSpan = getStyleSpan(Typeface.ITALIC, start - 1, start);
        return isRangeInSpan(styleSpan, start, end);
    }

    @Override
    public void changeSpanByTextChanged(int start,int lengthBefore, int lengthAfter)
    {
        setTextSpanByTextChanged(Typeface.BOLD, start, lengthAfter);
    }

    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Italic;
    }
}
