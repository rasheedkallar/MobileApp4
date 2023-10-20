package com.example.myapplication.model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.FileEntity;
import cz.msebera.android.httpclient.util.ExceptionUtils;
import kotlin.text.Charsets;


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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DataService {



    //private static String serverIp = "10.207.176.91"; //office

    //private static String serverIp = "lp-22-0331.adt.ae/"; //office guest
    // private static String serverIp = "10.207.176.91"; //office
    private static String serverIp = "192.168.0.126"; //home
    //private static String serverIp = "192.168.0.139"; //homeWifi
    //192.168.0.126
    private static String  serverPort = "80";
    public static String getRootUrl(){
        if(serverPort == "80")return  "http://" + serverIp + "/api/";
        else return  "http://" + serverIp + ":" + serverPort + "/api/";
    }

    public  void get(String url, AsyncHttpResponseHandler response){
        System.out.println(url);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().get(finalUrl, response);
    }
    public  void getString(String url, Function<String,Void> success, Function<String,Void> failure){
        get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(responseBody == null)success.apply(null);
                String result = new String(responseBody);
                success.apply(result);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : " + url;
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();
                    StackTraceElement trace[] = error.getStackTrace();
                    for (StackTraceElement element : trace)
                        result = result +  "\r\n" + element.toString() ;
                }
                failure.apply(result);
            }
        });
    }
    public  void getArray(String url, Function<JSONArray,Void> success, Function<String,Void> failure){
        getString(url, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        JSONArray array = new JSONArray(s);
                        success.apply(array);

                    } catch (JSONException e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                failure.apply(s);
                return null;
            }
        });

    }
    public  void getArray(String url, Function<JSONArray,Void> success, Context context){
        getArray(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                System.out.println(s);
                Toast.makeText(context,s,Toast.LENGTH_SHORT);

                return null;
            }
        });
    }

    public  void getObject(String url, Function<JSONObject,Void> success, Context context){
        getObject(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                System.out.println(s);
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }

    public  void getLookupList(String url, Function<ArrayList<Lookup>,Void> success, Function<String,Void> failure){
        getString(url, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s == "null")success.apply(new ArrayList<>());
                else{
                    Gson gson = new GsonBuilder().create();
                    Type listType = new TypeToken<ArrayList<Lookup>>() {}.getType();
                    ArrayList<Lookup> lookups = gson.fromJson(s, listType);
                    success.apply(lookups);
                }
                return null;
            }
        },failure);
    }
    public  void getLookupList(String url, Function<ArrayList<Lookup>,Void> success, Context context){
        getLookupList(url, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                System.out.println(s);
                return null;
            }
        });
    }


    public  void getObject(String url, Function<JSONObject,Void> success, Function<String,Void> failure){
        getString(url, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        JSONObject obj = new JSONObject(s);
                        success.apply(obj);

                    } catch (JSONException e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                failure.apply(s);
                return null;
            }
        });

    }
    public  void getString(String url, Function<String,Void> success, Context context){
        getString(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                System.out.println(s);
                return null;
            }
        });
    }





    public  void put(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(params);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().put(finalUrl,params, response);
    }

    public static abstract   class onImageUpload{
        public abstract void imageUpload(Bitmap image,String message);
    }
    private String URLEncode(String data){

        if(data == null)return  "";
        try{
            return URLEncoder.encode(data, Charsets.UTF_8.name());
        }catch (UnsupportedEncodingException e){
            return  data;
        }
    }
    public  void upload(Context context,File file,String fileName, String entity,Long id, AsyncHttpResponseHandler handler) {
        upload( context, file, fileName,  entity, id,null ,null,  handler);
    }
    public  void upload(Context context,File file,String fileName, String entity,Long id,String fileGroup , String path, AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        try{
            params.put("file",file,"image/jpeg");
        }
        catch (FileNotFoundException e){
            Toast.makeText(context, "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }
        String finalUrl= getRootUrl() + "refFile?fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&id=" + id + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);
        System.out.println(params);
        AsyncHttpClient  cl = new AsyncHttpClient();
        cl.setTimeout(1000);
        cl.post(finalUrl, params, handler);
    }



    public  void delete(String url, AsyncHttpResponseHandler response){
        System.out.println(url);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().delete(finalUrl, response);
    }
    public  void postForString(String url, RequestParams params, Function<String,Void> success, Function<String,Void> failure){
        System.out.println(params);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().post(finalUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                success.apply(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : " + url;
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();
                    StackTraceElement trace[] = error.getStackTrace();
                    for (StackTraceElement element : trace)
                        result = result +  "\r\n" + element.toString() ;
                }
                failure.apply(result);
            }
        });
    }
    public  void postForString(String url, RequestParams params, Function<String,Void> success, Context context){
        postForString(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                System.out.println(s);
                return null;
            }
        });
    }
    public  void postForLong(String url, RequestParams params, Function<Long,Void> success, Context context){
        postForString(url, params, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.equals("null"))success.apply(null);
                else {
                    success.apply(Long.parseLong(s));
                }
                return null;
            }
        },context);
    }


    public  void post(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(params);
        String finalUrl= getRootUrl() + url;  //office
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
                String errorMessage = "getById Error:";
                if(responseBody != null)errorMessage =  errorMessage  +  new String(responseBody);
                errorMessage = errorMessage +  error.getMessage() + error.getStackTrace().toString();
                System.out.println(errorMessage);
                Toast.makeText(context,  errorMessage, Toast.LENGTH_SHORT).show();
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
                //ArrayList<Lookup> list = gson.fromJson(result, listType);
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
    public static class Lookup implements Serializable {

        private String Properties;
        private Long Id;
        private String Name;

        public  Lookup()
        {

        }
        public Lookup(Long id,String name){
            Id = id;
            Name = name;
        }



        public Long getId() {
            return Id;
        }

        public void setId(Long id) {
            Id = id;
        }



        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }



        public String getProperties() {
            return Properties;
        }

        public void setProperties(String properties) {
            Properties = properties;
        }
        //public JSONObject getDatas() throws JSONException {
        //    return  new JSONObject(Properties);
        //}

        public JSONObject convertProperties() throws JSONException{
            return  new JSONObject(Properties);
        }
    }
}

