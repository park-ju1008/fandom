package com.info.idol.community;

import android.app.Application;

import com.info.idol.community.Adapter.KakaoSDKAdapter;
import com.info.idol.community.Class.Star;
import com.kakao.auth.KakaoSDK;

public class GlobalApplication extends Application {
    private static GlobalApplication instance;
    private Star mStar;

    public static GlobalApplication getGlobalApplicationContext() {
        if (instance == null) {
            throw new IllegalStateException("This Application does not inherit com.kakao.GlobalApplication");
        }
        return instance;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Kakao Sdk 초기화
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    public Star getStar() {
        return mStar;
    }

    public void setStar(Star star) {
        mStar = star;
    }
}
