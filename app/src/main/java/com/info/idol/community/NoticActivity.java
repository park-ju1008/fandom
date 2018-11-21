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
import com.info.idol.community.Adapter.NoticAdapter;
import com.info.idol.community.Adapter.StarSelAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.Event;
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

public class NoticActivity extends AppCompatActivity implements NoticAdapter.OnLoadMoreListener{
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_SEV = 6;
    private String notic_url;
    private NoticAdapter mAdapter;
    boolean endLine=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notic);
        initView();
        //Application 클래스를 상속받은 클래스로부터 저장되어있던 Star 객체가져옴.
        Star star=((GlobalApplication)getApplication()).getStar();
        if(star.getEnt()==ENT_SM){
            notic_url="http://"+star.getDomainkey()+"."+star.getDomain()+"/"+star.getNotic()+"/"+star.getBoardkey();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
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
        mAdapter=new NoticAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager=(LinearLayoutManager)recyclerView.getLayoutManager();
                if(!endLine&&dy>0&&llManager.findLastCompletelyVisibleItemPosition()==(mAdapter.getItemCount()-2)){
                    mAdapter.showLoading();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onLoadMore() {
        new AsyncTask<Void,Void,List<Board>>(){

            @Override
            protected List<Board> doInBackground(Void... voids) {
                List<Board> list=null;
                Map<String, String> input = new HashMap<>();
                input.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
                try {
                    Document doc = Jsoup.connect(notic_url)
                            .headers(input)
                            .get();
                    Elements elements = doc.select("tbody"); //필요한 녀석만 꼬집어서 지정
                    Log.d("abs", "schWrap" + elements);
//                    for (Element event:elements) {
//                        Elements info=event.getElementsByTag("span");
//                        Log.d("momo",info.get(1).text()+"///"+info.get(2).text());
//                        events.add(new Event(null,info.get(1).text(),info.get(2).text(),info.get(3).text()));
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<Board> boards) {
                super.onPostExecute(boards);
                mAdapter.dismissLoading();
                mAdapter.addItemMore(boards);
                mAdapter.setMore(true);
            }
        }.execute();
    }



    private void loadData(){
        final ArrayList<Board> itemList=new ArrayList<>();
        Thread thread=new Thread(){

            @Override
            public void run() {
                try {
                    Map<String, String> input = new HashMap<>();
                    input.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
                    List<Board> list= null;
                    Document doc = Jsoup.connect(notic_url)
                            .headers(input)
                            .get();
                    Element elements = doc.selectFirst("tbody");
                    Log.d("abs", "schWrap" + elements);
//                    for (Board item:list) {
//                        itemList.add(item);
//                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join();
//            mAdapter.addAll(itemList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
