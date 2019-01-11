package com.info.idol.community.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.info.idol.community.BoardDetailActivity;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.Event;
import com.info.idol.community.Class.JsoupParser;
import com.info.idol.community.Class.Star;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.retrofit.ApiService;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM/dd HH:mm");

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
        //임시적인 코드 if조건
        retrofitApiService = GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
        Intent intent = getIntent();
        String starId = intent.getStringExtra("starId");
        if (starId == null) {
            mStar = loadInfo();
        }
        if (mStar == null) {

            // 스타의 고유 아이디 값을 받아와서 서버로 부터스타의 정보를 가져온다.

            final String starID = intent.getStringExtra("starId");

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
        int day = cal.get(cal.DATE);
        today_text.setText(month + "월 " + day + "일");
        //스케줄 주소를 만들어냄
        schedule_url = getScheduleUrl(mStar.getEnt());
        //만들어진 스케줄 주소로 스케줄을 가져온다.

        Description description = new Description();
        description.execute(schedule_url, mStar.getEnt(), day);

        HashMap<String, Object> input = new HashMap<>();
        input.put("artistCode", mStar.getId());
        input.put("boardCode", "22");
        retrofitApiService.getBoardList(input).enqueue(new Callback<List<Board>>() {
            @Override
            public void onResponse(Call<List<Board>> call, Response<List<Board>> response) {

                if (response.body().size() != 0) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    LinearLayout root = (LinearLayout) findViewById(R.id.layout_hot);
                    Date now = new Date();
                    for (Board board : response.body()) {
                        //뷰를 생성해서 붙이고 생성된 뷰에서 textview를 찾아 값을 입력함.
                        View view = inflater.inflate(R.layout.item_board_common, null);
                        view.setTag(board);
                        TextView tv_title = (TextView) view.findViewById(R.id.textView_board_title);
                        TextView tv_body = (TextView) view.findViewById(R.id.textView_board_body);
                        ImageView iv_userImage = (ImageView) view.findViewById(R.id.imageView_board_userImage);
                        TextView tv_userName = (TextView) view.findViewById(R.id.textView_board_userName);
                        TextView tv_date = (TextView) view.findViewById(R.id.textView_board_date);
                        TextView tv_like = (TextView) view.findViewById(R.id.textView_board_like);
                        TextView tv_comment = (TextView) view.findViewById(R.id.tv_board_comment);
                        ImageView iv_thumbnail = (ImageView) view.findViewById(R.id.imageView_board_thumbnail);
                        tv_title.setText(board.getTitle());
                        tv_body.setText(board.getBody());
                        Glide.with(MainActivity.this).load(getResources().getString(R.string.URL) + board.getUser().getImage()).error(R.drawable.user).centerCrop().into(iv_userImage);
                        tv_userName.setText(board.getUser().getNickname());
                        try {
                            Date dateWrited = inputDateFormat.parse(board.getDate()); //String을 포맷에 맞게 변경
                            long duration = now.getTime() - dateWrited.getTime(); //현재 시간에서 작성시간의 차이를 구함.
                            long min = duration / 60000; //분단위로 바꿈
                            String date;
                            if (min < 60) {
                                date = min + "분 전";
                            } else if (min < 1440) {
                                long hour = min / 60;
                                date = hour + "시간 전";
                            } else {
                                date = outputDateFormat.format(dateWrited);
                            }
                            tv_date.setText(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        tv_like.setText(String.valueOf(board.getLike()));
                        tv_comment.setText(String.valueOf(board.getComment()));
                        iv_thumbnail.setVisibility(View.GONE);
                        root.addView(view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(MainActivity.this,BoardDetailActivity.class);
                                intent.putExtra("content",(Board)view.getTag());
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Board>> call, Throwable t) {
                Log.e("??R???", t.toString());
            }
        });

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
        GlobalApplication globalApplication = (GlobalApplication) getApplication();
        globalApplication.setStar(star);
    }

    private Star loadInfo() {
        //테스트 끝나고 복구
//        Gson gson = new GsonBuilder().create();
//        SharedPreferences sp = getSharedPreferences("star", MODE_PRIVATE);
//        String strContact = sp.getString("info", "");
//        // 변환
//        Star star = gson.fromJson(strContact, Star.class);
        GlobalApplication globalApplication = (GlobalApplication) getApplication();
        return globalApplication.getStar();
    }

    /**
     * @param ent 가수의 소속사 별로 url를 만들기 위해 소속사 구분번호
     * @return scheduleUrl을 반환함.
     */
    public String getScheduleUrl(int ent) {
        String url = null;
        switch (ent) {
            case ENT_SM:
            case ENT_JYP:
                url = "http://" + mStar.getDomainkey() + "." + mStar.getDomain() + "/" + mStar.getSchedule();
                break;
            case ENT_YG:
                url = "http://" + mStar.getDomain() + "/" + mStar.getSchedule() + "?artidx=" + mStar.getBoardkey();
                break;
            case ENT_FNC:
                url = "http://" + mStar.getDomain() + "/" + mStar.getDomainkey() + "/" + mStar.getSchedule();
                break;
            case ENT_BHIT:
                url = "http://" + mStar.getDomain() + "/" + mStar.getSchedule() + "/" + mStar.getBoardkey();
                break;
            case ENT_SEV:
                url = "http://" + mStar.getDomain() + "/" + mStar.getSchedule();
                break;
            default:
        }
        return url;
    }

    private class Description extends AsyncTask<Object, Void, List<Event>> {
        @Override
        protected void onPreExecute() {
            Log.e("속도1", "onstart");
            super.onPreExecute();
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout root = (LinearLayout) findViewById(R.id.layout_schedule);
            inflater.inflate(R.layout.item_progress, root, true);
        }

        @Override
        protected List<Event> doInBackground(Object... objects) {
            //objects={url,entnum,day}
            ArrayList<Event> events = new ArrayList<>();
            Elements elements;
            switch ((int) objects[1]) {
                case ENT_SM:
                    elements = JsoupParser.getDocument((String) objects[0], ".today").parent().select(".schGroup");
                    for (Element event : elements) {
                        Elements info = event.getElementsByTag("span");
                        String time = "";
                        if (info.size() == 4) {
                            time = info.get(3).text();
                        }
                        events.add(new Event(null, info.get(1).text(), info.get(2).text(), time));
                    }
                    break;
                case ENT_JYP:
                    elements = JsoupParser.getDocument((String) objects[0], "#sche-list").select("li > strong:contains(" + objects[2] + ")");
                    for (Element element : elements) {
                        String type;
                        if (!element.nextElementSibling().select("img[src$=T.gif]").isEmpty()) {
                            type = "TV";
                        } else if (!element.nextElementSibling().select("img[src$=M.gif]").isEmpty()) {
                            type = "Magazine";
                        } else if (!element.nextElementSibling().select("img[src$=R.gif]").isEmpty()) {
                            type = "Radio";
                        } else if (!element.nextElementSibling().select("img[src$=C.gif]").isEmpty()) {
                            type = "Concert";
                        } else {
                            type = "ETC";
                        }
                        events.add(new Event(null, type, element.nextElementSibling().text(), ""));
                    }
                    break;
                case ENT_YG:
                    elements = JsoupParser.getDocument((String) objects[0], "#calen_" + (((int) objects[2] < 10) ? "0" + objects[2] : objects[2])).select("li");
                    for (Element event : elements) {
                        Elements info = event.getElementsByTag("span");
                        String type = info.get(0).text();
                        String time = info.get(1).text();
                        String title = info.get(2).text();
                        events.add(new Event(null, type, title, time));
                    }
                    break;
                case ENT_FNC:
                    elements = JsoupParser.getDocument((String) objects[0], ".schedule_detail").select("ul >li");
                    for (Element element : elements) {
                        events.add(new Event(null, "", element.selectFirst("p").text(), ""));
                    }
                    break;
                case ENT_BHIT:
                    //방탄 소년단...
                    break;
                case ENT_SEV:
                    elements = JsoupParser.getDocument((String) objects[0], ".ai1ec-today").select(".ai1ec-event");
                    for (Element element : elements) {
                        events.add(new Event(null, "", element.selectFirst("span").text(), ""));
                    }
                    break;
            }


            return events;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            super.onPostExecute(events);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            LinearLayout root = (LinearLayout) findViewById(R.id.layout_schedule);
            root.removeViewAt(1);
            if (events.size() != 0) {
                for (Event event : events) {
                    //뷰를 생성해서 붙이고 생성된 뷰에서 textview를 찾아 값을 입력함.
                    View view = inflater.inflate(R.layout.item_mainschedule, null);
                    TextView text_type = (TextView) view.findViewById(R.id.text_type);
                    TextView text_title = (TextView) view.findViewById(R.id.text_title);
                    TextView text_time = (TextView) view.findViewById(R.id.text_time);
                    text_type.setText(event.getType());
                    text_title.setText(event.getTitle());
                    text_time.setText(event.getTime());
                    root.addView(view);
                }
            } else {
                TextView text_empty = new TextView(root.getContext());
                text_empty.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                text_empty.setText("오늘은 스케줄이 없습니다.");
                text_empty.setGravity(Gravity.CENTER_HORIZONTAL);

                root.addView(text_empty);
            }
            Log.e("속도2", "onstart");

        }
    }
}