package com.info.idol.community.Class;

public class Comment {
    private String cno;
    private String content;
    private String date;
    private String parent;
    private User user;
    private int state;

    public String getCno() {
        return cno;
    }

    public void setCno(String cno) {
        this.cno = cno;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "cno='" + cno + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", parent='" + parent + '\'' +
                ", user=" + user +
                ", state=" + state +
                '}';
    }
}


