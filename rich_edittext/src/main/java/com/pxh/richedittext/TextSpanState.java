package com.pxh.richedittext;

/**
 * Created by pxh on 2016/5/23.
 * the state of all TextSpans
 */
public class TextSpanState
{
    public enum TextSpan
    {
        Bold(1), Italic(2), UnderLine(4), Strikethrough(8), Quote(16), Bullet(32);

        int value;

        TextSpan(int value)
        {
            this.value = value;
        }
    }

    public TextSpanState()
    {
        spanSelection = 0;
    }

    private int spanSelection;

    RichEditText.TextSpanChangeListener spanChangeListener;

    public void enableBold(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.Bold);
    }

    public void enableItalic(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.Italic);
    }

    public void enableUnderLine(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.Strikethrough);
    }

    public void enableQuote(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.Quote);
    }

    public void enableBullet(boolean isValid)
    {
        setStateSelection(isValid, TextSpan.Bullet);
    }

    private void setStateSelection(boolean isValid, TextSpan type)
    {
        if (isValid == getStateSelection(type.value)) {
            return;
        }
        if (isValid)
            spanSelection |= type.value;
        else
            spanSelection &= (Integer.MAX_VALUE ^ type.value);
        if (spanChangeListener != null)
            spanChangeListener.OnTextSpanChanged(type, isValid);

    }

    public boolean isTextSpanEnable(TextSpan textSpan)
    {
        return getStateSelection(textSpan.value);
    }

    public boolean isBoldEnable()
    {
        return isTextSpanEnable(TextSpan.Bold);
    }

    public boolean isItalicEnable()
    {
        return isTextSpanEnable(TextSpan.Italic);
    }

    public boolean isUnderLineEnable()
    {
        return isTextSpanEnable(TextSpan.UnderLine);
    }

    public boolean isStrikethroughEnable()
    {
        return isTextSpanEnable(TextSpan.Strikethrough);
    }

    public boolean isQuoteEnable()
    {
        return isTextSpanEnable(TextSpan.Quote);
    }

    public boolean isBulletEnable()
    {
        return isTextSpanEnable(TextSpan.Bullet);
    }

    private boolean getStateSelection(int spanValue)
    {
        return (spanSelection & spanValue) != 0;
    }

    public void clearSelection()
    {
        if (spanChangeListener != null) {
            int i = 1;
            for (TextSpan type : TextSpan.values()) {
                if (getStateSelection(i))
                    spanChangeListener.OnTextSpanChanged(type, false);
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
