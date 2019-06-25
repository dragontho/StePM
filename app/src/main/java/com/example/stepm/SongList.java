package com.example.stepm;

public class SongList {
    private long id;
    private String title;
    private String artist;

    public SongList(long id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
