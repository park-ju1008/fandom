package com.info.idol.community.VideoBroadCast;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.info.idol.community.BaseActivity;
import com.info.idol.community.Class.User;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.chat.Chat;
import com.info.idol.community.chat.NettyChat;
import com.info.idol.community.chat.Room;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.antmedia.android.broadcaster.ILiveVideoBroadcaster;
import io.antmedia.android.broadcaster.LiveVideoBroadcaster;
import io.antmedia.android.broadcaster.utils.Resolution;

public class VideoBroadcaster extends BaseActivity {
    public static final String RTMP_BASE_URL = "rtmp://35.229.103.161:1935/live/";
    private static final String TAG = "VideoBroadcaster";
    //채팅을 위한 변수선언
    private NettyChat nettyChat;
    private RecyclerView recyclerView;
    private VideoChattingAdapter adapter;
    private Handler handler;
    private String data; //서버로부터 받아온 스트림
    private String message; //보내는 메시지
    private ObjectMapper mapper = new ObjectMapper();  //string to map mapping을 위한 객체 생성.
    private Room videoRoom;
    private User user;
    //
    private Intent mLiveVideoBroadcasterServiceIntent;
    private Timer mTimer;
    private CameraResolutionsFragment mCameraResolutionsDialog;
    public TimerHandler mTimerHandler;
    private ViewGroup mRootView;
    private EditText mStreamChatEditText;
    private ImageButton mSettingsButton;
    private GLSurfaceView mGLView;
    private TextView mStreamLiveStatus;
    private Button mBroadcastControlButton;
    private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    boolean mIsRecording = false;
    private long mElapsedTime;

    private ServiceConnection mConnection = new ServiceConnection() {

        //IBinder 서비스와 컴포넌트 사이의 인터페이스를 정의
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(VideoBroadcaster.this, mGLView);
                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
            }
            mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //화면 계속 켜짐과 상태바 제거
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
        startService(mLiveVideoBroadcasterServiceIntent);
        //레이아웃과 연결
        setContentView(R.layout.activity_videochat);

        //채팅을 위한 선언들
        String roomName = getIntent().getStringExtra("roomName");
        int capacity = getIntent().getIntExtra("capacity", -1);
        user = GlobalApplication.getGlobalApplicationContext().getUser();
        videoRoom = new Room(-1, roomName, user.getNickname(), 1, capacity);
        mTimerHandler = new TimerHandler();
        handler = new Handler();
        nettyChat = new NettyChat(getMessage("create_room", null));
        nettyChat.setOnDataListener(new NettyChat.OnDataListener() {
            @Override
            public void onUpdate(String loadData) {
                data = loadData;
                handler.post(showUpdate);
            }
        });

