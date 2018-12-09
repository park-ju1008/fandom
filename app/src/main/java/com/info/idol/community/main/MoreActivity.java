package com.info.idol.community.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.info.idol.community.NoteListActivity;
import com.info.idol.community.R;

public class MoreActivity extends BottomNavigationParentActivity implements View.OnClickListener {
    private Button bt_note;
    @Override
    public int getCurrentActivityLayoutName() {
        return R.layout.activity_more;
    }

    @Override
    public int getCurrentSelectedBottomMenuItemID() {
        return R.id.action_four;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){
        bt_note=(Button)findViewById(R.id.bt_note);
        bt_note.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_note:
                Intent intent=new Intent(view.getContext(),NoteListActivity.class);
                startActivity(intent);
                break;
        }
    }
}
