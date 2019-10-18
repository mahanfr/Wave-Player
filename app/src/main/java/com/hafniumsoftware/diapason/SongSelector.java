package com.hafniumsoftware.diapason;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import java.util.Objects;

public class SongSelector extends AppCompatActivity {
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_selector);
        Objects.requireNonNull(getSupportActionBar()).hide();
        TextView textView = findViewById(R.id.song_selector_artist_name_textview);
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