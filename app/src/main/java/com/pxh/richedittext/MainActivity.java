package com.pxh.richedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{

    private RichEditText richEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        richEditText = (RichEditText) findViewById(R.id.rich_edit_text);
    }

}
