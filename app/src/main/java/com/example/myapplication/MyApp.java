package com.example.myapplication;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Register your global exception handler here
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
    }
}
