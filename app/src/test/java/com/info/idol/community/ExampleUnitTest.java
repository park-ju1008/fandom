package com.info.idol.community;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    private class Description extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Map<String, String> input = new HashMap<>();
//                input.put("Connection","keep-alive");
//                input.put("Upgrade-Insecure-Requests","1");
//                input.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
//                input.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//                input.put("Referer","http://exo.smtown.com/Main");
//                input.put("Accept-Encoding","gzip, deflate");
                input.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

                Document doc = Jsoup.connect("http://exo.smtown.com//Board/List/?kind=1")
                        .headers(input)
                        .get();
                Elements elements = doc.select("h2"); //필요한 녀석만 꼬집어서 지정
                Log.d("sch", "doInBackground:doc " + doc);
                Log.d("sch", "schWrap" + elements);
//                for(Element elem : elements){
//                    String num=elem.select("span[class=num]").text();
//                    Log.d("sch", "doInBackground:date "+num);
//                    Elements item=elem.select("div[class=schGroup]");
//                    for (Element sch:item){
//                        Log.d("sch", "doInBackground: "+sch.select("eventTitle").text());
//                    }
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}