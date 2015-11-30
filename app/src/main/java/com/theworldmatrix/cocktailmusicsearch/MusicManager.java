package com.theworldmatrix.cocktailmusicsearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class MusicManager {

    private static MusicManager instance;
    private static Cursor mediaCursor;
    private static Cursor audioCursor;
    private static Context context;
    private static MusicDatabaseHandler db;
    private static EchoNest echoNest;
    private static SharedPreferences prefs;
    private static String PREFERENCES = "preferences";
    private static String TASTE_ID_KEY = "taste_id";
    public static int SONG_REQUEST = 0;
    public static int UPDATE_REQUEST = 1;

//    private static Cursor genresCursor;

    private static String[] audioProjection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE
    };

    private static String[] imageProjection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
    };
//    private static String[] genresProjection = {
//            MediaStore.Audio.Genres.NAME,
//            MediaStore.Audio.Genres._ID,
//    };

//    private boolean gettingData;
//    private boolean tasteCreated;
    private String taste_id;

    private MusicManager(Context ctx) {
        context = ctx;
        context.deleteDatabase("musicManager");
        db = new MusicDatabaseHandler(context);
        echoNest = EchoNest.getInstance(context);
        prefs = ctx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor temp = prefs.edit();
//        temp.remove(TASTE_ID_KEY);
//        temp.commit();
        if (!prefs.contains(TASTE_ID_KEY)) {
            //Log.d("MusicManager", "prefs failed, calling createTaste");
            CreateTasteListener listener = new CreateTasteListener(prefs.edit(), TASTE_ID_KEY);
            echoNest.createTasteProfile(ctx, listener);
            echoNest.runRequests(echoNest.getUpdateRequests());
        }
    }

    public static synchronized MusicManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new MusicManager(ctx);
        }
        return instance;
    }

    public List<Song> getSongList() {
        List<Song> songs = db.getAllSongs();
        Collections.shuffle(songs, new Random(System.nanoTime()));
        return songs;
    }

    private ConcurrentHashMap<JsonObjectRequest, Boolean> getRequestType(int requestType) {
        ConcurrentHashMap<JsonObjectRequest, Boolean> requestsMap;
        if (requestType == SONG_REQUEST) requestsMap = echoNest.getSongRequests();
        else if (requestType == UPDATE_REQUEST) requestsMap = echoNest.getUpdateRequests();
        else throw new Error("Invalid requestType.");
        return requestsMap;
    }

    public boolean isReady(int requestType) {
        return echoNest.requestsFinished(getRequestType(requestType));
    }

    public void runRequests(int requestType) {
        echoNest.runRequests(getRequestType(requestType));
    }

//    public boolean isTasteCreated() {return tasteCreated;}

    public void getMusicFromStorage() {
        //Log.d("MusicManager", "getMusicFromStorage called.");
        //tasteCreated = true;
        taste_id = prefs.getString(TASTE_ID_KEY, "NULL");
        Log.d("MusicManager", "taste id: "+taste_id);
        List<Song> songs = db.getAllSongs();
        Iterator<Song> songIter = songs.iterator();
        Set<Integer> setOfSongIDs = new HashSet<Integer>();
        if (songIter!=null && songIter.hasNext()) {
            do {
                Song song = songIter.next();
                setOfSongIDs.add(song.getId());
            } while (songIter.hasNext());
        }

        mediaCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection, MediaStore.Audio.Media.IS_MUSIC+" <> 0", null, null);

        int artist_col_index = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int album_col_index = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        int title_col_index = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int id_col_index = mediaCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);

        if (mediaCursor!=null && mediaCursor.moveToFirst()) {
            do {
                if (mediaCursor.isNull(artist_col_index)) continue;
                String artist=mediaCursor.getString(artist_col_index);
                String title=mediaCursor.getString(title_col_index);
                String album=mediaCursor.getString(album_col_index);
                int id = mediaCursor.getInt(id_col_index);
                if (artist.equals("<unknown>") || artist.equals("Facebook")) {
                    Log.d("Music Manager", artist+ " " + album + " " + title + " " + Integer.toString(id));
                    continue;
                }
                if (setOfSongIDs.contains(id)) {
                    //Log.d("Music Manager", artist+ " " + album + " " + title + " " + Integer.toString(id));
                    continue;
                } else {
//                    gettingData = true;

                    audioCursor = context.getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            imageProjection, MediaStore.Images.Media.TITLE+" = '"+artist+"'",
                            null, null);
                    int img_id_col_index = audioCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    audioCursor.moveToFirst();
                    String artist_img = audioCursor.getString(img_id_col_index);
                    audioCursor.close();
                    audioCursor = context.getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            imageProjection, MediaStore.Images.Media.TITLE+" = '"+album+"'",
                            null, null);
                    audioCursor.moveToFirst();
                    String album_img = audioCursor.getString(img_id_col_index);
                    audioCursor.close();
                    Log.d("MusicManager", "artist id "+artist_img+"; album id "+album_img);
                    SongDBListener listener = new SongDBListener(new Song(id, title, artist, album,
                            album_img, artist_img), taste_id, echoNest, db);
                    Log.d("Music Manager", "retrieving: " + artist+ " " + album + " " + title + " "
                            + Integer.toString(id));// + " " + audioCursor.getString(img_display_col_index));
                    echoNest.getSongID(artist, title, listener);
                }
//                ArrayList<String> genres = new ArrayList<String>();

                //Log.d("Music Manager", "Song: " + artist + " " + album + " " + title);

//                Uri uri= MediaStore.Audio.Genres.getContentUriForAudioId("external", id);
//                genresCursor = context.getContentResolver().query(uri, genresProjection, null, null, null);
//                int genre_col_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
//                if (genresCursor!=null && genresCursor.moveToFirst()) {
//                    do {
//                        genres.add(genresCursor.getString(genre_col_index));
//                    } while (genresCursor.moveToNext());
//                }
//                genresCursor.close();

//                Log.e("Music Manager", "Song: " + title);
            } while (mediaCursor.moveToNext());
            mediaCursor.close();
        }
        echoNest.runRequests(echoNest.getSongRequests());
    }
}
