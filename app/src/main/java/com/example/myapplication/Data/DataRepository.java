package com.example.myapplication.Data;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import com.example.myapplication.BaseActivity;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import cz.msebera.android.httpclient.Header;

public class DataRepository {

    private static DataRepository.MobileConnection CurrentConnection = null;

    private static DataRepository.Company CurrentCompany = null;
    public static DataRepository.Company getCurrentCompany(){
        return CurrentCompany;
    }
    public static void setCurrentCompany(DataRepository.Company company){
        CurrentCompany = company;
        if(BaseActivity.MenuBar != null){
            if(getCurrentCompany() == null){
                BaseActivity.MenuBar.setTitle("Bytes Mobile");
            }
            else{
                String companyName = DataRepository.getCurrentCompany().name;
                BaseActivity.MenuBar.setTitle(companyName);
            }
        }

    }
    public static Date CurrentConnectionLastCall = null;

    public static   List<DataRepository.MobileConnection> Connections = null;
    public static   List<DataRepository.Company> Companies = null;
    public static   Date CompaniesRefreshDate = null;
    public static Date ConnectionsRefreshDate = null;



    private static final String KEY_CONNECTIONS = "connections";
    private static final String KEY_COMPANIES = "Companies";
    private static final String KEY_ACTIVE_URL = "active_base_url";
    private final SecureStore store;
    private final Gson gson = new Gson();

    public  static MobileConnection getCurrentConnection(){
        return  CurrentConnection;
    }

    public static void ChooseBestConnection(Context context,java.util.List<DataRepository.MobileConnection> list, Function<DataRepository.MobileConnection,Void> callBack){
        for (DataRepository.MobileConnection mc : list) {
            mc.ValidateConnection(context, new Function<Boolean, Void>() {
                @Override
                public Void apply(Boolean aBoolean) {
                    RespondBestIfAllConnectionValidate(list, callBack);
                    return null;
                }
            });
        }
    }




    private static void RespondBestIfAllConnectionValidate(
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
    public static void setCurrentConnection(MobileConnection connection){

        CurrentConnection = connection;
        if(BaseActivity.ConnectionMenu != null){
            if (DataRepository.CurrentConnection == null) {
                BaseActivity.ConnectionMenu.setTitle("Net");
            } else {
                BaseActivity.ConnectionMenu.setTitle(DataRepository.CurrentConnection.name);
            }
            // 2. Now build Spannable from the *new* title
            SpannableString span = new SpannableString(BaseActivity.ConnectionMenu.getTitle());
            // 3. Apply color
            if (DataRepository.CurrentConnection != null &&
                    DataRepository.CurrentConnection.Valid) {
                span.setSpan(new ForegroundColorSpan(Color.GREEN), 0, span.length(), 0);
            } else {
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, span.length(), 0);
            }
            // 4. Important: Set the spannable back to the menu item
            BaseActivity.ConnectionMenu.setTitle(span);
        }

    }

    public DataRepository(Context ctx) {
        this.store = new SecureStore(ctx);
    }

    public static class Company {
        @SerializedName(value = "code", alternate = {"Code"})
        public String code;

        @SerializedName(value = "name", alternate = {"Name"})
        public String name;
    }

    // Model (maps both PascalCase and camelCase from server)
    public static class MobileConnection {
        @SerializedName(value = "order", alternate = {"Order"})
        public String order;

        @SerializedName(value = "name", alternate = {"Name"})
        public String name;

        // Expect base host (no /api); we will normalize anyway
        @SerializedName(value = "url", alternate = {"Url"})
        public String url;

        @SerializedName(value = "updateDate", alternate = {"UpdateDate"})
        public String updateDate;



        /** -------------------- Device-managed (persist locally) -------------------- */
        /** Per-connection token stored locally (populated/updated by device code) */
        public String Token = null;

        /** When the Token was last retrieved on device (UTC recommended) */

        public Date TokenRetrieveTime = null;

        //public String TokenCompany = null;


        /** -------------------- Optional helpers -------------------- */

        /** Mark as valid now */
        public void markValidNow() {
            this.Valid = true;
            this.ValidDate = new Date();
        }

