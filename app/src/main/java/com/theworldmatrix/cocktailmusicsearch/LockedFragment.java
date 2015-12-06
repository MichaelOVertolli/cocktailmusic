package com.theworldmatrix.cocktailmusicsearch;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by micha_000 on 2015-12-05.
 */
public class LockedFragment extends MusicFragment {

    private MainActivity main;
    private SharedPreferences prefs;

    private ImageButton lockImage;

    private TextView lockText;

    private TextView lockTimePassed;
    private TextView lockTimeMax;

    private ImageButton lockPlay;
    private ImageButton lockForward;
    private ImageButton lockBack;
    private ImageButton lockShuffle;
    private ImageButton lockRepeat;

    private SeekBar lockSeek;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locked, container, false);

        main = (MainActivity) getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(main);

        lockImage = (ImageButton) view.findViewById(R.id.lockImage);

        lockText = (TextView) view.findViewById(R.id.lockSong);

        lockTimePassed = (TextView) view.findViewById(R.id.lockTimePassed);
        lockTimeMax = (TextView) view.findViewById(R.id.lockTimeMax);

        lockPlay = (ImageButton) view.findViewById(R.id.lockPlay);
        lockBack = (ImageButton) view.findViewById(R.id.lockBack);
        lockForward = (ImageButton) view.findViewById(R.id.lockForward);
        lockShuffle = (ImageButton) view.findViewById(R.id.lockShuffle);
        lockRepeat = (ImageButton) view.findViewById(R.id.lockRepeat);

        lockSeek = (SeekBar) view.findViewById(R.id.lockSeek);

        lockImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.unlock();
            }
        });


        lockText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.unlock();
            }
        });


        lockPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton self = (ImageButton) v;
                if (main.isPlaying()) {
                    main.pause();
                    lockSeek.setEnabled(false);
                    self.setImageResource(R.drawable.play);
                }
                else {
                    main.start();
                    lockSeek.setEnabled(true);
                    self.setImageResource(R.drawable.pause);
                }
            }
        });
        lockBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.playPrev();
            }
        });
        lockForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.playNext();
            }
        });
        lockShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.shuffle();
            }
        });
        lockRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isRepeating()) {
                    lockRepeat.setImageResource(R.drawable.repeat);
                    main.setRepeat(false);
                } else {
                    lockRepeat.setImageResource(R.drawable.repeat_highlight);
                    main.setRepeat(true);
                }
            }
        });
        lockSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && main.isPlaying()) {
                    seekBar.setProgress(progress);
                    main.seekTo(progress);
                    lockTimePassed.setText(main.timeToString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return view;
    }

    @Override
    public void setFocus(int location) {

    }

    @Override
    public void setSongAssets(int location, Song song) {
//        Log.d("MainFragment", "Preferences: "+temp);
        int display = Integer.parseInt(prefs.getString(MainActivity.DISPLAY_PREF, ""));
        boolean songNames = prefs.getBoolean(MainActivity.SONG_PREF, true);
        Uri uri;
        ImageButton imgBtn = lockImage;
        TextView text = lockText;
        if (display == MainActivity.SONGS_ONLY_PREF_VAL) {
            imgBtn.setImageResource(android.R.color.transparent);
            text.setText(song.getTitle());
            text.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            if (songNames) {
                text.setText(song.getTitle());
                text.setBackgroundColor(getResources().getColor(R.color.white));
            }
            else {
                text.setText("");
                text.setBackgroundColor(getResources().getColor(R.color.gray));
            }
            if (display == MainActivity.ALBUM_PREF_VAL) {
                uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        Integer.parseInt(song.getAlbum_img()));
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(main.getContentResolver(), uri);
                    imgBtn.setImageBitmap(bmp);
                } catch (IOException e) {e.printStackTrace();}
            }
            else if (display == MainActivity.ARTIST_PREF_VAL) {
                uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        Integer.parseInt(song.getArtist_img()));
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(main.getContentResolver(), uri);
                    imgBtn.setImageBitmap(bmp);
                } catch (IOException e) {e.printStackTrace();}
            } else throw new Error("MainFragment: Invalid display preference value in setSongAssets.");

        }

    }

    @Override
    public void setSeekData(int pos, int max) {
//        Log.d("MainFragment", "setSeekData called.");
        lockSeek.setMax(max);
        lockSeek.setProgress(pos);
        lockTimePassed.setText(main.timeToString(pos));
        lockTimeMax.setText(main.timeToString(max));
    }

}
