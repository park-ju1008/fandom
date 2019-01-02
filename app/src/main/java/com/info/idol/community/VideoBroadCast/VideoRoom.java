package com.info.idol.community.VideoBroadCast;

import com.info.idol.community.chat.Room;

public class VideoRoom extends Room {
    String thumbnail;
    String nickname;

    public VideoRoom(int id, String name, int peopleNum,int capacity,String nickname,String thumbnail) {
        super(id, name, peopleNum,capacity);
        this.nickname=nickname;
        this.thumbnail=thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

