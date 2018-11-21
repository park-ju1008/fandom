package com.info.idol.community.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.info.idol.community.Class.Event;
import com.info.idol.community.Class.Star;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.retrofit.ApiService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends BottomNavigationParentActivity {
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_FNC = 4;
    final static int ENT_BHIT = 5;
    final static int ENT_SEV = 6;
    private TextView today_text, star_text;
    private Star mStar;
    private ApiService retrofitApiService;
    String schedule_url;

    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_main;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_one;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        //테스트 완료후 주석 제거해야함
        //정상적인 경로 로그인 체크-(엑세스토큰)->좋아하는스타(starid)->아이디에 해당하는 스타정보 저장 (로그아웃시다 날려버려야함)
        mStar = loadInfo();
        if (mStar == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofitApiService = retrofit.create(ApiService.class);
            // 스타의 고유 아이디 값을 받아와서 서버로 부터스타의 정보를 가져온다.
            Intent intent = getIntent();
            final String starID = intent.getStringExtra("starId");
            Log.d("sss","start"+starID);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mStar = retrofitApiService.getStarInfo(starID).execute().body();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                //서버로부터 통신이 끝나서 완료 된후 진행 되어야 하므로 join을 써서 기다림.
                thread.join();
                saveInfo(mStar);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        star_text.setText(mStar.getName());
        Calendar cal = Calendar.getInstance();
        int month = cal.get(cal.MONTH) + 1;
        int date = cal.get(cal.DATE);
        today_text.setText(month + "월 " + date + "일");
        Log.d("sss", "onCreate: "+mStar.getEnt());
        //스케줄 주소를 만들어냄
        if (mStar.getEnt() == ENT_SM) {
            schedule_url = "http://"+mStar.getDomainkey()+"."+ mStar.getDomain() + "/" + mStar.getSchedule();
        }
        Log.v("sss",schedule_url);
        //만들어진 스케줄 주소로 스케줄을 가져온다.
        Description description=new Description();
        Log.v("sss",schedule_url);
        description.execute(schedule_url,Integer.toString(mStar.getEnt()));
    }

    private void initView() {
        star_text = (TextView) findViewById(R.id.starname_text);
        today_text = (TextView) findViewById(R.id.today_text);
    }

    private void saveInfo(Star star) {
        Gson gson = new GsonBuilder().create();
        String strContact = gson.toJson(star, Star.class);
        SharedPreferences sp = getSharedPreferences("star", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("info", strContact); // JSON으로 변환한 객체를 저장한다.
        editor.commit();
        //전역변수로 저장
        GlobalApplication globalApplication=(GlobalApplication)getApplication();
        globalApplication.setStar(star);
    }

    private Star loadInfo() {
        //테스트 끝나고 복구
//        Gson gson = new GsonBuilder().create();
//        SharedPreferences sp = getSharedPreferences("star", MODE_PRIVATE);
//        String strContact = sp.getString("info", "");
//        // 변환
//        Star star = gson.fromJson(strContact, Star.class);
        GlobalApplication globalApplication=(GlobalApplication)getApplication();
        return globalApplication.getStar();
    }

    private class Description extends AsyncTask<String, Void, List<Event>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout root=(LinearLayout)findViewById(R.id.layout_schedule);
            inflater.inflate(R.layout.item_progress,root,true);
        }

        @Override
        protected List<Event> doInBackground(String... strings) {
            ArrayList<Event> events=new ArrayList<>();
            if(Integer.parseInt(strings[1])==ENT_SM){
                try {
                    Map<String, String> input = new HashMap<>();
                    input.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
                    Log.d("sch", "doInBackground: "+strings[0]);
                    Document doc = Jsoup.connect(strings[0])
                            .headers(input)
                            .get();
                    Elements elements = doc.selectFirst(".today").parent().select(".schGroup"); //필요한 녀석만 꼬집어서 지정
                    Log.d("abs", "schWrap" + elements);
                    for (Element event:elements) {
                        Elements info=event.getElementsByTag("span");
                        Log.d("momo",info.get(1).text()+"///"+info.get(2).text());
                        events.add(new Event(null,info.get(1).text(),info.get(2).text(),info.get(3).text()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return events;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            super.onPostExecute(events);
            Log.d("eee",""+events.size());

            LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout root=(LinearLayout)findViewById(R.id.layout_schedule);
            root.removeViewAt(1);
            if(events.size()!=0){
                for (Event event:events) {
                    //뷰를 생성해서 붙이고 생성된 뷰에서 textview를 찾아 값을 입력함.
                    View view=inflater.inflate(R.layout.item_schedule,root,true);
                    TextView text_type=(TextView)view.findViewById(R.id.text_type);
                    TextView text_title=(TextView)view.findViewById(R.id.text_title);
                    TextView text_time=(TextView)view.findViewById(R.id.text_time);
                    text_type.setText(event.getType());
                    text_title.setText(event.getTitle());
                    text_time.setText(event.getTime());
                }
            }else{
                TextView text_empty=new TextView(root.getContext());
                text_empty.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                text_empty.setText("오늘은 스케줄이 없습니다.");
                text_empty.setGravity(Gravity.CENTER_HORIZONTAL);

                root.addView(text_empty);
            }
        }
    }
}