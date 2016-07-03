package com.pxh.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pxh.richedittext.RichEditText;
import com.pxh.richedittext.TextSpanState;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private RichEditText richEditText;
    private HashMap<TextSpanState.TextSpan, View> views = new HashMap<>();

    private TextView content;


    String html = "<html>下面是图片了 " +
            "<img src='http://www.qqpk.cn/Article/UploadFiles/201411/20141116135722282.jpg' width=\"50\" "+
            "height=\"50\"/>" +
            "<br>这也是图片<br>" +
            "<img src='http://h.hiphotos.baidu.com/image/pic/item/d000baa1cd11728b2027e428cafcc3cec3fd2cb5.jpg'/>" +
            "<br>还有一张<br>" +
            "<img src='http://img.61gequ.com/allimg/2011-4/201142614314278502.jpg' /></html>";
//    String html = "<p dir=\"ltr\">12<b>3123</b><b><i>21312</i></b><b><i><u>3131</u></i></b><b><i><u><strike" +
//        ">2323123123123123123123123123123123131</strike></u></i></b><b><i><u>23123</u></i></b><i><u>123</u></i><u" +
//        ">1231</u>23123</p>";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        views.put(TextSpanState.TextSpan.Bold, findViewById(R.id.bold));
        views.put(TextSpanState.TextSpan.Italic, findViewById(R.id.italic));
        views.put(TextSpanState.TextSpan.UnderLine, findViewById(R.id.underline));
        views.put(TextSpanState.TextSpan.Strikethrough, findViewById(R.id.strikethrough));
        views.put(TextSpanState.TextSpan.Quote, findViewById(R.id.quote));
        views.put(TextSpanState.TextSpan.Bullet, findViewById(R.id.bullet));
        richEditText = (RichEditText) findViewById(R.id.rich_edit_text);
        content = (TextView) findViewById(R.id.content);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        richEditText.setSpanChangeListener(new RichEditText.TextSpanChangeListener()
        {
            @Override
            public void OnTextSpanChanged(TextSpanState.TextSpan type, boolean isValid)
            {
                View v = views.get(type);
                changeTextColor(v,isValid);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void test()
    {
        Log.v("1","1231");
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser newSAXParser = saxParserFactory.newSAXParser();
            XMLReader parser = newSAXParser.getXMLReader();
            parser.setContentHandler(new MyContentHandler());
            parser.parse(new InputSource(new StringReader(html)));

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    class MyContentHandler implements ContentHandler
    {
        @Override
        public void setDocumentLocator(Locator locator)
        {

        }

        @Override
        public void startDocument() throws SAXException
        {
            Log.v("start", "Document");
        }

        @Override
        public void endDocument() throws SAXException
        {
            Log.v("end", "Document");
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
            Log.v("start", "mapping"+"prefix:"+prefix+"\nuri"+uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException
        {
            Log.v("start", "mapping"+"prefix:"+prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
            Log.d(TAG, "startElement() called with: " + "uri = [" + uri + "], localName = [" + localName + "], qName " +
                    "= [" + qName + "], atts = [" + atts.getValue("width") + "]");

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            Log.d(TAG, "endElement() called with: " + "uri = [" + uri + "], localName = [" + localName + "], qName = " +
                    "[" + qName + "]");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            Log.d(TAG, "characters() called with: "+new String(ch));
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
            Log.d(TAG, "ignorableWhitespace() called with: " + "ch = [" + ch + "], start = [" + start + "], length = " +
                    "[" + length + "]");
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException
        {

        }

        @Override
        public void skippedEntity(String name) throws SAXException
        {
            Log.d(TAG, "skippedEntity() called with: " + "name = [" + name + "]");
        }
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.insert_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
                break;
            case R.id.post:
                test();
                Log.v("html", richEditText.getHtml());
                content.setText(Html.fromHtml(richEditText.getHtml()));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void linkClick(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(true);
        final View v = getLayoutInflater().inflate(R.layout.dialog_link, null, false);

        final EditText description = (EditText) v.findViewById(R.id.description);
        final EditText url = (EditText) v.findViewById(R.id.input);

        builder.setView(v);
        builder.setTitle("input url");
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                richEditText.insertUrl(description.getText().toString(), url.getText().toString());
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    public void imgClick(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }

    private void changeTextColor(View view, boolean isValid)
    {
        if (!isValid) {
            ((TextView) view).setTextColor(0x88000000);
        } else {
            ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    public void boldClick(View view)
    {
        richEditText.enableBold(!richEditText.isBoldEnable());
    }

    public void italicClick(View view)
    {
        richEditText.enableItalic(!richEditText.isItalicEnable());
    }

    public void underLineClick(View view)
    {
        richEditText.enableUnderLine(!richEditText.isUnderLineEnable());
    }

    public void strikeClick(View view)
    {
        richEditText.enableStrikethrough(!richEditText.isStrikethroughEnable());
    }

    public void quoteClick(View view)
    {
        richEditText.enableQuote(true);
    }

    public void bulletClick(View view)
    {
        richEditText.enableBullet(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                richEditText.insertImage(intent.getData());
            }
        }
    }

}
