package com.info.idol.community.retrofit;

import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.Comment;
import com.info.idol.community.Class.MyResponse;
import com.info.idol.community.Class.Star;
import com.info.idol.community.Class.User;

import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    public static final String API_URL = "http://35.237.204.193/";

    /**
     * POST 방식, 주소는 위들과 같음.
     *
     * @param param 요청에 필요한 값들.
     * @return Data 객체를 JSON 형태로 반환.
     * @FieldMap HashMap<String   ,       Object> param :
     * Field 형식을 통해 넘겨주는 값들이 여러 개일 때 FieldMap을 사용함.
     * Retrofit에서는 Map 보다는 HashMap 권장.
     * @FormUrlEncoded Field 형식 사용 시 Form이 Encoding 되어야 하기 때문에 사용하는 어노테이션
     * Field 형식은 POST 방식에서만 사용가능.
     */
    @FormUrlEncoded
    @POST("join.php")
    Call<String> postUserInfo(@FieldMap HashMap<String, Object> param);

    @GET("getuser.php")
    Call<User> getUserInfo(@Query("access_code") String access_token);

    @GET("UserValidate.php")
    Call<Boolean> getValidateId(@Query("userId") String userId);

    @GET("UserValidate.php")
    Call<Boolean> getValidateNick(@Query("userNick") String userNick);

    @FormUrlEncoded
    @POST("login.php")
    Call<String> postUserLogin(@FieldMap HashMap<String, Object> param);

    @GET("starList.php")
    Call<List<Star>> getStarList(@Query("start") int start, @Query("end") int end);

    @GET("starInfo.php")
    Call<Star> getStarInfo(@Query("starId") String id);

    @GET("schedule.php")
    Call<List<Board>> getSchedule(@Query("year") int year, @Query("month") int month, @Query("sno") String sno);

    @Multipart
    @POST("write.php")
    Call<MyResponse> uploadImage(@Part("item") RequestBody data, @Part List<MultipartBody.Part> files);

    @GET("commentLoad.php")
    Call<List<Comment>>getCommentList(@Query("bno") String bno);

    @FormUrlEncoded
    @POST("comment_insert.php")
    Call<Comment> postComment(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("comment_delete.php")
    Call<Boolean>postDeleteComment(@Field("cno") String cno);
}
