package com.info.idol.community.Class;

public class Board {
    private String bno;
    private String title;
    private String date;
    private String writer;
    private String image;

    public Board(String bno,String date){
        this.bno = bno;
        this.date = date;
    }
    public Board(String bno, String title, String date) {
        this.bno = bno;
        this.title = title;
        this.date = date;
    }

    public Board(String bno, String title, String date, String writer,String image) {
        this.bno = bno;
        this.title = title;
        this.date = date;
        this.writer = writer;
        this.image=image;
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

    public String getImage() {
        return image;
    }

    public void setBno(String bno) {
        this.bno = bno;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
