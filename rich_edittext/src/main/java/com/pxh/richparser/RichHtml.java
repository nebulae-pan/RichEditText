package com.pxh.richparser;

import android.text.Html;
import android.text.Spanned;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXNotSupportedException;

/**
 * Created by pxh on 2016/7/3.
 * RichHtml
 */
public class RichHtml
{
    public static String toHtml()
    {
        return null;
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

    private static class HtmlParser
    {
        private static final HTMLSchema schema = new HTMLSchema();
    }
}
