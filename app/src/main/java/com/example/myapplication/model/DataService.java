package com.example.myapplication.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataService {

    //private final String rootUrl = "http://10.207.176.91/api/"; //office
    private final String rootUrl = "http://192.168.0.126/api/"; //home

    public  void get(String url, AsyncHttpResponseHandler response){
        System.out.println(url);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().get(finalUrl, response);
    }
    public  void post(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(params);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().post(finalUrl,params, response);
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
                new PopupHtml(context,"Get Lookup Error",result);
            }
        });
    }
    public  void getLookups(Context context, String[] lookups, DataService.LookupsResponse response){
        get("Lookup?types=" + String.join("&types=", lookups),new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Gson gson = new GsonBuilder().create();
                Type listType = new TypeToken<List<Lookup>[]>(){}.getType();
                List<Lookup>[] list = gson.fromJson(result, listType);

                response.onSuccess(list);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                new PopupHtml(context,"Get Lookup Error",result);
            }
        });
    }


    public abstract  class DataResponse {
        public abstract void onSuccess(String data);
    }
    public static abstract  class LookupResponse {
        public abstract void onSuccess(List<Lookup> lookup);
    }
    public static abstract  class LookupsResponse {
        public abstract void onSuccess(List<Lookup>[] lookups);
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

