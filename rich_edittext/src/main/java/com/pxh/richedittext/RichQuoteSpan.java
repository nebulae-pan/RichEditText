package com.pxh.richedittext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;
import android.text.style.ReplacementSpan;

/**
 * Created by pxh on 2016/7/26.
 */
public class RichQuoteSpan extends QuoteSpan
{
    private int quoteStripeWidth = 5;
    private int quoteGapWidth = 12;

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

    static public class ReplaceQuoteSpan extends ReplacementSpan
    {
        private int quoteStripeWidth = 5;
        private int quoteGapWidth = 12;

        public ReplaceQuoteSpan()
        {

        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
        {
            return quoteStripeWidth + quoteGapWidth;
        }

        @Override
        public void draw(Canvas c, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                         Paint p)
        {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xff000000);
            c.drawRect(x + quoteGapWidth / 2, top, x + quoteGapWidth / 2 + quoteStripeWidth, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }
    }
}
