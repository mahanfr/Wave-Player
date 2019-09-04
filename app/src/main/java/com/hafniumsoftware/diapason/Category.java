package com.hafniumsoftware.diapason;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.InputStream;
import java.util.ArrayList;

public class Category {
    private String title;
    private ArrayList<Audio> audios;
    private long albumId;
    private String artist;

    public Category(String title, ArrayList<Audio> audios) {
        this.title = title;
        this.audios = audios;
    }

    public Category(String title,String artist, ArrayList<Audio> audios,Long albumId) {
        this.title = title;
        this.audios = audios;
        this.albumId = albumId;
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getCategoryName(){
        return title;
    }

    public ArrayList<Audio> getCategoryAudios() {
        return audios;
    }

    public void setCategoryAudios(ArrayList<Audio> audios) {
        this.audios = audios;
    }

    public void setCategoryTitle(String title) {
        this.title = title;
    }

    public Bitmap getImage(Context ctx){
        Bitmap bitmap = null;
        ContentResolver contentResolver = ctx.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart");
        Uri CoverUri = ContentUris.withAppendedId(uri, albumId);
        try{
            InputStream inputStream = contentResolver.openInputStream(CoverUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(inputStream,null,options);
        }catch (Exception e){
            e.printStackTrace();
        }
        assert bitmap != null;
        //return Bitmap.createScaledBitmap(bitmap, 120, 120, false);
        return bitmap;
    }
}