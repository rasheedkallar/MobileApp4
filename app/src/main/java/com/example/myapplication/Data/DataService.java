package com.example.myapplication.Data;
import com.example.myapplication.BaseActivity;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.PopupHtml;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataService {

    private final Context appContext;
    private final DataRepository connRepo;

    public DataService(Context ctx){
        if(ctx == null){
            throw new IllegalArgumentException("Context cannot be null");
        }

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
                        if(DataRepository.getCurrentConnection() == null){
                            DataRepository.ChooseBestConnection(appContext,DataRepository.Connections,null);
                        }
                        if(callBack != null) callBack.apply(DataRepository.Connections);
                        return null; // IMPORTANT: Function<..., Void> must return null
                    },
                    (String error) -> {
                        if (callBack != null) callBack.apply(null);
                        if(DataRepository.getCurrentConnection() == null){
                            DataRepository.ChooseBestConnection(appContext,DataRepository.Connections,null);
                        }
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
    public  void  GetCompanies(Function<List<DataRepository.Company>,Void> callBack) {
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
                    DataRepository.CompaniesRefreshDate = new Date();
                    if(DataRepository.CurrentSettings.Company != null && DataRepository.Companies != null && DataRepository.getCurrentCompany() == null){
                        for (DataRepository.Company c : DataRepository.Companies) {
                            if (c.code != null && c.code.equals(DataRepository.CurrentSettings.Company)) {
                                DataRepository.setCurrentCompany(c);
                                break;
                            }
                        }
                    }
                    else{
                        DataRepository.setCurrentCompany(null);
                    }
                    if(callBack != null) callBack.apply(DataRepository.Companies);
                    return null; // IMPORTANT: Function<..., Void> must return null
                },
                (String error) -> {
                    if(callBack != null) callBack.apply(null);
                    return null; // IMPORTANT: Function<..., Void> must return null
                });
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String msg = ServerErrorExtractor.extractError(responseBody);
                System.out.println("Error on GetCompanies : https://api.greenleafuae.com/api/MobileApi/GetCompanies msg : "  + msg);
            }
        },null,1500);
    }


    public  void GetConnection(Function<DataRepository.MobileConnection,Void> callBack){
        long thirtyMinutes = 30 * 60 * 1000; // 30 minutes in milliseconds
        long now = new Date().getTime();
        if(DataRepository.getCurrentConnection() != null){
            if(callBack !=null)callBack.apply(DataRepository.getCurrentConnection());
            return;
        }
        else if(DataRepository.Connections == null || DataRepository.Connections.isEmpty()){
            DataRepository.Connections = connRepo.getSavedConnections();
            if(DataRepository.Connections == null || DataRepository.Connections.size() == 0 ){
                GetConnections(new Function<List<DataRepository.MobileConnection>, Void>() {
                    @Override
                    public Void apply(List<DataRepository.MobileConnection> mobileConnections) {
                        DataRepository.ChooseBestConnection(appContext,DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                            DataRepository.setCurrentConnection(mc);
                            if(callBack !=null)callBack.apply(DataRepository.getCurrentConnection());
                            return null;
                        });
                        return null;
                    }
                });
            }
            else{
                DataRepository.ChooseBestConnection(appContext, DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                    DataRepository.setCurrentConnection(mc);
                    if(callBack !=null)callBack.apply(DataRepository.getCurrentConnection());
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
                }
            }
        }
        else{
            DataRepository.ChooseBestConnection(appContext, DataRepository.Connections, (DataRepository.MobileConnection mc) -> {
                DataRepository.setCurrentConnection(mc);
                if(callBack !=null)callBack.apply(DataRepository.getCurrentConnection());
                return null;
            });
        }
    }

    public  void MakeSureToken(DataRepository.MobileConnection mc, Function<DataRepository.MobileConnection,Void> callBack) {
        if(mc.hasRecentToken(24 * 60 * 60 * 6)){
            if(callBack !=null)callBack.apply(mc);
        }
        else{
            RequestParams param = new RequestParams();
            param.put("UserId","rasheedkallar@gmail.com");
            param.put("Password","Gold123#");
            param.put("Company",DataRepository.CurrentSettings.Company);
            String finalUrl = mc.url + "/api/MobileApi/GetToken";
            httpAction("POST",finalUrl,param, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String json = new String(responseBody);
                    TypeToken<String>  token = new TypeToken<String>() {};
                    convertResult(token,json,
                        (String tokenKey) -> {
                            System.out.println(finalUrl + "-" + DataRepository.CurrentSettings.Company + "-" + tokenKey);
                            mc.setTokenNow(tokenKey,DataRepository.CurrentSettings.Company, mc.name);
                            if(callBack !=null)callBack.apply(mc);
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
                    if(callBack !=null)callBack.apply(null);

                }
            },null,10000);
        }
    }

    public void httpEntityAction(String type, String url,RequestParams params,AsyncHttpResponseHandler response){
        GetConnection(new Function<DataRepository.MobileConnection, Void>() {
            @Override
            public Void apply(DataRepository.MobileConnection connection) {
                MakeSureToken(connection,mc -> {
                    if(mc == null){
                        String errorMessage = "Token retrieve fails";
                        response.onFailure(
                                400,                   // statusCode
                                null,                  // headers
                                errorMessage.getBytes(), // responseBody as bytes
                                null                   // error
                        );
                    }
                    else {
                        Map<String, String> headers = null;
                        if (mc.hasRecentToken(24 * 60 * 60 * 6)) {
                            headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " + mc.getToken());   // same as your C# DefaultRequestHeaders.Authorization
                        }
                        httpAction(type, mc.url + "/api/" + url, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                DataRepository.CurrentConnectionLastCall = new Date();
                                if (!connection.Valid)
                                    connection.ValidateConnection(appContext, null);
                                response.onSuccess(statusCode, headers, responseBody);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                connection.ValidateConnection(appContext, null);
                                String msg = ServerErrorExtractor.extractError(responseBody);
                                System.out.println("Error on : " + mc.url + "/api/" + url + " msg : " + msg);
                                response.onFailure(statusCode, headers, responseBody, error);
                            }
                        }, headers);
                    }
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
    public <T extends Serializable> void postForSave(String path,String saveJson ,Function<Long,Void>  success, Function<String,Void>  failure){
        try {
            JSONObject obj = new JSONObject(saveJson);
            postForSave( path, obj , success,  failure);
        } catch (JSONException e) {
            if(failure !=null)failure.apply(e.getMessage());
        }
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
                if(responseBody == null && success != null)success.apply(null);
                String result = new String(responseBody);
                System.out.println(result);
                if(success !=null)success.apply(result);
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
                if(failure !=null)failure.apply(result);

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
                if(failure !=null)failure.apply(error);
                return;

            } catch (Exception e) {
                String error = formatError(data);
                if(failure !=null)failure.apply(error);
                return;

            }
        }
        if(success !=null)success.apply(result);
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

    private RequestParams buildParamsDynamically(JSONObject obj) throws JSONException {
        RequestParams params = new RequestParams();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = obj.get(key);

            if (value == JSONObject.NULL) {
                params.put(key, "");
            } else {
                params.put(key, value.toString());
            }
        }
        return params;
    }


    public  void upload(File file,String fileName, String entity,String guid,String fileGroup , String path,Function<JSONObject,Void> success, Context context){
        System.out.println("&fileName," + entity + "," + guid );
        RequestParams params = new RequestParams();
        try{
            params.put("file",file,"image/jpeg");
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
            Toast.makeText(context, "Invalid image", Toast.LENGTH_SHORT).show();
            return;
        }
        String url= "MobileApi/Upload?select=" + URLEncode(Control.ImageControl.SelectQuery) + "&fileName=" + URLEncode(fileName) + "&entity=" + URLEncode(entity) + "&guid=" + URLEncode(guid) + "&fileGroup=" + URLEncode(fileGroup) + "&path=" + URLEncode(path);
        System.out.println(params + "; Url: " + url);
        httpEntityAction("POST",url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody, StandardCharsets.UTF_8);
                System.out.println(result);
                JSONObject obj;
                try {
                    obj = new JSONObject(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if(success !=null)success.apply(obj);
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
                if(success !=null)success.apply(result);
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
                if(failure !=null)failure.apply(result);
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

