package com.info.idol.community;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.info.idol.community.Class.FileHandler;
import com.info.idol.community.Class.MyResponse;
import com.info.idol.community.Class.Schedule;
import com.info.idol.community.retrofit.ApiService;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

}


//package com.info.idol.community.Class;
//
//        import android.net.Uri;
//        import android.os.AsyncTask;
//        import android.util.Log;
//
//        import com.info.idol.community.retrofit.ApiService;
//
//        import org.json.JSONObject;
//
//        import java.io.File;
//        import java.io.IOException;
//        import java.util.ArrayList;
//        import java.util.HashMap;
//        import java.util.Map;
//
//        import okhttp3.FormBody;
//        import okhttp3.MediaType;
//        import okhttp3.MultipartBody;
//        import okhttp3.Request;
//        import okhttp3.RequestBody;
//
//
//public class FileHandler extends AsyncTask<Uri, Void, MyResponse> {
//    private ApiService mApiService;
//    private Object data;
//    private String accessToken;
//    private TaskCallback mTaskCallback;
//
//    public FileHandler(ApiService apiService, Object data, String accessToken) {
//        mApiService = apiService;
//        this.data = data;
//        this.accessToken = accessToken;
//    }
//
//    public void SetCallback(TaskCallback taskCallback) {
//        mTaskCallback = taskCallback;
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//    }
//
//    @Override
//    protected MyResponse doInBackground(Uri... uris) {
//        return upload(data, accessToken, uris);
//    }
//
//    @Override
//    protected void onPostExecute(MyResponse myResponse) {
//        super.onPostExecute(myResponse);
//        Log.e("TEST", "실행후");
//        Log.e("TEST", "" + myResponse.message);
//        if (data instanceof Schedule) {
//            Log.e("TEST", "실행후?");
//            Schedule schedule = (Schedule) data;
//            schedule.setBno(myResponse.bno);
//            mTaskCallback.done(schedule);
//        }
//
//    }
//
//    private MyResponse upload(Object object, String usercode, Uri... uris) {
//        MyResponse myResponse = null;
//
//        JSONObject json=null;
//        if (object instanceof Schedule) {
//            Map<String, String> data = new HashMap<String, String>();
//            Schedule schedule = (Schedule) object;
//            data.put("type", "schedule");
//            data.put("eventtime", schedule.getEventtime());
//            data.put("write", schedule.getWrite());
//            data.put("accesscode", usercode);
//            json=new JSONObject(data);
//            Log.e("TEST",json.toString());
//        }
//        RequestBody item=RequestBody.create(MediaType.parse("text/plain"),json.toString());
//
//
//        MultipartBody.Part[] parts=new MultipartBody.Part[uris.length];
//        //이미지 수만큼할당
//        for (int index = 0; index < uris.length; index++) {
//            Log.d("upload", "requestUpload:  image " + index + "  " + uris[index]);
//            File file = new File("" + uris[index]);
//            RequestBody requestFile=RequestBody.create(MediaType.parse("image/*"),file);
//            parts[index]=MultipartBody.Part.createFormData("file[]",file.getName(),requestFile);
//        }
//
//        try {
//            myResponse = mApiService.uploadImage(item,parts).execute().body();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return myResponse;
//    }
//
//    public interface TaskCallback {
//        void done(Object object);
//    }
//}
