package com.theworldmatrix.cocktailmusicsearch;

import java.util.ArrayList;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class Song {

    private int id;
    private String echoNestID;
    private String title;
    private String artist;
    private String album;
    private String album_img;

    public Song(int songID, String songTitle, String songArtist,
                String songAlbum, String songAlbumImg, String songEchoID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        album=songAlbum;
        album_img=songAlbumImg;
        echoNestID=songEchoID;
    }

    public Song(int songID, String songTitle, String songArtist,
                String songAlbum, String songAlbumImg) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        album=songAlbum;
        album_img=songAlbumImg;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Song && id==((Song)other).id;
    }

    @Override
    public int hashCode() {
        //if (id > Integer.MAX_VALUE) throw new Error("Bad hash conversion in Song. Int overflow.");
        return id;
    }

    public int getId(){return id;}
    public String getEchoNestID() {return echoNestID;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
    public String getAlbum_img(){return album_img;}

    public void setId(int id){this.id=id;}
    public void setAlbum(String album) {
        this.album = album;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public void setEchoNestID(String echoNestID) {
        this.echoNestID = echoNestID;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAlbum_img(String img) {this.album_img = img;}
}
