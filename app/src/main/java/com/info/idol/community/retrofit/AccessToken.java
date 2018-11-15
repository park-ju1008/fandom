package com.info.idol.community.retrofit;

import com.google.gson.annotations.SerializedName;

public class AccessToken {
    @SerializedName("accesstoken")
    String accesstoken;

    public String getAccesstoken(){
        return accesstoken;
    }
}
