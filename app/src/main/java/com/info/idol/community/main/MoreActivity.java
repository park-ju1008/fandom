package com.info.idol.community.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.info.idol.community.chat.ChatLobbyMainActivity;
import com.info.idol.community.chat.ChattingRoomActivity;
import com.info.idol.community.Class.FileHandler;
import com.info.idol.community.Class.Star;
import com.info.idol.community.Class.User;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.NoteListActivity;
import com.info.idol.community.R;
import com.info.idol.community.SelectStarActivity;
import com.info.idol.community.UpdateNicknameActivity;
import com.info.idol.community.custom.CircleImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MoreActivity extends BottomNavigationParentActivity implements View.OnClickListener {
    final static int INTENT_REQUEST_GET_IMAGES = 1;
    final static int INTENT_REQUEST_GET_NICKNAME = 2;
    private Button bt_note;
    private Button bt_more_talk;
    private LinearLayout ll_userInfo;
    private CircleImageView iv_userImage;
    private TextView tv_nickName;
    private FileHandler fileHandler;
    private User user;



    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_more;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_four;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        fileHandler = new FileHandler(this, GlobalApplication.getGlobalApplicationContext().getRetrofitApiService());
    }

    private void initView() {
        bt_note = (Button) findViewById(R.id.bt_note);
        bt_note.setOnClickListener(this);
        bt_more_talk=(Button)findViewById(R.id.bt_more_talk);
        bt_more_talk.setOnClickListener(this);
        ll_userInfo = (LinearLayout) findViewById(R.id.ll_more_userInfo);
        ll_userInfo.setOnClickListener(this);
        iv_userImage = (CircleImageView) findViewById(R.id.circle_more_userimage);
        user = GlobalApplication.getGlobalApplicationContext().getUser();
        Star star = GlobalApplication.getGlobalApplicationContext().getStar();
        Glide.with(this).load("http://35.229.103.161/uploads/" + user.getImage()).error(R.drawable.user).into(iv_userImage);
        tv_nickName = (TextView) findViewById(R.id.textview_more_nickname);
        tv_nickName.setText(user.getNickname());
        TextView tv_starName = (TextView) findViewById(R.id.textview_more_starname);
        tv_starName.setText("최애스타 : "+star.getName());
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.bt_note:
                 intent = new Intent(view.getContext(), NoteListActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_more_talk:
                intent=new Intent(view.getContext(),ChatLobbyMainActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_more_userInfo:
                showDialog();
                break;
        }
    }

    private void showDialog() {
        CharSequence[] items = {"프로필 이미지 변경", "프로필 기본 이미지로 변경", "닉네임 변경", "최애스타 변경"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                switch (position) {
                    case 0:
                        TedPermission.with(MoreActivity.this)
                                .setPermissionListener(mPermissionListener)
                                .setRationaleMessage("카메라 사용을 위한 권한이 필요합니다.")
                                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .check();
                        break;
                    case 1:
                        if (user.getImage() != null) {
                            Glide.with(MoreActivity.this).load(R.drawable.user).into(iv_userImage);
                            fileHandler.updateUserInfo(2, user, null, mUserCallback);
                        }
                        break;
                    case 2:
                        Intent intent = new Intent(MoreActivity.this, UpdateNicknameActivity.class);
                        intent.putExtra("nickname", user.getNickname());
                        startActivityForResult(intent, INTENT_REQUEST_GET_NICKNAME);
                        break;
                    case 3:
                        SharedPreferences pref = getSharedPreferences("star", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove("starId");
                        editor.commit();
                        GlobalApplication.getGlobalApplicationContext().setStar(null);
                        Intent starInent=new Intent(MoreActivity.this,SelectStarActivity.class);
                        startActivity(starInent);
                        finish();
                        break;
                }
            }
        });
        builder.show();

    }

    PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //카메라와 저장소 권한이 허용되었을 경우.
            Config config = new Config();
            config.setSelectionMin(1);
            config.setSelectionLimit(1);
            ImagePickerActivity.setConfig(config);
            Intent intent = new Intent(MoreActivity.this, ImagePickerActivity.class);
            startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }
    };

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == RESULT_OK) {
            ArrayList<Uri> image = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
            Glide.with(this).load(image.get(0).getPath()).error(R.drawable.user).into(iv_userImage);
            fileHandler.updateUserInfo(1, user, image.get(0), mUserCallback);
        } else if (requestCode == INTENT_REQUEST_GET_NICKNAME && resultCode == RESULT_OK) {
            user.setNickname(data.getStringExtra("nickname"));
            tv_nickName.setText(data.getStringExtra("nickname"));
            fileHandler.updateUserInfo(0, user, null, mUserCallback);
        }
    }

    Callback<User> mUserCallback = new Callback<User>() {
        @Override
        public void onResponse(Call<User> call, Response<User> response) {
            Log.e("USERTEST", response.body().toString());
            user = response.body();
            GlobalApplication.getGlobalApplicationContext().setUser(user);
        }

        @Override
        public void onFailure(Call<User> call, Throwable t) {

        }
    };
}
