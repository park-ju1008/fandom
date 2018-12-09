package com.info.idol.community.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.info.idol.community.R;

//추상클래스
public abstract class BottomNavigationParentActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public BottomNavigationView mBottomNavigationView;
    //액티비디들이 구현할 추상 메소드

    //현재 탭에 부착할 액티비티의 실제 레이아웃리소스값
    public abstract int getCurrentActivityLayoutName();

    //현재 사용자가 클릭한 메뉴객체
    public abstract int getCurrentSelectedBottomMenuItemID();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //연결된 액티비티의레이아웃리소스 값으로 연결 시킨다.
        setContentView(getCurrentActivityLayoutName());
        mBottomNavigationView=(BottomNavigationView)findViewById(R.id.navigation);

        //애니메이션 디폴트 효과 제거 부분
        // 테스트 후 추가

        //하단탭을 눌렀을때 발생하는 이벤트
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
    }


    //bottomNavigation 클릭했을 때 해당 액티비티로 이동
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        mBottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int menuID=item.getItemId();
                Intent menuIntent=null;
                switch (menuID){
                    case R.id.action_one:
                        menuIntent=new Intent(BottomNavigationParentActivity.this,MainActivity.class);
                        break;
                    case R.id.action_two:
                        menuIntent=new Intent(BottomNavigationParentActivity.this,BoardActivity.class);
                        break;
                    case R.id.action_three:
                        menuIntent=new Intent(BottomNavigationParentActivity.this,ScheduleActivity.class);
                        break;
                    case R.id.action_four:
                        menuIntent=new Intent(BottomNavigationParentActivity.this,MoreActivity.class);
                        break;
                }
                startActivity(menuIntent);
                overridePendingTransition(0,0);
                finish();
            }
        },0);
        return true;
    }

    /*
      Activity Life Cycle를 이용해 해당 눌려진  하단탭  icon을  알아낸다.
     */
    @Override
    protected void onStart() {
        super.onStart();
        //현재 사용자가 선택한 하단탭을 찾아 효과(selector)를 준다
        setCurrenttSelectedNavigationBarItem(getCurrentSelectedBottomMenuItemID());
    }

    //현재 선언된 액티비티를 찾는 메소드
    public void setCurrenttSelectedNavigationBarItem(int itemId) {
        Menu menu = mBottomNavigationView.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                //이 메소드가 호출시 메뉴아이템에 selector(우린 mainbutton.xml)효과가 나타남
                item.setChecked(true);
                break;
            }
        }
    }
}
