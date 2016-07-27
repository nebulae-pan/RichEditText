package com.pxh.richedittext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;

/**
 * Created by pxh on 2016/7/26.
 */
public class RichQuoteSpan extends QuoteSpan
{
    private int quoteStripeWidth = 5;
    private int quoteGapWidth = 20;

    @Override
    public int getLeadingMargin(boolean first)
    {
        return quoteStripeWidth + quoteGapWidth;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout)
    {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xff000000);
        c.drawRect(x, top, x + dir * quoteStripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }

}
