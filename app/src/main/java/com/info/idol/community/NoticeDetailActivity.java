package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.info.idol.community.Class.JsoupParser;
import com.info.idol.community.Class.Star;
import com.info.idol.community.custom.HtmlTextView;


public class NoticeDetailActivity extends AppCompatActivity {
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_SEV = 6;
    private String url;
    private String content;
    private HtmlTextView mHtmlTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticedetail);
        Intent intent=getIntent();
        String bno=intent.getStringExtra("bno");
        String title=intent.getStringExtra("title");
        String date=intent.getStringExtra("date");

        mHtmlTextView=(HtmlTextView)findViewById(R.id.htmlTextView);
        TextView text_title=(TextView)findViewById(R.id.text_title);
        TextView text_date=(TextView)findViewById(R.id.text_date);
        text_title.setText(title);
        text_date.setText(date);
        //주소와 어떤 소속사인지 만들기위한 객체
        Star star=((GlobalApplication)getApplication()).getStar();

        if(star.getEnt()==ENT_SM){
           url="http://"+star.getDomainkey()+"."+star.getDomain()+"/Board/Details/"+bno;
        }

        Thread thread=new Thread(){
            @Override
            public void run() {
                content=JsoupParser.getDocument(url,".xed").toString();
            }
        };
        thread.start();
        try {
            thread.join();
            Log.d("fef",""+date+"//"+content);
            mHtmlTextView.setHtmlText(content);
            mHtmlTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
