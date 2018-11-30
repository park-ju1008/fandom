package com.info.idol.community.Class;

public class Event {
    private String date;
    private String type;
    private String title;
    private String time;

    public Event(String date, String type, String title, String time) {
        this.date = date;
        this.type = type;
        this.title = title;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
