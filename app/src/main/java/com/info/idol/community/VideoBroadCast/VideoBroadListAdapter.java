package com.info.idol.community.VideoBroadCast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.info.idol.community.Adapter.OnLoadMoreListener;
import com.info.idol.community.R;
import com.info.idol.community.VideoBroadCast.liveVideoPlayer.VideoPlayerActivity;
import com.info.idol.community.chat.Room;

import java.util.ArrayList;
import java.util.List;

public class VideoBroadListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Context context;
    private ArrayList<Room> itemList;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isMoreLoading = true;


    public VideoBroadListAdapter(Context context) {
        this.context = context;
        this.itemList = new ArrayList<>();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new VideoRoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof VideoRoomViewHolder){
            ((VideoRoomViewHolder)holder).tv_title.setText(itemList.get(position).getName());
            ((VideoRoomViewHolder)holder).tv_name.setText(itemList.get(position).getNickname());
            ((VideoRoomViewHolder)holder).tv_people.setText(""+itemList.get(position).getPeopleNum());
            //이미지도 넣어줘야함.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(context,VideoPlayerActivity.class);
                    intent.putExtra("method","enter_room");
                    intent.putExtra("roomName", itemList.get(position).getName());
                    intent.putExtra("roomId",itemList.get(position).getId());
                    ((Activity)context).startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItems(List<Room> lst) {
        if (lst != null) {
            int sizeInit = itemList.size();
            itemList.addAll(lst);
            notifyItemRangeChanged(sizeInit, itemList.size());
        }
    }

    public void cleanItem() {
        itemList.clear();
        notifyDataSetChanged();
    }



    class VideoRoomViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_title;
        public TextView tv_name;
        public TextView tv_people;
        public ImageView iv_thumbnail;

        public VideoRoomViewHolder(View itemView) {
            super(itemView);
            tv_title=(TextView)itemView.findViewById(R.id.textView_video_title);
            tv_name=(TextView)itemView.findViewById(R.id.textView_video_name);
            tv_people=(TextView)itemView.findViewById(R.id.textView_video_people);
            iv_thumbnail=(ImageView)itemView.findViewById(R.id.imageView_video_thumbnail);
        }
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar mProgressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.pBar);
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
}
