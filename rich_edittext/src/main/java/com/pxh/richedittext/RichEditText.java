package com.pxh.richedittext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

public class RichEditText extends AppCompatEditText
{
    private Context context;

    private BitmapCreator bitmapCreator;


    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(final Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        post(new Runnable()
        {
            @Override
            public void run()
            {
                DisplayMetrics metric = new DisplayMetrics();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metric);
                int maxWidth = RichEditText.this.getMeasuredWidth() - 2;
                int maxHeight = metric.heightPixels;
                bitmapCreator = new InsertBitmapCreator(maxWidth, maxHeight);
            }
        });

        this.addTextChangedListener(new TagTextWatcher());
    }

    public void insertImage(Uri uri)
    {
        String path = UriUtils.getValidPath(context, uri);
        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);

        SpannableString ss = new SpannableString(path);

        //construct a drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(context.getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, path, DynamicDrawableSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();
        getEditableText().insert(start, ss);
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }

//    public void insertImage(Uri uri)
//    {
//        String path = UriUtils.getValidPath(context, uri);
//        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);
//
//        SpannableString ss = new SpannableString(path);
//
//        ImageSpan span = new ImageSpan(context, bitmap);
//        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        int start = this.getSelectionStart();
//        imgArray.put(start + ss.length(), new ImageSite(start, start + ss.length(), ss.toString()));
//        Editable et = getEditableText();// 先获取EditText中的内容
//        et.insert(start, ss);// 设置ss要添加的位置
//        setText(et);// 把et添加到EditText中
//        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
//    }

//    /**
//     * replace a string start to end with s, and s replaced by image
//     *
//     * @param s      the string to be replaced by image
//     * @param start  start
//     * @param end    end
//     * @param bitmap insert bitmap
//     */
//    public void insertImageByReplace(String s, int start, int end, Bitmap bitmap)
//    {
//        SpannableString ss = new SpannableString(s);
//
//        ImageSpan span = new ImageSpan(context, bitmap);
//        ss.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        imgArray.put(end, new ImageSite(start, end, ss.toString()));
//        Editable et = getEditableText();// 先获取EditText中的内容
//        et.replace(start, end, ss);// 设置ss要添加的位置
//        setText(et);// 把et添加到EditText中
//        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
//    }


    public void setHtml(final String html)
    {
        Html.ImageGetter imgGetter = new RichEditorImageGetter(this);
        setText(Html.fromHtml(html, imgGetter, null));
    }

    public String getHtml()
    {
        return Html.toHtml(getText());
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
