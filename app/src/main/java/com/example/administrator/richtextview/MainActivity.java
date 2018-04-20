package com.example.administrator.richtextview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spanned;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);

        String content = "<p><span style=\"text-decoration: underline;\">测试</span></p>";

        //记住要加上html，body的标签不然会异常
        content = "<html><body>" + content + "</body></html>";
        Spanned s = UPHtmlTagHandler.fromHtml(content,null,new UPExtendTagHandler(this,tv.getTextColors()));
        tv.setText(s);
    }
}
