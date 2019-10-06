package com.hafniumsoftware.diapason;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Objects;

public class SongSelector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_selector);
        Objects.requireNonNull(getSupportActionBar()).hide();
        TextView textView = findViewById(R.id.song_selector_artist_name_textview);
        textView.setText(getIntent().getStringExtra("CName"));

    }
}