package com.example.tpa_android_decomics.models;

import java.util.ArrayList;

public class Comic {

    private String id;
    private String name;
    private String description;
    private String genre;
    private String image;
    private ArrayList<Integer> ratings;
    private ArrayList<ComicChapter> chapters;
    private float rating;
    private String premium;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPremium() {
        return premium;
    }

    public void setPremium(String premium) {
        this.premium = premium;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public ArrayList<Integer> getRatings() {
        return ratings;
    }

    public void setRatings(ArrayList<Integer> ratings) {
        this.ratings = ratings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<ComicChapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<ComicChapter> chapters) {
        this.chapters = chapters;
    }
}
