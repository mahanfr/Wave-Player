package com.hafniumsoftware.diapason;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener {
    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private ArrayList<Audio> songslist;
    private int songPose;
    //Notification
    public static final String ACTION_PLAY = "com.hafniumsoftware.Diapason.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.hafniumsoftware.Diapason.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.hafniumsoftware.Diapason.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.hafniumsoftware.Diapason.ACTION_NEXT";
    public static final String ACTION_STOP = "com.hafniumsoftware.Diapason.ACTION_STOP";
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    Bitmap bitmap;
    //metadata
    private boolean activePlaying = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    public void onCreate(){
        super.onCreate();
        songPose = 0;
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        registerBecomingNoisyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            initMediaSession();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }
    public void playSong(){
        mediaPlayer.reset();
        Audio playSong = songslist.get(songPose);
        //long currSong = playSong.getId();
        //Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currSong);
        Uri trackUri = Uri.parse(playSong.getData());
        try{
            mediaPlayer.setDataSource(getApplicationContext(),trackUri);

        }catch (Exception e){
            Log.e("MUSIC SERVICE","Error setting source",e);
        }
        try{
            mediaPlayer.prepare();
            //mediaPlayer.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Audio getCurrentSong(){ return songslist.get(songPose); }

    private void initMusicPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
    }

    public void playNext(){
        songPose++;
        if(songPose >= songslist.size())
            songPose = 0;
        mediaPlayer.stop();
        playSong();
    }

    public void playPrev(){
        songPose--;
        if(songPose <= 0)
            songPose = songslist.size() - 1;
        mediaPlayer.stop();
        playSong();
    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    public void Stop(){
        mediaPlayer.stop();
    }

    public void setList(ArrayList<Audio> theSongs){
        songslist = theSongs;
    }
    public void setSong(int songIndex){
        songPose = songIndex;
    }

    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public int GetDuration(){
        return  mediaPlayer.getDuration();
    }

    public void Play(){
        mediaPlayer.start();
        activePlaying = true;
        triggerChanges("Play");
        buildNotification(ACTION_PLAY);
    }
    public void Pause(){
        mediaPlayer.pause();
        triggerChanges("Pause");
        activePlaying = false;
        buildNotification(ACTION_PAUSE);
    }

    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    public int getSongPose(){
        return songPose;
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null)
                    initMusicPlayer();
                else if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Could not gain focus
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            Pause();
            buildNotification(ACTION_PAUSE);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext();
    }


    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
    private void initMediaSession() throws RemoteException{
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Play();
            }

            @Override
            public void onPause() {
                super.onPause();
                Pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrev();
            }

            @Override
            public void onStop() {
                super.onStop();
                mediaPlayer.stop();
                triggerChanges("Stop");
                removeNotification();
                //stopSelf();
                //System.exit(0);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                Toast.makeText(getApplicationContext(),"Pressed",Toast.LENGTH_LONG).show();
                return super.onMediaButtonEvent(mediaButtonEvent);
            }
        });
    }
    private void buildNotification(String startingAction){
        String action = "Pause";
        int btn = android.R.drawable.ic_media_pause;
        int am = 0;
        boolean autoCancel = false , onGoing = true;
        if(startingAction.equals(ACTION_PLAY)){
            action = "pause";
            btn = android.R.drawable.ic_media_pause;
            autoCancel = false;
            onGoing = true;
            am = 0;
        }else if(startingAction.equals(ACTION_PAUSE)){
            action = "Play";
            btn = android.R.drawable.ic_media_play ;
            autoCancel = true;
            onGoing = false;
            am = 2;
        }
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart");
        Uri Coveruri = ContentUris.withAppendedId(uri,getCurrentSong().getAlbumID());
        try{
            InputStream inputStream = contentResolver.openInputStream(Coveruri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(this,"Id")
                .setSmallIcon(R.drawable.ic_forground)
                .setLargeIcon(bitmap)
                .setContentTitle(getCurrentSong().getTitle())
                .setContentText(getCurrentSong().getArtist())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous,"Previous",pendingAction(-1))
                .addAction(btn,action,pendingAction(am))
                .addAction(android.R.drawable.ic_media_next,"Next",pendingAction(1))
                .addAction(android.R.drawable.ic_delete,"Exit",pendingAction(-2))
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0,1,2,3))
                .setAutoCancel(autoCancel)
                .setOngoing(onGoing);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, notificationCompat.build());
    }
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(100);
        }
    }
    private PendingIntent pendingAction(int action){
        Intent serviceIntent = new Intent(this,MediaPlayerService.class);
        switch (action){
            case 0:
                serviceIntent.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, action, serviceIntent, 0);
            case 1:
                serviceIntent.setAction(ACTION_NEXT);
                return PendingIntent.getService(this,action,serviceIntent,0);
            case -1:
                serviceIntent.setAction(ACTION_PREVIOUS);
                return  PendingIntent.getService(this,action,serviceIntent,0);
            case -2:
                serviceIntent.setAction(ACTION_STOP);
                return PendingIntent.getService(this,action,serviceIntent,0);
            case 2:
                serviceIntent.setAction(ACTION_PLAY);
                return PendingIntent.getService(this,action,serviceIntent,0);
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
    public boolean isActivePlaying(){
        return activePlaying;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        try {
            mediaPlayer.prepareAsync();
        }catch (Exception e){
            e.printStackTrace();
        }
        activePlaying = true;
        try {
            initMediaSession();
        }catch (Exception e){
            e.printStackTrace();
        }
        buildNotification(ACTION_PLAY);
        triggerChanges("Play");
    }

    private void triggerChanges(String Action) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        // Create intent with action
        Intent localIntent = new Intent("NEW_SONG");
        localIntent.putExtra("Status",Action);
        // Send local broadcast
        localBroadcastManager.sendBroadcast(localIntent);
    }

    @Override
    public void onDestroy() {
        removeNotification();
        removeAudioFocus();
        super.onDestroy();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {}

    public class MusicBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}