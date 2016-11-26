package com.pxh.adapter;

import android.text.style.LeadingMarginSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpans;
import com.pxh.span.RichBulletSpan;

/**
 * Created by pxh on 2016/9/25.
 */
public class BulletSpanAdapter extends ParagraphAdapter
{
    public BulletSpanAdapter(RichEditText editor) {
        super(editor);
        leadingMarginSpan = new RichBulletSpan();
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end) {

    }

    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Bullet;
    }

    @Override
    protected Class<? extends LeadingMarginSpan> getSpanClass() {
        return RichBulletSpan.class;
    }
}
