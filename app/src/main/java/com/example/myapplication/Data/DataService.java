package com.example.myapplication.Data;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataService {

    private final Context appContext;
    private final DataRepository connRepo;

    public DataService(Context ctx){
        this.appContext = ctx.getApplicationContext();
        this.connRepo = new DataRepository(appContext);
    }
    public  void  GetConnections(Function<List<DataRepository.MobileConnection>,Void> callBack) {
        String fullUrl = "https://api.greenleafuae.com/api/MobileApi/GetMobileConnections";
        httpAction("GET",fullUrl,null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                System.out.println(json);
                TypeToken<java.util.List<DataRepository.MobileConnection>>  token = new TypeToken<java.util.List<DataRepository.MobileConnection>>() {};
                convertResult(token,json,(java.util.List<DataRepository.MobileConnection> list) -> {
                        DataRepository.Connections = list;
                        System.out.println(fullUrl + "-"  + list.size());
                        DataRepository.ConnectionsRefreshDate = new Date();
                        connRepo.saveConnections(list);
                        callBack.apply(DataRepository.Connections);
                        return null; // IMPORTANT: Function<..., Void> must return null
                    },
                    (String error) -> {
                        return null; // IMPORTANT: Function<..., Void> must return null
                    }
                );
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String msg = ServerErrorExtractor.extractError(responseBody);
                System.out.println("Error on GetMobileConnections : https://api.greenleafuae.com/api/MobileApi/GetMobileConnections msg : "  + msg);
            }
        },null,1500);
    }
    public  void  GetCCompanies(Function<List<DataRepository.Company>,Void> callBack) {
        String fullUrl = "https://api.greenleafuae.com/api/MobileApi/GetCompanies";

        httpAction("GET",fullUrl,null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                System.out.println(json);
                TypeToken<java.util.List<DataRepository.Company>>  token = new TypeToken<java.util.List<DataRepository.Company>>() {};
                convertResult(token,json,(java.util.List<DataRepository.Company> list) -> {
                            DataRepository.Companies = list;
                            System.out.println(fullUrl + "-"  + list.size());

                            connRepo.saveCompanies(list);
                            callBack.apply(DataRepository.Companies);


                            return null; // IMPORTANT: Function<..., Void> must return null
                        },
                        (String error) -> {
                            return null; // IMPORTANT: Function<..., Void> must return null
                        }
                );
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String msg = ServerErrorExtractor.extractError(responseBody);
                System.out.println("Error on GetCompanies : https://api.greenleafuae.com/api/MobileApi/GetCompanies msg : "  + msg);
            }
        },null,1500);
    }


    public  void GetConnection(Function<DataRepository.MobileConnection,Void> callBack){
        //final int PING_TIMEOUT_MS = 1500; // per your environment; adjust as needed
        //final String DISCOVERY_URL = "https://api.greenleafuae.com/api/MobileApi/GetMobileConnections";


        long thirtyMinutes = 30 * 60 * 1000; // 30 minutes in milliseconds
        long now = new Date().getTime();

        if(DataRepository.Companies == null) {
            GetCCompanies(new Function<List<DataRepository.Company>, Void>() {
                @Override
                public Void apply(List<DataRepository.Company> companies) {
                    return null;
                }
            });
        }


        if(DataRepository.getCurrentConnection() != null && DataRepository.CurrentConnectionLastCall != null && (now - DataRepository.CurrentConnectionLastCall.getTime()) < thirtyMinutes){
            callBack.apply(DataRepository.getCurrentConnection());
            return;
        }
        else if(DataRepository.Connections == null || DataRepository.Connections.isEmpty()){
            DataRepository.Connections = connRepo.getSavedConnections();
            if(DataRepository.Connections == null || DataRepository.Connections.size() == 0 ){
                GetConnections(new Function<List<DataRepository.MobileConnection>, Void>() {
                    @Override
                    public Void apply(List<DataRepository.MobileConnection> mobileConnections) {
                        ChooseBestConnection(DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                            DataRepository.setCurrentConnection(mc);
                            callBack.apply(DataRepository.getCurrentConnection());
                            return null;
                        });
                        return null;
                    }
                });
            }
            else{
                ChooseBestConnection(DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                    DataRepository.setCurrentConnection(mc);
                    callBack.apply(DataRepository.getCurrentConnection());
                    return null;
                });
                if (DataRepository.ConnectionsRefreshDate == null || DataRepository.ConnectionsRefreshDate.before(new Date(System.currentTimeMillis() - 15 * 60 * 1000))) {
                    System.out.println( "Current connections refreshing from server after last refresh 15 minutes earlier");
                    GetConnections(new Function<List<DataRepository.MobileConnection>, Void>() {
                        @Override
                        public Void apply(List<DataRepository.MobileConnection> mobileConnections) {
                            return null;
                        }
                    });
                    GetCCompanies(new Function<List<DataRepository.Company>, Void>() {
                        @Override
                        public Void apply(List<DataRepository.Company> companies) {
                            return null;
                        }
                    });
                }
            }
        }
        else {
            ChooseBestConnection(DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                DataRepository.setCurrentConnection(mc);;
                callBack.apply(DataRepository.getCurrentConnection());
                return null;
            });
        }
        //System.out.println("No active connection available");
    }
    private void ChooseBestConnection(java.util.List<DataRepository.MobileConnection> list, Function<DataRepository.MobileConnection,Void> callBack){
        for (DataRepository.MobileConnection mc : list) {
            mc.ValidateConnection(appContext, new Function<Boolean, Void>() {
                @Override
                public Void apply(Boolean aBoolean) {
                    RespondBestIfAllConnectionValidate(list, callBack);
                    return null;
                }
            });
        }
    }


    private void RespondBestIfAllConnectionValidate(
            java.util.List<DataRepository.MobileConnection> list,
            Function<DataRepository.MobileConnection, Void> callBack) {
        for (DataRepository.MobileConnection mc : list) {
            if(mc.ValidDate == null)return;
            if(mc.Valid){
                System.out.println("Valid best Connection :" + mc.name + " " + mc.url);
                callBack.apply(mc);
                return;
            }
        }
        System.out.println("No valid connection available");
    }
    public  void MakeSureToken(DataRepository.MobileConnection mc, Function<DataRepository.MobileConnection,Void> callBack) {
        if(mc.hasRecentToken(24 * 60 * 60 * 6)){
            callBack.apply(mc);
        }
        else{
            RequestParams param = new RequestParams();
            param.put("UserId","rasheedkallar@gmail.com");
            param.put("Password","Gold123#");
            param.put("Company",BaseActivity.Company);
            String finalUrl = mc.url + "/api/MobileApi/GetToken";
            httpAction("POST",finalUrl,param, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String json = new String(responseBody);
                    System.out.println(json);
                    TypeToken<String>  token = new TypeToken<String>() {};
                    convertResult(token,json,
                        (String tokenKey) -> {
                            System.out.println(finalUrl + "-" + BaseActivity.Company + "-" + tokenKey);
                            mc.setTokenNow(tokenKey);
                            //mc.Token = tokenKey;
                            //mc.TokenRetrieveTime = new Date();
                            callBack.apply(mc);
                            return null; // IMPORTANT: Function<..., Void> must return null
                        },
                        (String error) -> {
                            return null; // IMPORTANT: Function<..., Void> must return null
                        }
                    );
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String msg = ServerErrorExtractor.extractError(responseBody);
                    System.out.println("Token retrieve : " + mc.url + "/api/MobileApi/GetToken" + " msg : "  + msg);

                }
            },null,1000);
        }
    }

    public void httpEntityAction(String type, String url,RequestParams params,AsyncHttpResponseHandler response){
        GetConnection(new Function<DataRepository.MobileConnection, Void>() {
            @Override
            public Void apply(DataRepository.MobileConnection connection) {
                MakeSureToken(connection,mc -> {
                    Map<String, String> headers = null;
                    if(mc.hasRecentToken(24 * 60 * 60 * 6)){
                        headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + mc.getToken());   // same as your C# DefaultRequestHeaders.Authorization
                    }
                    httpAction(type,mc.url + "/api/" + url,params,new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            //DataRepository.CurrentConnection = mc;
                            DataRepository.CurrentConnectionLastCall = new Date();
                            response.onSuccess(statusCode,headers,responseBody);
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String msg = ServerErrorExtractor.extractError(responseBody);
                            System.out.println("Error on Ping : " + mc.url + "/" + url + " msg : "  + msg);
                            response.onFailure(statusCode,headers,responseBody,error);
                        }
                    },headers);
                    return null;
                });
                return null;
            }
        });
    }
    public void httpAction(String type, String url,RequestParams params,AsyncHttpResponseHandler response,Map<String, String> headers) {
        httpAction(type,url,params,response,headers,50000);
    }
    public void httpAction(String type,String url,RequestParams params,AsyncHttpResponseHandler response,Map<String, String> headers,int timeOut) {

        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.setResponseTimeout(timeOut);
        ahc.setConnectTimeout(timeOut);

        // Add headers if provided
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    ahc.addHeader(e.getKey(), e.getValue());
                }
            }
        }

        String verb = (type == null ? "" : type.trim().toUpperCase());
        switch (verb) {
            case "GET":
                ahc.get(url, response);
                break;
            case "POST":
                ahc.post(url, params, response);
                break;
            case "PUT":
                ahc.put(url, params, response);
                break;
            case "DELETE":
                // LoopJ delete() does not have a body variant; this is header-only
                ahc.delete(url, response);
                break;
            default:
                ahc.get(url, response);
                break;
        }
    }
    public  void postForSelect(String path, String select,Function<JSONObject,Void>  success, Context context){
        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Select",select);
        postForObject(JSONObject.class,"MobileApi/Select",param,success,context);
    }
    public <T extends Serializable> void postForSelect(Class<T> type,String path, String select,Function<T,Void>  success, Context context){
        RequestParams param = new RequestParams();
        param.put("Path",path);
        param.add("Select",select);
        postForObject(type,"MobileApi/Select",param,success,context);
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
        postForObject(JSONArray.class,"MobileApi/List",param,success,failure);
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
        postForString("MobileApi/List", param, s -> {
            convertResult(TypeToken.getParameterized(ArrayList.class, type),s,success,failure);
            return null;
        },failure);
    }
    public <T extends Serializable> void postForDelete(String path, Function<Boolean,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        postForObject(Boolean.class,"MobileApi/Delete",param,success,failure);
    }
    public <T extends Serializable> void postForSave(String path,JSONObject saveJson ,Function<Long,Void>  success, Function<String,Void>  failure){
        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("SaveJson",saveJson);
        postForObject(Long.class,"MobileApi/Save",param,success,failure);
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
        postForObject(type,"MobileApi/Execute", param, success, failure);
    }

    public  void postForExecuteList(String path, JSONObject argsJson,Function<JSONArray,Void>  success, Function<String,Void>  failure){

        RequestParams param = new RequestParams();
        param.add("Path",path);
        param.put("ArgsJson",argsJson);
        postForObject(JSONArray.class,"MobileApi/Execute",param,success,failure);
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
        postForString("MobileApi/Execute", param, s -> {
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
    public  void getObject(String url,Function<JSONObject,Void>  success, Function<String,Void>  failure){
        getString(url, s -> {
            convertResult(TypeToken.get(JSONObject.class),s,success,failure);
            return null;
        },failure);
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
        httpEntityAction("GET",url,null ,new AsyncHttpResponseHandler() {
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
        httpEntityAction("GET",url,null,response);

    }
    private String URLEncode(String data){

        if(data == null)return  "";
        try{
            return URLEncoder.encode(data, Charsets.UTF_8.name());
        }catch (UnsupportedEncodingException e){
            return  data;
        }
    }
    /*
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

        String finalUrl= getRootUrl() + "/api/" + "MobileApi/Upload?fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&id=" + id + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);

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
    */


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
        String url= "MobileApi/Upload?fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&id=" + id + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);
        httpEntityAction("POST",url, params, new AsyncHttpResponseHandler() {
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
        httpEntityAction("POST",url, params, new AsyncHttpResponseHandler() {
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

