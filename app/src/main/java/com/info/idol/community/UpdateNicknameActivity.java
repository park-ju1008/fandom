package com.info.idol.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class UpdateNicknameActivity extends BaseActivity {
    private EditText tv_nickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatenickname);
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("이름 변경");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tv_nickname=(EditText)findViewById(R.id.edittext_nickname);
        tv_nickname.setText(getIntent().getStringExtra("nickname"));
    }

    //ToolBar에 writemenu.xml 불러와 넣음
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.writemenu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                Intent resultIntent = new Intent();
                Log.e("TEST","넘겨줌"+tv_nickname.getText());
                resultIntent.putExtra("nickname",tv_nickname.getText().toString());
                setResult(RESULT_OK,resultIntent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
