package com.info.idol.community.Adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.info.idol.community.Class.Schedule;
import com.info.idol.community.R;

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
    private ArrayList<Schedule> mSchedules;
    private SimpleDateFormat outputdateFormat = new SimpleDateFormat("HH : mm", Locale.getDefault());
    private SimpleDateFormat inputdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        Schedule schedule = mSchedules.get(position);
        Log.d("TEST",schedule.getEventtime());
        try {
            Date date=inputdateFormat.parse(schedule.getEventtime());
            holder.text_time.setText(outputdateFormat.format(date));
            holder.text_title.setText(schedule.getWrite());
            holder.text_reply.setText("0");
            if(!schedule.getImage().isEmpty()){
                JSONArray jarray = new JSONArray(schedule.getImage());
                Glide.with(context).load("http://35.237.204.193/uploads/"+jarray.get(0).toString()).centerCrop().into(holder.img_main);

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
    public void addSchedules(List<Schedule> schedules){
        mSchedules.clear();
        mSchedules.addAll(schedules);
        notifyDataSetChanged();
    }

    public void addSchedule(Schedule schedule){
        mSchedules.add(schedule);
        notifyItemInserted(mSchedules.size()-1);
        //체인지 알려줘야함
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text_time, text_title, text_reply;
        ImageView img_main;

        public ViewHolder(View itemView) {
            super(itemView);
            text_time = (TextView) itemView.findViewById(R.id.text_time);
            text_title = (TextView) itemView.findViewById(R.id.text_title);
            text_reply = (TextView) itemView.findViewById(R.id.text_reply);
            img_main=(ImageView)itemView.findViewById(R.id.Iv_schedule_image);
        }

        @Override
        public void onClick(View view) {
            Log.e("TEST", mSchedules.get(getAdapterPosition()).getWrite());
        }
    }




}
