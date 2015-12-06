package com.theworldmatrix.cocktailmusicsearch;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by M on 2015-11-15.
 */
public class EchoNest {
    private static EchoNest instance;
    private static String BASEURL = "http://developer.echonest.com/api/v4/";
    private static String CATALOGUE = "catalog/";
    private static String SONG = "song/";
    private static String SEARCH = "search?";
    private static String CREATE = "create?";
    private static String UPDATE = "update?";
    private static String APIKEY = "api_key=ATQ062PLMZDCJK4UW";
    private static String FORMAT = "&format=json";
    private static String TYPE = "&type=song";
    private static String TASTEID = "&id=";
    private static String DATA = "&data=";
    private static String NAME = "&name=";
    private static String ARTIST = "&artist=";
    private static String TITLE = "&title=";


    private static String ACTION = "action";
    private static String ITEMUPDATE = "update";
    private static String ITEM = "item";
    private static String ITEMID = "item_id";

    private RequestQueue queue;
    private ConcurrentHashMap<JsonObjectRequest, Boolean> songRequests;
    private ConcurrentHashMap<JsonObjectRequest, Boolean> updateRequests;

    private EchoNest(Context ctx) {
        queue = Volley.newRequestQueue(ctx);
        songRequests = new ConcurrentHashMap<JsonObjectRequest, Boolean>();
        updateRequests = new ConcurrentHashMap<JsonObjectRequest, Boolean>();
    }

    public static synchronized EchoNest getInstance(Context ctx) {
        if (instance == null) {
            instance = new EchoNest(ctx);
        }
        return instance;
    }

    public ConcurrentHashMap<JsonObjectRequest, Boolean> getSongRequests() {return songRequests;}
    public ConcurrentHashMap<JsonObjectRequest, Boolean> getUpdateRequests() {return updateRequests;}

    public void send(int method, String url, JSONObject obj, EchoNestListener response,
                     ConcurrentHashMap<JsonObjectRequest, Boolean> requestMap) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, obj, response,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("EchoNest", "Failed to get JsonObj."+error);
                    }
                });
        response.setRequestParams(request, requestMap);
        requestMap.put(request, false);
    }

    public void createTasteProfile(Context ctx, CreateTasteListener response) {
        String url = BASEURL+CATALOGUE+CREATE+APIKEY+FORMAT+TYPE+NAME+
                Uri.encode(Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID));
        send(Request.Method.POST, url, null, response, updateRequests);
        Log.d("EchoNest", url);
    }


    public void updateTasteProfile(String taste_id, String song_id) {
        JSONObject update = new JSONObject();
        JSONObject item = new JSONObject();
        try {
            item.put(ITEMID, song_id);
            update.put(ACTION, ITEMUPDATE);
            update.put(ITEM, item);
        } catch (JSONException e) {e.printStackTrace();}
        String url = BASEURL+CATALOGUE+UPDATE+APIKEY+FORMAT+TASTEID+ Uri.encode(taste_id) +DATA+ update.toString();
        Log.d("updateTasteProfile", url);
        send(Request.Method.POST, url, null, new EchoNestListener(), updateRequests);
    }

    public void getSongID(String artist, String title, SongDBListener response) {
        //could have some exceptions where the artist has acoustic versions or something that
        //would make artist+title non-unique
        String url = BASEURL+SONG+SEARCH+APIKEY+FORMAT+ARTIST+ Uri.encode(artist) +TITLE+ Uri.encode(title);
        //Log.d("EchoNest", url);
        send(Request.Method.GET, url, null, response, songRequests);
    }

    public void runRequests(ConcurrentHashMap<JsonObjectRequest, Boolean> requestMap) {
//        Log.d("EchoNest", "runrequests called");
        for (Iterator<JsonObjectRequest> requestsIter = requestMap.keySet().iterator();
             requestsIter.hasNext();) {
            queue.add(requestsIter.next());
//            requestsIter.remove();
//            Log.d("EchoNest", "something to run");
        }
    }


    public boolean requestsFinished(ConcurrentHashMap<JsonObjectRequest, Boolean> requestMap) {
        boolean state = true;
        for (Iterator<Boolean> requestsIter = requestMap.values().iterator();
             requestsIter.hasNext();) {
            if (!requestsIter.next()) state = false;
        }
        return state;
    }


}
