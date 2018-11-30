package com.info.idol.community.Class;

import android.os.Parcel;
import android.os.Parcelable;


public class Schedule extends Board implements Parcelable {
    private String eventtime;
    private String write;

    protected Schedule(Parcel in) {
        super(in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
        eventtime = in.readString();
        write = in.readString();
    }

    public Schedule(String bno, String date, String eventtime, String write) {
        super(bno, date);
        this.eventtime = eventtime;
        this.write = write;
    }


    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    public String getEventtime() {
        return eventtime;
    }

    public String getWrite() {
        return write;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getBno());
        parcel.writeString(getTitle());
        parcel.writeString(getDate());
        parcel.writeString(getWriter());
        parcel.writeString(getImage());
        parcel.writeString(eventtime);
        parcel.writeString(write);
    }
}
