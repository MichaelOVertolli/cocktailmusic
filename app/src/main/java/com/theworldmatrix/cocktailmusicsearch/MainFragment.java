package com.theworldmatrix.cocktailmusicsearch;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

/**
 * Created by micha_000 on 2015-10-21.
 */
public class MainFragment extends Fragment {

    private MainActivity main;

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

        return view;
    }
}
