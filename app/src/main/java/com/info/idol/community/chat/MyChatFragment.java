package com.info.idol.community.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.info.idol.community.R;

public class MyChatFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RoomListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_lobby_joined,container,false);
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipelayout_joined);
        swipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_joined);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RoomListAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.cleanItem();
        adapter.addItems(MyDataBase.getInstance(getContext()).getRoomList());
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener=new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.cleanItem();
            adapter.addItems(MyDataBase.getInstance(getContext()).getRoomList());
            swipeRefreshLayout.setRefreshing(false);
        }
    };
}
