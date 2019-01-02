package com.info.idol.community.VideoBroadCast;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.info.idol.community.Adapter.OnLoadMoreListener;
import com.info.idol.community.BaseActivity;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.chat.Room;
import com.info.idol.community.chat.SettingDialog;
import com.info.idol.community.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoBroadListActivity extends BaseActivity implements OnLoadMoreListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabRoom;
    private VideoBroadListAdapter adapter;
    private int start = 0;
    boolean endLine = false;
    ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        apiService = GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
        initView();
        onLoadMore();

    }

    void initView() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("라이브톡");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout_lobby);
        swipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        recyclerView = (RecyclerView) findViewById(R.id.recycler_lobby);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VideoBroadListAdapter(this);
        adapter.setOnLoadMoreListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!endLine && dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == (adapter.getItemCount() - 3)) {
                    adapter.showLoading();
                }
            }
        });
        fabRoom = (FloatingActionButton) findViewById(R.id.fab_lobby);
        fabRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingDialog dialog = new SettingDialog(view.getContext());
                dialog.setOnDialogListener(new SettingDialog.onDialogListener() {
                    @Override
                    public void sendRoomInfo(String title, int capacity) {
                        Intent intent = new Intent(VideoBroadListActivity.this, VideoBroadcaster.class);
                        intent.putExtra("method", "create_room");
                        intent.putExtra("roomName", title);
                        intent.putExtra("capacity", capacity);
                        startActivity(intent);
                    }
                });
                dialog.show();

            }
        });
    }



    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.cleanItem();
            start = 0;
            onLoadMore();
        }
    };

    @Override
    public void onLoadMore() {
        apiService.getChatList(start,1).enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful()) {
                    List<Room> roomList = response.body();
                    if(roomList!=null){
                        if(start!=0){
                            //첫번째로 불러오는 리스트가 아닐경우에는 progressBar처리 해야함.
                            if (roomList.size() < 15) {
                                endLine = true;
                            }
                            adapter.dismissLoading();
                        }
                        start+=roomList.size();
                        adapter.setMore(true);
                        adapter.addItems(roomList);
                    }

                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                Toast.makeText(VideoBroadListActivity.this,"방 목록을 불러올수 없습니다.",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
