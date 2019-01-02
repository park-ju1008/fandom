package com.info.idol.community.chat;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.info.idol.community.R;

public class SettingDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private EditText et_title;
    private EditText et_capacity;
    private Button bt_cancle;
    private Button bt_ok;
    private onDialogListener onDialogListener;

    public SettingDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void setOnDialogListener(SettingDialog.onDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_room_setting);
        et_title = (EditText) findViewById(R.id.editText_setting_title);
        et_capacity = (EditText) findViewById(R.id.editText_setting_capacity);
        bt_cancle = (Button) findViewById(R.id.button_setting_cancle);
        bt_ok = (Button) findViewById(R.id.button_setting_ok);
        bt_cancle.setOnClickListener(this);
        bt_ok.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_setting_ok:
                if (et_title.getText().toString().isEmpty()) {
                    Toast.makeText(context, "방제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (et_capacity.getText().toString().isEmpty()) {
                    Toast.makeText(context, "방인원수을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(et_capacity.getText().toString()) < 2 || Integer.parseInt(et_capacity.getText().toString()) > 50) {
                    Toast.makeText(context, "최대 인원수는 2~50까지 지정할수 있습니다..", Toast.LENGTH_SHORT).show();
                } else {
                    dismiss();
                    onDialogListener.sendRoomInfo(et_title.getText().toString(), Integer.parseInt(et_capacity.getText().toString()));
                }
                break;
            case R.id.button_setting_cancle:
                dismiss();
                break;
        }
    }

    public interface onDialogListener {
        void sendRoomInfo(String title, int capacity);
    }
}


