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
}
