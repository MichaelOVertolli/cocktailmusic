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

    public static final String LOCATION = "com.theworldmatrix.cocktailmusicsearch.LOCATION";
    public static final String SONG_STRING = "com.theworldmatrix.cocktailmusicsearch.SONG_STRING";

    private MusicFragment fragment;

    public MainIncomingReceiver (MusicFragment fragment) {
        this.fragment=fragment;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(FADE_INTENT)) {
            fragment.setFocus(intent.getIntExtra(LOCATION, 0));
        } else if (intent.getAction().equals(SET_ASSET_INTENT)) {
            fragment.setSongAssets(intent.getIntExtra(LOCATION, 0), new Song(intent.getStringExtra(SONG_STRING)));
        } else throw new Error("MainIncomingReceiver invalid intent.");
    }

    public void setFragment(MusicFragment fragment) {this.fragment=fragment;}
}
