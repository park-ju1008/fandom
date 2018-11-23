package com.info.idol.community.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.info.idol.community.Class.Star;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.NoticeActivity;
import com.info.idol.community.R;

public class BoardActivity extends BottomNavigationParentActivity implements TextView.OnClickListener {
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_SEV = 6;
    private TextView text_write,text_reply,b_sc,b_pick,b_art,b_cheer,b_free,b_info,b_goods;
    private Star mStar;
    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_board;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_two;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        GlobalApplication globalApplication=(GlobalApplication)getApplication();
        mStar=globalApplication.getStar();
        int ent=mStar.getEnt();
        if(ent==ENT_SM||ent==ENT_JYP||ent==ENT_YG||ent==ENT_SEV){
            LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout root=(LinearLayout)findViewById(R.id.layout_board);
            TextView textView=new TextView(this);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,0,0,dpToPx(this,20));
            textView.setLayoutParams(layoutParams);
            textView.setText("공홈 공지사항");
            textView.setTextColor(Color.BLACK);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(16);
            root.addView(textView,0);
            textView.setOnClickListener(this);
        }
    }

    private void initView(){
        text_write=(TextView)findViewById(R.id.text_write);
        text_reply=(TextView)findViewById(R.id.text_reply);
        b_sc=(TextView)findViewById(R.id.b_sc);
        b_pick=(TextView)findViewById(R.id.b_pick);
        b_art=(TextView)findViewById(R.id.b_art);
        b_cheer=(TextView)findViewById(R.id.b_cheer);
        b_free=(TextView)findViewById(R.id.b_free);
        b_info=(TextView)findViewById(R.id.b_info);
        b_goods=(TextView)findViewById(R.id.b_goods);
        text_reply.setOnClickListener(this);
        text_write.setOnClickListener(this);
        b_sc.setOnClickListener(this);
        b_pick.setOnClickListener(this);
        b_art.setOnClickListener(this);
        b_cheer.setOnClickListener(this);
        b_free.setOnClickListener(this);
        b_info.setOnClickListener(this);
        b_goods.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.text_write:
                break;
            case R.id.text_reply:
                break;
            case R.id.b_sc:
                break;
            case R.id.b_pick:
                break;
            case R.id.b_art:
                break;
            case R.id.b_cheer:
                break;
            case R.id.b_free:
                break;
            case R.id.b_info:
                break;
            case R.id.b_goods:
                break;
                default:
                    Intent intent=new Intent(this,NoticeActivity.class);
                    startActivity(intent);
                    Log.v("www","ee");
                    break;

        }
    }

    private int dpToPx(Context context,int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}
