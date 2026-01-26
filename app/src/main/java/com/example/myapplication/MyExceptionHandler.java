package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.PopupHtml;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;

    public MyExceptionHandler(Context context) {
        this.context =  context;
    }



    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        // Log error details
        System.out.println("Unhandled Exception in thread: " + t.getName());
        System.out.println("Error message: " + e.getMessage());
        e.printStackTrace();

        // Show popup on main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;

                    // Try PopupHtml first
                    try {
                        PopupHtml.create("Error", e.getMessage())
                                .show(activity.getSupportFragmentManager(), null);
                    } catch (Exception popupEx) {
                        System.out.println("PopupHtml failed: " + popupEx.getMessage());

                        // Fallback to AlertDialog
                        new AlertDialog.Builder(activity)
                                .setTitle("Unhandled Exception")
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null) // No exit
                                .show();
                    }
                } else {
                    System.out.println("Context is not AppCompatActivity. Cannot show popup.");
                }
            } catch (Exception ex) {
                System.out.println("Error showing popup: " + ex.getMessage());
            }
        });

        // Do NOT exit the app
    }


}
