package com.example.xyzreader;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created on 11/08/2015.
 */
public class XYZApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
    }
}
