package com.info.idol.community;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.util.Log;
import android.widget.TextView;

import com.info.idol.community.Adapter.StarSelAdapter;
import com.info.idol.community.Class.Star;
import com.info.idol.community.main.MainActivity;
import com.info.idol.community.retrofit.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectStarActivity extends AppCompatActivity implements StarSelAdapter.OnLoadMoreListener {

    private StarSelAdapter mAdapter;
    private ArrayList<Star> itemList;
    private ApiService retrofitApiService;
    private int start, end;
    boolean endLine = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectstar);
        SharedPreferences pref = getSharedPreferences("star", MODE_PRIVATE);
        String starId = pref.getString("starId", "");
        //if 스타 아이디 있다면 바로 넘어가게 나중에 고치기
        if (!starId.isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("starId", starId);
            startActivity(intent);
            finish();
        }
        itemList = new ArrayList<>();
        initView();
        GlobalApplication globalApplication = (GlobalApplication) getApplication();
        retrofitApiService = globalApplication.getRetrofitApiService();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new StarSelAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!endLine && dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == (mAdapter.getItemCount() - 2)) {
                    mAdapter.showLoading();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onLoadMore() {
        Log.d("star", "onLoadMore");
        new AsyncTask<Void, Void, List<Star>>() {

            @Override
            protected List<Star> doInBackground(Void... voids) {
                List<Star> list = null;
                try {
                    list = retrofitApiService.getStarList(start, end).execute().body();
                    if (list == null) {
                        endLine = true;
                    } else {
                        start += list.size();
                        end = start + 20;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<Star> stars) {
                super.onPostExecute(stars);
                mAdapter.dismissLoading();
                mAdapter.addItemMore(stars);
                mAdapter.setMore(true);
            }
        }.execute();
    }

    private void loadData() {
        itemList.clear();
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    List<Star> list;
                    start = 0;
                    end = 20;
                    list = retrofitApiService.getStarList(start, end).execute().body();
                    for (Star item : list) {
                        itemList.add(item);
                    }
                    start = list.size();
                    end = start + 20;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join();
            mAdapter.addAll(itemList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
