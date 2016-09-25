package com.pxh.adapter;

import com.pxh.richedittext.RichEditText;

/**
 * Created by pxh on 2016/9/25.
 */
public class BulletSpanAdapter extends SpanAdapter
{
    public BulletSpanAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {

    }

    @Override
    public boolean changeStatusBySelection(int start, int end)
    {
        return false;
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthAfter)
    {

    }

    @Override
    public int getSpanStatusCode()
    {
        return 0;
    }
}
