package com.info.idol.community.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.info.idol.community.R;


public class LoginButton extends LinearLayout {
    LinearLayout bg;
    ImageView symbol;
    TextView text;

    //view를 코드상에서 생성할때
    public LoginButton(Context context) {
        super(context);
        initView();
    }
    //view를 xml상에서 inflating 될때
    public LoginButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
    }

    public LoginButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        getAttrs(attrs,defStyleAttr);
    }

    private void initView(){
        String infService=Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li=(LayoutInflater)getContext().getSystemService(infService);
        View vIew=li.inflate(R.layout.custom_login_button,this,false);
        addView(vIew);

        bg=(LinearLayout)findViewById(R.id.bg);
        symbol=(ImageView)findViewById(R.id.symbol);
        text=(TextView)findViewById(R.id.text);
    }

    private void getAttrs(AttributeSet attrs){
        TypedArray typedArray=getContext().obtainStyledAttributes(attrs,R.styleable.LoginButton);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle){
        TypedArray typedArray=getContext().obtainStyledAttributes(attrs,R.styleable.LoginButton,defStyle,0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typeArray){
        int bg_resID=typeArray.getResourceId(R.styleable.LoginButton_bg,R.drawable.kakao_login_button_background);
        bg.setBackgroundResource(bg_resID);

        int symbol_resID=typeArray.getResourceId(R.styleable.LoginButton_symbol,R.drawable.kakao_login_symbol);
        symbol.setImageResource(symbol_resID);

        int textColor=typeArray.getColor(R.styleable.LoginButton_textColor,0);
        text.setTextColor(textColor);

        String text_string=typeArray.getString(R.styleable.LoginButton_text);
        text.setText(text_string);

        typeArray.recycle();
    }

    public void setBg(int bg_resID) {
        bg.setBackgroundResource(bg_resID);
    }

    public void setSymbol(int symbol_resID) {
        symbol.setImageResource(symbol_resID);
    }

    public void setTextColor(int color) {
        text.setTextColor(color);
    }

    void setText(String text_string){
        text.setText(text_string);
    }
    void setText(int text_resID){
        text.setText(text_resID);
    }
}
