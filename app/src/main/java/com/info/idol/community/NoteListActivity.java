package com.info.idol.community;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

public class NoteListActivity extends BaseActivity {
    private Button bt_recv,bt_send;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        initVIew();
    }

    private void initVIew(){
        bt_recv=(Button)findViewById(R.id.bt_recv);
        bt_send=(Button)findViewById(R.id.bt_send);
        bt_recv.setOnClickListener(topButtonsListener);
        bt_send.setOnClickListener(topButtonsListener);
    }

    View.OnClickListener topButtonsListener  = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.bt_recv){
                bt_recv.setSelected(true);
                bt_send.setSelected(false);
            }
            else{
                bt_recv.setSelected(false);
                bt_send.setSelected(true);
            }
        }
    };
}
