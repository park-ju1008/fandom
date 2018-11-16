package com.info.idol.community.Class;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.info.idol.community.AddInfoActivity;
import com.info.idol.community.retrofit.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FacebookloginCallback implements FacebookCallback<LoginResult> {
    Context mContext;
    public FacebookloginCallback(Context context){
        this.mContext=context;
    }
    // 로그인 성공 시 호출 됩니다. Access Token 발급 성공.

    @Override

    public void onSuccess(LoginResult loginResult) {
        Log.e("Callback :: ", "onSuccess"+loginResult);
        requestMe(loginResult.getAccessToken());
    }

    // 로그인 창을 닫을 경우, 호출됩니다.
    @Override
    public void onCancel() {
        Log.e("Callback :: ", "onCancel");
    }

    // 로그인 실패 시에 호출됩니다.
    @Override
    public void onError(FacebookException error) {
        Log.e("Callback :: ", "onError : " + error.getMessage());
    }

    // 사용자 정보 요청

    public void requestMe(AccessToken token) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {

                            Retrofit retrofit=new Retrofit.Builder()
                                    .baseUrl(ApiService.API_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            ApiService retrofitApiService =retrofit.create(ApiService.class);
                            HashMap<String, Object> input = new HashMap<>();
                            input.put("userId", object.getString("id"));
                            input.put("userNick", object.getString("name"));
                            input.put("route","2");
                            retrofitApiService.postUserInfo(input).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.isSuccessful()){
                                        //가입이 완료 되면 서버로 부터 AccessToken을 발행 시켜서 로컬에 저장한다.
                                        String token=response.body();
                                        Log.d("to", "onResponse: "+token);
                                        SharedPreferences pref=mContext.getSharedPreferences("user",Activity.MODE_PRIVATE);
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

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

    }
}
