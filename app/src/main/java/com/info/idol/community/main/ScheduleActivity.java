package com.info.idol.community.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.info.idol.community.Adapter.ScheduleAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.Star;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.WriteActivity;
import com.info.idol.community.retrofit.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends BottomNavigationParentActivity {
    public final static int WRITE_REQUEST=1;
    public final static int COMMENT_COUNT_REQUEST=2;
    private TextView curMonth;
    private Star mStar;
    private CompactCalendarView compactCalendarView;
    private ApiService retrofitApiService;
    private RecyclerView recyclerView;
    private ScheduleAdapter mAdapter;
    private Calendar currentCalender  = Calendar.getInstance();
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("yyyy-MM-dd hh:mm",Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat(" yyyy - MMM", Locale.getDefault());

    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_schedule;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_three;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalApplication globalApplication = (GlobalApplication) getApplication();
        //각 가수마다의 스케줄 주소를 만들기 위한 준비.
        mStar = globalApplication.getStar();
        //통신을 위한 설정
        retrofitApiService = globalApplication.getRetrofitApiService();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initView() {
        curMonth = (TextView) findViewById(R.id.text_month);
        Button pre_btn = (Button) findViewById(R.id.pre_button);
        Button next_btn = (Button) findViewById(R.id.next_button);
        ImageButton write_btn=(ImageButton)findViewById(R.id.write_Button);
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        //리사이클러뷰 설정
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter=new ScheduleAdapter(this);
        recyclerView.setAdapter(mAdapter);


        curMonth.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        //처음에 보이는 달에 일정들 표시
        loadEvents(currentCalender.get(Calendar.YEAR),currentCalender.get(Calendar.MONTH)+1);


        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentCalender.setTime(dateClicked);
                //해당 날짜가 클릭되었을때 리사이클러뷰에 해당 날짜에 해당하는 데이터들을 넣어주고 변경사항을 알려준다.
                Log.d("date", "Day was clicked: " + dateClicked + " with events ");
                mAdapter.addSchedules(loadDayEvents(dateClicked));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                //달이 바뀌었을때 서버로부터 해당 달에 해당하는 스케줄을 받아와서 event 체크 한다.
                curMonth.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                currentCalender.setTime(firstDayOfNewMonth);
                int year=currentCalender.get(Calendar.YEAR);
                int month = currentCalender.get(Calendar.MONTH)+1;
                Log.e("TEST","curMonth"+month);
                //현재 달의 이벤트 목록을 가져옴
                //선택된 달의 이벤트가 없다면 아직 서버에서 가져오지 않은 것으로 판단하여 서버로 데이터 요청.
                List<Event> eventList=compactCalendarView.getEventsForMonth(firstDayOfNewMonth);
                if(eventList.isEmpty()){
                    Log.e("TEST","없으니가져옴");
                    loadEvents(year,month);
                }else{
                    mAdapter.addSchedules(loadDayEvents(firstDayOfNewMonth));
                }
            }
        });

        pre_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compactCalendarView.scrollLeft();
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compactCalendarView.scrollRight();
            }
        });
        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ScheduleActivity.this,WriteActivity.class);
                intent.putExtra("boardCode",7);
                intent.putExtra("TimeInMillis",currentCalender.getTimeInMillis());
                startActivityForResult(intent,WRITE_REQUEST);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==WRITE_REQUEST&&resultCode==RESULT_OK){
            Board loadSchedule = (Board) data.getParcelableExtra("content");
            Log.e("TEST","로드"+loadSchedule.getBody());
            compactCalendarView.addEvent(new Event(Color.WHITE,currentCalender.getTimeInMillis(),loadSchedule));
            mAdapter.addSchedule(loadSchedule);
            recyclerView.smoothScrollToPosition(mAdapter.getItemCount()-1);
            Log.e("TEST",loadSchedule.toString());
        }else if(requestCode==COMMENT_COUNT_REQUEST&&resultCode==RESULT_OK){
            Board loadSchedule = (Board) data.getParcelableExtra("content");
            mAdapter.changeSchedule(loadSchedule,data.getIntExtra("position",0));
        }
    }

    private void loadEvents(int year, int month){
        Log.e("TESTLOAD",""+year+month+mStar.getId());
        retrofitApiService.getSchedule(year,month,mStar.getId()).enqueue(new Callback<List<Board>>() {
            @Override
            public void onResponse(Call<List<Board>> call, Response<List<Board>> response) {
                if(response.body()!=null){
                    ArrayList<Event> events=new ArrayList<>();
                    //서버로부터 가져온 값 bno,body,eventtime,image,writer
                    for (Board schedule:response.body()) {
                        try {
                            //이벤트 시간을 포멧팅
                            Date date=dateFormatForDisplaying.parse(schedule.getTitle());
                            events.add(new Event(Color.WHITE,date.getTime(),schedule));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    compactCalendarView.addEvents(events);
                    mAdapter.addSchedules(loadDayEvents(currentCalender.getTime()));
                    Log.e("TEST",""+currentCalender.getTime());
                }
            }

            @Override
            public void onFailure(Call<List<Board>> call, Throwable t) {
                Log.e("error@",t.toString());
            }
        });
    }

    private List<Board> loadDayEvents(Date date){
        List<Event> today=compactCalendarView.getEvents(date);
        ArrayList<Board> schedules=new ArrayList<>();
        for (Event e:today) {
            Board a=(Board)e.getData();
            Log.e("TEST","for문"+a.getBody());
            schedules.add((Board)e.getData());
        }
        return schedules;
    }
}
