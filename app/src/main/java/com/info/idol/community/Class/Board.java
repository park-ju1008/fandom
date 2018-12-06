package com.info.idol.community.Class;

import android.os.Parcel;
import android.os.Parcelable;

public class Board implements Parcelable {
    private String bno;
    private String title;
    private String body;
    private String date;
    private int like;
    private int comment;
    private User user;
    private String image;

    public Board(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Board(String bno, String title, String date) {
        this.bno = bno;
        this.title = title;
        this.date = date;
    }

    public Board(String bno, String title, String date, String image) {
        this.bno = bno;
        this.title = title;
        this.date = date;
        this.image = image;
    }


    protected Board(Parcel in) {
        bno = in.readString();
        title = in.readString();
        body = in.readString();
        date = in.readString();
        like = in.readInt();
        comment = in.readInt();
        user = in.readParcelable(User.class.getClassLoader());
        image = in.readString();
    }

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        @Override
        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    public String getBno() {
        return bno;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public int getLike() {
        return like;
    }

    public int getComment() {
        return comment;
    }

    public User getUser() {
        return user;
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

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setImage(String image) {
        this.image = image;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(bno);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(date);
        parcel.writeInt(like);
        parcel.writeInt(comment);
        parcel.writeParcelable(user, i);
        parcel.writeString(image);
    }

    @Override
    public String toString() {
        return "Board{" +
                "bno='" + bno + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", date='" + date + '\'' +
                ", like=" + like +
                ", comment=" + comment +
                ", user=" + user +
                ", image='" + image + '\'' +
                '}';
    }
}
