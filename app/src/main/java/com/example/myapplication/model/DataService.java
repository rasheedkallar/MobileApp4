package com.example.myapplication.model;
import com.example.myapplication.BaseActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DataService {


    //private static String serverIp = "192.168.0.199"; //production



    private static String serverIp = "10.207.176.91"; //office

    //private static String serverIp = "lp-22-0331.adt.ae/"; //office guest
    //private static String serverIp = "10.207.176.91"; //office CORP
    //private static String serverIp = "10.205.50.22"; //office ADT HUB





    //private static String serverIp = "abunaser01/"; //shop

    //private static String serverIp = "192.168.0.126"; //home
    //private static String serverIp = "192.168.0.139"; //homeWifi
    //192.168.0.126
    private static String  serverPort = "80";
    public static String getRootUrl(){

        String ip = serverIp;
        String port = serverPort;
        if(BaseActivity.IpAddress != null && BaseActivity.IpAddress.length() != 0)ip = BaseActivity.IpAddress;
        if(BaseActivity.Port != null && BaseActivity.Port != 0)port = BaseActivity.Port.toString();
        if(port.equals("80"))return  "http://" + ip + "/api/";
        else return  "http://" + ip + ":" + port + "/api/";
    }


    public  void postForSelect(String path, String select,Function<JSONObject,Void>  success, Context context){
        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Select",select);
        postForObject(JSONObject.class,"EntityApi/Select",param,success,context);
    }
    public <T extends Serializable> void postForSelect(Class<T> type,String path, String select,Function<T,Void>  success, Context context){
        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Select",select);
        postForObject(type,"EntityApi/Select",param,success,context);
    }

    public  void postForList(String path, String select,String where,String orderBy,Function<JSONArray,Void>  success, Context context){

        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Where",where);
        param.add("OrderBy",orderBy);
        param.add("Select",select);
        postForObject(JSONArray.class,"EntityApi/List",param,success,context);
    }
    public <T extends Serializable> void postForList(Class<T> type,String path, String select,String where,String orderBy,Function<ArrayList<T>,Void>  success, Context  context) {
        postForList(type, path,  select, where,orderBy, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }
    public <T extends Serializable> void postForList(Class<T> type,String path, String select,String where,String orderBy,Function<ArrayList<T>,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Select",select);
        param.add("Where",where);
        param.add("OrderBy",orderBy);
        postForString("EntityApi/List", param, s -> {
            try{
                Gson gson = new GsonBuilder().create();
                ArrayList<T> result = (ArrayList<T>)gson.fromJson(s, TypeToken.getParameterized(ArrayList.class, type));
                success.apply(result);
            }
            catch (Exception e ){
                String result =formatError(s);
                failure.apply(result);
            }
            return null;
        },failure);
    }
    public <T extends Serializable> void postForDelete(String path, Function<Boolean,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        postForObject(Boolean.class,"EntityApi/Delete",param,success,failure);
    }
    public <T extends Serializable> void postForSave(String path,JSONObject saveJson ,Function<Long,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("SaveJson",saveJson);
        postForObject(Long.class,"EntityApi/Save",param,success,failure);
    }

    public <T> void postForObject(Class<T> type,String url,RequestParams param,Function<T,Void>  success, Context context){
        postForObject(type, url, param, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }

    private <T>  ArrayList<T> ConvertToList(Class<T> type, String json){
        Gson gson = new GsonBuilder().create();
        return  (ArrayList<T>)gson.fromJson(json, type);
    }
    private <T> ArrayList<T> ConvertToMyList(Class<T> type, String json) {
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<ArrayList<T>>(){}.getType();
        return gson.fromJson(json, listType);
    }


    public <T> void postForObject(Class<T> type,String url,RequestParams param,Function<T,Void>  success, Function<String,Void>  failure){
        postForString(url,param , s -> {
            try{
                if(JSONObject.class.isAssignableFrom(type)){
                    success.apply((T)new JSONObject(s));
                }else if(JSONArray.class.isAssignableFrom(type)){
                    success.apply((T)new JSONArray(s));
                }else{
                    Gson gson = new GsonBuilder().create();
                    T result = (T)gson.fromJson(s, type);
                    success.apply(result);
                }
            }
            catch (JSONException e ){
                String result = formatError(s);
                failure.apply(result);
            }
            catch (Exception e ){
                String result =formatError(s);
                failure.apply(result);
            }
            return null;
        },failure);
    }


    public <T  extends Serializable>  void getList(Class<T> type,String url,Function<ArrayList<T>,Void>  success, Context context){
        ParameterizedType parameterizedType = (ParameterizedType) TypeToken.getParameterized(ArrayList.class, type).getType();
        getObject((Class<ArrayList<T>>) parameterizedType.getRawType(), url, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }

    public  void getList(String url,Function<JSONArray,Void>  success, Context context){
        getObject(JSONArray.class, url, success, context);
    }

    public  void getObject(String url,Function<JSONObject,Void>  success, Context context){
        getObject(JSONObject.class, url, success, context);
    }
    public <T>  void getObject(Class<T> type,String url,Function<T,Void>  success, Context context){

        getObject(type, url, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }
    public <T> void getObject(Class<T> type,String url,Function<T,Void>  success, Function<String,Void>  failure){
        getString(url, s -> {
            try{
                if(JSONObject.class.isAssignableFrom(type)){
                    success.apply((T)new JSONObject(s));
                }else if(JSONArray.class.isAssignableFrom(type)){
                    success.apply((T)new JSONArray(s));
                }else{
                    Gson gson = new GsonBuilder().create();
                    T result = (T)gson.fromJson(s, type);
                    success.apply(result);
                }
            }
            catch (JSONException e ){
                String result = formatError(s);
                failure.apply(result);
            }
            catch (Exception e ){
                String result =formatError(s);
                failure.apply(result);
            }
            return null;
        },failure);
    }

    public  void getString(String url, Function<String,Void> success, Function<String,Void> failure){
        System.out.println(url);
        get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(responseBody == null)success.apply(null);
                String result = new String(responseBody);
                System.out.println(result);
                success.apply(result);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : " + url;
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();

                }
                for (String item: result.split("\r\n")) {
                    System.out.println(result);
                }
                failure.apply(result);

            }
        });
    }
    public  void getString(String url, Function<String,Void> success, Context context){
        getString(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }


    /*

    public  void getJArray(String url, Function<JSONArray,Void> success, Function<String,Void> failure){
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
    public  void getJArray(String url, Function<JSONArray,Void> success, Context context){
        getJArray(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }

    public  void getJObject(String url, Function<JSONObject,Void> success, Context context){
        getJObject(url,success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }




    public  void getJObject(String url, Function<JSONObject,Void> success, Function<String,Void> failure){
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

     */

    private String formatError(String json){
        System.out.println("Error on service request");
        try{
            Gson gson = new GsonBuilder().create();
            String result = gson.fromJson(json,String.class);
            for (String item: result.split("\r\n")) {
                System.out.println(item);
            }
            return result;
        }
        catch (Exception e){
            return json;
        }
    }


    public  void get(String url, AsyncHttpResponseHandler response){

        String finalUrl= getRootUrl() + url;  //office
        AsyncHttpClient hc = new AsyncHttpClient();

        hc.get(finalUrl, response);
    }




    public  void put(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(url);
        System.out.println(params);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().put(finalUrl,params, response);
    }


    private String URLEncode(String data){

        if(data == null)return  "";
        try{
            return URLEncoder.encode(data, Charsets.UTF_8.name());
        }catch (UnsupportedEncodingException e){
            return  data;
        }
    }
    //public  void upload(Context context,File file,String fileName, String entity,String fileGroup,Long id, AsyncHttpResponseHandler handler) {
    //    upload( context, file, fileName,  entity, id,fileGroup ,null,  handler);
    //}

    //Maximum request length exceeded.

    public  void upload(File file,String fileName, String entity,Long id,String fileGroup , String path, Function<Long,Void> success, Context context){

        System.out.println("&fileName," + entity + "," + id );
        RequestParams params = new RequestParams();
        try{
            params.put("file",file,"image/jpeg");
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
            Toast.makeText(context, "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println(params);

        String finalUrl= getRootUrl() + "refFile/Post?fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&id=" + id + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);

        AsyncHttpClient  cl = new AsyncHttpClient();
        //cl.setTimeout(10000);
        cl.setResponseTimeout(10000);
        //cl.setConnectTimeout(10000);
        cl.post(finalUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                System.out.println(result);
                success.apply(Long.parseLong(result));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : Post Image";
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();

                }
                for (String item: result.split("\r\n")) {
                    System.out.println(result);
                }
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*



    public  void delete(String url, Function<String,Void> success,Context context){
        System.out.println(url);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().delete(finalUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                System.out.println(result);
                if(result.equals("\"Success\"")){
                    success.apply("Success");
                }
                else{
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : Delete file";
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();

                }
                for (String item: result.split("\r\n")) {
                    System.out.println(result);
                }
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public  void postForJArray(String url, RequestParams params,Function<JSONArray,Void> success, Context context){
        postForJArray(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {

                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }
    public  void postForJArray(String url, RequestParams params,Function<JSONArray,Void> success, Function<String,Void> failure){
        postForString(url, params, new Function<String, Void>() {
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
        }, failure);
    }
    public  void postForLong(String url, RequestParams params, Function<Long,Void> success, Context context){
        postForLong(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {

                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }
    public  void postForLong(String url, RequestParams params,Function<Long,Void> success, Function<String,Void> failure){
        postForString(url, params, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        Long id = Long.parseLong(s);
                        success.apply(id);

                    } catch (Exception e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, failure);
    }

    public  void postForLookups(String url, RequestParams params,Function<ArrayList<Lookup>,Void> success, Context context){
        postForLookups(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {

                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }
    public  void postForLookups(String url, RequestParams params,Function<ArrayList<Lookup>,Void> success, Function<String,Void> failure){
        postForString(url, params, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        Gson gson = new GsonBuilder().create();
                        Type listType = new TypeToken<ArrayList<Lookup>>(){}.getType();
                        ArrayList<DataService.Lookup> lookups = gson.fromJson(s, listType);
                        success.apply(lookups);

                    } catch (Exception e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, failure);
    }

    public  void postForLookup(String url, RequestParams params,Function<Lookup,Void> success, Context context){
        postForLookup(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {

                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }
    public  void postForLookup(String url, RequestParams params,Function<Lookup,Void> success, Function<String,Void> failure){
        postForString(url, params, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        Gson gson = new GsonBuilder().create();
                        Type listType = new TypeToken<Lookup>(){}.getType();
                        DataService.Lookup lookup = gson.fromJson(s, listType);
                        success.apply(lookup);

                    } catch (Exception e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, failure);
    }



    public  void postForJObject(String url, RequestParams params,Function<JSONObject,Void> success, Context context){
        postForJObject(url, params, success, new Function<String, Void>() {
            @Override
            public Void apply(String s) {

                Toast.makeText(context,s,Toast.LENGTH_SHORT);
                return null;
            }
        });
    }
    public  void postForJObject(String url, RequestParams params,Function<JSONObject,Void> success, Function<String,Void> failure){
        postForString(url, params, new Function<String, Void>() {
            @Override
            public Void apply(String s) {
                if(s == null || s.length() == 0 || s.equals("null"))success.apply(null);
                else {
                    try {
                        JSONObject object = new JSONObject(s);
                        success.apply(object);

                    } catch (JSONException e) {
                        failure.apply(s);
                    }
                }
                return null;
            }
        }, failure);
    }

    */
    public  void postForString(String url, RequestParams params, Function<String,Void> success, Function<String,Void> failure){
        System.out.println(url);
        System.out.println(params);
        String finalUrl= getRootUrl() + url;  //office
        new AsyncHttpClient().post(finalUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                System.out.println(result);
                success.apply(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result ="Error on : " + url;
                if(responseBody != null)result = result + "\r\n" + new String(responseBody);
                if(error != null) {
                    result = result + "\r\n" + error.getMessage();

                }
                for (String item: result.split("\r\n")) {
                    System.out.println(result);
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

                return null;
            }
        });
    }


    /*
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

                return null;
            }
        });
    }


     */

    //public  void getObject(String[] lookups, Function<ArrayList<Lookup>,Void> success, Function<String,Void> failure) {
    //
    //}


    //public  void getLookups(String[] lookups, Function<ArrayList<Lookup>,Void> success, Context context) {
    //
    //}

/*


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
*/

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

