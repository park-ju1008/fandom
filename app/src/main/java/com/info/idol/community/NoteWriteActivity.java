package com.info.idol.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.info.idol.community.Class.User;
import com.info.idol.community.retrofit.ApiService;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoteWriteActivity extends BaseActivity {
    private EditText et_content;
    private User receiverUser, sendUser;
    private ApiService mApiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_write);
        Intent intent = getIntent();
        receiverUser = intent.getParcelableExtra("recipient");
        Log.e("USERRE",receiverUser.toString());
        sendUser = GlobalApplication.getGlobalApplicationContext().getUser();
        initView();
        mApiService=GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("쪽지 보내기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);
        et_content=(EditText)findViewById(R.id.et_content);

        //키보드 올리는 부분
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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
                HashMap<String, Object> input = new HashMap<>();
                input.put("recvId",receiverUser.getUid());
                input.put("sendId",sendUser.getUid());
                input.put("message",et_content.getText().toString());
                mApiService.postSendNote(input).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if(response.body().intValue()==0){
                            //탕퇴하지 않은 회원이라면 0을 반환한다.
                            finish();
                            overridePendingTransition(R.anim.stay,R.anim.sliding_down);
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {

                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

