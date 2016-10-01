package com.pxh.adapter;

import android.text.style.UnderlineSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpans;

/**
 * Created by pxh on 2016/9/25.
 */
public class UnderLineSpanAdapter extends SpanAdapter
{
    public UnderLineSpanAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {
        setSelectionTextSpan(isEnable, new UnderlineSpan(), start, end);
    }

    @Override
    public boolean changeStatusBySelection(int start, int end)
    {
        UnderlineSpan[] underLineSpans = getAssignSpans(UnderlineSpan.class, start - 1, start);
        return underLineSpans.length != 0 && isRangeInSpan(underLineSpans[0], start, end);
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter)
    {
        setTextSpanByTextChanged(UnderlineSpan.class, start, lengthAfter);
    }

    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.UnderLine;
    }
}
