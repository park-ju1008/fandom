package com.info.idol.community.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

import com.info.idol.community.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class HtmlTextView extends AppCompatTextView implements Html.ImageGetter {
    public HtmlTextView(Context context) {
        super(context);
    }

    public HtmlTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
    * @param source HTML 형식의 문자열
    */

    public void setHtmlText(String source){
        Spanned spanned;
//        Html.fromHtml API 24 부터 달라짐.
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            spanned=Html.fromHtml(source,Html.FROM_HTML_MODE_COMPACT,this,null);
        }else{
            spanned=Html.fromHtml(source,this,null);
        }
        this.setText(spanned);
    }

    /**
     * Html.ImageGetter
     * @param source HTML 파서가 <img> 태그를 만났을때 <img> 태그의 주소가 넘어온다.
     * @return 먼저 LevelListDrawable을 넘겨 이미지가 들어갈 자리를 만들고 AsyncTask를 이용해 이미지 다운로드후 붙여줌
     */
    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable tempHolder=new LevelListDrawable();
        Drawable empty= ContextCompat.getDrawable(getContext(), R.drawable.img_not_found);
        tempHolder.addLevel(0,0,empty);
        tempHolder.setBounds(0,0,empty.getIntrinsicWidth(),empty.getIntrinsicHeight());
        new LoadImage().execute(source,tempHolder);

        return tempHolder;
    }



    class LoadImage extends AsyncTask<Object,Void,Bitmap>{
        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... objects) {
            String source=(String)objects[0];
            mDrawable=(LevelListDrawable)objects[1];

            try {
                InputStream is=new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap!=null){
                BitmapDrawable image=new BitmapDrawable(getContext().getResources(),bitmap);
                mDrawable.addLevel(1,1,image);
                mDrawable.setBounds(0,0,bitmap.getWidth(),bitmap.getHeight());
                mDrawable.setLevel(1);
                // 이미지 다운로드 완료 후, invalidate 의 개념으로, 다시한번 텍스트를 설정해준것이다. 더 좋은방법이 있을법도 하다
                CharSequence t = getText();
                setText(t);
            }

        }
    }
}
