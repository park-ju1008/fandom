package com.info.idol.community.Class;


import java.io.Serializable;

@SuppressWarnings("serial")
public class Star implements Serializable {
    private String id;
    private String name;
    private String domainkey;
    private String boardkey;
    private String domain;
    private String notic;
    private String schedule;
    private int ent;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDomainkey() {
        return domainkey;
    }

    public String getBoardkey() {
        return boardkey;
    }

    public String getDomain() {
        return domain;
    }

    public String getNotic() {
        return notic;
    }

    public String getSchedule() {
        return schedule;
    }

    public int getEnt() {
        return ent;
    }
}
