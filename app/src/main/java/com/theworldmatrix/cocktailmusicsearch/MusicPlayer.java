package com.theworldmatrix.cocktailmusicsearch;

import android.media.MediaPlayer;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M on 2015-11-15.
 */
public class MusicPlayer extends MediaPlayer {


    final Handler fadeHandler = new Handler();
    final Runnable fadeIn = new Runnable() {
        @Override
        public void run() {
            if (atMax()) {
                //Log.d("fadeIn", name+" max reached.");
                fadeHandler.removeCallbacks(this);
            } else {
                int rate;
                if (volumeMax[0] >= volumeMax[1]) rate = (volumeMax[0]-FADEMIN)/(FADELENGTH/FADEDELAY);
                else rate = (volumeMax[1]-FADEMIN)/(FADELENGTH/FADEDELAY);
                int[] vol = {volumeCur[0] + rate, volumeCur[1] + rate};
                setVol(vol);
//                Log.d("fadeIn", name + " called " + Integer.toString(rate) + " " + Integer.toString(volumeCur[0]) + ":" + Integer.toString(volumeCur[1]));
                fadeHandler.postDelayed(this, FADEDELAY);
            }
        }
    };
    final Runnable fadeOut = new Runnable() {
        public void run() {
            if (atMin()) {
                //Log.d("fadeOut", name+" min reached.");
                fadeHandler.removeCallbacks(this);
                setVolMax();
                fadeHandler.postDelayed(fadeIn, FADEDELAY);
            } else {
                int rate;
                if (volumeMax[0] >= volumeMax[1]) rate = (volumeMax[0]-FADEMIN)/(FADELENGTH/FADEDELAY);
                else rate = (volumeMax[1]-FADEMIN)/(FADELENGTH/FADEDELAY);
                int[] vol = {volumeCur[0]-rate, volumeCur[1]-rate};
                setVol(vol);
//                Log.d("fadeOut", name + " called " + Integer.toString(rate) + " " + Integer.toString(volumeCur[0]) + ":" + Integer.toString(volumeCur[1]));
                fadeHandler.postDelayed(this, FADEDELAY);
            }
        }
    };

    private int[] volumeBalance;
    private int[] volumeCur;
    private int[] volumeMax = {0, 0};
    private boolean inFocus;
    private int maxVolumeRng;
    private int lowVolume;
    private int hiVolume;
    //means there are 10 steps
    private final int FADEDELAY = 200;
    private final int FADELENGTH = 2000;
    private final int FADEMIN = 5;
    private String name;
    private List<Integer> lastSongs;
    private int lastSongIndex;
    private int curSong;

    public MusicPlayer (String newname, int[] balance, boolean focus, int max, int low, int hi) {
        super();
        name = newname;
        volumeBalance = balance;
        inFocus = focus;
        maxVolumeRng = max;
        lowVolume = low;
        hiVolume = hi;
        setVolMax();
        setVol(volumeMax);
        curSong = 0;
        lastSongs = new ArrayList<Integer>();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void pause() {
        super.pause();
        fadeHandler.removeCallbacks(fadeIn);
        fadeHandler.removeCallbacks(fadeOut);
    }

    @Override
    public void stop() {
        super.stop();
        fadeHandler.removeCallbacks(fadeIn);
        fadeHandler.removeCallbacks(fadeOut);
    }

    public float convertVol(int vol) {
        float val = (float) (Math.log(maxVolumeRng-vol)/Math.log(maxVolumeRng));
        return 1-val;
    }

    public void setVolMax() {
        int vol;
        if (inFocus) vol = hiVolume;
        else vol = lowVolume;
        volumeMax[0] = volumeBalance[0]*vol;
        volumeMax[1] = volumeBalance[1]*vol;
    }

    public void setVol(int[] vols) {
        for (int i=0; i<2; i++) {
            if (vols[i] < FADEMIN) vols[i] = FADEMIN;
            if (vols[i] > volumeMax[i]) vols[i] = volumeMax[i];
        }
        volumeCur = vols;
        super.setVolume(convertVol(vols[0]), convertVol(vols[1]));
    }

    private boolean atMax() {
        boolean state;
        if (volumeCur[0] == volumeMax[0] && volumeCur[1] == volumeMax[1]) state = true;
        else state = false;
        return state;
    }

    private boolean atMin() {
        boolean state;
        if (volumeCur[0]<=5 && volumeCur[1]<=5) state = true;
        else state = false;
        return state;
    }

    public void setInFocus(boolean focus) {
        this.inFocus = focus;
        fadeHandler.postDelayed(fadeOut, FADEDELAY);
    }

    public void setInFocusNoFade(boolean focus) {
        this.inFocus = focus;
    }

    public boolean isInFocus() { return inFocus; }

    public void setCurSong(int songNum) {
        int index = lastSongs.indexOf(songNum);
        if (index == -1) {
            lastSongs.add(songNum);
            curSong = lastSongs.size()-1;
        } else {
            curSong = index;
        }
    }
    public void setPrevSong() {
        if (curSong != 0) {
            curSong = curSong-1;
        }
    }
    public void setNextSong() {
        curSong = curSong+1;
    }
    public boolean isCurrent() {
        boolean state = false;
        if (curSong == lastSongs.size()-1 || lastSongs.size() == 0) state = true;
        return state;
    }

    public void resetLastSongs() {
        lastSongs.clear();
        curSong = 0;
    }

    public void setPrepListener() {
        super.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }


    public int getCurSong() {return lastSongs.get(curSong);}

    public String getName() {return name;}

}
