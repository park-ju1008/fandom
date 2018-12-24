package com.info.idol.community.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.info.idol.community.BaseActivity;
import com.info.idol.community.Class.SoftKeyboard;
import com.info.idol.community.Class.User;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ChattingRoomActivity extends BaseActivity {
    private Handler handler;
    private String data; //서버로부터 받아온 스트림
    private String message; //보내는 메시지
    private SocketChannel socketChannel;
    private static final String HOST = "35.229.103.161";
    private static final int PORT = 5001;
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
        room = new Room(roomId, roomName, 1, capacity);
        user = GlobalApplication.getGlobalApplicationContext().getUser();
        myDataBase = MyDataBase.getInstance(this);
        //방에 입장 하는 것이라면
        if (method.equals("enter_room")) {
            myDataBase.insertRoom(roomId, roomName);
        }
        //서버로 전송할 데이터를만들어준다 (방생성,방입장)
        message = getMessage(method, null);
        Log.e("START", message);
        startClient();
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            recyclerView.smoothScrollToPosition(
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
                message = getMessage("send", et_message.getText().toString());
                int cid = myDataBase.insertChat(user.getUid(), et_message.getText().toString(),0, room.getId());
                adapter.addItem(new Chat(cid, et_message.getText().toString(), 0,user));
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                new SendmsgTask().execute(message);
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


    private class SendmsgTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.e("SEND_MESSAGE", strings[0]);
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8"));
            } catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
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
            int uid=0;
            String nickName=null;
            String profileImage = null;
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map = mapper.readValue(receive, new TypeReference<Map<String, Object>>() {
                });
                if (map.get("method").toString().equals("create_room")) {
                    room.setId((int) map.get("roomId"));
                    myDataBase.insertRoom(room.getId(), room.getName());
                } else if (map.get("method").toString().equals("enter_room")) {
                    //닉네임 날리기
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    if (map.get("profileImage") != null) {
                        profileImage = map.get("profileImage").toString();
                    }
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

    private void startClient() {
        //서버로 접속
        new Thread() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                } catch (IOException e) {
                    Log.e("CHAT", "IOException");
                    e.printStackTrace();
                }
                if (socketChannel.isOpen()) {
                    new SendmsgTask().execute(message);
                    //소켓 채널을 성공적으로 열었다면 버퍼읽기를 시작.
                    receive();
                }
            }
        }.start();
    }

    private void receive() {
        final StringBuilder strBuilder = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Charset charset = Charset.forName("UTF-8");
        while (true) {
            try {
                int readByteCount = 0;
                do {
                    readByteCount = socketChannel.read(byteBuffer); //데이터받기
                    byteBuffer.flip(); //저장한 바이트 끝으로 limit 가 이동한다.
                    strBuilder.append(charset.decode(byteBuffer).toString());
                    byteBuffer.compact();
                } while (readByteCount == 1024);

                /*
                 * read() 메소드를 호출하면 상대방이 데이터를 보내기 전까지는 블로킹
                 * 블로킹이 해제되는 경우는 아래의 세가지 경우
                 * 서버가 정상적으로 Socket의 close()를 호출했을 경우 -1
                 * 서버가 비정상적으로 종료됬을때 IOException 발생
                 * 서버가 비정상적으로 종료됬을때 IOException 발생
                 * 서버가 데이터를 보냈을때 읽은 바이트 수
                 */
                if (readByteCount == -1) {
                    throw new IOException();
                }


                data = strBuilder.toString();
                Log.e("CHAT_reeive", "msg: " + data);
                strBuilder.delete(0, strBuilder.length());
                //UI를 변경할수 있도록 핸들러로 던져줌.
                handler.post(showUpdate);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private String getMessage(String method, String message) {
        HashMap<String, Object> map = new HashMap<>();
        if (method.equals("create_room")) {
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            String accessToken = pref.getString("AccessToken", "");
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
