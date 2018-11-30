package com.info.idol.community.Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.info.idol.community.retrofit.ApiService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FileHandler{
    private Context mContext;
    private ApiService mApiService;
    private String sid;
    private String accessToken;
    private Callback<MyResponse> mCallback;

    public FileHandler(Context context,ApiService apiService, String sid,String accessToken) {
        mContext=context;
        mApiService = apiService;
        this.sid=sid;
        this.accessToken = accessToken;
    }

    public void SetCallback(Callback<MyResponse> callback) {
        mCallback = callback;
    }


    public void upload(Object object,  Uri... uris) {

        JSONObject json=null;
        if (object instanceof Schedule) {
            Map<String, String> data = new HashMap<String, String>();
            Schedule schedule = (Schedule) object;
            data.put("type", "schedule");
            data.put("eventtime", schedule.getEventtime());
            data.put("write", schedule.getWrite());
            data.put("accesscode", accessToken);
            json=new JSONObject(data);
            Log.e("TEST",json.toString());
        }
        RequestBody item=RequestBody.create(MediaType.parse("text/plain"),json.toString());

        ArrayList<MultipartBody.Part> parts=new ArrayList<>();
        //이미지 수만큼할당
        for (int index = 0; index < uris.length; index++) {
            Log.d("upload", "requestUpload:  image " + index + "  " + uris[index]);
//            File file = new File("" + uris[index]);
            File file=resizingImage(uris[index]);
            RequestBody requestFile=RequestBody.create(MediaType.parse("image/*"),file);
            parts.add(MultipartBody.Part.createFormData("file[]",file.getName(),requestFile));
        }

            mApiService.uploadImage(item,parts).enqueue(mCallback);

    }


    private File resizingImage(Uri uri){
        File temp=new File(uri.toString());
        //비트 맵 데이터를 쓸수있는 파일을 만든다.
        Log.e("TEST","파일이름"+temp.getName());
        File file=new File(mContext.getCacheDir(),temp.getName());
        try {
            file.createNewFile();
            BitmapFactory.Options options = new BitmapFactory.Options();
            //비트맵을 byte array로 변환
            Bitmap bitmap=rotateImage(BitmapFactory.decodeFile(uri.toString(),options),uri);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    private Bitmap rotateImage(Bitmap bitmap, Uri uri) {
        ExifInterface exif = null;
        Bitmap result=null;
        try {
            exif = new ExifInterface(uri.toString());
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            result=rotate(bitmap,exifDegree);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap  비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;

    }
}
