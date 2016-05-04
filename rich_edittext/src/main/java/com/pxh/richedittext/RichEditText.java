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

    SparseArray<ImageSite> imgArray = new SparseArray<>();

    /**
     * Description:用于存储插入editText的图片信息:起止位置，路径信息 <br/>
     * <p/>
     * CodeTime:2015年9月20日下午7:31:39
     *
     * @author pxh
     */
    public static class ImageSite
    {
        public int start;
        public int end;
        public String path;

        public ImageSite(int start, int end, String path)
        {
            this.start = start;
            this.end = end;
            this.path = path;
        }

        @Override
        public String toString()
        {
            return "ImageSite{" +
                    "start=" + start +
                    ", end=" + end +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

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

        String tempUri = "<img src=\"" + path + "\"/>";
        SpannableString ss = new SpannableString(tempUri);

        //construct a drawable and set Bounds
        Drawable mDrawable = new BitmapDrawable(context.getResources(), bitmap);
        int width = mDrawable.getIntrinsicWidth();
        int height = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);

        ImageSpan span = new ImageSpan(mDrawable, tempUri, DynamicDrawableSpan.ALIGN_BASELINE);
        ss.setSpan(span, 0, tempUri.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int start = this.getSelectionStart();
        Editable et = getEditableText();// 先获取EditText中的内容
        et.insert(start, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
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

        ImageSpan span = new ImageSpan(context, bitmap);
        ss.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        imgArray.put(end, new ImageSite(start, end, ss.toString()));
        Editable et = getEditableText();// 先获取EditText中的内容
        et.replace(start, end, ss);// 设置ss要添加的位置
        setText(et);// 把et添加到EditText中
        setSelection(start + ss.length());// 设置EditText中光标在最后面显示
    }


    public void setHtml(final String html)
    {
        Html.ImageGetter imgGetter = new RichEditorImageGetter(this);
        setText(Html.fromHtml(html, imgGetter, null));
    }

    public String getHtml()
    {
        Log.v("taga", getText().toString());

        StringBuilder out = new StringBuilder();
        withinHtml(out, getText());
//        Log.v("tag",out.toString());
        return out.toString();
//        return Html.toHtml(getText());
    }

    private static void withinHtml(StringBuilder out, Spanned text)
    {
        int len = text.length();

        int next;
        for (int i = 0; i < text.length(); i = next) {
            next = text.nextSpanTransition(i, len, ParagraphStyle.class);
            ParagraphStyle[] style = text.getSpans(i, next, ParagraphStyle.class);
            String elements = " ";
            boolean needDiv = false;

            for (int j = 0; j < style.length; j++) {
                if (style[j] instanceof AlignmentSpan) {
                    Layout.Alignment align =
                            ((AlignmentSpan) style[j]).getAlignment();
                    needDiv = true;
                    if (align == Layout.Alignment.ALIGN_CENTER) {
                        elements = "align=\"center\" " + elements;
                    } else if (align == Layout.Alignment.ALIGN_OPPOSITE) {
                        elements = "align=\"right\" " + elements;
                    } else {
                        elements = "align=\"left\" " + elements;
                    }
                }
            }
            if (needDiv) {
                out.append("<div ").append(elements).append(">");
            }

            withinDiv(out, text, i, next);

            if (needDiv) {
                out.append("</div>");
            }
        }
    }

    private static void withinDiv(StringBuilder out, Spanned text,
                                  int start, int end)
    {
        int next;
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, QuoteSpan.class);
            QuoteSpan[] quotes = text.getSpans(i, next, QuoteSpan.class);

            for (QuoteSpan quote : quotes) {
                out.append("<blockquote>");
            }

            withinBlockquote(out, text, i, next);

            for (QuoteSpan quote : quotes) {
                out.append("</blockquote>\n");
            }
        }
    }

    private static String getOpenParaTagWithDirection(Spanned text, int start, int end)
    {
        return "<p dir=\"ltr\">";
    }

    private static void withinBlockquote(StringBuilder out, Spanned text,
                                         int start, int end)
    {
        out.append(getOpenParaTagWithDirection(text, start, end));

        int next;
        for (int i = start; i < end; i = next) {
            next = TextUtils.indexOf(text, '\n', i, end);
            if (next < 0) {
                next = end;
            }

            int nl = 0;

            while (next < end && text.charAt(next) == '\n') {
                nl++;
                next++;
            }

            if (withinParagraph(out, text, i, next - nl, nl, next == end)) {
                /* Paragraph should be closed */
                out.append("</p>\n");
                out.append(getOpenParaTagWithDirection(text, next, end));
            }
        }

        out.append("</p>\n");
    }

    /* Returns true if the caller should close and reopen the paragraph. */
    private static boolean withinParagraph(StringBuilder out, Spanned text,
                                           int start, int end, int nl,
                                           boolean last)
    {
        int next;
        Log.d("with", "withinParagraph() called with: " + "start = [" + start + "], end = [" + end + "], nl = [" + nl + "], last = [" + last + "]");
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);
            Log.v("next", next + "");
            CharacterStyle[] style = text.getSpans(i, next, CharacterStyle.class);
            for (CharacterStyle style1 : style) {
                Log.v("style", style1.toString());
            }
            for (int j = 0; j < style.length; j++) {
                if (style[j] instanceof StyleSpan) {
                    int s = ((StyleSpan) style[j]).getStyle();

                    if ((s & Typeface.BOLD) != 0) {
                        out.append("<b>");
                    }
                    if ((s & Typeface.ITALIC) != 0) {
                        out.append("<i>");
                    }
                }
                if (style[j] instanceof TypefaceSpan) {
                    String s = ((TypefaceSpan) style[j]).getFamily();

                    if ("monospace".equals(s)) {
                        out.append("<tt>");
                    }
                }
                if (style[j] instanceof SuperscriptSpan) {
                    out.append("<sup>");
                }
                if (style[j] instanceof SubscriptSpan) {
                    out.append("<sub>");
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append("<u>");
                }
                if (style[j] instanceof StrikethroughSpan) {
                    out.append("<strike>");
                }
                if (style[j] instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) style[j]).getURL());
                    out.append("\">");
                }
                if (style[j] instanceof ImageSpan) {
                    out.append("<img src=\"");
                    out.append(((ImageSpan) style[j]).getSource());
                    out.append("\">");

                    // Don't output the dummy character underlying the image.
                    i = next;
                }
                if (style[j] instanceof AbsoluteSizeSpan) {
                    out.append("<font size =\"");
                    out.append(((AbsoluteSizeSpan) style[j]).getSize() / 6);
                    out.append("\">");
                }
                if (style[j] instanceof ForegroundColorSpan) {
                    out.append("<font color =\"#");
                    String color = Integer.toHexString(((ForegroundColorSpan)
                            style[j]).getForegroundColor() + 0x01000000);
                    while (color.length() < 6) {
                        color = "0" + color;
                    }
                    out.append(color);
                    out.append("\">");
                }
            }

            withinStyle(out, text, i, next);

            for (int j = style.length - 1; j >= 0; j--) {
                if (style[j] instanceof ForegroundColorSpan) {
                    out.append("</font>");
                }
                if (style[j] instanceof AbsoluteSizeSpan) {
                    out.append("</font>");
                }
                if (style[j] instanceof URLSpan) {
                    out.append("</a>");
                }
                if (style[j] instanceof StrikethroughSpan) {
                    out.append("</strike>");
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append("</u>");
                }
                if (style[j] instanceof SubscriptSpan) {
                    out.append("</sub>");
                }
                if (style[j] instanceof SuperscriptSpan) {
                    out.append("</sup>");
                }
                if (style[j] instanceof TypefaceSpan) {
                    String s = ((TypefaceSpan) style[j]).getFamily();

                    if (s.equals("monospace")) {
                        out.append("</tt>");
                    }
                }
                if (style[j] instanceof StyleSpan) {
                    int s = ((StyleSpan) style[j]).getStyle();

                    if ((s & Typeface.BOLD) != 0) {
                        out.append("</b>");
                    }
                    if ((s & Typeface.ITALIC) != 0) {
                        out.append("</i>");
                    }
                }
            }
        }

        if (nl == 1) {
            out.append("<br>\n");
            return false;
        } else {
            for (int i = 2; i < nl; i++) {
                out.append("<br>");
            }
            return !last;
        }
    }

    private static void withinStyle(StringBuilder out, CharSequence text,
                                    int start, int end)
    {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
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
                    insertImageByReplace(tag + ",", pre, curr + 1, bitmapCreator.getBitmapByString(tag,
                            0xff000000, 0xffffffff));
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
