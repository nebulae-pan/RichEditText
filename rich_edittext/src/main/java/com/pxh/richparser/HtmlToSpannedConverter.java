package com.pxh.richparser;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.pxh.richedittext.R;
import com.pxh.richedittext.RichBulletSpan;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Created by pxh on 2016/7/3.
 * htmlToSpannedConverter
 */
public class HtmlToSpannedConverter implements ContentHandler
{
    private String text;
    private XMLReader mReader;
    private Html.TagHandler mTagHandler;
    private Html.ImageGetter mImageGetter;
    private SpannableStringBuilder mSpannableStringBuilder;

    public HtmlToSpannedConverter(String text, Html.ImageGetter imageGetter, Html.TagHandler handler, XMLReader parser)
    {
        this.text = text;
        this.mReader = parser;
        this.mImageGetter = imageGetter;
        this.mTagHandler = handler;
        mSpannableStringBuilder = new SpannableStringBuilder();
    }

    public Spanned convert()
    {
        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(text)));
        } catch (SAXException | IOException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        }

        // Fix flags and range for paragraph-type markup.
        Object[] obj = mSpannableStringBuilder.getSpans(0, mSpannableStringBuilder.length(), ParagraphStyle.class);
        for (Object ob : obj) {
            int start = mSpannableStringBuilder.getSpanStart(ob);
            int end = mSpannableStringBuilder.getSpanEnd(ob);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (mSpannableStringBuilder.charAt(end - 1) == '\n' &&
                        mSpannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }
            if (end == start) {
                mSpannableStringBuilder.removeSpan(ob);
            } else {
                mSpannableStringBuilder.setSpan(ob, start, end, Spannable.SPAN_PARAGRAPH);
            }
        }
        return mSpannableStringBuilder;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
    {
        handleStartTag(localName, attrs);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        handleEndTag(localName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        StringBuilder sb = new StringBuilder();
        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */
        for (int i = 0; i < length; i++) {
            char c = ch[i + start];
            if (c == ' ' || c == '\n') {
                char preC;
                int len = sb.length();
                if (len == 0) {
                    len = mSpannableStringBuilder.length();
                    if (len == 0) {
                        preC = '\n';
                    } else {
                        preC = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    preC = sb.charAt(len - 1);
                }
                if (preC != ' ' && preC != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }
        mSpannableStringBuilder.append(sb);
    }

    private void handleStartTag(String tag, Attributes attributes)
    {
        switch (tag.toLowerCase()) {
            case "p":
                handleP(mSpannableStringBuilder);
                break;
            case "div":
                handleP(mSpannableStringBuilder);
                break;
            case "strong":
            case "b":
                start(mSpannableStringBuilder, new Bold());
                break;
            case "em":
            case "cite":
            case "dfn":
            case "i":
                start(mSpannableStringBuilder, new Italic());
                break;
            case "font":
                startFont(mSpannableStringBuilder, attributes);
                break;
            case "blockquote":
                handleP(mSpannableStringBuilder);
                start(mSpannableStringBuilder, new Blockquote());
                break;
            case "li":
                handleP(mSpannableStringBuilder);
                start(mSpannableStringBuilder, new Li());
                break;
            case "a":
                startA(mSpannableStringBuilder, attributes);
                break;
            case "u":
                start(mSpannableStringBuilder, new Underline());
                break;
            case "img":
                startImg(mSpannableStringBuilder, attributes, mImageGetter);
            case "strike":
                start(mSpannableStringBuilder, new Strike());
                break;
            default:
                if (tag.length() == 2 &&
                        Character.toLowerCase(tag.charAt(0)) == 'h' &&
                        tag.charAt(1) >= '1' &&
                        tag.charAt(1) <= '6') {
                    handleP(mSpannableStringBuilder);
                    start(mSpannableStringBuilder, new Header(tag.charAt(1) - '1'));
                    return;
                }
                if (mTagHandler != null) {
                    mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
                }
        }
    }

    private void handleEndTag(String tag)
    {
        switch (tag.toLowerCase()) {
            case "br":
                handleBr(mSpannableStringBuilder);
                break;
            case "p":
            case "div":
                handleP(mSpannableStringBuilder);
                break;
            case "strong":
            case "b":
                end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
                break;
            case "em":
            case "cite":
            case "dfn":
            case "i":
                end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
                break;
            case "font":
                endFont(mSpannableStringBuilder);
                break;
            case "blockquote":
                handleP(mSpannableStringBuilder);
                end(mSpannableStringBuilder, Blockquote.class, new QuoteSpan());
                break;
            case "li":
                handleP(mSpannableStringBuilder);
                end(mSpannableStringBuilder, Li.class, new RichBulletSpan());
                break;
            case "a":
                endA(mSpannableStringBuilder);
                break;
            case "u":
                end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
                break;
            case "strike":
                end(mSpannableStringBuilder, Strike.class, new StrikethroughSpan());
            default:
                if (tag.length() == 2 &&
                        Character.toLowerCase(tag.charAt(0)) == 'h' &&
                        tag.charAt(1) >= '1' &&
                        tag.charAt(1) <= '6') {
                    handleP(mSpannableStringBuilder);
                    endHeader(mSpannableStringBuilder);
                } else if (mTagHandler != null) {
                    mTagHandler.handleTag(false, tag, mSpannableStringBuilder, mReader);
                }
        }

    }

    /**
     * handler &lt;/p&gt; , add double "\n"
     *
     * @param text spannableBuilder
     */
    private static void handleP(SpannableStringBuilder text)
    {
        int len = text.length();
        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static void start(SpannableStringBuilder text, Object mark)
    {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void startA(SpannableStringBuilder text, Attributes attributes)
    {
        String href = attributes.getValue("", "href");

        int len = text.length();
        text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind,
                            Object repl)
    {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void handleBr(SpannableStringBuilder text)
    {
        text.append("\n");
    }

    @SuppressWarnings("deprecation")
    private static void startImg(SpannableStringBuilder text,
                                 Attributes attributes, Html.ImageGetter img)
    {
        String src = attributes.getValue("", "src");
        Drawable d = null;
        if (img != null) {
            d = img.getDrawable(src);
        }
        if (d == null) {
            d = Resources.getSystem().getDrawable(R.drawable.unknown_image);
            if (d == null) {
                return;
            }
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
        int len = text.length();
        text.append("\uFFFC");

        text.setSpan(new ImageSpan(d, src), len, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void startFont(SpannableStringBuilder text,
                                  Attributes attributes)
    {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        int len = text.length();
        text.setSpan(new Font(color, face), len, len, Spannable.SPAN_MARK_MARK);
    }

    @SuppressWarnings("deprecation")
    private static void endFont(SpannableStringBuilder text)
    {
        int len = text.length();
        Object obj = getLast(text, Font.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;
            if (f == null) {
                return;
            }
            if (!TextUtils.isEmpty(f.mColor)) {
                if (f.mColor.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = f.mColor.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    int c = getHtmlColor(f.mColor);
                    if (c != -1) {
                        text.setSpan(new ForegroundColorSpan(c | 0xFF000000),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (f.mFace != null) {
                text.setSpan(new TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static int getHtmlColor(String color)
    {
        Integer i = sColorNameMap.get(color.toLowerCase());
        if (i != null) {
            return i;
        } else {
            try {
                return convertValueToInt(color, -1);
            } catch (NumberFormatException nfe) {
                return -1;
            }
        }
    }

    public static int convertValueToInt(CharSequence charSeq, int defaultValue)
    {
        if (null == charSeq)
            return defaultValue;
        String nm = charSeq.toString();
        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!
        int sign = 1;
        int index = 0;
        int len = nm.length();
        int base = 10;
        if ('-' == nm.charAt(0)) {
            sign = -1;
            index++;
        }
        if ('0' == nm.charAt(index)) {
            //  Quick check for a zero by itself
            if (index == (len - 1))
                return 0;
            char c = nm.charAt(index + 1);
            if ('x' == c || 'X' == c) {
                index += 2;
                base = 16;
            } else {
                index++;
                base = 8;
            }
        } else if ('#' == nm.charAt(index)) {
            index++;
            base = 16;
        }
        return Integer.parseInt(nm.substring(index), base) * sign;
    }

    private static Object getLast(Spanned text, Class kind)
    {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void endA(SpannableStringBuilder text)
    {
        int len = text.length();
        Object obj = getLast(text, Href.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Href h = (Href) obj;
            if (h == null) {
                Log.e("endA", "h = null");
                return;
            }
            if (h.mHref != null) {
                text.setSpan(new URLSpan(h.mHref), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void endHeader(SpannableStringBuilder text)
    {
        int len = text.length();
        Object obj = getLast(text, Header.class);

        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        // Back off not to change only the text, not the blank line.
        while (len > where && text.charAt(len - 1) == '\n') {
            len--;
        }

        if (where != len) {
            Header h = (Header) obj;
            if (h == null) {
                Log.e("endHeader", "h = null");
                return;
            }
            text.setSpan(new RelativeSizeSpan(HEADER_SIZES[h.mLevel]),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
    }

    @Override
    public void startDocument() throws SAXException
    {
    }

    @Override
    public void endDocument() throws SAXException
    {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
    }


    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
    }

    private static class Strike
    {
    }

    private static class Bold
    {
    }

    private static class Italic
    {
    }

    private static class Underline
    {
    }

    private static class Blockquote
    {
    }

    private static class Bullet
    {
    }

    private static class Li
    {
    }

    private static class Font
    {
        public String mColor;
        public String mFace;

        public Font(String color, String face)
        {
            mColor = color;
            mFace = face;
        }
    }

    private static class Href
    {
        public String mHref;

        public Href(String href)
        {
            mHref = href;
        }
    }

    private static class Header
    {
        private int mLevel;

        public Header(int level)
        {
            mLevel = level;
        }

    }

    private static final float[] HEADER_SIZES = {
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
    };

    @ColorInt
    public static final int BLACK = 0xFF000000;
    @ColorInt
    public static final int DKGRAY = 0xFF444444;
    @ColorInt
    public static final int GRAY = 0xFF888888;
    @ColorInt
    public static final int LTGRAY = 0xFFCCCCCC;
    @ColorInt
    public static final int WHITE = 0xFFFFFFFF;
    @ColorInt
    public static final int RED = 0xFFFF0000;
    @ColorInt
    public static final int GREEN = 0xFF00FF00;
    @ColorInt
    public static final int BLUE = 0xFF0000FF;
    @ColorInt
    public static final int YELLOW = 0xFFFFFF00;
    @ColorInt
    public static final int CYAN = 0xFF00FFFF;
    @ColorInt
    public static final int MAGENTA = 0xFFFF00FF;
//    @ColorInt
//    public static final int TRANSPARENT = 0;

    private static final HashMap<String, Integer> sColorNameMap;

    static {
        sColorNameMap = new HashMap<>();
        sColorNameMap.put("black", BLACK);
        sColorNameMap.put("darkgray", DKGRAY);
        sColorNameMap.put("gray", GRAY);
        sColorNameMap.put("lightgray", LTGRAY);
        sColorNameMap.put("white", WHITE);
        sColorNameMap.put("red", RED);
        sColorNameMap.put("green", GREEN);
        sColorNameMap.put("blue", BLUE);
        sColorNameMap.put("yellow", YELLOW);
        sColorNameMap.put("cyan", CYAN);
        sColorNameMap.put("magenta", MAGENTA);
        sColorNameMap.put("aqua", 0xFF00FFFF);
        sColorNameMap.put("fuchsia", 0xFFFF00FF);
        sColorNameMap.put("darkgrey", DKGRAY);
        sColorNameMap.put("grey", GRAY);
        sColorNameMap.put("lightgrey", LTGRAY);
        sColorNameMap.put("lime", 0xFF00FF00);
        sColorNameMap.put("maroon", 0xFF800000);
        sColorNameMap.put("navy", 0xFF000080);
        sColorNameMap.put("olive", 0xFF808000);
        sColorNameMap.put("purple", 0xFF800080);
        sColorNameMap.put("silver", 0xFFC0C0C0);
        sColorNameMap.put("teal", 0xFF008080);

    }
}
