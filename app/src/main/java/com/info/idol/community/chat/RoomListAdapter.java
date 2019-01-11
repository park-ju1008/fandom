package com.info.idol.community.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.info.idol.community.Adapter.OnLoadMoreListener;
import com.info.idol.community.Class.Star;
import com.info.idol.community.GlobalApplication;
import com.info.idol.community.R;
import com.info.idol.community.VideoBroadCast.liveVideoPlayer.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Context context;
    private ArrayList<Room> itemList;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isMoreLoading = true;


    public RoomListAdapter(Context context) {
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
            return new RoomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof RoomViewHolder) {
            ((RoomViewHolder) holder).tv_name.setText(itemList.get(position).getName());
            ((RoomViewHolder) holder).tv_capacity.setText(itemList.get(position).getPeopleNum() + "/" + itemList.get(position).getCapacity());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlobalApplication.getGlobalApplicationContext().getRetrofitApiService().getAvailableRoom(itemList.get(position).getId()).enqueue(new Callback<RoomFactory>() {
                        @Override
                        public void onResponse(Call<RoomFactory> call, Response<RoomFactory> response) {
                            Intent intent = new Intent(context, ChattingRoomActivity.class);
                            switch (response.body().getCondition()) {
                                case 0:
                                    //방에 여유 공간이 있을때
                                    Room room=response.body().getRoom();
                                    room.setPeopleNum(room.getPeopleNum()+1);
                                    MyDataBase.getInstance(context).insertRoom(room);
                                    intent.putExtra("method", "enter_room");
//                                    intent.putExtra("roomName", itemList.get(position).getName());
                                    intent.putExtra("roomId", itemList.get(position).getId());
//                                    intent.putExtra("capacity", itemList.get(position).getCapacity());
                                    ((Activity) context).startActivity(intent);
                                    break;
                                case 1:
                                    //방인원이 다 찼을때
                                    if (MyDataBase.getInstance(context).findMyRoom(itemList.get(position).getId())) {
                                        intent.putExtra("method", "enter_room");
//                                        intent.putExtra("roomName", itemList.get(position).getName());
                                        intent.putExtra("roomId", itemList.get(position).getId());
//                                        intent.putExtra("capacity", itemList.get(position).getCapacity());
                                        ((Activity) context).startActivity(intent);
                                    }else {
                                        Toast.makeText(context, R.string.full_videoRoom, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 2:
                                    //방이 없다면
                                    Toast.makeText(context, R.string.end_room, Toast.LENGTH_SHORT).show();
                                    itemList.remove(position);
                                    notifyItemRemoved(position);
                                    break;
                                default:
                                    break;
                            }
                        }

                        @Override
                        public void onFailure(Call<RoomFactory> call, Throwable t) {
                            Log.e("TEST",t.toString());
                        }
                    });
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

    //뷰홀더 클래스
    class RoomViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name;
        public TextView tv_capacity;

        public RoomViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.text_title);
            tv_capacity = (TextView) itemView.findViewById(R.id.text_date);
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
