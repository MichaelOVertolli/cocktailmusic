package com.theworldmatrix.cocktailmusicsearch;

import android.support.v4.app.Fragment;

/**
 * Created by micha_000 on 2015-11-28.
 */
public abstract class MusicFragment extends Fragment {

    public abstract void setSongAssets(int location, Song song);
    public abstract void setFocus(int location);
    public abstract void setSeekData(int pos, int max);
}
