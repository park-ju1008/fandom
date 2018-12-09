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
import com.info.idol.community.SelectStarActivity;
import com.info.idol.community.retrofit.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

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
                    public void onCompleted(final JSONObject object, GraphResponse response) {
                        try {

                            Retrofit retrofit=new Retrofit.Builder()
                                    .baseUrl(ApiService.API_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            final ApiService retrofitApiService =retrofit.create(ApiService.class);

                            //FIXME////////////////////////////////////////////////////
                            Thread netThread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        HashMap<String, Object> input = new HashMap<>();
                                        input.put("userId", object.getString("id"));
                                        input.put("userNick", object.getString("name"));
                                        input.put("route","2");
                                        SharedPreferences pref=mContext.getSharedPreferences("user",Activity.MODE_PRIVATE);
                                        String accessToken=pref.getString("AccessToken","");
                                        input.put("accessToken",accessToken);
                                        retrofitApiService.postUserInfo(input).execute().body();
                                        SharedPreferences.Editor editor= pref.edit();
                                        editor.putBoolean("autoLogin",false);
                                        editor.commit();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            };
                            netThread.start();
                            netThread.join();
                            //좋아하는 연예인 설정 화면으로 넘어감.
                            Intent intent=new Intent(mContext,SelectStarActivity.class);
                            ((Activity)mContext).startActivity(intent);
                            ((Activity)mContext).finish();


                        } catch (InterruptedException e) {
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
