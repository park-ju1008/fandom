package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class TermsActivity extends AppCompatActivity {
    private WebView mWebView;
    private Button btn_ok;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("이용약관");
        //이용약관을 위한 웹뷰
        mWebView=(WebView)findViewById(R.id.webView);
        mWebView.loadUrl("http://35.237.204.193/terms.html");

        btn_ok=(Button)findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(view.getContext(),JoinActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
