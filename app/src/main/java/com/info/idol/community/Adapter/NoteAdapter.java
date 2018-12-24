package com.info.idol.community.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.info.idol.community.Class.Board;
import com.info.idol.community.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private ArrayList<Board> itemList;
    private SimpleDateFormat outputdateFormat = new SimpleDateFormat("yy/HH/dd HH:mm", Locale.getDefault());
    private SimpleDateFormat inputdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private OnItemListener onItemListener;

    public NoteAdapter(OnItemListener onItemListener) {
        this.onItemListener=onItemListener;
        itemList = new ArrayList<>();
    }

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        Board note = itemList.get(position);
        holder.tv_name.setText(note.getUser().getNickname());
        holder.tv_content.setText(note.getBody());
        try {
            Date date = inputdateFormat.parse(note.getDate());
            holder.tv_time.setText(outputdateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void cleanItem() {
        itemList.clear();
        notifyDataSetChanged();
    }

    public void addItems(List<Board> lst) {
        if (lst != null) {
            int sizeInit = itemList.size();
            itemList.addAll(lst);
            notifyItemRangeChanged(sizeInit, itemList.size());
        }
    }

    public Board getItem(int position){
        return itemList.get(position);
    }

    public void removeItem(int position){
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_content;
        private TextView tv_time;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemListener.onItemClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view) {
                    onItemListener.onItemLongClick(getAdapterPosition());
                    return false;
                }
            });
        }
    }
}
