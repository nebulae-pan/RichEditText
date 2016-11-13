package com.pxh.adapter;

import android.text.style.StrikethroughSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpans;

/**
 * Created by pxh on 2016/9/25.
 */
public class StrikethroughSpanAdapter extends SpanAdapter
{
    public StrikethroughSpanAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {
        setSelectionTextSpan(isEnable, new StrikethroughSpan(), start, end);
    }

    @Override
    public boolean changeStatusBySelectionChanged(int start, int end)
    {
        StrikethroughSpan[] strikethroughSpans = getAssignSpans(StrikethroughSpan.class,start - 1,start);
        return strikethroughSpans.length != 0 && isRangeInSpan(strikethroughSpans[0], start, end);
    }

    @Override
    public void changeSpanByTextChanged(int start,int lengthBefore, int lengthAfter)
    {
        setTextSpanByTextChanged(StrikethroughSpan.class, start, lengthAfter);
    }

    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Strikethrough;
    }
}
