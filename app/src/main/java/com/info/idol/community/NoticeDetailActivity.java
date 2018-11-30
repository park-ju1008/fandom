package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.info.idol.community.Class.JsoupParser;
import com.info.idol.community.Class.Star;
import com.info.idol.community.custom.HtmlTextView;

import org.jsoup.nodes.Element;


public class NoticeDetailActivity extends YouTubeBaseActivity {
    final static int ENT_SM = 1;
    final static int ENT_JYP = 2;
    final static int ENT_YG = 3;
    final static int ENT_SEV = 6;
    private String url;
    private String videoUrl;
    private String content;
    private Star mStar;
    private HtmlTextView mHtmlTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticedetail);
        Intent intent = getIntent();
        String bno = intent.getStringExtra("bno");
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        int page = intent.getIntExtra("page", 1);
        initView(title, date);


        mStar = ((GlobalApplication) getApplication()).getStar();
        url = getUrl(bno, page);

        Thread thread = new Thread() {
            Element element;

            @Override
            public void run() {
                switch (mStar.getEnt()) {
                    case ENT_SM:
                        element = JsoupParser.getDocument(url, ".xed");
                        break;
                    case ENT_JYP:
                        element = JsoupParser.getDocument(url, ".board_article");
                        break;
                    case ENT_YG:
                        Log.d("url",url);
                        element = JsoupParser.getDocument(url, ".list_cont").selectFirst(".on").child(1);
                        break;
                    default:
                }
                Log.d("leeee",element.toString());
                Element frame = element.selectFirst("iframe");
                if (frame!=null) {
                    String[] s = frame.attr("src").toString().split("/");
                    videoUrl = s[4];
                }
                content = element.toString();
            }
        };
        thread.start();
        try {
            thread.join();
            mHtmlTextView.setHtmlText(content,mStar.getEnt());
            mHtmlTextView.setMovementMethod(LinkMovementMethod.getInstance());
            //글 내용중에 유튜브 동영상이 포함 되어있다면
            if (videoUrl!=null) {
                YouTubePlayerView mYouTubePlayerView = new YouTubePlayerView(this);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
                linearLayout.addView(mYouTubePlayerView, 2);
                mYouTubePlayerView.initialize("AIzaSyAuegdMHvajk50gVGGh1O7iILG4lRixUY4", new YouTubePlayer.OnInitializedListener() {

                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        // 비디오 아이디
                        youTubePlayer.loadVideo(videoUrl);
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                    }

                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void initView(String title, String date) {
        mHtmlTextView = (HtmlTextView) findViewById(R.id.htmlTextView);
        TextView text_title = (TextView) findViewById(R.id.text_title);
        TextView text_date = (TextView) findViewById(R.id.text_date);
        text_title.setText(title);
        text_date.setText(date);
    }

    private String getUrl(String bno, int page) {

        String detail_url = null;
        switch (mStar.getEnt()) {
            case ENT_SM:
                detail_url = "http://" + mStar.getDomainkey() + "." + mStar.getDomain() + "/Board/Details/" + bno;
                break;
            case ENT_JYP:
                detail_url = "http://" + mStar.getDomainkey() + "." + mStar.getDomain() + bno;
                break;
            case ENT_YG:
                detail_url = "http://" + mStar.getDomain() + "/" + mStar.getNotice() + "?ARTIDX=" + mStar.getBoardkey() + "&page=" + page+"&n2PageSize=10" + "&IDX=" + bno;
            default:
        }
        return detail_url;
    }

}
