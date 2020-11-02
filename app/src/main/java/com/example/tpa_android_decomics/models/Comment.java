package com.example.tpa_android_decomics.models;

import java.util.ArrayList;

public class Comment {
    protected String name,desc;
    private String id;
    private int like = 0, dislike = 0;
    private ArrayList<ReplyComment> repCom = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<ReplyComment> getRepCom() {
        return repCom;
    }

    public void addRep(ReplyComment replyComment){
        repCom.add(replyComment);
    }

    public void setRepCom(ArrayList<ReplyComment> repCom) {
        this.repCom = repCom;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
