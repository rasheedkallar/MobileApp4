package com.example.myapplication.Data;



import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TokenRefreshWorker extends Worker {

    public TokenRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        ConnectionRepository repo = new ConnectionRepository(getApplicationContext());
        TokenManager tm = new TokenManager(getApplicationContext());

        List<ConnectionRepository.MobileConnection> conns = repo.getSavedConnections();
        // Include active even if not in list
        String active = repo.getActiveBaseUrl();
        if (active != null) {
            tm.getValidTokenOrRefresh(active);
        }
        for (ConnectionRepository.MobileConnection mc : conns) {
            String base = ConnectionRepository.normalizeBase(mc.url);
            tm.getValidTokenOrRefresh(base);
        }
        return Result.success();
    }

    public static void schedule(Context ctx) {
        PeriodicWorkRequest req =
                new PeriodicWorkRequest.Builder(TokenRefreshWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork("TokenRefreshWork",
                        ExistingPeriodicWorkPolicy.UPDATE, req);
    }
}
