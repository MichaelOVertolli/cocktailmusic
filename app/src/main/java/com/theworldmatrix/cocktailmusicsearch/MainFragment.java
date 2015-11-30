package com.theworldmatrix.cocktailmusicsearch;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

        focusLayout = (LinearLayout) view.findViewById(R.id.focusLayout);
        leftLayout = (LinearLayout) view.findViewById(R.id.leftContextLayout);
        rightLayout = (LinearLayout) view.findViewById(R.id.rightContextLayout);

        focusImage = (ImageButton) view.findViewById(R.id.focusImage);
        leftImage = (ImageButton) view.findViewById(R.id.leftContextImage);
        rightImage = (ImageButton) view.findViewById(R.id.rightContextImage);

        focusText = (TextView) view.findViewById(R.id.focusSong);
        leftText = (TextView) view.findViewById(R.id.leftContextSong);
        rightText = (TextView) view.findViewById(R.id.rightContextSong);

        mainPlay = (ImageButton) view.findViewById(R.id.mainPlay);
        mainBack = (ImageButton) view.findViewById(R.id.mainBack);
        mainForward = (ImageButton) view.findViewById(R.id.mainForward);
        mainShuffle = (ImageButton) view.findViewById(R.id.mainShuffle);
        mainRepeat = (ImageButton) view.findViewById(R.id.mainRepeat);
        mainClearContext = (ImageButton) view.findViewById(R.id.mainClear);

        mainSeek = (SeekBar) view.findViewById(R.id.mainSeek);

        mainPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main.isPlaying()) main.pause();
                else main.start();
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
        mainClearContext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.changeContext();
            }
        });


        return view;
    }

    @Override
    public void setFocus(int location) {
        if (location == FOCUS) {
            focusLayout.setAlpha(FULL);
            leftLayout.setAlpha(FADE);
        } else if (location == RIGHT) {
            rightLayout.setAlpha(FULL);
            focusLayout.setAlpha(FADE);
        } else if (location == LEFT) {
            leftLayout.setAlpha(FULL);
            rightLayout.setAlpha(FADE);
        } else throw new Error("MainFragment: Invalid location.");
    }

    @Override
    public void setSongAssets(int location, Song song) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                Integer.parseInt(song.getAlbum_img()));
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
        } else throw new Error("MainFragment: Invalid location.");
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(main.getContentResolver(), uri);
            imgBtn.setImageBitmap(bmp);
        }
        catch (IOException e) {e.printStackTrace();}
        text.setText(song.getTitle());

    }
}
