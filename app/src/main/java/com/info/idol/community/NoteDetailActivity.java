package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.info.idol.community.Class.Board;

public class NoteDetailActivity extends BaseActivity {
    private Board note;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        initView();
    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        note = (Board) getIntent().getParcelableExtra("note");
        ImageView iv_user_image = (ImageView) findViewById(R.id.iv_user_image);
        TextView tv_writer = (TextView) findViewById(R.id.tv_writer);
        TextView tv_write_time = (TextView) findViewById(R.id.tv_write_time);
        TextView tv_board_text = (TextView) findViewById(R.id.tv_board_text);
        Glide.with(this).load(domain + note.getUser().getImage()).error(R.drawable.user).into(iv_user_image);
        tv_writer.setText(note.getUser().getNickname());
        tv_write_time.setText(note.getDate());
        tv_board_text.setText(note.getBody());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_send:
                Intent noteIntent=new Intent(this,NoteWriteActivity.class);
                noteIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                noteIntent.putExtra("recipient",note.getUser());
                startActivity(noteIntent);
                break;
            case R.id.delete:
                Intent intent=new Intent();
                intent.putExtra("position",getIntent().getIntExtra("position",0));
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
        return false;
    }
}
