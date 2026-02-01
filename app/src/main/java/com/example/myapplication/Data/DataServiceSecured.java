package com.example.myapplication.Data;

import android.content.Context;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Centralized HTTP client with:
 *  - Active base URL from ConnectionRepository
 *  - Bearer token injection from TokenManager
 *  - One-time retry on 401 (refresh token and retry)
 *  - Support for form params (RequestParams) and raw JSON (StringEntity)
 */
public class DataServiceSecured {

    private final Context appContext;
    private final ConnectionRepository connRepo;
    private final TokenManager tokenManager;

    public DataServiceSecured(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        this.connRepo = new ConnectionRepository(appContext);
        this.tokenManager = new TokenManager(appContext);
    }

    /** Resolve the current base URL (without trailing /api) */
    /** Resolve the best base URL:
     *  1) If an active URL exists and responds to ping -> use it
     *  2) Else, try all saved connections -> pick the fastest responding
     *  3) Else, call discovery endpoint (no headers), save, ping all, pick the fastest
     *  4) Persist chosen active base and return
     *  5) Fallback to legacy IP if nothing responds
     *
     *  NOTE: This performs network calls; do NOT call on main thread.
     */
    private String getRootUrl() {
        final int PING_TIMEOUT_MS = 1500; // per your environment; adjust as needed
        final String DISCOVERY_URL = "https://api.greenleafuae.com/api/token/GetMobileConnections";

        // 1) Try previously active
        String active = connRepo.getActiveBaseUrl();
        if (!TextUtils.isEmpty(active) && connRepo.ping(active, PING_TIMEOUT_MS)) {
            return ConnectionRepository.normalizeBase(active);
        }

        // 2) Try saved list
        List<ConnectionRepository.MobileConnection> saved = connRepo.getSavedConnections();
        if (saved != null && !saved.isEmpty()) {
            String best = connRepo.selectBestConnectionUrl(saved, PING_TIMEOUT_MS);
            if (!TextUtils.isEmpty(best)) {
                connRepo.setActiveBaseUrl(best);
                return best;
            }
        }

        // 3) Discover from server (no header), save, then try again
        List<ConnectionRepository.MobileConnection> discovered =
                connRepo.fetchConnectionsFromDiscovery(DISCOVERY_URL);

        if (discovered != null && !discovered.isEmpty()) {
            String best = connRepo.selectBestConnectionUrl(discovered, PING_TIMEOUT_MS);
            if (!TextUtils.isEmpty(best)) {
                connRepo.setActiveBaseUrl(best);
                return best;
            }
        }

        // 4) Fallback to your legacy default
        String ip = "10.207.176.109";
        String port = "80";
        return port.equals("80") ? ("http://" + ip) : ("http://" + ip + ":" + port);
    }



