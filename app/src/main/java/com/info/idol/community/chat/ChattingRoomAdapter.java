package com.info.idol.community.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.info.idol.community.R;
import com.info.idol.community.custom.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class ChattingRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_MY=0;
    private final int VIEW_OTHER=1;
    private final int VIEW_ACTION=2;
    private final String URL="http://35.229.103.161/uploads/";
    private Context context;
    private ArrayList<Chat> itemList;
    private int myuid;

    public ChattingRoomAdapter(Context context,int uid) {
        this.context = context;
        this.myuid=uid;
        this.itemList=new ArrayList<>();
    }


    @Override
    public int getItemViewType(int position) {
        int ret;
        if(itemList.get(position).getAct()==0&&itemList.get(position).getUser().getUid()==myuid){
            ret=VIEW_MY;
        }else if(itemList.get(position).getAct()==0&&itemList.get(position).getUser().getUid()!=myuid){
            ret=VIEW_OTHER;
        }else{
            ret=VIEW_ACTION;
        }
        return ret;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_MY){
            return new MyChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_my,parent,false));
        }else if(viewType==VIEW_OTHER){
            return new OtherChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other,parent,false));
        }else{
            return new ActionChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_action,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyChatViewHolder) {
            ((MyChatViewHolder)holder).tv_content.setText(itemList.get(position).getContent());
        }else if(holder instanceof OtherChatViewHolder){
            Glide.with(context).load(URL+itemList.get(position).getUser().getImage()).error(R.drawable.user).into(((OtherChatViewHolder)holder).cv_userImage);
            ((OtherChatViewHolder)holder).tv_content.setText(itemList.get(position).getContent());
            ((OtherChatViewHolder)holder).tv_nickname.setText(itemList.get(position).getUser().getNickname());
        }else{
            ((ActionChatViewHolder)holder).tv_action.setText(itemList.get(position).getContent());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItems(List<Chat> lst){
        if(lst!=null) {
            int sizeInit = itemList.size();
            itemList.addAll(lst);
            notifyItemRangeChanged(sizeInit, itemList.size());

        }
    }

    public void addItem(Chat chat){
        itemList.add(chat);
        notifyItemInserted(itemList.size()-1);
    }


    //뷰홀더 클래스
    static class MyChatViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_content;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tv_content=(TextView)itemView.findViewById(R.id.textView_chat_content);
        }
    }

    static class OtherChatViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView cv_userImage;
        public TextView tv_nickname;
        public TextView tv_content;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            cv_userImage=(CircleImageView)itemView.findViewById(R.id.circleview_chat_user);
            tv_nickname=(TextView)itemView.findViewById(R.id.textView_chat_nickname);
            tv_content=(TextView)itemView.findViewById(R.id.textView_chat_content);
        }
    }

    static class ActionChatViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_action;

        public ActionChatViewHolder(View itemView) {
            super(itemView);
            tv_action=(TextView)itemView.findViewById(R.id.textView_chat_action);
        }
    }

}
