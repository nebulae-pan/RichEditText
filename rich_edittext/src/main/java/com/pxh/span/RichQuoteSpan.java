package com.pxh.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.TextUtils;
import android.text.style.QuoteSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by pxh on 2016/7/26.
 */
public class RichQuoteSpan extends QuoteSpan
{
    private int quoteStripeWidth = 5;
    private int quoteGapWidth = 12;

    public RichQuoteSpan()
    {
    }

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
        c.drawRect(x + quoteGapWidth / 2, top, x + quoteGapWidth / 2 + dir * quoteStripeWidth, bottom, p);
        p.setStyle(style);
        p.setColor(color);
    }
}
