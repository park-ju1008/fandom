package com.info.idol.community.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.info.idol.community.BaseActivity;
import com.info.idol.community.Class.User;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChattingRoomActivity extends BaseActivity {
    private Handler handler;
    private String data; //서버로부터 받아온 스트림
    private NettyChat nettyChat;
    private ObjectMapper mapper = new ObjectMapper();  //string to map mapping을 위한 객체 생성.
    private User user;
    private Room room;
    private MyDataBase myDataBase;
    private ChattingRoomAdapter adapter;
    private EditText et_message;
    private ImageView iv_sendBtn;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);
        Log.e("CHAT", "create");
        handler = new Handler();
        //인텐트로 넘어온정보
        String method = getIntent().getStringExtra("method");
        String roomName = getIntent().getStringExtra("roomName");
        int roomId = getIntent().getIntExtra("roomId", -1);
        int capacity = getIntent().getIntExtra("capacity", -1);
        user = GlobalApplication.getGlobalApplicationContext().getUser();
        myDataBase = MyDataBase.getInstance(this);
        //방에 입장 하는 것이라면
        if (method.equals("enter_room")) {
            room = myDataBase.getRoom(roomId);
        } else {
            room = new Room(roomId, roomName, 1, capacity);
        }
        //서버로 전송할 데이터를만들어준다 (방생성,방입장)
        nettyChat = new NettyChat(getMessage(method, null));
        nettyChat.setOnDataListener(new NettyChat.OnDataListener() {
            @Override
            public void onUpdate(String loadData) {
                data = loadData;
                handler.post(showUpdate);
            }

        });
        nettyChat.startClient();
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nettyChat.closeSocketChannel();
    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(room.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //RecyclerView Setting
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChattingRoomAdapter(this, user.getUid());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom && adapter.getItemCount() != 0) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(
                                    recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });
        //방생성이 아니고 이전에 대화 목록이있다면 불러옴.
        if (room.getId() != -1) {
            adapter.addItems(myDataBase.getChatList(room.getId()));
            if (adapter.getItemCount() != 0) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        }

        iv_sendBtn = (ImageView) findViewById(R.id.ImageView_chat_send);
        iv_sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 * 메시지 전송을 할떄
                 * 서버로 보낼 데이터를 만들고
                 * 로컬데이터베이스에 저장후
                 * 화면에 뿌려준후 서버 전송
                 */
//                message = getMessage("send", et_message.getText().toString());
                int cid = myDataBase.insertChat(user.getUid(), et_message.getText().toString(), 0, room.getId());
                adapter.addItem(new Chat(cid, et_message.getText().toString(), 0, user));
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
//                new SendmsgTask().execute(message);
                nettyChat.sendMessage(getMessage("send", et_message.getText().toString()));
                et_message.setText(null);
            }
        });

        et_message = (EditText) findViewById(R.id.editText_chat_content);
        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    iv_sendBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    iv_sendBtn.setClickable(true);
                } else {
                    iv_sendBtn.setBackgroundColor(getResources().getColor(R.color.custom_txt_darkgray));
                    iv_sendBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private Runnable showUpdate = new Runnable() {
        @Override
        public void run() {
            String receive = data;
            int act = 0;
            String content = null;
            int uid = 0;
            String nickName = null;
            String profileImage = null;
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map = mapper.readValue(receive, new TypeReference<Map<String, Object>>() {
                });
                if (map.get("method").toString().equals("create_room")) {
                    room.setId((int) map.get("roomId"));
                    myDataBase.insertRoom(room);
                } else if (map.get("method").toString().equals("enter_room")) {
                    //닉네임 날리기
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    if (map.get("profileImage") != null) {
                        profileImage = map.get("profileImage").toString();
                    }
                    myDataBase.updateUserNum(room.getId(),1);
                    myDataBase.insertUser(uid, nickName, profileImage);
                    //입장말을 위한 셋팅
                    act = 1;
                    content = nickName + "님이 들어왔습니다.";
                } else if (map.get("method").toString().equals("send")) {
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    if (map.get("profileImage") != null) {
                        profileImage = map.get("profileImage").toString();
                    }
                    myDataBase.insertUser(uid, nickName, profileImage);
                    content = map.get("content").toString();
                    //디비에 채팅정보를 저장
                } else {
//                    act=1;
//                    content=nickName+"님이 들어왔습니다.";
                }

                if (!map.get("method").toString().equals("create_room")) {
                    int cid = myDataBase.insertChat(uid, content, act, room.getId());
                    //유저 아이디로 유저정보를 디비에서 가져오고 그걸로 챗객체 구성
                    Chat chat = new Chat(cid, content, act, new User(uid, nickName, profileImage));
                    adapter.addItem(chat);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private String getMessage(String method, String message) {
        HashMap<String, Object> map = new HashMap<>();
        if (method.equals("create_room")) {
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            String accessToken = pref.getString("AccessToken", "");
            map.put("type", 0);
            map.put("deviceToken", accessToken);
            map.put("roomName", room.getName());
            map.put("capacity", room.getCapacity());
        } else if (method.equals("enter_room")) {
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            String accessToken = pref.getString("AccessToken", "");
            map.put("deviceToken", accessToken);
            map.put("roomId", room.getId());
        } else if (method.equals("send")) {
            map.put("content", message);
            map.put("roomId", room.getId());
        } else {
            map.put("roomId", room.getId());
        }
        map.put("method", method);
        map.put("userId", user.getUid());
        map.put("nickName", user.getNickname());
        map.put("profileImage", user.getImage());
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
