package com.example.myapplication.model;
import com.example.myapplication.BaseActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import kotlin.text.Charsets;
import android.content.Context;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.function.Function;

public class DataService {


    //private static String serverIp = "192.168.0.199"; //production



    //private static String serverIp = "10.207.176.91"; //office

    //private static String serverIp = "lp-22-0331.adt.ae/"; //office guest
    private static String serverIp = "10.207.176.109"; //office CORP
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
        if(port.equals("80"))return  "http://" + ip ;
        else return  "http://" + ip + ":" + port ;
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
        ListParams lp = new ListParams();
        lp.Where = where;
        lp.OrderBy = orderBy;
        lp.Select = select;
        postForList(path,lp,success,context);
    }
    public  void postForList(String path,ListParams params ,Function<JSONArray,Void>  success, Context context){

        postForList(path, params, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return  null;
        });
    }
    public  void postForList(String path,ListParams params ,Function<JSONArray,Void>  success, Function<String,Void>  failure){

        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Where",params.Where);
        param.add("OrderBy",params.OrderBy);
        param.add("Select",params.Select);
        param.put("Take",params.Take);
        postForObject(JSONArray.class,"EntityApi/List",param,success,failure);
    }
    public  void postForList(String path, String select,String where,String orderBy,Function<JSONArray,Void>  success, Function<String,Void>  failure){
        ListParams lp = new ListParams();
        lp.Where = where;
        lp.OrderBy = orderBy;
        lp.Select = select;
        postForList(path,lp,success,failure);




    }


    public static class ListParams{
        public String Select;
        public String Where;
        public String OrderBy;
        public int Take;
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
            convertResult(TypeToken.getParameterized(ArrayList.class, type),s,success,failure);
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

    public <T extends Serializable> void postForExecute(Class<T> type,String path,JSONObject argsJson ,Function<T,Void>  success, Context context){
        postForExecute(type,path,argsJson, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }
    public <T extends Serializable> void postForExecute(Class<T> type,String path,JSONObject argsJson ,Function<T,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("ArgsJson",argsJson);
        postForObject(type,"EntityApi/Execute", param, success, failure);
    }

    public  void postForExecuteList(String path, JSONObject argsJson,Function<JSONArray,Void>  success, Function<String,Void>  failure){

        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("ArgsJson",argsJson);
        postForObject(JSONArray.class,"EntityApi/Execute",param,success,failure);
    }
    public <T extends Serializable> void postForExecuteList(String path, JSONObject argsJson,Function<JSONArray,Void>  success, Context context){
        postForExecuteList(path,argsJson, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }


    public <T extends Serializable> void postForExecuteList(Class<T> type,String path,JSONObject argsJson ,Function<ArrayList<T>,Void>  success, Context context){
        postForExecuteList(type,path,argsJson, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }
    public <T extends Serializable> void postForExecuteList(Class<T> type,String path,JSONObject argsJson ,Function<ArrayList<T>,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("ArgsJson",argsJson);
        postForString("EntityApi/Execute", param, s -> {
            convertResult(TypeToken.getParameterized(ArrayList.class, type),s,success,failure);
            return null;
        },failure);
    }




    public <T> void postForObject(Class<T> type,String url,RequestParams param,Function<T,Void>  success, Context context){
        postForObject(type, url, param, success, s -> {
            Toast.makeText(context,s,Toast.LENGTH_SHORT);
            return null;
        });
    }


    public void postForObject(String url,RequestParams param,Function<JSONObject,Void>  success, Function<String,Void>  failure){
        postForObject(JSONObject.class,url,param,success,failure);
    }

    public <T> void postForObject(Class<T> type,String url,RequestParams param,Function<T,Void>  success, Function<String,Void>  failure){
        postForString(url,param , s -> {
            convertResult(TypeToken.get(type),s,success,failure);
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
            convertResult(TypeToken.get(type),s,success,failure);
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


    private <T> void convertResult(TypeToken token, String data,Function<T,Void>  success, Function<String,Void>  failure){
        T result = null;
        if(!data.equals("null")) { //true
            try {
                if (JSONObject.class.isAssignableFrom(token.getRawType())) {
                    result = (T) new JSONObject(data);
                } else if (JSONArray.class.isAssignableFrom(token.getRawType())) {
                    result = (T) new JSONArray(data);
                } else {
                    if(Boolean.class.isAssignableFrom(token.getRawType()) && !data.equals("null") && !data.equals("true")&& !data.equals("false"))
                        throw new Exception("Value error");
                    Gson gson = new GsonBuilder().create();
                    result = (T) gson.fromJson(data, token);
                }
            } catch (JSONException e) {
                String error = formatError(data);
                failure.apply(error);
                return;

            } catch (Exception e) {
                String error = formatError(data);
                failure.apply(error);
                return;

            }
        }
        success.apply(result);
    }


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

        String finalUrl= getRootUrl()  + "/api/" + url;  //office
        AsyncHttpClient ahc = new AsyncHttpClient();

        ahc.setResponseTimeout(50000);

        ahc.get(finalUrl, response);
    }




    public  void put(String url, RequestParams params, AsyncHttpResponseHandler response){
        System.out.println(url);
        System.out.println(params);
        String finalUrl= getRootUrl() + "/api/" + url;  //office
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

        String finalUrl= getRootUrl() + "/api/" + "EntityApi/Upload?fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&id=" + id + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);

        AsyncHttpClient  cl = new AsyncHttpClient();
        //cl.setTimeout(10000);
        cl.setResponseTimeout(50000);
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
    public  void postForString(String url, RequestParams params, Function<String,Void> success, Function<String,Void> failure){
        System.out.println(url);
        System.out.println(params);
        String finalUrl= getRootUrl() + "/api/" + url;  //office

        AsyncHttpClient ahc  = new AsyncHttpClient();
        ahc.setResponseTimeout(50000);
        ahc.post(finalUrl, params, new AsyncHttpResponseHandler() {
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
        public JSONObject getDatas() throws JSONException {
            return  new JSONObject(Properties);
        }

        public JSONObject convertProperties() throws JSONException{
            return  new JSONObject(Properties);
        }
    }
}