        initView();

    }

    public void changeCamera(View v) {
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.changeCamera();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this lets activity bind
        bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LiveVideoBroadcaster.PERMISSIONS_REQUEST: {
                if (mLiveVideoBroadcaster.isPermissionGranted()) {
                    mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.RECORD_AUDIO)) {
                        mLiveVideoBroadcaster.requestPermission();
                    } else {
                        new AlertDialog.Builder(VideoBroadcaster.this)
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        //hide dialog if visible not to create leaked window exception
        if (mCameraResolutionsDialog != null && mCameraResolutionsDialog.isVisible()) {
            mCameraResolutionsDialog.dismiss();
        }
        mLiveVideoBroadcaster.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //방을 나갈때 방을 종료 하기위해 서버로 메시지 전송. (여기가 안된다면 백버튼 눌럿을때 보내기)
        if(nettyChat.getSocketChannel()!=null){
            nettyChat.setFinish(true);
            nettyChat.sendMessage(getMessage("remove_room", null));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //화면 회전시에 카메라에서 가져오는 화면도 회전
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    public void showSetResolutionDialog(View v) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragmentDialog = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragmentDialog != null) {

            ft.remove(fragmentDialog);
        }

        ArrayList<Resolution> sizeList = mLiveVideoBroadcaster.getPreviewSizeList();


        if (sizeList != null && sizeList.size() > 0) {
            mCameraResolutionsDialog = new CameraResolutionsFragment();

            mCameraResolutionsDialog.setCameraResolutions(sizeList, mLiveVideoBroadcaster.getPreviewSize());
            mCameraResolutionsDialog.show(ft, "resolutiton_dialog");
        } else {
            Snackbar.make(mRootView, "No resolution available", Snackbar.LENGTH_LONG).show();
        }

    }

    public void toggleBroadcasting(View v) {
        Log.e(TAG, "ddd");
        if (!mIsRecording) {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {
                    String streamName = videoRoom.getName();

                    new AsyncTask<String, String, Boolean>() {
                        ContentLoadingProgressBar progressBar;

                        @Override
                        protected void onPreExecute() {
                            Log.e(TAG, "dddddd");
                            progressBar = new ContentLoadingProgressBar(VideoBroadcaster.this);
                            progressBar.show();
                        }

                        @Override
                        protected Boolean doInBackground(String... url) {
                            Log.e(TAG, "rrrrr");
                            return mLiveVideoBroadcaster.startBroadcasting(url[0]);

                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            progressBar.hide();
                            mIsRecording = result;
                            if (result) {
                                mStreamLiveStatus.setVisibility(View.VISIBLE);

                                mBroadcastControlButton.setText(R.string.stop_broadcasting);
                                mSettingsButton.setVisibility(View.GONE);
                                startTimer();//start the recording duration
                                nettyChat.startClient();//채팅 시작을 위한 연결
                            } else {
                                Snackbar.make(mRootView, R.string.stream_not_started, Snackbar.LENGTH_LONG).show();

                                triggerStopRecording();
                            }
                        }
                    }.execute(RTMP_BASE_URL + streamName);
                } else {
                    Snackbar.make(mRootView, R.string.streaming_not_finished, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(mRootView, R.string.oopps_shouldnt_happen, Snackbar.LENGTH_LONG).show();
            }
        } else {
            triggerStopRecording();
        }

    }

    public void triggerStopRecording() {
        if (mIsRecording) {
            mBroadcastControlButton.setText(R.string.start_broadcasting);

            mStreamLiveStatus.setVisibility(View.GONE);
            mStreamLiveStatus.setText(R.string.live_indicator);
            mSettingsButton.setVisibility(View.VISIBLE);

            stopTimer();
            mLiveVideoBroadcaster.stopBroadcasting();

        }

        mIsRecording = false;
    }

    //This method starts a mTimer and updates the textview to show elapsed time for recording
    public void startTimer() {

        if (mTimer == null) {
            mTimer = new Timer();
        }

        mElapsedTime = 0;
        mTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                mElapsedTime += 1; //increase every sec
                mTimerHandler.obtainMessage(TimerHandler.INCREASE_TIMER).sendToTarget();

                if (mLiveVideoBroadcaster == null || !mLiveVideoBroadcaster.isConnected()) {
                    mTimerHandler.obtainMessage(TimerHandler.CONNECTION_LOST).sendToTarget();
                }
            }
        }, 0, 1000);
    }


    public void stopTimer() {
        if (mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.mElapsedTime = 0;
    }

    public void setResolution(Resolution size) {
        mLiveVideoBroadcaster.setResolution(size);
    }

    private void initView() {
        mStreamChatEditText = (EditText) findViewById(R.id.stream_chat_edit_text);
        mRootView = (ViewGroup) findViewById(R.id.root_layout);
        mSettingsButton = (ImageButton) findViewById(R.id.settings_button);
        mStreamLiveStatus = (TextView) findViewById(R.id.stream_live_status);

        mBroadcastControlButton = (Button) findViewById(R.id.toggle_broadcasting);

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL activity.
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }
        //채팅 위한 초기화
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VideoChattingAdapter();
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

        mStreamChatEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        //메시지 전송
                        if (nettyChat.getSocketChannel().isOpen()) {
                            adapter.addItem(new Chat(-1, textView.getText().toString(), 0, user));
                            recyclerView.smoothScrollToPosition(adapter.getItemCount());
                            nettyChat.sendMessage(getMessage("send", textView.getText().toString()));
                        } else {
                            Snackbar.make(mRootView, "방송을 시작하지 않았거나 채팅연결이 좋지 않습니다.", Snackbar.LENGTH_LONG).show();
                        }
                        textView.setText(null);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }


    private class TimerHandler extends Handler {
        static final int CONNECTION_LOST = 2;
        static final int INCREASE_TIMER = 1;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INCREASE_TIMER:
                    mStreamLiveStatus.setText(getString(R.string.live_indicator) + " - " + getDurationString((int) mElapsedTime));
                    break;
                case CONNECTION_LOST:
                    triggerStopRecording();
                    new AlertDialog.Builder(VideoBroadcaster.this)
                            .setMessage(R.string.broadcast_connection_lost)
                            .setPositiveButton(android.R.string.yes, null)
                            .show();

                    break;
            }
        }
    }

    public static String getDurationString(int seconds) {

        if (seconds < 0 || seconds > 2000000)//there is an codec problem and duration is not set correctly,so display meaningfull string
            seconds = 0;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours == 0)
            return twoDigitString(minutes) + " : " + twoDigitString(seconds);
        else
            return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    public static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    private Runnable showUpdate = new Runnable() {
        @Override
        public void run() {
            String receive = data;
            int act = 0;
            String content = null;
            int uid = 0;
            String nickName = null;
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map = mapper.readValue(receive, new TypeReference<Map<String, Object>>() {
                });
                if (map.get("method").toString().equals("create_room")) {
                    videoRoom.setId((int) map.get("roomId"));
                } else if (map.get("method").toString().equals("enter_room")) {
                    //닉네임 날리기
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    act = 1;
                    content = nickName + "님이 들어왔습니다.";
                } else if (map.get("method").toString().equals("send")) {
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    content = map.get("content").toString();
                } else if (map.get("method").toString().equals("exit_room")) {
                    nickName = map.get("nickName").toString();
                    act = 1;
                    content = nickName + "님이 나갔습니다.";
                }

                if (!map.get("method").toString().equals("create_room")) {
                    Chat chat = new Chat(-1, content, act, new User(uid, nickName, null));
                    adapter.addItem(chat);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * @param method  전달하는 메시지의 유형
     * @param message 전달하는 메시지 내용
     * @return
     */
    private String getMessage(String method, String message) {
        HashMap<String, Object> map = new HashMap<>();
        if (method.equals("create_room")) {
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            String accessToken = pref.getString("AccessToken", "");
            map.put("type", 1);
            map.put("deviceToken", accessToken);
            map.put("roomName", videoRoom.getName());
            map.put("capacity", videoRoom.getCapacity());
        } else if (method.equals("send")) {
            map.put("content", message);
            map.put("roomId", videoRoom.getId());
        } else {
            map.put("roomId", videoRoom.getId());
        }
        map.put("method", method);
        map.put("userId", user.getUid());
        map.put("nickName", user.getNickname());
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}

