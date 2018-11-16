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

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.info.idol.community.Class.FacebookloginCallback;
import com.info.idol.community.retrofit.ApiService;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

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
    KakaologinCallback mCallback;
    ApiService retrofitApiService;
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
                break;
            case R.id.btn_join:
                Intent intent=new Intent(this,TermsActivity.class);
                startActivity(intent);
                break;
        }
    }
    private class KakaologinCallback implements ISessionCallback {
        Context mContext;
        public KakaologinCallback(Context context){
            this.mContext=context;
        }
        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            requestMe();
        }
        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }
        // 사용자 정보 요청
        public void requestMe() {
            // 사용자정보 요청 결과에 대한 Callback
            UserManagement.getInstance().me(new MeV2ResponseCallback(){
                //사용자정보 요청에 성공한 경우
                @Override
                public void onSuccess(MeV2Response result) {
                    //여기서 디비저장 및 데이터 쉐어드
                    long id=result.getId();
                    String nickname=result.getNickname();
                    Log.e("Profile : ", nickname + " id"+id);
                    Intent intent=new Intent(mContext,AddInfoActivity.class);
                    intent.putExtra("type","kakao");
                    intent.putExtra("userId",Long.toString(id));
                    intent.putExtra("userName",nickname);
                    startActivity(intent);
                }
                //세션 오픈 실패. 세션이 삭제된 경우.
                @Override
                public void onSessionClosed(ErrorResult errorResult) {

                }
            });
        }

    }

}
