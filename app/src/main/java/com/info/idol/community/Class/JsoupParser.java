package com.info.idol.community.Class;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsoupParser {
    public static Element getDocument(String url, String selecter){
        Log.e("JSOUPPARSER",url+"///"+selecter);
        Map<String, String> input = new HashMap<>();
        input.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .headers(input)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc.selectFirst(selecter);
    }

}
