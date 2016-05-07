package com.pxh.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pxh.richedittext.RichEditText;

public class MainActivity extends AppCompatActivity
{

    private RichEditText richEditText;

    String html = "下面是图片了 " +
            "<img src='http://www.qqpk.cn/Article/UploadFiles/201411/20141116135722282.jpg' width=\"50\" height=\"50\"/>" +
            "<br>这也是图片<br>" +
            "<img src='http://h.hiphotos.baidu.com/image/pic/item/d000baa1cd11728b2027e428cafcc3cec3fd2cb5.jpg'/>" +
            "<br>还有一张<br>" +
            "<img src='http://img.61gequ.com/allimg/2011-4/201142614314278502.jpg' />";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        richEditText = (RichEditText) findViewById(R.id.rich_edit_text);

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
//                richEditText.setHtml(html);
                break;
            case R.id.post:
                Log.v("html", richEditText.getHtml());
                break;
        }
        return super.onOptionsItemSelected(item);
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
