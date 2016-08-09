package com.pxh.richedittext;

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

    TextView textView;

    public RichQuoteSpan(TextView tv)
    {
        this.textView = tv;
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
        Log.d("123", "c = [" + c + "], p = [" + p + "], x = [" + x + "], dir = ["
                + dir + "], top = [" + top + "], baseline = [" + baseline + "], bottom = [" + bottom + "], text = ["
                + text + "], start = [" + start + "], end = [" + end + "], first = [" + first + "], layout = [" +
                layout + "]");
        final long lineRange = getLineRangeForDraw(c);
        int firstLine = unpackRangeStartFromLong(lineRange);
        int lastLine = unpackRangeEndFromLong(lineRange);
        Log.v("first : last", firstLine + " : " + lastLine);


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

    public long getLineRangeForDraw(Canvas canvas)
    {
        int dtop, dbottom;

        synchronized (sTempRect) {
            if (!canvas.getClipBounds(sTempRect)) {
                // Negative range end used as a special flag
                return packRangeInLong(0, -1);
            }

            dtop = sTempRect.top;
            dbottom = sTempRect.bottom;
        }

        final int top = Math.max(dtop, 0);
        final int bottom = Math.min(textView.getLayout().getLineTop(textView.getLayout().getLineCount()), dbottom);

        if (top >= bottom) return packRangeInLong(0, -1);
        return packRangeInLong(textView.getLayout().getLineForVertical(top), textView.getLayout().getLineForVertical
                (bottom));
    }

    final Rect sTempRect = new Rect();

    public static long packRangeInLong(int start, int end)
    {
        return (((long) start) << 32) | end;
    }

    /**
     * Get the start value from a range packed in a long by {@link #packRangeInLong(int, int)}
     *
     * @hide
     * @see #unpackRangeEndFromLong(long)
     * @see #packRangeInLong(int, int)
     */
    public static int unpackRangeStartFromLong(long range)
    {
        return (int) (range >>> 32);
    }

    /**
     * Get the end value from a range packed in a long by {@link #packRangeInLong(int, int)}
     *
     * @hide
     * @see #unpackRangeStartFromLong(long)
     * @see #packRangeInLong(int, int)
     */
    public static int unpackRangeEndFromLong(long range)
    {
        return (int) (range & 0x00000000FFFFFFFFL);
    }
}
