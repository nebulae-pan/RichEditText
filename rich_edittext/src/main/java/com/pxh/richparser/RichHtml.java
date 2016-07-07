package com.pxh.richparser;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
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
import android.util.Log;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXNotSupportedException;

/**
 * Created by pxh on 2016/7/3.
 * RichHtml
 */
public class RichHtml
{
    public static String toHtml(Spanned text)
    {
        StringBuilder out = new StringBuilder();
        withinHtml(out, text);
        return out.toString();
    }

    public static Spanned fromHtml(String text, Html.ImageGetter imageGetter, Html.TagHandler tagHandler)
    {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
        } catch (org.xml.sax.SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }
        HtmlToSpannedConverter converter = new HtmlToSpannedConverter(text, imageGetter, tagHandler, parser);
        return converter.convert();
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

    /*private static void withinDiv(StringBuilder out, Spanned text, int start, int end)
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
    }*/
    private static void withinDiv(StringBuilder out, Spanned text, int start, int end)
    {
        int next;
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, ParagraphStyle.class);
            ParagraphStyle[] spans = text.getSpans(i, next, ParagraphStyle.class);
            for (ParagraphStyle style : spans) {
                if (style instanceof BulletSpan) {
                    out.append("<ul>");
                }
                if (style instanceof QuoteSpan) {
                    out.append("<blockquote>");
                }
            }
            for (int j = spans.length - 1; j >= 0; j--) {
                if (spans[j] instanceof QuoteSpan) {
                    out.append("</blockquote>");
                }
                if (spans[j] instanceof BulletSpan) {
                    out.append("</ul>");
                }
            }
        }
    }

    private static void withinBullet(StringBuilder out, Spanned text, int start, int end)
    {
        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, BulletSpan.class);

            BulletSpan[] spans = text.getSpans(i, next, BulletSpan.class);
            for (BulletSpan span : spans) {
                out.append("<li>");
            }

            withinBlockquote(out, text, i, next);
            for (BulletSpan span : spans) {
                out.append("</li>");
            }
        }
    }

    private static void withinBlockquote(StringBuilder out, Spanned text, int start, int end)
    {
        //out.append(getOpenParaTagWithDirection(text, start, end));

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
                Log.v("tag", "withinParagraph");
                //out.append("</p>\n");
                //out.append(getOpenParaTagWithDirection(text, next, end));
            }
        }
    }

    /* Returns true if the caller should close and reopen the paragraph. */
    private static boolean withinParagraph(StringBuilder out, Spanned text, int start, int end, int nl, boolean last)
    {
        int next;
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);
            CharacterStyle[] style = text.getSpans(i, next,
                    CharacterStyle.class);

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
                        int codePoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codePoint).append(";");
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

    private static class HtmlParser
    {
        private static final HTMLSchema schema = new HTMLSchema();
    }
}
