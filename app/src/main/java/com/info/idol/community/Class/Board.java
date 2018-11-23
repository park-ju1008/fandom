package com.info.idol.community.Class;

public class Board {
    private String bno;
    private String title;
    private String date;
    private String writer;

    public Board(String bno, String title, String date) {
        this.bno = bno;
        this.title = title;
        this.date = date;
    }

    public Board(String bno, String title, String date, String writer) {
        this.bno = bno;
        this.title = title;
        this.date = date;
        this.writer = writer;
    }

    public String getBno() {
        return bno;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getWriter() {
        return writer;
    }
}
