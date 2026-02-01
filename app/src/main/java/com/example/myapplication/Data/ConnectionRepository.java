package com.example.myapplication.Data;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

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
import java.util.concurrent.CountDownLatch;

import cz.msebera.android.httpclient.Header;

public class ConnectionRepository {

    private static final String KEY_CONNECTIONS = "connections";
    private static final String KEY_ACTIVE_URL = "active_base_url";
    private final SecureStore store;
    private final Gson gson = new Gson();

    public ConnectionRepository(Context ctx) {
        this.store = new SecureStore(ctx);
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

    /* ----------------------- Networking ------------------------ */

    /** Simple availability ping (no timing), true if HTTP 2xx */
    public boolean ping(String baseUrl, int timeoutMs) {
        return pingLatencyMs(baseUrl, timeoutMs) < Long.MAX_VALUE;
    }

    /** Measure ping latency to /api/system/ping. Returns Long.MAX_VALUE on failure. */
    public long pingLatencyMs(String baseUrl, int timeoutMs) {
        final String pingUrl = normalizeBase(baseUrl) + "/api/system/ping";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(timeoutMs);
        client.setResponseTimeout(timeoutMs);

        final long start = SystemClock.elapsedRealtime();
        final long[] result = { Long.MAX_VALUE };
        final CountDownLatch latch = new CountDownLatch(1);

        client.get(pingUrl, new AsyncHttpResponseHandler() {
            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode >= 200 && statusCode < 300) {
                    result[0] = SystemClock.elapsedRealtime() - start;
                } else {
                    result[0] = Long.MAX_VALUE;
                }
                latch.countDown();
            }
            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                result[0] = Long.MAX_VALUE;
                latch.countDown();
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {}
        return result[0];
    }

    /**
     * Fetch connections from a discovery endpoint (no headers), save them, and return the list.
     * @param discoveryUrl absolute URL, e.g. "https://api.greenleafuae.com/token/GetMobileConnections"
     */
    /**
     * Fetch connections from a discovery endpoint (no headers), save them, and return the list.
     * @param discoveryUrl absolute URL, e.g. "https://api.greenleafuae.com/token/GetMobileConnections"
     */
    public List<MobileConnection> fetchConnectionsFromDiscovery(String discoveryUrl) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(1000);
        client.setResponseTimeout(1000);

        final List<MobileConnection> out = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        System.out.println("Attempting to fetch from: " + discoveryUrl);
        client.get(discoveryUrl, new AsyncHttpResponseHandler() {
            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String json = new String(responseBody);
                    System.out.println("SUCCESS Response: " + json);
                    Type t = new TypeToken<List<MobileConnection>>(){}.getType();
                    List<MobileConnection> list = gson.fromJson(json, t);
                    if (list != null) {
                        // Normalize each URL
                        for (MobileConnection mc : list) {
                            if (mc != null && mc.url != null) {
                                mc.url = normalizeBase(mc.url);
                            }
                        }
                        out.addAll(list);
                        saveConnections(list);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing successful response: " + e.getMessage());
                }
                latch.countDown();
            }
            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.err.println("--- Network Call Failed ---");
                System.err.println("URL: " + discoveryUrl);
                System.err.println("Status Code: " + statusCode);
                if (responseBody != null) {
                    System.err.println("Response Body: " + new String(responseBody));
                }
                if (error != null) {
                    System.err.println("Error Message: " + error.getMessage());
                    error.printStackTrace();
                }
                System.err.println("---------------------------");
                latch.countDown();
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {}
        return out;
    }

    /**
     * Iterate connections, ping each, and return the baseUrl with the lowest latency.
     * Returns null if none respond within timeout.
     */
    public String selectBestConnectionUrl(List<MobileConnection> list, int timeoutMs) {
        long bestLatency = Long.MAX_VALUE;
        String bestBase = null;
        if (list == null) return null;

        for (MobileConnection mc : list) {
            if (mc == null || TextUtils.isEmpty(mc.url)) continue;
            String base = normalizeBase(mc.url);
            long latency = pingLatencyMs(base, timeoutMs);
            if (latency < bestLatency) {
                bestLatency = latency;
                bestBase = base;
            }
        }
        return bestBase;
    }
}