package com.theworldmatrix.cocktailmusicsearch;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by M on 2015-11-16.
 */
public class SongDBListener extends EchoNestListener {

    private Song song;

    private String taste_id;
    private EchoNest echoNest;

    private MusicDatabaseHandler db;

    public SongDBListener(Song song, String taste_id, EchoNest echoNest, MusicDatabaseHandler db) {
        this.song = song;

        this.taste_id = taste_id;
        this.echoNest = echoNest;
        this.db = db;
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        try {
            String echoID = response.getJSONObject("response").getJSONArray("songs").getJSONObject(0).getString("id");
            song.setEchoNestID(echoID);
            db.addSong(song);
//            echoNest.updateTasteProfile(taste_id, echoID);
            //echoNest.getSongRequests().remove(super.getThisRequest());
//            Log.d("SongDBListener", Boolean.toString(echoNest.requestsFinished(echoNest.getSongRequests())));
        } catch (JSONException e) {
            Log.d("SongDBListener", song.toString());
            e.printStackTrace();
        }
    }
}
