package com.info.idol.community.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.info.idol.community.BoardDetailActivity;
import com.info.idol.community.Class.Board;
import com.info.idol.community.R;
import com.info.idol.community.main.ScheduleActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Board> mSchedules;
    private SimpleDateFormat outputdateFormat = new SimpleDateFormat("HH : mm", Locale.getDefault());
    private SimpleDateFormat inputdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ScheduleAdapter(Context context) {
        this.context=context;
        mSchedules=new ArrayList<>();
    }

    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ViewHolder holder, int position) {
        Board schedule = mSchedules.get(position);
        Log.d("TEST","/"+position);
        try {
            Date date=inputdateFormat.parse(schedule.getTitle());
            holder.text_time.setText(outputdateFormat.format(date));
            holder.text_body.setText(schedule.getBody());
            holder.text_reply.setText(""+schedule.getComment());
            if(!schedule.getImage().isEmpty()){
                JSONArray jarray = new JSONArray(schedule.getImage());
                Glide.with(context).load("http://35.229.103.161/uploads/"+jarray.get(0).toString()).centerCrop().into(holder.img_main);

            }else{
                Glide.with(context).load(R.color.white).into(holder.img_main);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mSchedules.size();
    }
    public void addSchedules(List<Board> schedules){
        mSchedules.clear();
        mSchedules.addAll(schedules);
        notifyDataSetChanged();
    }

    public void addSchedule(Board schedule){

        mSchedules.add(schedule);
        Log.e("TESTadd",schedule.toString());
        notifyItemInserted(mSchedules.size()-1);
        Log.e("TEST","/////"+(mSchedules.size()-1));
        //체인지 알려줘야함
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text_time, text_body, text_reply;
        ImageView img_main;

        public ViewHolder(View itemView) {
            super(itemView);
            text_time = (TextView) itemView.findViewById(R.id.text_time);
            text_body = (TextView) itemView.findViewById(R.id.text_body);
            text_reply = (TextView) itemView.findViewById(R.id.text_reply);
            img_main=(ImageView)itemView.findViewById(R.id.Iv_schedule_image);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Board schedule=mSchedules.get(getAdapterPosition());
            Log.e("TESTCLICK",schedule.toString());
            Intent intent=new Intent(context,BoardDetailActivity.class);
            intent.putExtra("schedule",schedule);
            ((Activity)context).startActivityForResult(intent,ScheduleActivity.COMMENT_COUNT_REQUEST);
        }
    }




}
