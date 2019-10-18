package com.hafniumsoftware.diapason;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.Serializable;

public class Audio  {

    private String data;
    private String title;
    private String album;
    private String artist;
    private long albumID;
    private long Id;

    Audio(long Id, String data, String title, String album, String artist, long albumID){
        this.Id = Id;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumID = albumID;
    }

    Audio(long Id, String data, String title, String album, String artist){
        this.Id = Id;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public long getAlbumID(){
        return albumID;
    }

    public void setCover(long albumID){
        this.albumID = albumID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getId(){
        return Id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
