package com.theworldmatrix.cocktailmusicsearch;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by M on 2015-11-26.
 */
public class EchoNestListener implements Response.Listener<JSONObject> {

    private JsonObjectRequest thisRequest;
    private ConcurrentHashMap<JsonObjectRequest, Boolean> requestsMap;

    public void setRequestParams(JsonObjectRequest thisRequest,
                                 ConcurrentHashMap<JsonObjectRequest, Boolean> requestsMap) {
        this.thisRequest = thisRequest;
        this.requestsMap = requestsMap;
    }

    public JsonObjectRequest getThisRequest() {return thisRequest;}

    @Override
    public void onResponse(JSONObject response) {
        checkStatus(response);
//        Log.d("EchoNestListener", response.toString());
        requestsMap.put(thisRequest, true);
    }

    private void checkStatus(JSONObject status) {
        try {
            String message = status.getJSONObject("response").getJSONObject("status").getString("message");
            if (message.equals("Success")) {
            } else if (message.equals("Rate Limit Exceeded")) {
                //will add other code later to handle this for user
                throw new Error(message);
            } else {
//                Log.d("EchoNestListener", status.toString());
                throw new Error(message);
            }
        } catch (JSONException e) {e.printStackTrace();}
    }
}
