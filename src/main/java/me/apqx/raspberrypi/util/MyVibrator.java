package me.apqx.raspberrypi.util;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by chang on 2016/8/28.
 */
public class MyVibrator {
    private Vibrator vibrator;
    public MyVibrator(Context context){
        vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    public void doVibrator(){
        vibrator.vibrate(500);
    }
}
