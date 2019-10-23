package com.hafniumsoftware.diapason;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Objects;

public class SongSelector extends AppCompatActivity {
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_selector);
        Objects.requireNonNull(getSupportActionBar()).hide();
        TextView textView = findViewById(R.id.song_selector_artist_name_textview);
        ImageView imageView = findViewById(R.id.song_selector_album_image_imageview);
        String string = getIntent().getStringExtra("CImage");
        Uri uri = Uri.parse(string);
        Bitmap bitmap = null;
        ContentResolver contentResolver = getContentResolver();
        try{
            InputStream inputStream = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (Exception e){
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        textView.setText(getIntent().getStringExtra("CName"));
        RecyclerView recyclerView = findViewById(R.id.song_selector_reletivelayout);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecycleViewListAdapter recycleViewListAdapter = new RecycleViewListAdapter(new StorageUtil(this)
                .loadCategory("Artist").get(getIntent().getIntExtra("CVal",0)).getCategoryAudios()
                ,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recycleViewListAdapter);
    }

}