        /** Mark as invalid (no ping) */
        public void markInvalid() {
            this.Valid = false;
            this.ValidDate = null;
        }







        public void  ValidateConnection(Context context,Function<Boolean,Void> callBack){
            MobileConnection connection = this;


            String fullUrl = url + "/api/MobileApi/Ping?company=" + BaseActivity.Company;
            new DataService(context).httpAction("GET",fullUrl,null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    System.out.println(fullUrl + "-" + response);
                    if(response.equals("\"Success\"")) {
                        Valid = true;
                        ValidDate = new Date();
                        if(DataRepository.CurrentConnection != null && DataRepository.CurrentConnection.name.equals(name)){
                            CurrentConnectionLastCall = new Date();
                            DataRepository.setCurrentConnection(connection);
                        }

                        callBack.apply(true);
                    }
                    else{
                        Valid = false;
                        ValidDate = new Date();
                        if(DataRepository.CurrentConnection != null && DataRepository.CurrentConnection.name.equals(name)){
                            CurrentConnectionLastCall = new Date();
                            DataRepository.setCurrentConnection(connection);
                        }
                        callBack.apply(false);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String msg = ServerErrorExtractor.extractError(responseBody);
                    System.out.println(fullUrl + "-"  + msg);
                    Valid = false;
                    ValidDate = new Date();
                    if(DataRepository.CurrentConnection != null && DataRepository.CurrentConnection.name.equals(name)){
                        CurrentConnectionLastCall = new Date();
                        DataRepository.setCurrentConnection(connection);
                    }
                    callBack.apply(false);
                }
            },null,1000);
        }



        /** Returns true if a token exists and is recent based on a threshold (seconds) */
        public boolean hasRecentToken( long maxAgeSeconds) {
            //if (TokenCompany == null || TokenCompany.isEmpty() ) return false;
            //if(!TokenCompany.equals(company))return  false;
            if (Token == null || Token.isEmpty() || TokenRetrieveTime == null) return false;
            long ageSec = (System.currentTimeMillis() - TokenRetrieveTime.getTime()) / 1000L;
            return ageSec >= 0 && ageSec <= maxAgeSeconds;
        }

        /** Update token and retrieval time to now */
        public void setTokenNow(String token) {
            this.Token = token;
            this.TokenRetrieveTime = new Date();
            //this.TokenCompany = company;
        }
        public String getToken(){
            return Token;
        }
        public transient boolean Valid = false;
        public transient Date ValidDate = null;

    }

    /* ----------------------- Persistence ----------------------- */

    public List<MobileConnection> getSavedConnections() {
        String json = store.getString(KEY_CONNECTIONS, null);
        if (TextUtils.isEmpty(json)) return new ArrayList<>();
        Type t = new TypeToken<List<MobileConnection>>(){}.getType();
        return gson.fromJson(json, t);
    }
    public void saveConnections(List<MobileConnection> list) {
        store.putString(KEY_CONNECTIONS, gson.toJson(list));
    }

    public List<Company> getSavedCompanies() {
        String json = store.getString(KEY_COMPANIES, null);
        if (TextUtils.isEmpty(json)) return new ArrayList<>();
        Type t = new TypeToken<List<Company>>(){}.getType();
        return gson.fromJson(json, t);
    }

    public void saveCompanies(List<Company> list) {
        store.putString(KEY_COMPANIES, gson.toJson(list));
    }


    public String getActiveBaseUrl() {
        return store.getString(KEY_ACTIVE_URL, null);
    }

    public void setActiveBaseUrl(String baseUrl) {
        if (baseUrl != null) baseUrl = normalizeBase(baseUrl);
        store.putString(KEY_ACTIVE_URL, baseUrl);
    }

    /* ----------------------- URL helpers ----------------------- */

    public static String normalizeBase(String url) {
        if (url == null) return null;
        url = url.trim();
        if (url.endsWith("/")) url = url.substring(0, url.length()-1);
        if (url.toLowerCase(Locale.US).endsWith("/api")) {
            url = url.substring(0, url.length()-4);
        }
        return url;
    }


}