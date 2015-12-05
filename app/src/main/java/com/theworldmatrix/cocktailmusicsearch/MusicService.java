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
import java.util.Random;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class MusicService extends Service {

    private final IBinder musicBind = new MusicBinder();
    private final Handler focusHandler = new Handler();
    private final Handler songPosHandler = new Handler();
    private final int SONGPOSCHECKDELAY = 1000;
    private final int FOCUSDELAY = 10000;
    private final int FADEDELAY = 2000;
    private final boolean FORWARDS = false;
    private final boolean BACKWARDS = true;

    private Runnable songPosRunnable;
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
        playerLeft.setPrepListener();
        playerLeft.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                Song playSong = prepPlayer(player, FORWARDS);
                sendSetAssetBroadcast(MainFragment.LEFT, playSong);
//                Log.d("MusicService", "Left onCompletionListener called.");
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
        playerRight.setPrepListener();
        playerRight.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                Song playSong = prepPlayer(player, FORWARDS);
                sendSetAssetBroadcast(MainFragment.RIGHT, playSong);
//                Log.d("MusicService", "Right onCompletionListener called.");
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
        playerCenter.setPrepListener();
        playerCenter.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MusicPlayer player = (MusicPlayer) mp;
                Song playSong = prepPlayer(player, FORWARDS);
                sendSetAssetBroadcast(MainFragment.FOCUS, playSong);
//                Log.d("MusicService", "Center onCompletionListener called.");
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

        songPosRunnable = new Runnable() {
            @Override
            public void run() {
//                Log.d("MusicService", "songPosRunnable called");
                if (playerCenter.isInFocus() & playerCenter.isPlaying()) {
                    sendSetSongPos(playerCenter.getCurrentPosition(), playerCenter.getDuration());
                } else if (playerRight.isInFocus() & playerRight.isPlaying()) {
                    sendSetSongPos(playerRight.getCurrentPosition(), playerRight.getDuration());
                } else if (playerLeft.isInFocus() & playerLeft.isPlaying()) {
                    sendSetSongPos(playerLeft.getCurrentPosition(), playerLeft.getDuration());
                }
                songPosHandler.postDelayed(this, SONGPOSCHECKDELAY);
            }
        };
        songPosHandler.postDelayed(songPosRunnable, SONGPOSCHECKDELAY);
    }

    private Song prepPlayer(MusicPlayer mp, boolean backwards) {
        mp.reset();
        Song playSong;
        if (backwards) {
            playSong = getPrevSong(mp);
        } else if (!mp.isCurrent()) {
            mp.setNextSong();
            playSong = songs.get(mp.getCurSong());
        } else {
            mp.setCurSong(songPosn);
            playSong = getSong();
        }
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            mp.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source: "+playSong.toString(), e);
        }
        mp.prepareAsync();
        return playSong;
    }

    public void setList(List<Song> theSongs) {
        songs=theSongs;
    }

    public Song[] playSong(){
        Song playSong = prepPlayer(playerLeft, FORWARDS);
        Song playSong2 = prepPlayer(playerRight, FORWARDS);
        Song playSong3 = prepPlayer(playerCenter, FORWARDS);
        return new Song[]{playSong, playSong2, playSong3};
    }

    private Song getSong() {
        Song next = songs.get(songPosn);
        songPosn++;
        if (songPosn>=songs.size()) songPosn=0;
        return next;
    }

    private Song getPrevSong(MusicPlayer mp) {
        mp.setPrevSong();
        Song prev = songs.get(mp.getCurSong());
        return prev;
    }

    private void sendSetSongPos(int pos, int max) {
        Intent intent = new Intent(MainIncomingReceiver.SET_SONG_POS_INTENT);
        intent.putExtra(MainIncomingReceiver.SONG_POS, pos);
        intent.putExtra(MainIncomingReceiver.SONG_MAX, max);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    private void nextSong(MusicPlayer mp, int location, boolean backwards) {
        mp.stop();
        Song playSong = prepPlayer(mp, backwards);
        sendSetAssetBroadcast(location, playSong);
    }

    public void changeContext() {
        if (playerCenter.isInFocus()) {
            nextSong(playerRight, MainFragment.RIGHT, FORWARDS);
            nextSong(playerLeft, MainFragment.LEFT, FORWARDS);
        } else if (playerLeft.isInFocus()) {
            nextSong(playerCenter, MainFragment.FOCUS, FORWARDS);
            nextSong(playerRight, MainFragment.RIGHT, FORWARDS);
        } else if (playerRight.isInFocus()) {
            nextSong(playerCenter, MainFragment.FOCUS, FORWARDS);
            nextSong(playerLeft, MainFragment.LEFT, FORWARDS);
        } else throw new Error("MusicService invalid focus in changeContext");
    }

    public void shuffle() {
        Random r = new Random();
        int index = r.nextInt(songs.size()-5)+5;
        List<Song> temp = new ArrayList<Song>(songs.subList(0, index));
        List<Song> temp2 = new ArrayList<Song>(songs.subList(index, songs.size()));
        temp2.addAll(temp);
        songs = temp2;
        songPosn = 0;
        playerCenter.resetLastSongs();
        playerRight.resetLastSongs();
        playerLeft.resetLastSongs();
        nextSong(playerCenter, MainFragment.FOCUS, FORWARDS);
        nextSong(playerRight, MainFragment.RIGHT, FORWARDS);
        nextSong(playerLeft, MainFragment.LEFT, FORWARDS);
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
        songPosHandler.removeCallbacks(songPosRunnable);
    }

    public void seek(int posn) {
        playerCenter.seekTo(posn);
    }

    public void go() {
        playerLeft.start();
        playerRight.start();
        playerCenter.start();
        focusHandler.postDelayed(focusRunnable, FOCUSDELAY);
        songPosHandler.postDelayed(songPosRunnable, SONGPOSCHECKDELAY);
    }

    public void playPrev() {
        if (playerCenter.isInFocus()) {
            nextSong(playerCenter, MainFragment.FOCUS, BACKWARDS);
        } else if (playerLeft.isInFocus()) {
            nextSong(playerLeft, MainFragment.LEFT, BACKWARDS);
        } else if (playerRight.isInFocus()) {
            nextSong(playerRight, MainFragment.RIGHT, BACKWARDS);
        } else throw new Error("MusicService invalid focus in changeContext");
    }

    public void playNext() {
        if (playerCenter.isInFocus()) {
            nextSong(playerCenter, MainFragment.FOCUS, FORWARDS);
        } else if (playerLeft.isInFocus()) {
            nextSong(playerLeft, MainFragment.LEFT, FORWARDS);
        } else if (playerRight.isInFocus()) {
            nextSong(playerRight, MainFragment.RIGHT, FORWARDS);
        } else throw new Error("MusicService invalid focus in changeContext");
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
        songPosHandler.removeCallbacks(songPosRunnable);
        return false;
    }

}
