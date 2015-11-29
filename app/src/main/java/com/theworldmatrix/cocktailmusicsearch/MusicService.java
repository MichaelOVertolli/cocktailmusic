package com.theworldmatrix.cocktailmusicsearch;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class MusicService extends Service {

    private final IBinder musicBind = new MusicBinder();
    private final Handler focusHandler = new Handler();
    private final int FOCUSDELAY = 10000;
    private final int FADEDELAY = 2000;
    private Runnable focusRunnable;
    private MusicPlayer playerLeft;
    private MusicPlayer playerRight;
    private MusicPlayer playerCenter;
    private int maxVolume = 100;
    private int lowVolume = 35;
    private int hiVolume = 65;
    private List<Song> songs;
    private int songPosn;

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn=0;
        playerLeft = new MusicPlayer("left", new int[] {1, 0}, false, maxVolume, lowVolume, hiVolume);
        playerRight = new MusicPlayer("right", new int[] {0, 1}, false, maxVolume, lowVolume, hiVolume);
        playerCenter = new MusicPlayer("center", new int[] {1, 1}, true, maxVolume-10, lowVolume, hiVolume);
        initMusicPlayer();
        Log.d("Music Service", "Initialized.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

//    @Override
//    public void onDestroy() {
//        Log.d("MusicService", "onDestroy called");
//        playerLeft.stop();
//        playerRight.stop();
//        playerCenter.stop();
//        playerLeft.release();
//        playerRight.release();
//        playerCenter.release();
//        focusHandler.removeCallbacks(focusRunnable);
//        super.onDestroy();
//    }

    public void initMusicPlayer() {
        playerLeft.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        playerLeft.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playerLeft.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("playerLeft", "onPrepared called");
                mp.start();
            }
        });
        playerLeft.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                player.reset();
                player.setLastSong(songPosn);
                Song playSong = getSong();
                long currSong = playSong.getId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currSong);
                try {
                    player.setDataSource(getApplicationContext(), trackUri);
                } catch(Exception e) {
                    Log.e("MUSIC SERVICE", "Error setting data source", e);
                }
                player.prepareAsync();
                sendSetAssetBroadcast(MainFragment.LEFT, playSong);
                Log.d("MusicService", "Left onCompletionListener called.");
            }
        });
        playerLeft.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        playerRight.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        playerRight.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playerRight.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("playerRight", "onPrepared called");
                mp.start();
            }
        });
        playerRight.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                player.reset();
                player.setLastSong(songPosn);
                Song playSong = getSong();
                long currSong = playSong.getId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currSong);
                try {
                    player.setDataSource(getApplicationContext(), trackUri);
                } catch(Exception e) {
                    Log.e("MUSIC SERVICE", "Error setting data source", e);
                }
                player.prepareAsync();
                sendSetAssetBroadcast(MainFragment.RIGHT, playSong);
                Log.d("MusicService", "Right onCompletionListener called.");
            }
        });
        playerRight.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

        playerCenter.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        playerCenter.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playerCenter.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("playerCenter", "onPrepared called");
                mp.start();
            }
        });
        playerCenter.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                player.reset();
                player.setLastSong(songPosn);
                Song playSong = getSong();
                long currSong = playSong.getId();
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currSong);
                try {
                    player.setDataSource(getApplicationContext(), trackUri);
                } catch(Exception e) {
                    Log.e("MUSIC SERVICE", "Error setting data source", e);
                }
                player.prepareAsync();
                sendSetAssetBroadcast(MainFragment.FOCUS, playSong);
                Log.d("MusicService", "Center onCompletionListener called.");
            }
        });
        playerCenter.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

        focusRunnable = new Runnable() {
            @Override
            public void run() {
//                Log.d("focusRunnable", "called.");
                if (playerCenter.isInFocus()) {
                    playerCenter.setInFocus(false);
                    playerRight.setInFocus(true);
                    playerLeft.setInFocus(false);
                    sendFadeBroadcast(MainFragment.RIGHT);
                } else if (playerRight.isInFocus()) {
                    playerCenter.setInFocus(false);
                    playerRight.setInFocus(false);
                    playerLeft.setInFocus(true);
                    sendFadeBroadcast(MainFragment.LEFT);
                } else if (playerLeft.isInFocus()) {
                    playerCenter.setInFocus(true);
                    playerRight.setInFocus(false);
                    playerLeft.setInFocus(false);
                    sendFadeBroadcast(MainFragment.FOCUS);
                }
                focusHandler.postDelayed(this, FOCUSDELAY+FADEDELAY);
            }
        };
        focusHandler.postDelayed(focusRunnable, FOCUSDELAY);
    }

    public void setList(List<Song> theSongs) {
        songs=theSongs;
    }

    public Song[] playSong(){
        playerLeft.reset();
        playerLeft.setLastSong(songPosn);
        Song playSong = getSong();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            playerLeft.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        playerLeft.prepareAsync();
        playerRight.reset();
        playerRight.setLastSong(songPosn);
        Song playSong2 = getSong();
        long currSong2 = playSong2.getId();
        Uri trackUri2 = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong2);
        try {
            playerRight.setDataSource(getApplicationContext(), trackUri2);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        playerRight.prepareAsync();
        playerCenter.reset();

        Song playSong3 = getSong();
        long currSong3 = playSong3.getId();
        Uri trackUri3 = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong3);
        try {
            playerCenter.setDataSource(getApplicationContext(), trackUri3);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        playerCenter.prepareAsync();
        return new Song[]{playSong, playSong2, playSong3};
    }

    private Song getSong() {
        Song next = songs.get(songPosn);
        songPosn++;
        if (songPosn>=songs.size()) songPosn=0;
        return next;
    }

    private void sendFadeBroadcast(int location) {
        Intent intent = new Intent(MainIncomingReceiver.FADE_INTENT);
        intent.putExtra(MainIncomingReceiver.LOCATION, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendSetAssetBroadcast(int location, Song song) {
        Intent intent = new Intent(MainIncomingReceiver.SET_ASSET_INTENT);
        intent.putExtra(MainIncomingReceiver.LOCATION, location);
        intent.putExtra(MainIncomingReceiver.SONG_STRING, song.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int getPosn() {
        return playerCenter.getCurrentPosition();
    }

    public int getDur() {
        return playerCenter.getDuration();
    }

    public boolean isPng() {
        return playerCenter.isPlaying();
    }

    public void pausePlayer() {
        playerLeft.pause();
        playerRight.pause();
        playerCenter.pause();
        focusHandler.removeCallbacks(focusRunnable);
    }

    public void seek(int posn) {
        playerCenter.seekTo(posn);
    }

    public void go() {
        playerLeft.start();
        playerRight.start();
        playerCenter.start();
        focusHandler.postDelayed(focusRunnable, FOCUSDELAY);
    }

    public void playPrev() {
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
    }

    public void playNext() {
        songPosn++;
        if(songPosn>=songs.size()) songPosn=0;
        playSong();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        Log.d("MusicService", "onUnbind called");
        playerLeft.stop();
        playerRight.stop();
        playerCenter.stop();
        playerLeft.reset();
        playerRight.reset();
        playerCenter.reset();
        playerLeft.release();
        playerRight.release();
        playerCenter.release();
        focusHandler.removeCallbacks(focusRunnable);
        return false;
    }

}
