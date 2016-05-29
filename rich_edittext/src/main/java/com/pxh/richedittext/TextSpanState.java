package com.pxh.richedittext;

/**
 * Created by pxh on 2016/5/23.
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
        setSelection(isValid, 1, TextSpan.Bold);
    }

    public void enableItalic(boolean isValid)
    {
        setSelection(isValid, 2, TextSpan.Italic);
    }

    public void enableUnderLine(boolean isValid)
    {
        setSelection(isValid, 4, TextSpan.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        setSelection(isValid, 8, TextSpan.Strikethrough);
    }

    private void setSelection(boolean isValid, int spanValue, TextSpan type)
    {
        if (isValid)
            spanSelection |= spanValue;
        else
            spanSelection &= (Integer.MAX_VALUE ^ spanValue);
        if (spanChangeListener != null)
            spanChangeListener.OnTextSpanChanged(type, isValid);
    }

    public boolean isBoldEnable()
    {
        return (spanSelection & 1) != 0;
    }

    public boolean isItalicEnable()
    {
        return (spanSelection & 2) != 0;
    }

    public boolean isUnderLineEnable()
    {
        return (spanSelection & 4) != 0;
    }

    public boolean isStrikethroughEnable()
    {
        return (spanSelection & 8) != 0;
    }

    public void setSpanChangeListener(RichEditText.TextSpanChangeListener listener)
    {
        this.spanChangeListener = listener;
    }
}
