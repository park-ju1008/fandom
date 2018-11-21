package com.info.idol.community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.info.idol.community.retrofit.AccessToken;
import com.info.idol.community.retrofit.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class JoinActivity extends AppCompatActivity {
    private TextView text_id,text_nickname,text_pw,text_pw2;
    private ApiService retrofitApiService;
    private Button btn_join;
    private boolean id_ok,nick_ok,pw1_check,pw_ok;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        initView();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitApiService =retrofit.create(ApiService.class);
    }


    private void initView(){
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("회원가입");
        text_id=(TextView)findViewById(R.id.text_id);
        text_nickname=(TextView)findViewById(R.id.text_nickname);
        text_pw=(TextView)findViewById(R.id.text_pw);
        text_pw2=(TextView)findViewById(R.id.text_pw2);
        text_id.setOnFocusChangeListener(mFocusChangeListener);
        text_nickname.setOnFocusChangeListener(mFocusChangeListener);
        text_pw.setOnFocusChangeListener(mFocusChangeListener);
        text_pw2.setOnFocusChangeListener(mFocusChangeListener);
        btn_join=(Button)findViewById(R.id.btn_join);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View curView = getCurrentFocus();
                int viewID = curView.getId();
                if (viewID == R.id.text_id || viewID == R.id.text_nickname) {
                    Thread netThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                id_ok = retrofitApiService.getValidateId(text_id.getText().toString()).execute().body();
                                nick_ok = retrofitApiService.getValidateNick(text_nickname.getText().toString()).execute().body();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    netThread.start();
                    try {
                        netThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    curView.clearFocus();
                }

                if (!id_ok) {
                    Toast.makeText(view.getContext(), "아이디를 확인하세요", Toast.LENGTH_SHORT).show();
                } else if (!nick_ok) {
                    Toast.makeText(view.getContext(), "닉네임을 확인하세요", Toast.LENGTH_SHORT).show();
                } else if (!pw1_check || !pw_ok) {
                    Toast.makeText(view.getContext(), "비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                } else {
                    //모든 가입 조건을 만족 했을때 여기로 옴.
                    HashMap<String, Object> input = new HashMap<>();
                    input.put("userId", text_id.getText());
                    input.put("userNick", text_nickname.getText());
                    input.put("userPW", text_pw.getText());
                    input.put("route","0");
                    retrofitApiService.postUserInfo(input).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                //가입이 완료 되면 서버로 부터 AccessToken을 발행 시켜서 로컬에 저장한다.
                                String token=response.body();
                                Log.d("to", "onResponse: "+token);
                                SharedPreferences pref=getSharedPreferences("user",MODE_PRIVATE);
                                SharedPreferences.Editor editor= pref.edit();
                                editor.putString("AccessToken",token);
                                editor.commit();

                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                    //좋아하는 연예인 설정 화면으로 넘어감.
                    Intent intent=new Intent(view.getContext(),SelectStarActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    final Button.OnFocusChangeListener mFocusChangeListener= new Button.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                switch (view.getId()) {
                    case R.id.text_id:
                        if(TextUtils.isEmpty(text_id.getText())||!Patterns.EMAIL_ADDRESS.matcher(text_id.getText()).matches()){
                            Toast.makeText(view.getContext(),"올바른 이메일을 입력하세요",Toast.LENGTH_SHORT).show();
                            id_ok=false;
                        }else{

                            retrofitApiService.getValidateId(text_id.getText().toString()).enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    if(response.isSuccessful()){
                                        if(!response.body()){
                                            text_id.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.uncheck,0);
                                            Toast.makeText(getApplicationContext(),"이미 가입된 이메일 입니다.",Toast.LENGTH_SHORT).show();
                                            id_ok=false;
                                        }
                                        else{
                                            text_id.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                            id_ok=true;
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {
                                }
                            });
                        }
                        break;
                    case R.id.text_nickname:

                        if(text_nickname.length()<2||text_nickname.length()>20) {
                            Toast.makeText(view.getContext(), "닉네임은 2~20자 사이여야합니다.", Toast.LENGTH_SHORT).show();
                            nick_ok=false;
                        }else{
                            //닉네임 중복 체크결과 중복 되면 엑스박스와 함께 토스트 메시지 출력
                            //아닐시에 엑스박스 삭제
                            retrofitApiService.getValidateNick(text_nickname.getText().toString()).enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    if(response.isSuccessful()){
                                        if(!response.body()){
                                            text_nickname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.uncheck,0);
                                            Toast.makeText(getApplicationContext(),"중복된 닉네임입니다.",Toast.LENGTH_SHORT).show();
                                            nick_ok=false;
                                        }else{
                                            text_nickname.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                            nick_ok=true;
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {

                                }
                            });
                        }
                        break;
                    case R.id.text_pw:
                        if(!Pattern.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$",text_pw.getText())){
                            Toast.makeText(view.getContext(), "비밀번호는 8 자 이상, 하나 이상의 숫자 및 대문자 소문자와 특수 문자 모두를 포함해야합니다.", Toast.LENGTH_SHORT).show();
                            text_pw.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.uncheck,0);
                            pw1_check=false;
                        }else{
                            text_pw.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                            pw1_check=true;
                        }
                        break;
                    case R.id.text_pw2:
                        Log.e("eeee","오");
                        if(!text_pw.getText().toString().equals(text_pw2.getText().toString())){
                            Toast.makeText(view.getContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                            text_pw2.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.uncheck,0);
                            pw_ok=false;
                        }else{
                            text_pw2.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                            //두개의 패스워드가 일치 했을때 비밀번호의 유효성을 판단하여 패스워드 ok 처리.
                            if(pw1_check) {
                                pw_ok = true;
                            }else{
                                pw_ok=false;
                            }
                        }
                        break;
                }
            }
        }
    };
}
