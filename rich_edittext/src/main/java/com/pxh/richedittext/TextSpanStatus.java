package com.pxh.richedittext;

import com.pxh.RichEditText;

/**
 * Created by pxh on 2016/5/23.
 * status handler
 */
public class TextSpanStatus
{
    public TextSpanStatus()
    {
        spanSelection = 0;
    }

    private int spanSelection;

    RichEditText.TextSpanChangeListener spanChangeListener;

    public void enableBold(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.Bold);
    }

    public void enableItalic(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.Italic);
    }

    public void enableUnderLine(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.Strikethrough);
    }

    public void enableQuote(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.Quote);
    }

    public void enableBullet(boolean isValid)
    {
        setStateSelection(isValid, TextSpans.Bullet);
    }

    private void setStateSelection(boolean isValid, int type)
    {
        if (isValid == isStateSelection(type)) {
            return;
        }
        if (isValid)
            spanSelection |= type;
        else
            spanSelection &= (Integer.MAX_VALUE ^ type);
        if (spanChangeListener != null)
            spanChangeListener.onTextSpanChanged(type, isValid);

    }

    public void setTextSpanEnable(int textSpan, boolean isEnable)
    {
        setStateSelection(isEnable, textSpan);
    }


    public boolean isTextSpanEnable(int textSpan)
    {
        return isStateSelection(textSpan);
    }

    public boolean isBoldEnable()
    {
        return isTextSpanEnable(TextSpans.Bold);
    }

    public boolean isItalicEnable()
    {
        return isTextSpanEnable(TextSpans.Italic);
    }

    public boolean isUnderLineEnable()
    {
        return isTextSpanEnable(TextSpans.UnderLine);
    }

    public boolean isStrikethroughEnable()
    {
        return isTextSpanEnable(TextSpans.Strikethrough);
    }

    public boolean isQuoteEnable()
    {
        return isTextSpanEnable(TextSpans.Quote);
    }

    public boolean isBulletEnable()
    {
        return isTextSpanEnable(TextSpans.Bullet);
    }

    private boolean isStateSelection(int spanValue)
    {
        return (spanSelection & spanValue) != 0;
    }

    public void clearSelection()
    {
        if (spanChangeListener != null) {
            int i = 1;
            while (i <= spanSelection) {
                if (isStateSelection(i))
                    spanChangeListener.onTextSpanChanged(i, false);
                i <<= 1;
            }
        }
        spanSelection = 0;
    }

    public void setSpanChangeListener(RichEditText.TextSpanChangeListener listener)
    {
        this.spanChangeListener = listener;
    }
}
