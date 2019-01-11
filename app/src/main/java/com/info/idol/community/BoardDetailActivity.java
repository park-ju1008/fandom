package com.info.idol.community;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.info.idol.community.Adapter.BoardDetailAdapter;
import com.info.idol.community.Class.Board;
import com.info.idol.community.Class.BoardDetail;
import com.info.idol.community.Class.Comment;
import com.info.idol.community.Class.RecyclerViewTouchListener;
import com.info.idol.community.Class.SoftKeyboard;
import com.info.idol.community.Class.User;
import com.info.idol.community.retrofit.ApiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardDetailActivity extends BaseActivity {
    private BoardDetailAdapter mAdapter;
    private User mUser;
    private EditText et_input;
    private ImageView iv_send_btn;
    private RecyclerView recyclerView;
    private InputMethodManager imm;
    private ApiService apiService;
    private Board board;
    private HashMap<String, Object> input;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);
        initView();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ConstraintLayout constraintLayout = findViewById(R.id.constraint_wrapper);

        mUser = GlobalApplication.getGlobalApplicationContext().getUser();
        final Intent intent = getIntent();
        board = intent.getParcelableExtra("content");
        mAdapter.addItem(new BoardDetail(0, board));
        mAdapter.setOnLikeListener(new BoardDetailAdapter.onLikeListener() {
            @Override
            public void onClick(Boolean check) {
                if (check) {
                    input.put("type", "like");
                } else {
                    input.put("type", "unlike");
                }
                apiService.postBoardLike(input).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (!response.body()) {
                            Toast.makeText(BoardDetailActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {

                    }
                });
            }
        });
        apiService = GlobalApplication.getGlobalApplicationContext().getRetrofitApiService();
        input = new HashMap<>();
        input.put("type", "check");
        input.put("boardId", board.getBno());
        input.put("ucode", mUser.getUid());
        apiService.postBoardLike(input).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body()) {
                    mAdapter.setIslike(true);
                } else {
                    mAdapter.setIslike(false);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("TTTT", t.toString());
            }
        });
        apiService.getCommentList(board.getBno()).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        ArrayList<BoardDetail> boardDetails = new ArrayList<>();
                        for (Comment comment : response.body()) {
                            Log.e("TESTCOMM", comment.toString());
                            if (comment.getParent() == null) {
                                boardDetails.add(new BoardDetail(mAdapter.ITEM_VIEW_TYPE_COMMENT, comment));
                            } else {
                                boardDetails.add(new BoardDetail(mAdapter.ITEM_VIEW_TYPE_RECOMMENT, comment));

                            }
                        }
                        mAdapter.addItems(boardDetails);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {

            }
        });
        //키보드 닫으면 선택된 댓글의 대한 정보를 없애기위해서 키보드 닫힘 상태를 확인.
        SoftKeyboard softKeyboard = new SoftKeyboard(constraintLayout, imm);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {

                //댓글은 입력하지 않았고 댓글의 선택상황은 신경쓰지않음.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (et_input.length() != 0) {
                            checkDialog(0);
                        } else {
                            mAdapter.setSelectedPosition(0);
                            et_input.setHint("댓글을 입력하세요.");
                        }
                    }
                });

            }

            @Override
            public void onSoftKeyboardShow() {

            }
        });
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        et_input = (EditText) findViewById(R.id.et_input);
        iv_send_btn = (ImageView) findViewById(R.id.iv_write_btn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new BoardDetailAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, new RecyclerViewTouchListener.RecyclerViewClickListener() {

            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                Log.e("user", mUser.toString());
                BoardDetail boardDetail = mAdapter.getItem(position);

                if (boardDetail.getType() != mAdapter.ITEM_VIEW_TYPE_BODY) {

                    List<String> listItems = new ArrayList<>();
                    Comment selComment = (Comment) mAdapter.getItem(position).getData();
                    if (boardDetail.getType() == mAdapter.ITEM_VIEW_TYPE_COMMENT) {
                        //부모 댓글을 클릭했을시에
                        listItems.add("대댓글 달기");
                    }
                    if (selComment.getUser().getUid() == mUser.getUid()) {
                        listItems.add("삭제");
                    } else {
                        listItems.add("쪽지 보내기");
                        listItems.add("신고");
                    }
                    CharSequence[] items = listItems.toArray(new String[listItems.size()]);
                    //선택되는 댓글에 따라 리스트를 다르게 만들어서 다이얼로그로 넘긴다.
                    dialog(items, position);
                }
            }
        }));
        iv_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_input.length() == 0) {
                    Toast.makeText(view.getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    //서버로 데이터를 전송하는 부분
                    final int selComment = mAdapter.getSelectedPosition();
                    HashMap<String, Object> input = new HashMap<>();
                    input.put("content", et_input.getText().toString());
                    input.put("ucode", mUser.getUid());
                    input.put("bno", board.getBno());
                    if (selComment != 0) {
                        //대댓글을 다는 경우
                        Comment comment = (Comment) mAdapter.getItem(selComment).getData();
                        input.put("parent", comment.getCno());
                    }
                    progressON("작성중");
                    apiService.postComment(input).enqueue(commentCallback);

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.detailmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getIntent().putExtra("content", board);
                setResult(RESULT_OK, getIntent());
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        getIntent().putExtra("content", board);

        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void dialog(final CharSequence[] items, final int position) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                final Comment comment = (Comment) mAdapter.getItem(position).getData();
                String selectedText = items[index].toString();
                if (selectedText.equals("대댓글 달기")) {
                    if (et_input.length() != 0) {
                        checkDialog(position);
                    } else {
                        mAdapter.setSelectedPosition(position);
                        et_input.setHint("대댓글을 입력하세요.");
                        et_input.requestFocus();
//                        imm.showSoftInput(et_input,InputMethodManager.SHOW_IMPLICIT);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 1);
                    }

                } else if (selectedText.equals("삭제")) {
                    progressON("삭제중..");
                    apiService.postDeleteComment(comment.getCno(), board.getBno()).enqueue(commentCallback);

                } else if (selectedText.equals("쪽지 보내기")) {
                    User user = comment.getUser();
                    Intent intent = new Intent(BoardDetailActivity.this, NoteWriteActivity.class);
                    intent.putExtra("recipient", user);
                    startActivity(intent);
                    overridePendingTransition(R.anim.sliding_up, R.anim.stay);
                } else {
                    //신고하기
                }
            }
        });
        builder.show();
    }

    /**
     * 이미 작성중인 댓글이있다면 다이얼로그를 띄워 변경여부를 물어 현재 선택된 댓글을 바꿔줌.
     *
     * @param position 선택된 리사이클러뷰 아이템 position
     */
    private void checkDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BoardDetailActivity.this, R.style.AlertDialogCustom);
        builder.setMessage("작성중인 댓글이 있습니다. 삭제하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAdapter.setSelectedPosition(position);
                if (position != 0) {
                    et_input.setHint("대댓글을 입력하세요.");
                    et_input.requestFocus();
                    imm.showSoftInput(et_input,0);
//                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else {
                    et_input.setHint("댓글을 입력하세요.");
                }
                et_input.setText(null);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                et_input.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 1);
            }
        });
        builder.show();
    }

    private Callback commentCallback=new Callback<List<Comment>>() {
        @Override
        public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
            progressOFF();
            if (response.isSuccessful()) {
                ArrayList<BoardDetail> boardDetails = new ArrayList<>();
                int count = 0;
                for (Comment comment : response.body()) {
                    Log.e("TESTCOMM", comment.toString());
                    if (comment.getParent() == null) {
                        boardDetails.add(new BoardDetail(mAdapter.ITEM_VIEW_TYPE_COMMENT, comment));
                    } else {
                        boardDetails.add(new BoardDetail(mAdapter.ITEM_VIEW_TYPE_RECOMMENT, comment));

                    }
                    if (comment.getState() == 0) {
                        count++;
                    }
                }
                board.setComment(count);

                et_input.setText(null);
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                imm.hideSoftInputFromWindow(et_input.getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
//                imm.hideSoftInputFromWindow(et_input.getWindowToken(), 0);
                int position=mAdapter.getSelectedPosition();
                mAdapter.setSelectedPosition(0);
                mAdapter.changeComment(boardDetails);
                if(position!=0){
                    recyclerView.scrollToPosition(position);
                }
            }
        }

        @Override
        public void onFailure(Call<List<Comment>> call, Throwable t) {

        }
    };

}
