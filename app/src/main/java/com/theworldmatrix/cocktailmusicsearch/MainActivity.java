package com.theworldmatrix.cocktailmusicsearch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.Image;
import android.os.IBinder;
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


public class MainActivity extends ActionBarActivity implements MediaController.MediaPlayerControl {


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
    private ImageButton mainPlay;
    private ImageButton mainForward;
    private ImageButton mainBack;
    private ImageButton mainShuffle;
    private ImageButton mainRepeat;
    private ImageButton mainClearContext;

    private SeekBar mainSeek;

    private ImageButton lockPlay;
    private ImageButton lockForward;
    private ImageButton lockBack;
    private ImageButton lockShuffle;
    private ImageButton lockRepeat;

    private SeekBar lockSeek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        //setListeners();
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

    private void setListeners() {
        mainPlay = (ImageButton) findViewById(R.id.mainPlay);
        mainBack = (ImageButton) findViewById(R.id.mainBack);
        mainForward = (ImageButton) findViewById(R.id.mainForward);
        mainShuffle = (ImageButton) findViewById(R.id.mainShuffle);
        mainRepeat = (ImageButton) findViewById(R.id.mainRepeat);
        mainClearContext = (ImageButton) findViewById(R.id.mainClear);

        mainSeek = (SeekBar) findViewById(R.id.mainSeek);

        //lockPlay = (ImageButton) findViewById(R.id.lockPlay);
        //lockBack = (ImageButton) findViewById(R.id.lockBack);
        //lockForward = (ImageButton) findViewById(R.id.lockForward);
        //lockShuffle = (ImageButton) findViewById(R.id.lockShuffle);
        //lockRepeat = (ImageButton) findViewById(R.id.lockRepeat);

        //lockSeek = (SeekBar) findViewById(R.id.lockSeek);

        mainPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) pause();
                else start();
            }
        });
        mainBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        mainForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

//        controller = new MusicController(this);
//        controller.setPrevNextListeners(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playNext();
//            }
//        }, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 playPrev();
//            }
//        });
//
//        controller.setMediaPlayer(this);
//        //controller.setAnchorView(findViewById(R.id.musicController));
//        controller.setEnabled(true);
//        controller.setPadding(0, 0, 0, 100);
//        controller.show(0);
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