    /** Public API: form-style calls using RequestParams */
    public void httpAction(String type, String relativeUrl, RequestParams params, AsyncHttpResponseHandler response) {
        final String base = getRootUrl();
        final String finalUrl = base + "/api/" + relativeUrl;

        AsyncHttpClient client = new AsyncHttpClient();
        client.setResponseTimeout(50000);

        // Ensure we have a valid token (refresh if needed)
        TokenManager.TokenBundle tb = tokenManager.getValidTokenOrRefresh(base);
        if (tb != null && !TextUtils.isEmpty(tb.token)) {
            client.addHeader("Authorization", "Bearer " + tb.token);
        }

        // Wrap to handle 401 -> refresh token -> retry once
        AsyncHttpResponseHandler wrapped = new AsyncHttpResponseHandler() {
            private boolean retried = false;

            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                response.onSuccess(statusCode, headers, responseBody);
            }

            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (statusCode == 401 && !retried) {
                    retried = true;
                    // Try refresh
                    TokenManager.TokenBundle fresh = tokenManager.getValidTokenOrRefresh(base);
                    if (fresh != null && !TextUtils.isEmpty(fresh.token)) {
                        AsyncHttpClient retry = new AsyncHttpClient();
                        retry.setResponseTimeout(50000);
                        retry.addHeader("Authorization", "Bearer " + fresh.token);
                        dispatch(retry, type, finalUrl, params, this, null, null);
                        return;
                    }
                }
                response.onFailure(statusCode, headers, responseBody, error);
            }
        };

        dispatch(client, type, finalUrl, params, wrapped, null, null);
    }

    /**
     * Convenience for JSON body calls (e.g., POST/PUT with application/json).
     * Pass null for params, and provide a StringEntity (UTF-8) as body.
     */
    public void httpActionJson(String type, String relativeUrl, StringEntity jsonEntity, AsyncHttpResponseHandler response) {
        final String base = getRootUrl();
        final String finalUrl = base + "/api/" + relativeUrl;

        AsyncHttpClient client = new AsyncHttpClient();
        client.setResponseTimeout(50000);

        TokenManager.TokenBundle tb = tokenManager.getValidTokenOrRefresh(base);
        if (tb != null && !TextUtils.isEmpty(tb.token)) {
            client.addHeader("Authorization", "Bearer " + tb.token);
        }

        AsyncHttpResponseHandler wrapped = new AsyncHttpResponseHandler() {
            private boolean retried = false;

            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                response.onSuccess(statusCode, headers, responseBody);
            }

            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (statusCode == 401 && !retried) {
                    retried = true;
                    TokenManager.TokenBundle fresh = tokenManager.getValidTokenOrRefresh(base);
                    if (fresh != null && !TextUtils.isEmpty(fresh.token)) {
                        AsyncHttpClient retry = new AsyncHttpClient();
                        retry.setResponseTimeout(50000);
                        retry.addHeader("Authorization", "Bearer " + fresh.token);
                        dispatch(retry, type, finalUrl, null, this, "application/json", jsonEntity);
                        return;
                    }
                }
                response.onFailure(statusCode, headers, responseBody, error);
            }
        };

        dispatch(client, type, finalUrl, null, wrapped, "application/json", jsonEntity);
    }

    /**
     * Internal dispatcher that supports:
     *  - GET/DELETE (no body)
     *  - POST/PUT with RequestParams (form)
     *  - POST/PUT with StringEntity (JSON)
     */
    private void dispatch(AsyncHttpClient client,
                          String type,
                          String url,
                          RequestParams params,
                          AsyncHttpResponseHandler handler,
                          String contentTypeForEntity,     // e.g., "application/json"
                          StringEntity stringEntity) {     // if non-null, use entity post/put

        String verb = type == null ? "" : type.trim().toUpperCase();

        switch (verb) {
            case "GET":
                client.get(url, handler);
                break;

            case "DELETE":
                // LoopJ delete overload with params is not consistent; we use simple delete
                client.delete(url, handler);
                break;

            case "POST":
                if (stringEntity != null) {
                    client.post(null, url, stringEntity,
                            contentTypeForEntity != null ? contentTypeForEntity : "application/json", handler);
                } else {
                    client.post(url, params, handler);
                }
                break;

            case "PUT":
                if (stringEntity != null) {
                    client.put(null, url, stringEntity,
                            contentTypeForEntity != null ? contentTypeForEntity : "application/json", handler);
                } else {
                    client.put(url, params, handler);
                }
                break;

            default:
                // Fallback as GET if unknown verb
                client.get(url, handler);
                break;
        }
    }

    /* -------------------- Optional helpers -------------------- */

    /** GET helper */
    public void get(String relativeUrl, AsyncHttpResponseHandler response) {
        httpAction("GET", relativeUrl, null, response);
    }

    /** POST helper (form) */
    public void post(String relativeUrl, RequestParams params, AsyncHttpResponseHandler response) {
        httpAction("POST", relativeUrl, params, response);
    }

    /** POST helper (JSON) */
    public void postJson(String relativeUrl, StringEntity jsonEntity, AsyncHttpResponseHandler response) {
        httpActionJson("POST", relativeUrl, jsonEntity, response);
    }

    /** PUT helper (form) */
    public void put(String relativeUrl, RequestParams params, AsyncHttpResponseHandler response) {
        httpAction("PUT", relativeUrl, params, response);
    }

    /** PUT helper (JSON) */
    public void putJson(String relativeUrl, StringEntity jsonEntity, AsyncHttpResponseHandler response) {
        httpActionJson("PUT", relativeUrl, jsonEntity, response);
    }

    /** DELETE helper */
    public void delete(String relativeUrl, AsyncHttpResponseHandler response) {
        httpAction("DELETE", relativeUrl, null, response);
    }
}