package com.pxh.adapter;

import android.text.Spanned;

import com.pxh.RichEditText;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
import com.pxh.span.HolderSpan;
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
    public boolean changeStatusBySelectionChanged(int start, int end) {
        HolderSpan[] holderSpans = getAssignSpans(HolderSpan.class, start - 1, start);
        for (HolderSpan span : holderSpans) {
            if (span.getInnerSpan() instanceof RichBulletSpan) {
                return true;
            }
        }
        RichBulletSpan[] bulletSpans = getEditableText().getSpans(start - 1, start, RichBulletSpan.class);
        return (bulletSpans.length != 0 && isRangeInSpan(bulletSpans[0], start, end));
    }

    @Override
    public void changeSpanByTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        if (lengthBefore > lengthAfter) {//when text delete
            RichBulletSpan richBulletSpan = getAssignSpan(RichBulletSpan.class, start + lengthBefore - 1, start + lengthBefore + 1);
            if (spanDeletedIndex >= 0) {
                insertReplacement(spanDeletedIndex);
                spanDeletedIndex = -1;
            }
            if (richBulletSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richBulletSpan);
            int sEnd = getEditableText().getSpanEnd(richBulletSpan);
            if (sEnd - sStart == 1) {
                if (getEditableText().subSequence(sStart, sEnd).toString().equals("\n")) {
                    changeReplacement = true;
                    getEditableText().delete(sStart, sEnd);
                    changeReplacement = false;
                }
            }
        } else {
            //text insert or replace old text
            if (removeReplacementIfExist(start)) {
                start--;
            }
            RichBulletSpan richBulletSpan = getAssignSpan(RichBulletSpan.class, start - 1, start);
            int sEnd = getEditableText().getSpanEnd(richBulletSpan);
            if (start + lengthAfter >= 1
                    && getEditableText().charAt(start + lengthAfter - 1) == '\n'
                    && editor.getSelectionStart() == sEnd + lengthAfter
                    && !changeReplacement) {
                if (!(getEditableText().length() > sEnd + lengthAfter + 1
                        && getEditableText().charAt(sEnd + lengthAfter + 1) == '\n')) {
                    //while '\n' input and there only this '\n' at tail of text, add another '\n' after text to show quote effect
                    editor.setEnableStatusChangeBySelection(false);
                    getEditableText().insert(sEnd + lengthAfter, "\n");
                    editor.setSelection(start + lengthAfter);
                    editor.setEnableStatusChangeBySelection(true);
                }
                lengthAfter++;
            }
        }

        setTextSpanByTextChanged(RichBulletSpan.class, start, lengthAfter);

        if (start > 0 && lengthAfter == 1
                && getEditableText().charAt(start) == '\n'
                && getEditableText().charAt(start - 1) == '\n') {
            RichBulletSpan richBulletSpan = getAssignSpan(RichBulletSpan.class, start - 1, start);
            int sStart = getEditableText().getSpanStart(richBulletSpan);
            getEditableText().removeSpan(richBulletSpan);
            getEditableText().setSpan(richBulletSpan, sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public void changeSpanBeforeTextChanged(int start, int lengthBefore, int lengthAfter) {
        if (changeReplacement) {//add replacement will not change span
            return;
        }
        if (lengthBefore > lengthAfter) {
            RichBulletSpan richBulletSpan = getAssignSpan(RichBulletSpan.class, start + lengthBefore - 1, start + lengthBefore + 1);
            if (richBulletSpan == null) {
                return;
            }
            int sStart = getEditableText().getSpanStart(richBulletSpan);
            if (sStart == start && lengthAfter == 0) {
                spanDeletedIndex = start;
            }
        }
    }


    @Override
    public int getSpanStatusCode()
    {
        return TextSpans.Bullet;
    }
}
