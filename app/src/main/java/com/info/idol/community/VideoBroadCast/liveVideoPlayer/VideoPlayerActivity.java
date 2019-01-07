package com.info.idol.community.VideoBroadCast.liveVideoPlayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.info.idol.community.Class.User;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.VideoBroadCast.VideoChattingAdapter;
import com.info.idol.community.chat.Chat;
import com.info.idol.community.chat.NettyChat;
import com.info.idol.community.chat.Room;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity implements ExoPlayer.EventListener {
    public static final String RTMP_BASE_URL = "rtmp://35.229.103.161:1935/live/";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
//    private LinearLayout debugRootView;
//    private TextView debugTextView;
//    private Button retryButton;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    //    private DebugTextViewHelper debugViewHelper;
    private boolean needRetrySource;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;
    private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
    protected String userAgent;

    //채팅을 위한 변수
    private NettyChat nettyChat;
    private String data;//서버로 부터 받는 데이터
    private ObjectMapper mapper = new ObjectMapper();  //string to map mapping을 위한 객체 생성.
    private User user;
    private Room videoRoom;
    private RecyclerView recyclerView;
    private VideoChattingAdapter adapter;
    private ImageView imageViewSend;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        shouldAutoPlay = true;
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        user = GlobalApplication.getGlobalApplicationContext().getUser();
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        int roomId = intent.getIntExtra("roomId", -1);
        videoRoom = new Room(roomId, roomName, 0, 0);
        nettyChat = new NettyChat(getMessage("enter_room", null));
        nettyChat.startClient();
        nettyChat.setOnDataListener(new NettyChat.OnDataListener() {
            @Override
            public void onUpdate(String loadData) {
                data = loadData;
                mainHandler.post(showUpdate);
            }
        });
        setContentView(R.layout.activity_video_player);
        initVIew();
    }

    private void initVIew() {
        final View rootView = findViewById(R.id.root);
//        rootView.setOnClickListener(this);
//        debugRootView = (LinearLayout) findViewById(R.id.controls_root);
//        debugTextView = (TextView) findViewById(R.id.debug_text_view);
//        retryButton = (Button) findViewById(R.id.retry_button);
//        retryButton.setOnClickListener(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
//        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();
        //채팅 설정
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
                            recyclerView.scrollToPosition(
                                    recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });

        final EditText mStreamChatEditText = (EditText) findViewById(R.id.stream_chat_edit_text);
        mStreamChatEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        //메시지 전송
                        if (mStreamChatEditText.getText().toString().isEmpty()) {
                            Snackbar.make(rootView, "할말을 입력하세요.", Snackbar.LENGTH_LONG).show();
                        } else if (nettyChat.getSocketChannel().isOpen()) {
                            adapter.addItem(new Chat(-1, textView.getText().toString(), 0, user));
                            recyclerView.smoothScrollToPosition(adapter.getItemCount());
                            nettyChat.sendMessage(getMessage("send", textView.getText().toString()));
                        } else {
                            Snackbar.make(rootView, "채팅연결이 좋지 않습니다.", Snackbar.LENGTH_LONG).show();
                        }
                        textView.setText(null);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        imageViewSend = (ImageView) findViewById(R.id.ImageView_chat_send);
        imageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //메시지 전송
                if (mStreamChatEditText.getText().toString().isEmpty()) {
                    Snackbar.make(rootView, "할말을 입력하세요.", Snackbar.LENGTH_LONG).show();
                } else if (nettyChat.getSocketChannel().isOpen()) {
                    adapter.addItem(new Chat(-1, mStreamChatEditText.getText().toString(), 0, user));
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    nettyChat.sendMessage(getMessage("send", mStreamChatEditText.getText().toString()));
                } else {
                    Snackbar.make(rootView, "채팅연결이 좋지 않습니다.", Snackbar.LENGTH_LONG).show();
                }
                mStreamChatEditText.setText(null);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        play(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nettyChat.getSocketChannel().isConnected()) {
            nettyChat.setFinish(true);
            nettyChat.sendMessage(getMessage("exit_room", null));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            play(null);
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }


//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        // Show the controls on any key event.
//        simpleExoPlayerView.showController();
//        // If the event was not handled then see if the player view can handle it as a media key event.
//        return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
//    }

//    @Override
//    public void onClick(View view) {
//        if (view == retryButton) {
//            play(null);
//        }
//    }

    // PlaybackControlView.VisibilityListener implementation

//    @Override
//    public void onVisibilityChange(int visibility) {
//        debugRootView.setVisibility(visibility);
//    }

    // Internal methods

    private void initializePlayer(String rtmpUrl) {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {

            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    null, extensionRendererMode);
            //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
            //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
            //           null, extensionRendererMode);
            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
//            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
//            debugViewHelper.start();
        }
        if (needNewPlayer || needRetrySource) {
            //  String action = intent.getAction();
            Uri[] uris;
            String[] extensions;

            uris = new Uri[1];
            uris[0] = Uri.parse(rtmpUrl);
            extensions = new String[1];
            extensions[0] = "";
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);

            needRetrySource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                if (uri.getScheme().equals("rtmp")) {
                    return new ExtractorMediaSource(uri, rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV(),
                            mainHandler, eventLogger);
                } else {
                    return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                            mainHandler, eventLogger);
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    private void releasePlayer() {
        if (player != null) {
//            debugViewHelper.stop();
//            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            //trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
        Log.e("loadinggg", "" + isLoading);

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.e("loadinggg", "" + playbackState);

        if (playbackState == ExoPlayer.STATE_ENDED) {
//            simpleExoPlayerView.remo

        }
    }

    @Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.e("loadinggg", e.toString());
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        needRetrySource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            play(null);
        } else {
            updateResumePosition();
//            showControls();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_video);
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_audio);
            }
        }
    }

//    private void showControls() {
//        debugRootView.setVisibility(View.VISIBLE);
//    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }


    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    public void play(View view) {
        String URL = RTMP_BASE_URL + videoRoom.getName();
        Log.e("PLAY", URL);
        //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";
        initializePlayer(URL);
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
                if (map.get("method").toString().equals("enter_room")) {
                    //닉네임 날리기
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    int count = Integer.parseInt(map.get("userCount").toString());
                    Log.e("peopleNum", "" + count);
                    act = 1;
                    content = nickName + "님이 들어왔습니다.";
                } else if (map.get("method").toString().equals("send")) {
                    uid = Integer.parseInt(map.get("userId").toString());
                    nickName = map.get("nickName").toString();
                    content = map.get("content").toString();
                } else if (map.get("method").toString().equals("exit_room")) {
                    act = 1;
                    nickName = map.get("nickName").toString();
                    content = nickName + "님이 나갔습니다.";
                } else {
                    //방장이 방을 지웠다면 소켓 끊고 액티비티 종료.
                    new AlertDialog.Builder(VideoPlayerActivity.this)
                            .setMessage(R.string.end_play)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                if (!map.get("method").toString().equals("remove_room")) {
                    Chat chat = new Chat(-1, content, act, new User(uid, nickName, null));
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
        if (method.equals("send")) {
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
