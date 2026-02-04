package com.example.myapplication.Data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.google.gson.Gson;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecureStore {
    private static final String PREF_NAME = "secure_prefs";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public SecureStore(Context ctx) {
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    ctx,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to init SecureStore", e);
        }
    }

    public void putString(String key, String val) {
        prefs.edit().putString(key, val).apply();
    }
    public String getString(String key, String def) { return prefs.getString(key, def); }

    public <T> void putJson(String key, T obj) { putString(key, gson.toJson(obj)); }
    public <T> T getJson(String key, Class<T> cls) {
        String s = getString(key, null);
        return s == null ? null : gson.fromJson(s, cls);
    }
}
