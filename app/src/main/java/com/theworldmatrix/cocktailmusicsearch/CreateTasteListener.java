package com.theworldmatrix.cocktailmusicsearch;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by M on 2015-11-26.
 */
public class CreateTasteListener extends EchoNestListener {

    private SharedPreferences.Editor prefs;
    private String key;

    public CreateTasteListener(SharedPreferences.Editor prefs, String key) {
        this.prefs = prefs;
        this.key = key;
    }

    @Override
    public void onResponse(JSONObject response) {
        super.onResponse(response);
        try {
            String catalogueID = response.getJSONObject("response").getString("id");
            prefs.putString(key, catalogueID);
            prefs.commit();
            Log.d("CreateTasteListener", "got response now. id = "+catalogueID);
        } catch (JSONException e) {e.printStackTrace();}
    }
}
