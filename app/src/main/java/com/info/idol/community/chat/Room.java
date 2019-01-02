package com.info.idol.community.chat;

public class Room {
    private int id;
    private String name;
    private String nickname;
    private int peopleNum;
    private int capacity;

    public Room(int id, String name, String nickname, int peopleNum, int capacity) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.peopleNum = peopleNum;
        this.capacity = capacity;
    }

    public Room(int id, String name, int peopleNum, int capacity) {
        this.id = id;
        this.name = name;
        this.peopleNum = peopleNum;
        this.capacity = capacity;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(int peopleNum) {
        this.peopleNum = peopleNum;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", peopleNum=" + peopleNum +
                ", capacitiy=" + capacity +
                '}';
    }
}
