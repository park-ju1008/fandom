package com.info.idol.community;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.info.idol.community.Class.FacebookloginCallback;
import com.info.idol.community.Class.KakaologinCallback;
import com.info.idol.community.retrofit.ApiService;
import com.kakao.auth.Session;
import com.kakao.usermgmt.LoginButton;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    //구글로그인 result 상수
    private static final int RC_SIGN_IN=900;
    //구글 api 클라이언트
    private GoogleSignInClient googleSignInClient;
    private FacebookloginCallback mLoginCallback;
    private CallbackManager mCallbackManager;
    private com.info.idol.community.custom.LoginButton btn_kakao_custom,btn_facebook_custom,btn_google_custom;
    private LoginButton btn_kakao_login;
    private Button btn_login,btn_join;
    private TextView text_id,text_pw;
    KakaologinCallback mCallback;
    ApiService retrofitApiService;
    private String accessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //쉐어드로 토큰 있다면 바로 넘어가게하기
        SharedPreferences pref=getSharedPreferences("user",MODE_PRIVATE);
        String s=pref.getString("AccessToken","");
        Log.e("eeeaaae",s);


        //페이스북 CallbackManager를 통해 콜백 관리
        mCallbackManager = CallbackManager.Factory.create();
        mLoginCallback = new FacebookloginCallback(this);
        //카카오 콜백
        mCallback=new KakaologinCallback(this);
        Session.getCurrentSession().addCallback(mCallback);
        //구글
        // googleSignInOptions 개체를 구성할 때 requestIDToken을 호출
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitApiService =retrofit.create(ApiService.class);

        initView();
    }

    private void initView(){
        btn_facebook_custom=(com.info.idol.community.custom.LoginButton)findViewById(R.id.btn_custom_facebook_login);
        btn_facebook_custom.setOnClickListener(this);
        btn_kakao_custom=(com.info.idol.community.custom.LoginButton) findViewById(R.id.btn_custom_kakao_login);
        btn_kakao_custom.setOnClickListener(this);
        btn_kakao_login=(LoginButton)findViewById(R.id.btn_kakao_login);
        btn_google_custom=(com.info.idol.community.custom.LoginButton)findViewById(R.id.btn_custom_google_login);
        btn_google_custom.setOnClickListener(this);
        btn_login=(Button)findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_join=(Button)findViewById(R.id.btn_join);
        btn_join.setOnClickListener(this);
        //아이디와 비밀번호 textView
        text_id=(TextView)findViewById(R.id.text_id);
        text_pw=(TextView)findViewById(R.id.text_pw);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //구글로그인 버튼 응답
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //구글 계정 정보를 가져옴.
                GoogleSignInAccount account=task.getResult(ApiException.class);
                //구글 로그인 했을시 유저정보를 서버로 전송하여 디비에 저장후 accesstoken을 로컬에 저장한다.
                HashMap<String, Object> input = new HashMap<>();
                input.put("userId", account.getId());
                input.put("userNick", account.getDisplayName());
                input.put("route","1");
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
                Intent intent=new Intent(this,AddInfoActivity.class);
                startActivity(intent);
                finish();
            } catch (ApiException e) {

            }
        }
        //페이스북 로그인 요청시 결과를
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_custom_facebook_login:
                LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList("email", "user_age_range"));
                LoginManager.getInstance().registerCallback(mCallbackManager,mLoginCallback);
                break;
            case R.id.btn_custom_kakao_login:
                btn_kakao_login.performClick();
                break;
            case R.id.btn_custom_google_login:
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.btn_login:
                if(text_id.getText().toString().isEmpty()||text_pw.getText().toString().isEmpty()){
                    Toast.makeText(this,"아이디/비번을 입력하세요.",Toast.LENGTH_SHORT).show();
                }else{
                    Thread netThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                HashMap<String, Object> input = new HashMap<>();
                                input.put("userId", text_id.getText());
                                input.put("userPW", text_pw.getText());
                                accessToken = retrofitApiService.postUserLogin(input).execute().body();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    netThread.start();
                    try {
                        netThread.join();
                        if(!accessToken.isEmpty()){
                            SharedPreferences pref=getSharedPreferences("user",MODE_PRIVATE);
                            SharedPreferences.Editor editor= pref.edit();
                            editor.putString("AccessToken",accessToken);
                            editor.commit();
                            //좋아하는 연예인 설정 화면으로 넘어감.
                            Intent intent=new Intent(view.getContext(),AddInfoActivity.class);

                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(this,"아이디/비번이 일치하지않습니다.",Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_join:
                Intent intent=new Intent(this,TermsActivity.class);
                startActivity(intent);
                break;
        }
    }


}
