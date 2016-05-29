package com.pxh.richedittext;

/**
 * Created by pxh on 2016/5/23.
 * the state of all TextSpans
 */
public class TextSpanState
{
    public enum TextSpan
    {
        Bold, Italic, UnderLine, Strikethrough, Quote, Bullet
    }

    public TextSpanState()
    {
        spanSelection = 0;
    }

    private int spanSelection;

    RichEditText.TextSpanChangeListener spanChangeListener;

    public void enableBold(boolean isValid)
    {
        setStateSelection(isValid, 1, TextSpan.Bold);
    }

    public void enableItalic(boolean isValid)
    {
        setStateSelection(isValid, 2, TextSpan.Italic);
    }

    public void enableUnderLine(boolean isValid)
    {
        setStateSelection(isValid, 4, TextSpan.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        setStateSelection(isValid, 8, TextSpan.Strikethrough);
    }

    private void setStateSelection(boolean isValid, int spanValue, TextSpan type)
    {
        if (isValid == getStateSelection(spanValue)) {
            return;
        }
        if (isValid)
            spanSelection |= spanValue;
        else
            spanSelection &= (Integer.MAX_VALUE ^ spanValue);
        if (spanChangeListener != null)
            spanChangeListener.OnTextSpanChanged(type, isValid);

    }

    public boolean isBoldEnable()
    {
        return getStateSelection(1);
    }

    public boolean isItalicEnable()
    {
        return getStateSelection(2);
    }

    public boolean isUnderLineEnable()
    {
        return getStateSelection(4);
    }

    public boolean isStrikethroughEnable()
    {
        return getStateSelection(8);
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
