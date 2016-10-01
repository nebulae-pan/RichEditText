package com.pxh.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LeadingMarginSpan;
import android.text.style.ReplacementSpan;

/**
 * Created by pxh on 2016/9/29.
 * use leadingMarginSpan's draw code, as a place holder in line feed
 */
public class HolderSpan extends ReplacementSpan
{
    LeadingMarginSpan span;
    public HolderSpan(LeadingMarginSpan span)
    {
        this.span = span;
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
    {
        return span.getLeadingMargin(false);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint
            paint)
    {
        span.drawLeadingMargin(canvas, paint, (int) x, 1, top, 0, bottom, text, start, end, false, null);
    }

    public LeadingMarginSpan getInnerSpan()
    {
        return span;
    }
}
