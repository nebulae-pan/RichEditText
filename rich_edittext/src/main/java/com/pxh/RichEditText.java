package com.pxh;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

import com.pxh.adapter.BoldSpanAdapter;
import com.pxh.adapter.BulletSpanAdapter;
import com.pxh.adapter.ItalicSpanAdapter;
import com.pxh.adapter.QuoteSpanAdapter;
import com.pxh.adapter.SpanAdapter;
import com.pxh.adapter.StrikethroughSpanAdapter;
import com.pxh.adapter.UnderLineSpanAdapter;
import com.pxh.richedittext.BitmapCreator;
import com.pxh.richedittext.InsertBitmapCreator;
import com.pxh.richedittext.RichEditorImageGetter;
import com.pxh.richedittext.TextSpanStatus;
import com.pxh.richedittext.TextSpans;
import com.pxh.richedittext.UriUtils;
import com.pxh.richparser.RichHtml;

/**
 * Created by pxh on 2016/4/13.
 * RichEditText extend AppCompatEditText
 */
public class RichEditText extends AppCompatEditText
{
    private static final String TAG = "RichEditText";

    /**
     * use bitmap creator get a bitmap
     */
    private BitmapCreator bitmapCreator;

    TextSpanStatus state = new TextSpanStatus();

    /**
     * leave room for the extension
     */
    SparseArray<SpanAdapter> adapters = new SparseArray<>(10);

    boolean needToSetStatus = true;

    public RichEditText(Context context)
    {
        this(context, null);
    }

    public RichEditText(final Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initSpanAdapterArray();

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
    }

    private void initSpanAdapterArray()
    {
        addSpanAdapterInternal(new BoldSpanAdapter(this));
        addSpanAdapterInternal(new ItalicSpanAdapter(this));
        addSpanAdapterInternal(new UnderLineSpanAdapter(this));
        addSpanAdapterInternal(new StrikethroughSpanAdapter(this));
        addSpanAdapterInternal(new QuoteSpanAdapter(this));
        addSpanAdapterInternal(new BulletSpanAdapter(this));
    }

    private RichEditText addSpanAdapterInternal(SpanAdapter adapter)
    {
        adapters.put(adapter.getSpanStatusCode(), adapter);
        return this;
    }

    public RichEditText addSpanAdapter(SpanAdapter adapter)
    {
        if (adapters.get(adapter.getSpanStatusCode()) == null) {
            throw new IllegalArgumentException(TAG + ":adapter code already exist");
        }
        int code = adapter.getSpanStatusCode();
        if ((code & (code - 1)) != 0) {
            throw new IllegalArgumentException(TAG + ":status code must be the integer power of 2");
        }
        return addSpanAdapterInternal(adapter);
    }

    /**
     * use local uri to insert a image
     *
     * @param uri image uri
     */
    public void insertImage(Uri uri)
    {
        String path = UriUtils.getValidPath(getContext(), uri);
        Bitmap bitmap = bitmapCreator.getBitmapByDiskPath(path);

        SpannableString ss = new SpannableString(path);

        //construct a Drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(getContext().getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, path, DynamicDrawableSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();

        getEditableText().insert(start, ss);//insert the imageSpan
        setSelection(start + ss.length());  //set selection start position
    }

    /**
     * insert a hyperlink and display by describe text
     *
     * @param describe hyperlink display text
     * @param url      url
     */
    public void insertUrl(String describe, String url)
    {
        SpannableString ss = new SpannableString(describe);
        ss.setSpan(new URLSpan(url), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getEditableText().insert(getSelectionStart(), ss);
    }

    /**
     * enable bold span
     *
     * @param isValid if enable is true
     */
    public void enableBold(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Bold);
    }

    /**
     * enable italic span
     *
     * @param isValid if enable is true
     */
    public void enableItalic(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Italic);
    }

    /**
     * enable UnderLine
     *
     * @param isValid if enable is true
     */
    public void enableUnderLine(boolean isValid)
    {
        enableSpan(isValid, TextSpans.UnderLine);
    }

    public void enableStrikethrough(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Strikethrough);
    }

    /**
     * enable the span which is assigned by code, use for custom span
     *
     * @param isValid whether enable
     * @param code    span code, must be integer power of 2
     */
    public void enableSpan(boolean isValid, int code)
    {
        adapters.get(code).enableSpan(isValid, state, code);
    }

    public void enableQuote(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Quote);
    }

    public void enableBullet(boolean isValid)
    {
        enableSpan(isValid, TextSpans.Bullet);
    }

    public boolean isTextSpanEnable(int textSpan)
    {
        return state.isTextSpanEnable(textSpan);
    }

    public boolean isBoldEnable()
    {
        return state.isBoldEnable();
    }

    public boolean isUnderLineEnable()
    {
        return state.isUnderLineEnable();
    }

    public boolean isItalicEnable()
    {
        return state.isItalicEnable();
    }

    public boolean isStrikethroughEnable()
    {
        return state.isStrikethroughEnable();
    }

    public boolean isQuoteEnable()
    {
        return state.isQuoteEnable();
    }

    public boolean isBulletEnable()
    {
        return state.isBulletEnable();
    }

    public void setHtml(String html)
    {
        Html.ImageGetter imgGetter = new RichEditorImageGetter(this);
        setText(RichHtml.fromHtml(html, imgGetter, null));
    }

    public String getHtml()
    {
        return RichHtml.toHtml(getText());
    }

    public void setSpanChangeListener(TextSpanChangeListener listener)
    {
        state.setSpanChangeListener(listener);
    }

    public void setEnableStatusChangeBySelection(boolean isEnable)
    {
        needToSetStatus = isEnable;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selStart, selEnd);
        if (state == null) {
            return;
        }
        Log.d(TAG, "onSelectionChanged() called with: " + "selStart = [" + selStart + "], selEnd = [" + selEnd + "]");
        if (!needToSetStatus) return;
        state.clearSelection();
        for (int i = 0; i < adapters.size(); i++) {
            int key = adapters.keyAt(i);
            state.setTextSpanEnable(key, adapters.get(key).changeStatusBySelection(selStart, selEnd));
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
        if (state == null) {
            return;
        }
        Log.d(TAG, "onTextChanged() called with: " + "text = [" + text + "], start = [" + start + "], lengthBefore = " +
                "[" + lengthBefore + "], lengthAfter = [" + lengthAfter + "]");
        if (!needToSetStatus) return;
        for (int i = 0; i < adapters.size(); i++) {
            int key = adapters.keyAt(i);
            if (state.isTextSpanEnable(key)) {
                adapters.get(key).changeSpanByTextChanged(start, lengthBefore, lengthAfter);
            }
        }
    }

    public interface TextSpanChangeListener
    {
        /**
         * called when current text span changed
         *
         * @param type    span type
         * @param isValid is span Valid
         */
        void OnTextSpanChanged(int type, boolean isValid);
    }
}