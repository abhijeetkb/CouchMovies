package com.couchbase.couchmovies.movies;

public class Movie {
    private String title;
    private String genre;
    private int year;
    private String director;
    private float rating;
    private String imageUrl;
    private String description;
    private String category;
    private int watchProgress;
    private boolean isTop10;
    private int top10Rank;

    // Full constructor
    public Movie(String title, String genre, String description, int year, String director, 
                float rating, String imageUrl, String category, int watchProgress, 
                boolean isTop10, int top10Rank) {
        this.title = title;
        this.genre = genre;
        this.description = description;
        this.year = year;
        this.director = director;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.category = category;
        this.watchProgress = watchProgress;
        this.isTop10 = isTop10;
        this.top10Rank = top10Rank;
    }

    // Constructor with basic fields
    public Movie(String title, String genre, int year, String director, float rating, 
                String imageUrl, String description) {
        this(title, genre, description, year, director, rating, imageUrl, "", 0, false, 0);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public float getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getWatchProgress() {
        return watchProgress;
    }

    public boolean isTop10() {
        return isTop10;
    }

    public int getTop10Rank() {
        return top10Rank;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setWatchProgress(int watchProgress) {
        this.watchProgress = watchProgress;
    }

    public void setTop10(boolean top10) {
        isTop10 = top10;
    }

    public void setTop10Rank(int top10Rank) {
        this.top10Rank = top10Rank;
    }
} 