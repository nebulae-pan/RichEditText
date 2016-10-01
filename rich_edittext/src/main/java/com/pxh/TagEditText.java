package com.pxh;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import com.pxh.RichEditText;

/**
 * Created by pxh on 2016/5/7.
 */
public class TagEditText extends RichEditText
{
    public TagEditText(Context context)
    {
        this(context, null);
    }

    public TagEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.addTextChangedListener(new TagTextWatcher());

    }

    /**
     * replace a string start to end with s, and s replaced by image
     *
     * @param s      the string to be replaced by image
     * @param start  start
     * @param end    end
     * @param bitmap insert bitmap
     */
    public void insertImageByReplace(String s, int start, int end, Bitmap bitmap)
    {
        SpannableString ss = new SpannableString(s);

        ImageSpan span = new ImageSpan(getContext(), bitmap);
        ss.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Editable et = getEditableText();// 先获取EditText中的内容
        et.replace(start, end, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

    class TagTextWatcher implements TextWatcher
    {
        boolean isNeedRefresh = true;
        int preLength = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (!isNeedRefresh)
                return;
            if (s.length() > preLength) {
                char input = s.charAt(start);
                if (input == ',' || input == '，') {
                    int curr = getSelectionStart() - 1;
                    int pre = getNearestPosition(s.toString(), curr);
                    String tag = s.subSequence(pre, curr).toString();
                    isNeedRefresh = false;
//                    insertImageByReplace(tag + ",", pre, curr + 1, bitmapCreator.getBitmapByString(tag,
//                            0xff000000, 0xffffffff));
                    isNeedRefresh = true;
                }
                preLength = s.length();
            } else {
                preLength = s.length();
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {


        }

        int getNearestPosition(String s, int position)
        {
            int p = 0;
            int start = -1;
            do {
                start = s.indexOf(',', start + 1);
                if (start < position)
                    p = start + 1;
                else
                    break;
            } while (start != -1);
            return p;
        }
    }
}
