package com.theworldmatrix.cocktailmusicsearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by micha_000 on 2015-11-28.
 */
public class MainIncomingReceiver extends BroadcastReceiver {

    public static final String FADE_INTENT = "com.theworldmatrix.cocktailmusicsearch.FADE";
    public static final String SET_ASSET_INTENT = "com.theworldmatrix.cocktailmusicsearch.SET_ASSET";
    public static final String SET_SONG_POS_INTENT = "com.theworldmatrix.cocktailmusicsearch.SET_SONG_POS";

    public static final String LOCATION = "com.theworldmatrix.cocktailmusicsearch.LOCATION";
    public static final String SONG_STRING = "com.theworldmatrix.cocktailmusicsearch.SONG_STRING";
    public static final String SONG_MAX = "com.theworldmatrix.cocktailmusicsearch.SONG_MAX";
    public static final String SONG_POS = "com.theworldmatrix.cocktailmusicsearch.SONG_POS";

    private MusicFragment fragment;

    public MainIncomingReceiver (MusicFragment fragment) {
        this.fragment=fragment;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (action.equals(SET_SONG_POS_INTENT)) {
            fragment.setSeekData(intent.getIntExtra(SONG_POS, 0), intent.getIntExtra(SONG_MAX, 0));
        } else if (action.equals(FADE_INTENT)) {
            fragment.setFocus(intent.getIntExtra(LOCATION, 0));
        } else if (action.equals(SET_ASSET_INTENT)) {
            fragment.setSongAssets(intent.getIntExtra(LOCATION, 0), new Song(intent.getStringExtra(SONG_STRING)));
        } else throw new Error("MainIncomingReceiver invalid intent.");
    }

    public void setFragment(MusicFragment fragment) {this.fragment=fragment;}
}
