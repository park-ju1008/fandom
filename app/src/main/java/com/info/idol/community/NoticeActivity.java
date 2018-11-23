package com.info.idol.community;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.info.idol.community.Adapter.NoticeAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.JsoupParser;
import com.info.idol.community.Class.Star;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeActivity extends AppCompatActivity implements NoticeAdapter.OnLoadMoreListener{
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_SEV = 6;
    private Star mStar;
    private String notice_url;
    private NoticeAdapter mAdapter;
    boolean endLine=false;
    private int page=2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardlist);
        initView();
        //Application 클래스를 상속받은 클래스로부터 저장되어있던 Star 객체가져옴.
        mStar=((GlobalApplication)getApplication()).getStar();
        notice_url=getNoticeUrl(mStar.getEnt());
        loadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView(){
        //광고 설정
        AdView adView=(AdView)findViewById(R.id.adView);
        AdRequest adRequest=new AdRequest.Builder()
                .addTestDevice("70BEC07C0802612A7EDABDF2E701EE4C")
                .build();
        adView.loadAd(adRequest);

        final Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("공지사항");
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter=new NoticeAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager=(LinearLayoutManager)recyclerView.getLayoutManager();
                if(!endLine&&dy>0&&llManager.findLastCompletelyVisibleItemPosition()>(mAdapter.getItemCount()-2)) {
                    mAdapter.showLoading();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onLoadMore() {
        new AsyncTask<Object,Void,List<Board>>(){

            @Override
            protected List<Board> doInBackground(Object... objects) {
                Elements elements;
                ArrayList<Board> itemList=new ArrayList<>();
                switch ((int)objects[1]){
                    case ENT_SM:
                        elements=JsoupParser.getDocument((String)objects[0]+"?page="+page,"tbody").select("tr");
                        for (Element element:elements) {
                            if(element.select(".board_nodata")==null) {
                                Log.d("ddd", "boardnull");
                                endLine = true;
                                break;
                            }else{
                                String bno=element.select(".boardDetails").attr("seq").toString();
                                String title=element.select(".boardDetails").text();
                                String date;
                                if(element.selectFirst(".ft11")!=null) {
                                    date=element.selectFirst(".ft11").text();
                                }else{
                                    date="";
                                }
                                itemList.add(new Board(bno,title,date));
                            }
                        }
                        break;
                    case ENT_JYP:
                        elements=JsoupParser.getDocument(notice_url,".board_list").select("ul > li");
                        for (Element element:elements) {
                            String bno=element.selectFirst("a").attr("href");
                            String title=element.selectFirst("a > span").text();
                            String date=element.child(2).text();
                            itemList.add(new Board(bno,title,date));
                        }
                        break;
                }
                return itemList;
            }

            @Override
            protected void onPostExecute(List<Board> boards) {
                super.onPostExecute(boards);
                mAdapter.dismissLoading();
                mAdapter.addItemMore(boards);
                mAdapter.setMore(true);
                page++;
            }
        }.execute(notice_url,mStar.getEnt());
//        final ArrayList<Board> itemList=new ArrayList<>();
//        Thread thread=new Thread(){
//
//            @Override
//            public void run() {
//                    Element doc=JsoupParser.getDocument(notice_url+"?page="+page,"tbody");
//                    Elements elements = doc.select("tr");
//                    Log.d("abs", "schWrap" + elements);
//                    for (Element element:elements) {
//                        if(element.select(".board_nodata")==null){
//                            Log.d("ddd","boardnull");
//                            endLine=true;
//                            break;
//                        }else {
//                            String bno = element.select(".boardDetails").attr("seq").toString();
//                            String title = element.select(".boardDetails").text();
//                            String date;
//                            if (element.selectFirst(".ft11") != null) {
//                                date = element.selectFirst(".ft11").text();
//                            } else {
//                                date = "";
//                            }
//                            Log.d("ddddd", bno + "/" + title + "/" + date);
//                            itemList.add(new Board(bno, title, date));
//                        }
//                    }
//            }
//        };
//        thread.start();
//        try {
//            thread.join();
//            mAdapter.dismissLoading();
//            mAdapter.addItemMore(itemList);
//            mAdapter.setMore(true);
//            page++;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void loadData(){
        final ArrayList<Board> itemList=new ArrayList<>();
        Thread thread=new Thread(){

            @Override
            public void run() {
                Elements elements;
                switch (mStar.getEnt()){
                    case ENT_SM:
                        elements=JsoupParser.getDocument(notice_url,"tbody").select("tr");
                        for (Element element:elements) {
                            String bno=element.select(".boardDetails").attr("seq").toString();
                            String title=element.select(".boardDetails").text();
                            String date;
                        if(element.selectFirst(".ft11")!=null) {
                            date=element.selectFirst(".ft11").text();
                        }else{
                            date="";
                        }
                        itemList.add(new Board(bno,title,date));
                    }
                        break;
                    case ENT_JYP:
                        elements=JsoupParser.getDocument(notice_url,".board_list").select("ul > li");
                        for (Element element:elements) {
                        String bno=element.selectFirst("a").attr("href");
                        String title=element.selectFirst("a > span").text();
                        String date=element.child(2).text();
                        itemList.add(new Board(bno,title,date));
                        }
                            break;
                }
            }
        };
        thread.start();
        try {
            thread.join();
            mAdapter.addAll(itemList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getNoticeUrl(int ent){
        String url=null;
        switch (ent){
            case ENT_SM:
                url="http://"+mStar.getDomainkey()+"."+mStar.getDomain()+"/"+mStar.getNotice()+"/"+mStar.getBoardkey();
                break;
            case ENT_JYP:
                url="http://"+mStar.getDomainkey()+"."+mStar.getDomain()+"/"+mStar.getNotice();
                break;
            case ENT_YG:
                break;
            case ENT_SEV:
                break;
            default:
        }
        return url;
    }
}