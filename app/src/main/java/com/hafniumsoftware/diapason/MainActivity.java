package com.hafniumsoftware.diapason;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Intent playIntent;
    private MediaPlayerService mediaPlayerService;
    private ArrayList<Audio> audioArrayList;
    private StorageUtil storageUtil = new StorageUtil(this);
    //
    private RelativeLayout musicBar;
    private TextView songTitle, artistTitle;
    //
    private ArrayList<Category> albums;
    private ArrayList<Long> albumIdList = new ArrayList<>();
    private ArrayList<String> artistNamesList = new ArrayList<>();
    //Init all runtime permissions
    String[] appPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_PHONE_STATE
    };
    //
    private Button ppBtn,nextBtn,periviesBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkAndRequestPermissions()){
            initUi();
            loadSongs();
            sortToCategory();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(listener,new IntentFilter("NEW_SONG"));
    }

    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission: appPermissions){
            if(ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permission);
            }
        }
        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(MainActivity.this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    69);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 69){
            HashMap<String,Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    permissionResults.put(permissions[i],grantResults[i]);
                    deniedCount++;
                }
            }
            if(deniedCount == 0){
                loadSongs();
                sortToCategory();
            }else {
                for (Map.Entry<String,Integer> entry :permissionResults.entrySet()){
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permName)){
                        ShowDialog("","This app needs to have these Permissions to be able to Play Music","Ok" ,
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        checkAndRequestPermissions();
                                    }
                                },"cancel",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        System.exit(0);
                                    }
                                }
                                ,false);
                    }
                    else {
                        ShowDialog("","You have denied some permissions. Allow all permissions at [Setting] > [Permissions]","Ok" ,
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",getPackageName(),null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        System.exit(0);
                                    }
                                },"cancel",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        System.exit(0);
                                    }
                                }
                                ,false);
                        break;
                    }
                }
            }
        }
    }

    private void ShowDialog(String title, String msg, String pBtn,
                            DialogInterface.OnClickListener pOnclick, String nBtn,
                            DialogInterface.OnClickListener nOnclick, boolean isCancelable)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(isCancelable)
                .setPositiveButton(pBtn,pOnclick)
                .setNegativeButton(nBtn,nOnclick);
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)iBinder;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setList(audioArrayList);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    private void initUi(){
        audioArrayList = storageUtil.loadAudio();
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager =  findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout =  findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        //tabLayout.setTabTextColors(Color.parseColor("#727272"),Color.parseColor("#000000"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        musicBar = findViewById(R.id.musicbar);
        songTitle = findViewById(R.id.musicbar_title_text);
        artistTitle = findViewById(R.id.musicbar_artist_text);
        ppBtn = findViewById(R.id.musicbar_pp_btn);
        nextBtn = findViewById(R.id.musicbar_next_btn);
        periviesBtn = findViewById(R.id.musicbar_prev_btn);
        musicBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InfoActivity.class);
                startActivity(intent);
            }
        });
        ppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayerService.isActivePlaying() || mediaPlayerService.isPlaying())
                    mediaPlayerService.Pause();
                else
                    mediaPlayerService.Play();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playNext();
            }
        });
        periviesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPrev();

            }
        });
    }

    private void loadSongs(){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if(cursor != null && cursor.getCount() > 0){
            audioArrayList = new ArrayList<>();
            while (cursor.moveToNext()){
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                audioArrayList.add(new Audio(id,data,title,album,artist,album_id));
                if(!albumIdList.contains(album_id))
                    albumIdList.add(album_id);
                if(!artistNamesList.contains(artist))
                    artistNamesList.add(artist);
            }
        }
        if(cursor != null)
            cursor.close();
        //Toast.makeText(this,String.valueOf(albumNamesList.size())+" "+String.valueOf(artistNamesList.size()),Toast.LENGTH_LONG).show();
        StorageUtil storageUtil = new StorageUtil(this);
        storageUtil.storeAudio(audioArrayList);
    }

    private void sortToCategory(){
        StorageUtil storageUtil = new StorageUtil(this);
        albums = new ArrayList<>();
        String name = "";
        String artist = "";
        for(Long albumId : albumIdList){
            ArrayList<Audio> list = new ArrayList<>();
            for(int i=0;i<audioArrayList.size()-1;i++){
                if(audioArrayList.get(i).getAlbumID() == albumId){
                    list.add(audioArrayList.get(i));
                    name = audioArrayList.get(i).getAlbum();
                    artist = audioArrayList.get(i).getArtist();
                }
            }
            albums.add(new Category(name,artist,list,albumId));
        }
        storageUtil.storeCategory("Album",albums);
        albums = new ArrayList<>();
        for(String artistlist : artistNamesList){
            ArrayList<Audio> list = new ArrayList<>();
            for(int i=0;i<audioArrayList.size()-1;i++){
                if(audioArrayList.get(i).getArtist().equals(artistlist)){
                    list.add(audioArrayList.get(i));
                    name = audioArrayList.get(i).getArtist();
                }
            }
            albums.add(new Category(name,list));
        }
        storageUtil.storeCategory("Artist",albums);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void OnSongClicked(View v){
        int id = Integer.parseInt(v.getTag().toString());
        mediaPlayerService.Pause();
        mediaPlayerService.Stop();
        mediaPlayerService.setSong(id);
        mediaPlayerService.playSong();
    }

    public void openView(View view){
        int id = Integer.parseInt(view.getTag().toString());
        mediaPlayerService.Pause();
        mediaPlayerService.Stop();
        mediaPlayerService.setList(storageUtil.loadCategory("Album").get(id).getCategoryAudios());
        mediaPlayerService.setSong(0);
        mediaPlayerService.playSong();
    }

    public void OnArtistClick(View v){
        int id = Integer.parseInt(v.getTag().toString());
        mediaPlayerService.Pause();
        mediaPlayerService.Stop();
        mediaPlayerService.setList(storageUtil.loadCategory("Artist").get(id).getCategoryAudios());
        mediaPlayerService.setSong(0);
        mediaPlayerService.playSong();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_section, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            TracksFragment tracksFragment = new TracksFragment();
            AlbumsFragment albumsFragment = new AlbumsFragment();
            ArtistsFragment artistsFragment = new ArtistsFragment();
            switch (position){
                case 0:
                    return tracksFragment;
                case 1:
                    return tracksFragment;
                case 2:
                    return artistsFragment;
                case 3:
                    return albumsFragment;
                case 4:
                    return tracksFragment;
                    default:
                        return tracksFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }
    }

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            songTitle.setText(mediaPlayerService.getCurrentSong().getTitle());
            artistTitle.setText(mediaPlayerService.getCurrentSong().getArtist());
            if(intent.getStringExtra("Status").equals("Play")){
                musicBar.setVisibility(View.VISIBLE);
                ppBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            }else if(intent.getStringExtra("Status").equals("Stop")){
                musicBar.setVisibility(View.GONE);
            }else if(intent.getStringExtra("Status").equals("Pause")){
                ppBtn.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        }
    };
}
