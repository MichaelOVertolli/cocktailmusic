package com.theworldmatrix.cocktailmusicsearch;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M on 2015-11-15.
 */
public class MusicDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "musicManager";
    private static final String TABLE_SONGS = "songs";

    private static final String KEY_ID = "id";
    private static final String KEY_ECHONEST_ID = "echonest_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_ALBUM_IMG = "album_img";
    private static final String KEY_ARTIST_IMG = "artist_img";

    public MusicDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SONGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_ECHONEST_ID + " TEXT," //space before TEXT is absolutely crucial.
                + KEY_TITLE + " TEXT,"
                + KEY_ARTIST + " TEXT,"
                + KEY_ALBUM + " TEXT,"
                + KEY_ALBUM_IMG + " TEXT,"
                + KEY_ARTIST_IMG + " TEXT" + ")";
//        Log.d("MusicDatabaseHandler", "onCreate called");
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_SONGS);

        // Create tables again
        onCreate(db);
    }

    public void emptyTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_SONGS);
        db.close();
    }

    public void addSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
//        Log.d("MusicDatabaseHandler", db.toString());

        ContentValues values = new ContentValues();
        values.put(KEY_ID, song.getId());
        values.put(KEY_ECHONEST_ID, song.getEchoNestID());
        values.put(KEY_TITLE, song.getTitle());
        values.put(KEY_ARTIST, song.getArtist());
        values.put(KEY_ALBUM, song.getAlbum());
        values.put(KEY_ALBUM_IMG, song.getAlbum_img());
        values.put(KEY_ARTIST_IMG, song.getArtist_img());

        // Inserting Row
        db.insert(TABLE_SONGS, null, values);
        db.close(); // Closing database connection
    }

    public Song getSong(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SONGS, new String[] { KEY_ID,
                        KEY_ECHONEST_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_ALBUM_IMG,
                        KEY_ARTIST_IMG}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Song song = new Song(Integer.parseInt(cursor.getString(0)), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6), cursor.getString(1));

        return song;
    }

    public List<Song> getAllSongs() {
        List<Song> songList = new ArrayList<Song>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SONGS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Song song = new Song(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6), cursor.getString(1));
//                Log.d("MusicDatabaseHandler", cursor.toString());
//                Log.d("MusicDatabaseHandler", song.toString());
                songList.add(song);
            } while (cursor.moveToNext());
        }
        return songList;
    }

    public int getSongsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SONGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public int updateSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, song.getId());
        values.put(KEY_ECHONEST_ID, song.getEchoNestID());
        values.put(KEY_TITLE, song.getTitle());
        values.put(KEY_ARTIST, song.getArtist());
        values.put(KEY_ALBUM, song.getAlbum());
        values.put(KEY_ALBUM_IMG, song.getAlbum_img());
        values.put(KEY_ARTIST_IMG, song.getArtist_img());


        // updating row
        return db.update(TABLE_SONGS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(song.getId())});
    }

    public void deleteSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SONGS, KEY_ID + " = ?",
                new String[] { String.valueOf(song.getId()) });
        db.close();
    }
}
