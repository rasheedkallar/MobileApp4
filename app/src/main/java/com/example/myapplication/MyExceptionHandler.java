package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;

    public MyExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        System.out.println("Unhandled Exception in thread: " + t.getName());
        System.out.println("Error message: " + e.getMessage());
        e.printStackTrace(); // This will print the full stack trace to the console


        // Show popup (AlertDialog)
        new Handler(Looper.getMainLooper()).post(() -> {

            // Print error details to console



            new AlertDialog.Builder(context)
                    .setTitle("Unhandled Exception")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Optionally restart app or close
                        System.exit(1);
                    })
                    .show();
        });
    }
}
