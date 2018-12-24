package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.info.idol.community.Adapter.NoteAdapter;
import com.info.idol.community.Adapter.OnItemListener;
import com.info.idol.community.Adapter.StarSelAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.retrofit.ApiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteListActivity extends BaseActivity implements OnItemListener {
    final static int TYPE_RECV_NOTE = 0;
    final static int TYPE_SEND_NOTE = 1;
    final static int INTENT_REQUEST_DELETE=2;
    private Button bt_recv, bt_send;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NoteAdapter mAdapter;
    private boolean[] endLine;
    private int type;
    private ApiService apiService;
    private ArrayList<Board> recvNote = new ArrayList<>();
    private ArrayList<Board> sendNote = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        apiService = GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
        initVIew();
        endLine = new boolean[2];
        bt_recv.callOnClick();
    }

    private void initVIew() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("쪽지함");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bt_recv = (Button) findViewById(R.id.bt_recv);
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_recv.setOnClickListener(topButtonsListener);
        bt_send.setOnClickListener(topButtonsListener);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new NoteAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!endLine[type] && dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == (mAdapter.getItemCount() - 5)) {
                    loadNote(type, mAdapter.getItemCount());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==INTENT_REQUEST_DELETE&&resultCode==RESULT_OK){
            final int position=data.getIntExtra("position",-1);
            if(type==TYPE_RECV_NOTE){
                recvNote.remove(position);
            }else{
                sendNote.remove(position);
            }
            apiService.deleteNote(type,mAdapter.getItem(position).getBno()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    mAdapter.removeItem(position);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getBaseContext(),"쪽지 삭제실패",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void loadNote(final int type, int start) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("type", type);
        map.put("start", start);
        map.put("userId", GlobalApplication.getGlobalApplicationContext().getUser().getUid());
        apiService.postLoadNote(map).enqueue(new Callback<List<Board>>() {
            @Override
            public void onResponse(Call<List<Board>> call, Response<List<Board>> response) {

                if (response.isSuccessful()) {
                    if (response.body().size() < 10) {
                        endLine[type] = true;
                    }
                    if (type == TYPE_RECV_NOTE) {
                        recvNote.addAll(response.body());
                        mAdapter.addItems(response.body());
                    } else {
                        sendNote.addAll(response.body());
                        mAdapter.addItems(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Board>> call, Throwable t) {
                Log.e("TESTFauilure", t.toString());

            }
        });
    }

    /**
     *  Item click을 위한 interface 메소드
     * @param position 어댑터에서의 아이템 위치
     */
    @Override
    public void onItemClick(int position) {
        Intent intent=new Intent(this,NoteDetailActivity.class);
        intent.putExtra("note",mAdapter.getItem(position));
        intent.putExtra("position",position);
        startActivityForResult(intent,INTENT_REQUEST_DELETE);
    }

    /**
     *  Item longclick을 위한 interface 메소드
     * @param position 어댑터에서의 아이템 위치
     */
    @Override
    public void onItemLongClick(int position) {

    }

    View.OnClickListener topButtonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAdapter.cleanItem();
            if (view.getId() == R.id.bt_recv) {
                type = TYPE_RECV_NOTE;
                bt_recv.setSelected(true);
                bt_send.setSelected(false);
                mAdapter.addItems(recvNote);
            } else {
                type = TYPE_SEND_NOTE;
                bt_recv.setSelected(false);
                bt_send.setSelected(true);
                mAdapter.addItems(sendNote);
            }
            //처음 쪽지함을 불러올떄의 조건
            if (!endLine[type] && mAdapter.getItemCount() == 0) {
                loadNote(type, 0);
            }
        }
    };

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mAdapter.cleanItem();
            if(type==TYPE_RECV_NOTE){
                recvNote.clear();
            }else{
                sendNote.clear();
            }
            endLine[type]=false;
            loadNote(type,0);
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
