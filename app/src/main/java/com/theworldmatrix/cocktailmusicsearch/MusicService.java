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

    public void onCreate() {
        super.onCreate();
        songPosn=0;
        playerLeft = new MusicPlayer("left", new int[] {1, 0}, false, maxVolume, lowVolume, hiVolume);
        playerRight = new MusicPlayer("right", new int[] {0, 1}, false, maxVolume, lowVolume, hiVolume);
        playerCenter = new MusicPlayer("center", new int[] {1, 1}, true, maxVolume-10, lowVolume, hiVolume);
        initMusicPlayer();
        Log.d("Music Service", "Initialized.");
    }

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
                Log.d("focusRunnable", "called.");
                if (playerCenter.isInFocus()) {
                    playerCenter.setInFocus(false);
                    playerRight.setInFocus(true);
                    playerLeft.setInFocus(false);
                } else if (playerRight.isInFocus()) {
                    playerCenter.setInFocus(false);
                    playerRight.setInFocus(false);
                    playerLeft.setInFocus(true);
                } else if (playerLeft.isInFocus()) {
                    playerCenter.setInFocus(true);
                    playerRight.setInFocus(false);
                    playerLeft.setInFocus(false);
                }
                focusHandler.postDelayed(this, FOCUSDELAY+FADEDELAY);
            }
        };
        focusHandler.postDelayed(focusRunnable, FOCUSDELAY);
    }

    public void setList(List<Song> theSongs) {
        songs=theSongs;
    }

    public void playSong(){
        playerLeft.reset();
        Song playSong = songs.get(songPosn);
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
        Song playSong2 = songs.get(songPosn+1);
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
        Song playSong3 = songs.get(songPosn+2);
        long currSong3 = playSong3.getId();
        Uri trackUri3 = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong3);
        try {
            playerCenter.setDataSource(getApplicationContext(), trackUri3);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        playerCenter.prepareAsync();
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
        playerLeft.stop();
        playerRight.stop();
        playerCenter.stop();
        playerLeft.release();
        playerRight.release();
        playerCenter.release();
        return false;
    }

}
