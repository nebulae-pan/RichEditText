package com.pxh.adapter;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
import com.pxh.span.RichBulletSpan;

/**
 * Created by pxh on 2016/9/25.
 */
public class BulletSpanAdapter extends ParagraphAdapter
{
    boolean addedEnter = false;

    public BulletSpanAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    public void enableSpan(boolean isEnable, TextSpanStatus state, int code)
    {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        if (end < start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        state.enableBullet(isEnable);
        if (start < end) {
        } else {
            if (isEnable) {
                int quoteStart = getParagraphStart(start);
                int quoteEnd = getParagraphEnd(start);
                //if there is just a single line,insert a replacement span
                if (quoteStart == start &&
                        (getEditableText().length() == quoteStart ||
                                getEditableText().charAt(quoteStart) == '\n')) {
                    editor.append("\n");
                    editor.setSelection(start);

                } else {
                    //else set whole paragraph by quote span
                }
            } else {
            }
        }
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {

    }

    @Override
    public boolean changeStatusBySelection(int start, int end)
    {
        RichBulletSpan[] bulletSpans = getEditableText().getSpans(start - 1, start, RichBulletSpan.class);
        if (bulletSpans.length != 0 && isRangeInSpan(bulletSpans[0], start, end)) {
            return true;
        } else {
            return isInHolderMode(start);
        }
    }

    private boolean isInHolderMode(int start)
    {
//        Log.v("tag", start + "");
        if (start >= getEditableText().length()) {
            return false;
        }
        if (getEditableText().charAt(start) != '\n') {
            return false;
        }
        RichBulletSpan[] bulletSpans = getEditableText().getSpans(start, start + 1, RichBulletSpan.class);
        if (bulletSpans.length > 0) {
            int len = getEditableText().getSpanEnd(bulletSpans[0]) - getEditableText().getSpanStart
                    (bulletSpans[0]);
            if (len == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter)
    {
        if (addedEnter) {
            return;
        }
        if (start + lengthAfter >= 1
                && getEditableText().charAt(start + lengthAfter - 1) == '\n'
                && editor.getSelectionStart() == getEditableText().length()) {
            addedEnter = true;
            editor.setEnableStatusChangeBySelection(false);
            getEditableText().append("\n");
            editor.setSelection(start + lengthAfter);
            editor.setEnableStatusChangeBySelection(true);
        }
        if (addedEnter) {
            addedEnter = false;
            lengthAfter++;
        }
        setTextSpanByTextChanged(RichBulletSpan.class, start, lengthAfter);
    }


    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Bullet;
    }
}
