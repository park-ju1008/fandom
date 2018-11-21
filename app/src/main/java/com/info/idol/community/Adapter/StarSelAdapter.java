package com.info.idol.community.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.info.idol.community.Class.Star;
import com.info.idol.community.main.MainActivity;
import com.info.idol.community.R;

import java.util.ArrayList;
import java.util.List;

public class StarSelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM=1;
    private final int VIEW_PROG=0;
    private ArrayList<Star> itemList;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isMoreLoading = true;

    public StarSelAdapter(OnLoadMoreListener onLoadMoreListener){
        this.mOnLoadMoreListener=onLoadMoreListener;
        itemList=new ArrayList<>();
    }
    //뷰생성
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==VIEW_ITEM){
            return new StarViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text,parent,false));
        }else{
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress,parent,false));
        }
    }

    //화면에 셀이 그려질 때마다 호출
    //viewHolder에 설정한 view안에 있는 위젯들에 데이터를 세팅
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof StarViewHolder){
            Star singleItem=(Star)itemList.get(position);
            ((StarViewHolder)holder).name_text.setText(singleItem.getName());
            ((StarViewHolder) holder).name_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //스타고유값 로컬 저장.
                    SharedPreferences pref=view.getContext().getSharedPreferences("star",Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putString("starId",itemList.get(position).getId());
                    editor.commit();
                    //다음 엑티비티로 스타 고유 값넘겨줌
                    Intent intent=new Intent(view.getContext(), MainActivity.class);
                    intent.putExtra("starId",itemList.get(position).getId());
                    view.getContext().startActivity(intent);
                    ((Activity)view.getContext()).finish();
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


    public void addAll(List<Star> lst){
        itemList.clear();
        itemList.addAll(lst);
        notifyDataSetChanged();
    }

    public void addItemMore(List<Star> lst){
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
    static class StarViewHolder extends RecyclerView.ViewHolder{
        public TextView name_text;

        public StarViewHolder(View itemView) {
            super(itemView);
            name_text=(TextView)itemView.findViewById(R.id.name_text);
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
