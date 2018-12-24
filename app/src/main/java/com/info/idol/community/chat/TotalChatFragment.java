package com.info.idol.community.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.info.idol.community.Adapter.OnLoadMoreListener;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TotalChatFragment extends Fragment implements OnLoadMoreListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RoomListAdapter adapter;
    private FloatingActionButton fabRoom;
    private int start=0;
    boolean endLine = false;
    ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
        onLoadMore();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lobby_chat, container, false);
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipelayout_lobby);
        swipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_lobby);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RoomListAdapter(getActivity());
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
        fabRoom=(FloatingActionButton)view.findViewById(R.id.fab_lobby);
        fabRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingDialog dialog=new SettingDialog(view.getContext());
                dialog.setOnDialogListener(new SettingDialog.onDialogListener() {
                    @Override
                    public void sendRoomInfo(String title, int capacity) {
                        Intent intent=new Intent(getActivity(),ChattingRoomActivity.class);
                        intent.putExtra("method","create_room");
                        intent.putExtra("roomName",title);
                        intent.putExtra("capacity",capacity);
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        });
        return view;
    }

    @Override
    public void onLoadMore() {
        //디비 댕겨오기
        apiService.getChatList(start).enqueue(new Callback<List<Room>>() {
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
                Toast.makeText(getActivity(),"방 목록을 불러올수 없습니다.",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener=new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.cleanItem();
            start=0;
            onLoadMore();
        }
    };
}
