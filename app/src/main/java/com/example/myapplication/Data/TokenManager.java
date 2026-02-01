package com.example.myapplication.Data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class TokenManager {

    // ✅ If you want to reference it as TokenManager.TokenBundle
    public static class TokenBundle {
        public String token;
        public long issuedAtUtcEpochSeconds;
        public long expiresAtUtcEpochSeconds;
    }

    private static final String KEY_CRED = "user_cred";
    private static final String KEY_TOKEN_PREFIX = "token_";

    public static class Credentials {
        public String userId;
        public String password;
        public String company;
    }

    private final SecureStore store;
    private final Gson gson = new Gson();

    public TokenManager(Context ctx) {
        this.store = new SecureStore(ctx);
    }

    public void saveCredentials(Credentials c) {
        store.putString(KEY_CRED, gson.toJson(c));
    }

    public Credentials getCredentials() {
        String json = store.getString(KEY_CRED, null);
        return TextUtils.isEmpty(json) ? null : gson.fromJson(json, Credentials.class);
    }

    public void saveTokenForBase(String baseUrl, TokenBundle token) {
        String key = KEY_TOKEN_PREFIX + baseUrl.replace("://","_").replace("/","_");
        store.putString(key, gson.toJson(token));
    }

    public TokenBundle getTokenForBase(String baseUrl) {
        String key = KEY_TOKEN_PREFIX + baseUrl.replace("://","_").replace("/","_");
        String json = store.getString(key, null);
        return TextUtils.isEmpty(json) ? null : gson.fromJson(json, TokenBundle.class);
    }

    public boolean isExpired(TokenBundle tb) {
        if (tb == null) return true;
        long now = System.currentTimeMillis() / 1000;
        if (tb.expiresAtUtcEpochSeconds > 0) return now >= tb.expiresAtUtcEpochSeconds;
        return now >= (tb.issuedAtUtcEpochSeconds + 24 * 3600);
    }

    // ✅ Ensure this signature and access modifier are EXACT
    public TokenBundle getValidTokenOrRefresh(String baseUrl) {
        TokenBundle tb = getTokenForBase(baseUrl);
        if (tb != null && !isExpired(tb)) return tb;

        Credentials c = getCredentials();
        if (c == null) return null;

        String url = baseUrl + "/api/auth/gettoken";
        AsyncHttpClient client = new AsyncHttpClient();

        final TokenBundle[] out = {null};
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            JSONObject payload = new JSONObject();
            payload.put("UserId", c.userId);
            payload.put("Password", c.password);
            payload.put("Company", c.company);

            StringEntity entity = new StringEntity(payload.toString(), "UTF-8");
            entity.setContentType("application/json");

            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String s = new String(responseBody);
                        JSONObject o = new JSONObject(s);
                        TokenBundle nb = new TokenBundle();
                        nb.token = o.optString("token");

                        long now = System.currentTimeMillis() / 1000;
                        String issued = o.optString("issuedAtUtc", null);
                        String expires = o.optString("expiresAtUtc", null);
                        nb.issuedAtUtcEpochSeconds = parseIsoToEpoch(issued, now);
                        nb.expiresAtUtcEpochSeconds = parseIsoToEpoch(expires, 0);

                        if (nb.expiresAtUtcEpochSeconds == 0 && !TextUtils.isEmpty(nb.token)) {
                            decodeJwtTimes(nb);
                        }

                        saveTokenForBase(baseUrl, nb);
                        out[0] = nb;
                    } catch (Exception ignored) {}
                    latch.countDown();
                }
                @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            latch.countDown();
        }

        try { latch.await(); } catch (InterruptedException ignored) {}
        return out[0];
    }

    private long parseIsoToEpoch(String iso, long fallback) {
        if (iso == null) return fallback;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try { return sdf.parse(iso).getTime() / 1000; }
        catch (ParseException e) {
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf2.parse(iso).getTime() / 1000;
            } catch (ParseException ex) { return fallback; }
        }
    }

    private void decodeJwtTimes(TokenBundle nb) {
        try {
            String[] parts = nb.token.split("\\.");
            if (parts.length != 3) return;
            byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            String payloadJson = new String(payloadBytes);
            JSONObject pj = new JSONObject(payloadJson);
            if (pj.has("exp")) nb.expiresAtUtcEpochSeconds = pj.optLong("exp", nb.expiresAtUtcEpochSeconds);
            if (pj.has("iat")) nb.issuedAtUtcEpochSeconds  = pj.optLong("iat", nb.issuedAtUtcEpochSeconds);
        } catch (Exception ignored) {}
    }
}