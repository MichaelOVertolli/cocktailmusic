package com.theworldmatrix.cocktailmusicsearch;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class MainFragment extends MusicFragment {

    private MainActivity main;
    private SharedPreferences prefs;

    public static final int FOCUS = 0;
    public static final int RIGHT = 1;
    public static final int LEFT = 2;

    private static final float FULL = 1.0f;
    private static final float FADE = 0.6f;

    private LinearLayout focusLayout;
    private LinearLayout leftLayout;
    private LinearLayout rightLayout;

    private ImageButton focusImage;
    private ImageButton leftImage;
    private ImageButton rightImage;

    private TextView focusText;
    private TextView leftText;
    private TextView rightText;

    private TextView timePassed;
    private TextView timeMax;

    private ImageButton mainPlay;
    private ImageButton mainForward;
    private ImageButton mainBack;
    private ImageButton mainShuffle;
    private ImageButton mainRepeat;
    private ImageButton mainClearContext;

    private SeekBar mainSeek;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        main = (MainActivity) getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(main);

        focusLayout = (LinearLayout) view.findViewById(R.id.focusLayout);
        leftLayout = (LinearLayout) view.findViewById(R.id.leftContextLayout);
        rightLayout = (LinearLayout) view.findViewById(R.id.rightContextLayout);

        focusImage = (ImageButton) view.findViewById(R.id.focusImage);
        leftImage = (ImageButton) view.findViewById(R.id.leftContextImage);
        rightImage = (ImageButton) view.findViewById(R.id.rightContextImage);

        focusText = (TextView) view.findViewById(R.id.focusSong);
        leftText = (TextView) view.findViewById(R.id.leftContextSong);
        rightText = (TextView) view.findViewById(R.id.rightContextSong);

        timePassed = (TextView) view.findViewById(R.id.timePassed);
        timeMax = (TextView) view.findViewById(R.id.timeMax);

        mainPlay = (ImageButton) view.findViewById(R.id.mainPlay);
        mainBack = (ImageButton) view.findViewById(R.id.mainBack);
        mainForward = (ImageButton) view.findViewById(R.id.mainForward);
        mainShuffle = (ImageButton) view.findViewById(R.id.mainShuffle);
        mainRepeat = (ImageButton) view.findViewById(R.id.mainRepeat);
        mainClearContext = (ImageButton) view.findViewById(R.id.mainClear);

        mainSeek = (SeekBar) view.findViewById(R.id.mainSeek);

        focusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(FOCUS)) {
                    main.lock();

                } else {
                    main.setSongFocus(FOCUS);
                }
            }
        });
        rightImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(RIGHT)) {
                    main.lock();

                } else {
                    main.setSongFocus(RIGHT);
                }
            }
        });
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(LEFT)) {
                    main.lock();

                } else {
                    main.setSongFocus(LEFT);
                }
            }
        });

        focusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(FOCUS)) {
                    main.lock();
                } else {
                    main.setSongFocus(FOCUS);
                }
            }
        });
        rightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(RIGHT)) {
                    main.lock();

                } else {
                    main.setSongFocus(RIGHT);
                }
            }
        });
        leftText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isInFocus(LEFT)) {
                    main.lock();
                } else {
                    main.setSongFocus(LEFT);
                }
            }
        });

        mainPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton self = (ImageButton) v;
                if (main.isPlaying()) {
                    main.pause();
                    mainSeek.setEnabled(false);
                    self.setImageResource(R.drawable.play);
                }
                else {
                    main.start();
                    mainSeek.setEnabled(true);
                    self.setImageResource(R.drawable.pause);
                }
            }
        });
        mainBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.playPrev();
            }
        });
        mainForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.playNext();
            }
        });
        mainShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.shuffle();
            }
        });
        mainRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                if (main.isRepeating()) {
                    mainRepeat.setImageResource(R.drawable.repeat);
                    main.setRepeat(false);
                } else {
                    mainRepeat.setImageResource(R.drawable.repeat_highlight);
                    main.setRepeat(true);
                }
            }
        });
        mainClearContext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.changeContext();
            }
        });
        mainSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && main.isPlaying()) {
                    seekBar.setProgress(progress);
                    main.seekTo(progress);
                    timePassed.setText(main.timeToString(progress));
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
        focusLayout.setAlpha(FADE);
        leftLayout.setAlpha(FADE);
        rightLayout.setAlpha(FADE);
        if (location == FOCUS) {
            focusLayout.setAlpha(FULL);
        } else if (location == RIGHT) {
            rightLayout.setAlpha(FULL);
        } else if (location == LEFT) {
            leftLayout.setAlpha(FULL);
        } else throw new Error("MainFragment: Invalid location.");
    }

    @Override
    public void setSongAssets(int location, Song song) {
//        Log.d("MainFragment", "Preferences: "+temp);
        int display = Integer.parseInt(prefs.getString(MainActivity.DISPLAY_PREF, ""));
        boolean songNames = prefs.getBoolean(MainActivity.SONG_PREF, true);
        Uri uri;
        ImageButton imgBtn;
        TextView text;
        if (location == FOCUS) {
            imgBtn=focusImage;
            text=focusText;
        } else if (location == RIGHT) {
            imgBtn=rightImage;
            text=rightText;
        } else if (location == LEFT) {
            imgBtn=leftImage;
            text=leftText;
        } else throw new Error("MainFragment: Invalid location in setSongAssets.");
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
        mainSeek.setMax(max);
        mainSeek.setProgress(pos);
        timePassed.setText(main.timeToString(pos));
        timeMax.setText(main.timeToString(max));
    }
}
