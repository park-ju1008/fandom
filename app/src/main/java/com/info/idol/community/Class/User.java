package com.info.idol.community.Class;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private int uid;
    private String nickname;
    private String image;

    public User(int uid, String nickname, String image) {
        this.uid = uid;
        this.nickname = nickname;
        this.image = image;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", nickname='" + nickname + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    protected User(Parcel in) {
        uid = in.readInt();
        nickname = in.readString();
        image = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeString(nickname);
        parcel.writeString(image);
    }
}
