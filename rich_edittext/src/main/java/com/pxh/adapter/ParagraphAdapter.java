package com.pxh.adapter;

import android.util.Log;

import com.pxh.RichEditText;

/**
 * Created by pxh on 2016/9/30.
 */
abstract public class ParagraphAdapter extends SpanAdapter
{
    private static final String TAG = "ParagraphAdapter";

    public ParagraphAdapter(RichEditText editor)
    {
        super(editor);
    }

    @Override
    protected void setSelectionText(boolean isEnable, int start, int end)
    {

    }

    @Override
    abstract public boolean changeStatusBySelectionChanged(int start, int end);

    @Override
    abstract public void changeSpanByTextChanged(int start,int lengthBefore, int lengthAfter);

    @Override
    abstract public int getSpanStatusCode();

    /**
     * get current paragraph's start
     *
     * @param selectionStart selectionStart
     * @return paragraph's start position
     */
    protected int getParagraphStart(int selectionStart)
    {
        for (int i = selectionStart - 1; i > 0; i--) {
            if (editor.getEditableText().charAt(i) == '\n') {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * exclude \n
     *
     * @param selectionEnd selectionEnd
     * @return paragraph's end position
     */
    protected int getParagraphEnd(int selectionEnd)
    {
        for (int i = selectionEnd; i < editor.getEditableText().length() - 1; i++) {
            if (editor.getEditableText().charAt(i) == '\n') {
                return i;
            }
        }
        return editor.getEditableText().length();
    }

    protected void setTextSpanBySpanBeforeReplacement(Class<?> clazz, int start, int lengthAfter, int replacementLength)
    {
        try {
            if (start == 0) {
                setSpan(clazz.newInstance(), start, start + lengthAfter);
            } else {
                Object preSpan = getAssignSpan(clazz, start - 1 - replacementLength, start - replacementLength);
                if (preSpan == null) {
                    setSpan(clazz.newInstance(), start, start + lengthAfter);
                } else {
                    changeSpanEnd(preSpan, lengthAfter + replacementLength, getEditableText());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "can not instantiated " + clazz);
            e.printStackTrace();
        }
    }

//    private void onEnabledInput(Class<?> clazz, CharSequence text, int start, int lengthAfter)
//    {
//        if (replaceMap.get(clazz).isNeedSet) {
//            if (lengthAfter == 1 && text.charAt(start) == '\n') {
//                if (!replaceMap.get(clazz).isEnterOnce) {
//                    replaceMap.get(clazz).isEnterOnce = true;
//                    insertReplacementSpan(clazz, start + 1);
//                } else {
//                    replaceMap.get(clazz).isEnterOnce = false;
//                    removeReplacementSpan(clazz, start);
//                    return;
//                }
//            } else {
//                replaceMap.get(clazz).isEnterOnce = false;
//            }
//            if (start == replaceMap.get(clazz).position) {
//                if (clazz.equals(RichBulletSpan.class)) {
//                    setTextSpan(clazz, start, lengthAfter);
//                } else {
//                    setTextSpanBySpanBeforeReplacement(clazz, start, lengthAfter, 7);
//                }
//                removeReplacementSpan(clazz, start);
//            } else {
//                setTextSpan(clazz, start, lengthAfter);
//            }
//        }
//    }
}
