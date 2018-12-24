package com.info.idol.community.chat;

import com.info.idol.community.Class.User;

public class Chat {
    int cid;
    String content;
    int act; //입장 글과 나간글을 표시하기위한 변수
    User user;

    public Chat(int cid, String content,int act, User user) {
        this.cid = cid;
        this.content = content;
        this.act=act;
        this.user = user;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
