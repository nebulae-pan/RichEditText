package com.pxh.adapter;

import android.text.style.LeadingMarginSpan;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
import com.pxh.span.RichBulletSpan;

/**
 * Created by pxh on 2016/9/25.
 */
public class BulletSpanAdapter extends ParagraphAdapter
{
    public BulletSpanAdapter(RichEditText editor) {
        super(editor);
        leadingMarginSpan = new RichBulletSpan();
    }

    @Override
    public void enableSpan(boolean isEnable, TextSpanStatus state, int code) {
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        if (end < start) {
            start = start ^ end;
            end = start ^ end;
            start = start ^ end;
        }
        state.enableQuote(isEnable);
        if (start < end) {
            int quoteStart = getParagraphStart(start);
            int quoteEnd = getParagraphEnd(end);
            if (isEnable) {
                setSelectionTextSpan(true, new RichBulletSpan(), quoteStart, quoteEnd);
            } else {
                removeSpan(RichBulletSpan.class, quoteStart, quoteEnd);
            }
        } else {
            if (isEnable) {
                int quoteStart = getParagraphStart(start);
                int quoteEnd = getParagraphEnd(start);
                //if there is just a single line,insert a replacement span
                if (quoteStart == start &&
                        (getEditableText().length() == quoteStart ||
                                getEditableText().charAt(quoteStart) == '\n')) {
                    if (leadingMarginSpan == null) {
                        leadingMarginSpan = new RichBulletSpan();
                    }
                    insertReplacement(quoteStart);
                } else {
                    //else set whole paragraph by quote span
                    setSelectionTextSpan(true, new RichBulletSpan(), quoteStart, quoteEnd);
                }
            } else {
                RichBulletSpan span = getAssignSpan(RichBulletSpan.class, start, end);
                getEditableText().removeSpan(span);
                removeReplacementIfExist(start);
            }
        }
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end) {

    }

    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Bullet;
    }

    @Override
    protected Class<? extends LeadingMarginSpan> getSpanClass() {
        return RichBulletSpan.class;
    }
}
