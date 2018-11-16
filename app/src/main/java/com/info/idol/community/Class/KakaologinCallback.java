package com.info.idol.community.Class;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.info.idol.community.AddInfoActivity;
import com.info.idol.community.retrofit.ApiService;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KakaologinCallback implements ISessionCallback {
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
                Retrofit retrofit=new Retrofit.Builder()
                        .baseUrl(ApiService.API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                ApiService retrofitApiService =retrofit.create(ApiService.class);
                HashMap<String, Object> input = new HashMap<>();
                input.put("userId", result.getId());
                input.put("userNick", result.getNickname());
                input.put("route","3");
                retrofitApiService.postUserInfo(input).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            //가입이 완료 되면 서버로 부터 AccessToken을 발행 시켜서 로컬에 저장한다.
                            String token=response.body();
                            Log.d("to", "onResponse: "+token);
                            SharedPreferences pref=mContext.getSharedPreferences("user", Activity.MODE_PRIVATE);
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
                Intent intent=new Intent(mContext,AddInfoActivity.class);
                ((Activity)mContext).startActivity(intent);




            }
            //세션 오픈 실패. 세션이 삭제된 경우.
            @Override
            public void onSessionClosed(ErrorResult errorResult) {

            }
        });
    }

}