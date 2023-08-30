package com.example.myapplication.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.FileEntity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataService {

    //private final String rootUrl = "http://10.207.176.91/api/"; //office
    private final String rootUrl = "http://192.168.0.126/api/"; //home

    //private final String rootUrl = "http://192.168.0.139/api/"; //home wifi



    public  void get(String url, AsyncHttpResponseHandler response){
        System.out.println(url);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().get(finalUrl, response);
    }

    public  void put(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(params);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().put(finalUrl,params, response);
    }

    public static abstract   class onImageUpload{
        public abstract void imageUpload(Bitmap image,String message);
    }

    public  void upload(Context context,File file,String fileName, String entity,Long id, AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        try{
            params.put("file",file,"image/jpeg");
        }
        catch (FileNotFoundException e){
            Toast.makeText(context, "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }
        //params.put("fileName",fileName);
        //params.put("entity",entity);
        //params.put("id",id);

        String finalUrl= rootUrl + "refFile?fileName=" + fileName + "&entity=" + entity + "&id=" + id;

        //String finalUrl= rootUrl;
        System.out.println(params);
        AsyncHttpClient  cl = new AsyncHttpClient();
        cl.setTimeout(1000);
        cl.post(finalUrl, params, handler);
    }



    public  void delete(String url, AsyncHttpResponseHandler response){
        System.out.println(url);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().delete(finalUrl, response);
    }
    public  void post(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(params);
        String finalUrl= rootUrl + url;  //office
        new AsyncHttpClient().post(finalUrl,params, response);
    }

    public  void getById(Context context, String url, Long id, DataService.GetByIdResponse response){
        get(url +  "?id=" + id.toString(),new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject data = new JSONObject(result);
                    response.onSuccess(data);
                }
                catch (JSONException ex){
                    Toast.makeText(context, "getById Error:" + result, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                Toast.makeText(context, "getById Error:" + result, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public  void deleteById(Context context, String url, Long id, DataService.DeleteByIdResponse response){
        delete(url +  "?id=" + id.toString(),new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String result = new String(responseBody);
                try {
                     boolean res   = Boolean.getBoolean(result);
                     response.onSuccess(true);
                }
                catch ( Exception ex) {
                    Toast.makeText(context, "deleteById Error:" + result, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                Toast.makeText(context, "deleteById Error:" + result, Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(context, "getLookup Error:" + result, Toast.LENGTH_SHORT).show();
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
                if(responseBody == null){
                    String result = error.toString();
                    Toast.makeText(context, "getLookups Error:" + result, Toast.LENGTH_SHORT).show();
                }
                else{
                    String result = new String(responseBody);
                    Toast.makeText(context, "getLookups Error:" + result, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    public abstract  class DataResponse {
        public abstract void onSuccess(String data);
    }
    public static abstract  class LookupResponse {
        public abstract void onSuccess(List<Lookup> lookup);
    }
    public static abstract  class GetByIdResponse {
        public abstract void onSuccess(JSONObject data);
    }

    public static abstract  class DeleteByIdResponse {
        public abstract void onSuccess(Boolean deleted);
    }

    public static abstract  class LookupsResponse {
        public abstract void onSuccess(List<Lookup>[] lookups);
    }
    public static class Lookup{
        public  Lookup()
        {

        }
        public Lookup(Long id,String name){
            Id = id;
            Name = name;
        }

        public Long Id;
        public String Name;
        public String Properties;
        public JSONObject getDatas() throws JSONException {
            return  new JSONObject(Properties);
        }
    }
}

