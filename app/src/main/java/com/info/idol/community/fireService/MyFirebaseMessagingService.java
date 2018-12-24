package com.info.idol.community.fireService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.User;
import com.info.idol.community.NoteDetailActivity;
import com.info.idol.community.R;
import com.info.idol.community.chat.Chat;
import com.info.idol.community.chat.ChattingRoomActivity;
import com.info.idol.community.chat.MyDataBase;

import java.util.Map;

/*
 * 상속 받는 클래스는 FCM으로 부터 메시지를 받는다.
 * */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private MyDataBase myDataBase = MyDataBase.getInstance(this);
    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //백그라운드일 경우 date 여기로 온다.
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //
                scheduleJob();
            } else {
                // Handle message within 10 seconds

                sendNotification(remoteMessage.getData());
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        /*
         * 토큰 정보가 바뀌는 경우.
         * 앱에서 인스턴스 ID 삭제 (로그아웃으로 활용)
         * 새 기기에서 앱 복원
         * 사용자가 앱 삭제/재설치
         * 사용자가 앱 데이터 소거
         * */

        //토큰 정보를 쉐어드로 저장
        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("AccessToken", token);
        editor.commit();
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(Map<String, String> board) {

        String method = board.get("method");
        String title=null;
        String text=null;
        PendingIntent pendingIntent=null;
        int notiId=0;
        if (method.equals("send")) {
            String content=board.get("content");
            int roomId=Integer.parseInt(board.get("roomId"));
            User user=setUserDataBase(board);

            int cid = myDataBase.insertChat(user.getUid(), content, 0, roomId);

            title=board.get("roomName");
            text=user.getNickname()+" : "+content;
            notiId=user.getUid();

            Intent intent = new Intent(this, ChattingRoomActivity.class);
            intent.putExtra("method", "enter_room");
            intent.putExtra("roomId",Integer.parseInt(board.get("roomId")));
            intent.putExtra("roomName",board.get("roomName"));

            pendingIntent = PendingIntent.getActivity(this, user.getUid() /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        } else if (method.equals("enter_room")) {
            User user=setUserDataBase(board);
            int roomId=Integer.parseInt(board.get("roomId"));
            myDataBase.insertChat(user.getUid(), user.getNickname()+"님이 들어왔습니다.", 1, roomId);
        } else if (method.equals("exit_room")) {

        } else {
            //메모장
            // ex)) {body=추가, date=2018-12-14 16:04:48, user={"uid":"17","image":"5b3d918c503e8658dad7ce24030164f8.jpg","nickname":"대박잉"}}
            JsonParser jsonParser = new JsonParser();
            JsonObject object = (JsonObject) jsonParser.parse(board.get("user"));
            Board note = new Board(board.get("body"), board.get("date"), new User(object.get("uid").getAsInt(), object.get("nickname").getAsString(), object.get("image").getAsString()));

            Intent intent = new Intent(this, NoteDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("note", note);
             pendingIntent = PendingIntent.getActivity(this, note.getUser().getUid() /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            title="[쪽지]";
            text=note.getUser().getNickname() + ":" + note.getBody();
            notiId=note.getUser().getUid();
        }

        if(method.equals("send")||method.equals("send_note")){
            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_home)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(notiId /* ID of notification */, notificationBuilder.build());
        }

    }

    private User setUserDataBase(Map<String, String> board) {
        int uid = Integer.parseInt(board.get("userId").toString());
        String nickName = board.get("nickName").toString();
        String profileImage = null;
        if (board.get("profileImage") != null) {
            profileImage = board.get("profileImage").toString();
        }
        myDataBase.insertUser(uid, nickName, profileImage);
        return new User(uid,nickName,profileImage);
    }
}