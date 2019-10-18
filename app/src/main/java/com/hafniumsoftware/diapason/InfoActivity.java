package com.hafniumsoftware.diapason;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Objects;

public class InfoActivity extends AppCompatActivity {

    private Intent playIntent;
    private MediaPlayerService mediaPlayerService;
    TextView t1 ,t2;
    //ImageView imageView;
    RelativeLayout rt;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        t1 = findViewById(R.id.textView);
        t2 = findViewById(R.id.textView2);
        t1.setSelected(true);
        t2.setSelected(true);
        //imageView = findViewById(R.id.imageView);
        rt = findViewById(R.id.bc_info);
        //Objects.requireNonNull(getSupportActionBar()).hide();
        LocalBroadcastManager.getInstance(this).registerReceiver(listener,new IntentFilter("NEW_SONG"));
    }


    @Override
    protected void onStart(){
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)iBinder;
            mediaPlayerService = binder.getService();
            updateMetaData();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    public void play(View v){
        if(!mediaPlayerService.isPlaying()){
            mediaPlayerService.Play();
            v.setBackgroundResource(android.R.drawable.ic_media_pause);
        }else{
            mediaPlayerService.Pause();
            v.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }
    public void next(View v){
        mediaPlayerService.playNext();
    }
    public void previous(View v){
        mediaPlayerService.playPrev();
    }

    private Bitmap CropBitmap(Bitmap bm){
        return Bitmap.createBitmap(bm, bm.getWidth()/4,0, bm.getWidth() / 2, bm.getHeight());
    }
    private Bitmap darkenBitMap(Bitmap bm) {
        Bitmap output = Bitmap.createBitmap(bm.getWidth(),bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint p = new Paint(Color.RED);
        //ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return Bitmap.createScaledBitmap(output, 720, 1280, true);
        //return Bitmap.createBitmap(output, 0,0,720,1280);
    }
    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            updateMetaData();
        }
    };

    private void updateMetaData() {
        t1.setText(mediaPlayerService.getCurrentSong().getTitle());
        t2.setText(mediaPlayerService.getCurrentSong().getArtist());
        ContentResolver contentResolver = getContentResolver();
        //Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri uri = Uri.parse("content://media/external/audio/albumart");
        Uri Coveruri = ContentUris.withAppendedId(uri, mediaPlayerService.getCurrentSong().getAlbumID());
        try{
            InputStream inputStream = contentResolver.openInputStream(Coveruri);
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (Exception e){
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
        if(bitmap != null){
            //imageView.setImageBitmap(CropBitmap(bitmap));
            //imageView.setImageBitmap(bitmap);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),darkenBitMap(CropBitmap(bitmap)));
            rt.setBackground(bitmapDrawable);
        }else {
            rt.setBackgroundColor(Color.parseColor("#383c4a"));
        }
    }
}
