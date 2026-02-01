package com.example.myapplication.Data;

// AppInitializer.java

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AppInitializer {
    public static void ensureConnectionSelected(Context ctx, String discoveryBase) {
        // discoveryBase = a known endpoint to get the connection list.
        // Example: http://10.207.176.109 (NO /api)
        ConnectionRepository repo = new ConnectionRepository(ctx);

        String active = repo.getActiveBaseUrl();
        if (!TextUtils.isEmpty(active) && repo.ping(active, 2000)) {
            return; // already valid
        }

        // fetch from discovery endpoint: GET {discoveryBase}/api/connections/getmobileconnections
        String url = ConnectionRepository.normalizeBase(discoveryBase) + "/api/connections/getmobileconnections";
        AsyncHttpClient client = new AsyncHttpClient();
        final List<ConnectionRepository.MobileConnection> conns = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String s = new String(responseBody);
                    Type t = new TypeToken<List<ConnectionRepository.MobileConnection>>(){}.getType();
                    List<ConnectionRepository.MobileConnection> list = new Gson().fromJson(s, t);
                    conns.addAll(list);
                } catch (Exception ignored) {}
                latch.countDown();
            }
            @Override public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                latch.countDown();
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {}

        // Normalize and probe
        String selected = null;
        for (ConnectionRepository.MobileConnection mc : conns) {
            String base = ConnectionRepository.normalizeBase(mc.url);
            if (repo.ping(base, 1500)) {
                selected = base;
                break;
            }
        }

        if (selected == null) {
            // fallback to discoveryBase
            if (repo.ping(discoveryBase, 2000)) selected = ConnectionRepository.normalizeBase(discoveryBase);
        }

        if (selected != null) {
            repo.saveConnections(conns);
            repo.setActiveBaseUrl(selected);
        }
    }
}
