package com.info.idol.community.Adapter;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.info.idol.community.Class.Board;
import com.info.idol.community.NoticViewActivity;
import com.info.idol.community.R;

import java.util.ArrayList;
import java.util.List;

public class NoticAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM=1;
    private final int VIEW_PROG=0;
    private ArrayList<Board> itemList;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isMoreLoading = true;

    public NoticAdapter(OnLoadMoreListener onLoadMoreListener){
        this.mOnLoadMoreListener=onLoadMoreListener;
        itemList=new ArrayList<>();
    }
    //뷰생성
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==VIEW_ITEM){
            return new BoardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notic,parent,false));
        }else{
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress,parent,false));
        }
    }

    //화면에 셀이 그려질 때마다 호출
    //viewHolder에 설정한 view안에 있는 위젯들에 데이터를 세팅
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof BoardViewHolder){
            Board singleItem=(Board) itemList.get(position);
            ((BoardViewHolder)holder).title_text.setText(singleItem.getTitle());
            ((BoardViewHolder)holder).date_text.setText(singleItem.getDate());
            ((BoardViewHolder) holder).root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //다음 엑티비티로 게시물 번호 넘겨줌
                    Intent intent=new Intent(view.getContext(), NoticViewActivity.class);
                    intent.putExtra("bno",itemList.get(position).getBno());
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position) !=null? VIEW_ITEM:VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public void addAll(List<Board> lst){
        itemList.clear();
        itemList.addAll(lst);
        notifyDataSetChanged();
    }

    public void addItemMore(List<Board> lst){
        if(lst!=null) {
            int sizeInit = itemList.size();
            itemList.addAll(lst);
            notifyItemRangeChanged(sizeInit, itemList.size());
        }
    }

    public void setMore(boolean isMore) {
        this.isMoreLoading = isMore;
    }

    public void showLoading() {
        if (isMoreLoading && itemList != null && mOnLoadMoreListener != null) {
            isMoreLoading = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    itemList.add(null);
                    notifyItemInserted(itemList.size() - 1);
                    mOnLoadMoreListener.onLoadMore();
                }
            });
        }
    }

    public void dismissLoading() {
        if (itemList != null && itemList.size() > 0) {
            itemList.remove(itemList.size() - 1);
            notifyItemRemoved(itemList.size());
        }
    }







    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    //뷰홀더 클래스
    static class BoardViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout root;
        public TextView title_text,date_text;

        public BoardViewHolder(View itemView) {
            super(itemView);
            root=(LinearLayout)itemView.findViewById(R.id.root_layout);
            title_text=(TextView)itemView.findViewById(R.id.text_title);
            date_text=(TextView)itemView.findViewById(R.id.text_date);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder{
        public ProgressBar mProgressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            mProgressBar=(ProgressBar)itemView.findViewById(R.id.pBar);
        }
    }

}
