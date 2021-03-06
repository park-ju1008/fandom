package com.info.idol.community;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.info.idol.community.Adapter.RecyclerImageAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.BoardDetail;
import com.info.idol.community.Class.FileHandler;
import com.info.idol.community.Class.MyResponse;
import com.info.idol.community.retrofit.ApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WriteActivity extends BaseActivity {
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private static final int BOARD_SCHEDULE=7;
    private TextView tv_day, tv_time;
    private EditText et_title;
    private EditText text_main;
    private RecyclerImageAdapter mImageAdapter;
    private ApiService mApiService;
    private ArrayList<Uri> image_uris = new ArrayList<>();
    private int selImage = 0;
    private String sid;
    private int bcode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        initView();
        GlobalApplication globalApplication = (GlobalApplication) getApplication();
        mApiService = globalApplication.getRetrofitApiService();
        sid = globalApplication.getStar().getId();
    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("글쓰기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
        tv_day = (TextView) findViewById(R.id.text_day);
        tv_time = (TextView) findViewById(R.id.text_time);
        et_title=(EditText)findViewById(R.id.editText_write_title);
        text_main = (EditText) findViewById(R.id.text_main);
        ImageButton hashtag = (ImageButton) findViewById(R.id.hashtag_Button);
        ImageButton camera = (ImageButton) findViewById(R.id.image_Button);
        hashtag.setOnClickListener(OnClickListener);
        camera.setOnClickListener(OnClickListener);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        LinearLayout timeLayout = (LinearLayout) findViewById(R.id.layout_time);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        mImageAdapter = new RecyclerImageAdapter(this, image_uris);
        recyclerView.setAdapter(mImageAdapter);

        Intent intent = getIntent();
        bcode=intent.getIntExtra("boardCode",-1);
        if(bcode==BOARD_SCHEDULE){
            //스케줄 작성시.
            et_title.setVisibility(View.GONE);
            Long time = intent.getLongExtra("TimeInMillis", 0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tv_day.setText(dateFormat.format(time));
            tv_time.setText("00:00");
            timeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //타임피커
                    TimePickerDialog timePickerDialog = new TimePickerDialog(WriteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int min) {
                            tv_time.setText(hour + ":" + min);
                        }
                    }, 0, 0, true);
                    timePickerDialog.show();
                }
            });
        }else{
            //스케줄 외의 게시판 작성시.
            timeLayout.setVisibility(View.GONE);
        }

    }

    //ToolBar에 writemenu.xml 불러와 넣음
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.writemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                final Board board;
                if(bcode==BOARD_SCHEDULE){
                    String eventTime = "" + tv_day.getText() + " " + tv_time.getText();
                    board = new Board(eventTime, text_main.getText().toString());
                }else{
                    board = new Board(et_title.getText().toString(), text_main.getText().toString());
                }

                SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
                String accessToken = pref.getString("AccessToken", "");
                FileHandler fileHandler = new FileHandler(this, mApiService, sid, bcode, accessToken);
                progressON("uploading..");
                fileHandler.upload(board, image_uris, new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.isSuccessful()) {
                            board.setBno(response.body().bno);
                            board.setUser(response.body().user);
                            board.setImage(response.body().image);
                            board.setDate(response.body().date);
                            //스케줄 완성 했으니 스케줄 란으로 넘겨서넣기
                            Intent intent = new Intent(WriteActivity.this,BoardDetailActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            intent.putExtra("content", board);
//                            setResult(RESULT_OK, intent);
                            startActivity(intent);
                            finish();
                        }
                        progressOFF();
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        progressOFF();
                        Toast.makeText(getApplicationContext(), "글 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    final ImageButton.OnClickListener OnClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.hashtag_Button:
                    text_main.setText(text_main.getText() + "#");
                    text_main.setSelection(text_main.length());
                    break;
                case R.id.image_Button:
                    TedPermission.with(view.getContext())
                            .setPermissionListener(mPermissionListener)
                            .setRationaleMessage("카메라 사용을 위한 권한이 필요합니다.")
                            .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .check();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == Activity.RESULT_OK) {
            //선택한 이미지들에 대한 Uri Arraylist를 받음
            List<Uri> selList = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
            selImage += selList.size();
            image_uris.addAll(selList);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //카메라와 저장소 권한이 허용되었을 경우.
            Config config = new Config();
            config.setSelectionLimit(5 - selImage);
            ImagePickerActivity.setConfig(config);
            Intent intent = new Intent(WriteActivity.this, ImagePickerActivity.class);
            startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }
    };

}
