package com.pxh.richedittext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

/**
 * Created by pxh on 2016/8/3.
 */
public class RichBulletQuote extends BulletSpan
{
    private final int mGapWidth;
    private final boolean mWantColor;
    private final int mColor;

    private static final int BULLET_RADIUS = 3;
    private static Path sBulletPath = null;
    public static final int STANDARD_GAP_WIDTH = 2;

    public RichBulletQuote()
    {
        mGapWidth = STANDARD_GAP_WIDTH;
        mWantColor = false;
        mColor = 0;
    }

    public RichBulletQuote(int gapWidth)
    {
        mGapWidth = gapWidth;
        mWantColor = false;
        mColor = 0;
    }

    public RichBulletQuote(int gapWidth, int color)
    {
        mGapWidth = gapWidth;
        mWantColor = true;
        mColor = color;
    }

    public RichBulletQuote(Parcel src)
    {
        mGapWidth = src.readInt();
        mWantColor = src.readInt() != 0;
        mColor = src.readInt();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags)
    {
        dest.writeInt(mGapWidth);
        dest.writeInt(mWantColor ? 1 : 0);
        dest.writeInt(mColor);
    }

    public int getLeadingMargin(boolean first)
    {
        return 2 * BULLET_RADIUS + mGapWidth;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l)
    {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldcolor = 0;

            if (mWantColor) {
                oldcolor = p.getColor();
                p.setColor(mColor);
            }

            p.setStyle(Paint.Style.FILL);

            c.drawCircle(x + dir * BULLET_RADIUS, (top + bottom) / 2.0f, BULLET_RADIUS, p);

            if (mWantColor) {
                p.setColor(oldcolor);
            }

            p.setStyle(style);
        }
    }
}
