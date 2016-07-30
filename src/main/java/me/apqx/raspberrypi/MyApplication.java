package me.apqx.raspberrypi;

import android.app.Application;
import android.content.Context;

/**
 * Created by chang on 2016/7/30.
 */
public class MyApplication extends Application {
    private static Context context;
    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
