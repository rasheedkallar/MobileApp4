package com.example.myapplication.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataService {
    public  void get(String url, AsyncHttpResponseHandler response){
        String finalUrl= "http://192.168.0.126/api/" + url;
        new AsyncHttpClient().get(finalUrl, response);
    }
    public  void getLookup(Context context, String lookup, DataService.LookupResponse response){
        get("Lookup?type=" + lookup,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Gson gson = new GsonBuilder().create();
                Type listType = new TypeToken<ArrayList<Lookup>>(){}.getType();
                ArrayList<Lookup> list = gson.fromJson(result, listType);
                List<DataService.Lookup> lookup = gson.fromJson(result, listType);
                response.onSuccess(lookup);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                G.showAlertDialog(context,"Error Alert",result);
            }
        });
    }
    public abstract  class DataResponse {
        public abstract void onSuccess(String data);
    }
    public abstract  class LookupResponse {
        public abstract void onSuccess(List<Lookup> lookup);
    }
    public  class Lookup{
        public Long Id;
        public String Name;
        public String Properties;
        public JSONObject getDatas() throws JSONException {
            return  new JSONObject(Properties);
        }
    }
}
