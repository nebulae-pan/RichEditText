package com.pxh.richedittext;

/**
 * Created by pxh on 2016/5/23.
 */
public class TextSpanState
{
    private int spanSelection = 0;

    RichEditText.TextSpanChangeListener spanChangeListener;

    public void enableBold(boolean isValid)
    {
        setSelection(isValid, 1);
    }

    public void enableItalic(boolean isValid)
    {
        setSelection(isValid, 2);
    }

    public void enableUnderLine(boolean isValid)
    {
        setSelection(isValid, 4);
    }

    public void enableStrikethrough(boolean isValid)
    {
        setSelection(isValid, 8);
    }

    private void setSelection(boolean isValid, int spanValue)
    {
        if (isValid)
            spanSelection |= spanValue;
        else
            spanSelection &= (Integer.MAX_VALUE ^ spanValue);
        if (spanChangeListener != null)
            spanChangeListener.OnTextSpanChanged(this);
    }

    public boolean isBoldEnable()
    {
        return (spanSelection & 1) == 0;
    }

    public boolean isItalicEnable()
    {
        return (spanSelection & 2) == 0;
    }

    public boolean isUnderLineEnable()
    {
        return (spanSelection & 4) == 0;
    }

    public boolean isStrikethroughEnable()
    {
        return (spanSelection & 8) == 0;
    }

    public void setSpanChangeListener(RichEditText.TextSpanChangeListener listener)
    {
        this.spanChangeListener = listener;
    }
}
