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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    private RichEditText richEditText;
    private HashMap<TextSpanState.TextSpan, View> views = new HashMap<>();

    private TextView content;

//    String html = "下面是图片了 " +
//            "<img src='http://www.qqpk.cn/Article/UploadFiles/201411/20141116135722282.jpg' width=\"50\"
// height=\"50\"/>" +
//            "<br>这也是图片<br>" +
//            "<img src='http://h.hiphotos.baidu.com/image/pic/item/d000baa1cd11728b2027e428cafcc3cec3fd2cb5.jpg'/>" +
//            "<br>还有一张<br>" +
//            "<img src='http://img.61gequ.com/allimg/2011-4/201142614314278502.jpg' />";

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
        if (isValid) {
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
