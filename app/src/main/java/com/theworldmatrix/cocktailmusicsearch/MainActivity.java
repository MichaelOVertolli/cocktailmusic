package com.theworldmatrix.cocktailmusicsearch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;

import android.os.Handler;

import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements MediaController.MediaPlayerControl, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DISPLAY_PREF = "displayType";
    public static final String SONG_PREF = "songNames";
    public static final int ALBUM_PREF_VAL = 0;
    public static final int ARTIST_PREF_VAL = 1;
    public static final int SONGS_ONLY_PREF_VAL = 2;

    private static MusicManager musicManager;
    private final Handler musicManagerHandler = new Handler();
    private final int READYCHECKDELAY = 2000;


    private Runnable startSetup;
    private Runnable getStorage;
    private Runnable updateTaste;

    private MainIncomingReceiver receiver;
    private MusicFragment curFragment;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
//    private MusicController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate called.");
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
//        Log.d("MainActivity", "Preference: " + PreferenceManager.getDefaultSharedPreferences(this).getString(DISPLAY_PREF, ""));
//        Log.d("MainActivity", "Preference: " + PreferenceManager.getDefaultSharedPreferences(this).getString(DISPLAY_PREF, ""));
        curFragment = new MainFragment();
        receiver = new MainIncomingReceiver(curFragment);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainIncomingReceiver.FADE_INTENT);
        filter.addAction(MainIncomingReceiver.SET_ASSET_INTENT);
        filter.addAction(MainIncomingReceiver.SET_SONG_POS_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, curFragment)
                    .commit();
        }
        musicManager = MusicManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Main Activity", "onStart called.");
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            startSetup = new Runnable() {
                @Override
                public void run() {
//                    if (musicManager.isReady(MusicManager.UPDATE_REQUEST)) {
                    if (true) {
                        Log.d("MainActivity", "startSetup runnable called");
                        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                        startService(playIntent);
                    } else {
                        musicManagerHandler.postDelayed(this, READYCHECKDELAY);
                    }
                }
            };
            updateTaste = new Runnable() {
                @Override
                public void run() {
                    if (musicManager.isReady(MusicManager.SONG_REQUEST)) {
                        Log.d("MainActivity", "update runnable called");
                        musicManager.runRequests(MusicManager.UPDATE_REQUEST);
                        musicManagerHandler.postDelayed(startSetup, READYCHECKDELAY);
                    } else musicManagerHandler.postDelayed(this, READYCHECKDELAY);
                }
            };
            getStorage = new Runnable() {
                @Override
                public void run() {
                    if (musicManager.isReady(MusicManager.UPDATE_REQUEST)) {
                        Log.d("MainActivity", "storage runnable called");
                        musicManager.getMusicFromStorage();
                        musicManagerHandler.postDelayed(updateTaste, READYCHECKDELAY);
                    } else musicManagerHandler.postDelayed(this, READYCHECKDELAY);
                }
            };
            musicManagerHandler.postDelayed(getStorage, 0);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv.pausePlayer();
        if (musicBound) {
            unbindService(musicConnection);
            musicBound=false;
        }
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
//
//    @Override
//    protected void onPause() {
    //doesn't work as the activity gets paused when preference fragment is called.
//        super.onPause();
//        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
//    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Music Activity", "onServiceConnected called");
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(musicManager.getSongList());
            musicBound = true;
            Song[] songs = musicSrv.playSong();
            curFragment.setSongAssets(MainFragment.LEFT, songs[0]);
            curFragment.setSongAssets(MainFragment.RIGHT, songs[1]);
            curFragment.setSongAssets(MainFragment.FOCUS, songs[2]);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("musicConnection", "disconnect called.");
            musicSrv.pausePlayer();
            musicSrv=null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, AppPreferences.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void lock() {
        curFragment = new LockedFragment();
        receiver.setFragment(curFragment);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, curFragment)
                .commit();
        musicSrv.changeToLocked();
        musicSrv.loadSongAssets();
    }

    public void unlock() {
        curFragment = new MainFragment();
        receiver.setFragment(curFragment);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, curFragment)
                .commit();
        musicSrv.changeToUnlocked();
        musicSrv.loadSongAssets();
    }

    public void shuffle() { musicSrv.shuffle(); }
    public void changeContext() {musicSrv.changeContext();}

    public void playNext() {
        musicSrv.playNext();
    }

    public void playPrev() {
        musicSrv.playPrev();
    }

    public boolean isRepeating() {
        if(musicSrv!=null&&musicBound) return musicSrv.isRepeating();
        else return false;
    }

    public void setRepeat(boolean repeat) {
        if(musicSrv!=null&&musicBound) musicSrv.setRepeat(repeat);
    }

    public boolean isInFocus(int location) {
        boolean state = false;
        if(musicSrv!=null&&musicBound) state = musicSrv.isInFocus(location);
        return state;
    }

    public void setSongFocus(int location) {
        if(musicSrv!=null&&musicBound) musicSrv.setSongFocus(location);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("MainActivity", "onSharedPrefs called. "+key);
        if (key.equals(DISPLAY_PREF) || key.equals(SONG_PREF)) musicSrv.loadSongAssets();
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null&&musicBound&&musicSrv.isPng()) return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null&&musicBound&&musicSrv.isPng()) return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        if(musicSrv!=null&&musicBound) musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null&&musicBound) return musicSrv.isPng();
        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public String timeToString(int ms) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) % TimeUnit.MINUTES.toSeconds(1));
    }
}
