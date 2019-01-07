package com.info.idol.community.VideoBroadCast;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.info.idol.community.R;
import com.info.idol.community.chat.Chat;

import java.util.ArrayList;

public class VideoChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_CHAT=0;
    private final int VIEW_ACTION=1;
    private ArrayList<Chat> itemList;

    public VideoChattingAdapter() {
        itemList=new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_CHAT){
            return new VideoChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_chat,parent,false));
        }else{
            return new ActionChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_action,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof VideoChatViewHolder){
            String color=stringToHex(itemList.get(position).getUser().getNickname());
            ((VideoChatViewHolder)holder).tv_name.setText(itemList.get(position).getUser().getNickname()+" :");
            ((VideoChatViewHolder)holder).tv_name.setTextColor(Color.parseColor(color));
            ((VideoChatViewHolder)holder).tv_content.setText(itemList.get(position).getContent());
            ((VideoChatViewHolder)holder).tv_content.setTextColor(Color.parseColor(color));
        }else{
            ((ActionChatViewHolder)holder).tv_action.setText(itemList.get(position).getContent());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getAct();
    }

    public void addItem(Chat chat){
        itemList.add(chat);
        notifyItemInserted(itemList.size()-1);
    }

    class ActionChatViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_action;

        public ActionChatViewHolder(View itemView) {
            super(itemView);
            tv_action=(TextView)itemView.findViewById(R.id.textView_chat_action);
        }
    }

    class VideoChatViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_name;
        public TextView tv_content;

        public VideoChatViewHolder(View itemView) {
            super(itemView);
            tv_name=(TextView)itemView.findViewById(R.id.textView_chat_nickname);
            tv_content=(TextView)itemView.findViewById(R.id.textView_chat_content);
        }
    }

    public String stringToHex(String s) {
        int len=3;
        String result = "#";

        if(s.length()<3){
            len=s.length();
        }
        for (int i = 0; i < len; i++) {
            result += String.format("%02X", ((int) s.charAt(i))%255);
        }

        for(int i=result.length();i<7;i++){
            result+="0";
        }
        return result;
    }

}
