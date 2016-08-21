package me.apqx.raspberrypi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import me.apqx.raspberrypi.View.OnControllerListener;

/**
 * Created by chang on 2016/8/21.
 */
public class UseSensor {
    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textView;
    private SensorEventListener listener;
    private OnControllerListener onControllerListener;
    public UseSensor(OnControllerListener onControllerListener){
        this.onControllerListener=onControllerListener;
    }
    public void start(final TextView textView){
        this.textView=textView;
        sensorManager=(SensorManager)MyApplication.getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
        listener=new SensorListener();
        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void stop(){
        if (sensorManager!=null){
            sensorManager.unregisterListener(listener,sensor);
            onControllerListener.stop();
        }
    }
    class SensorListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] sensorData=event.values;
            float x=sensorData[0];
            float y=sensorData[1];
            float z=sensorData[2];
            String text=null;
            if (onControllerListener!=null){
                if (x<-20){
                    onControllerListener.left();
                    text="left";
                }else if (x>25){
                    onControllerListener.right();
                    text="right";
                }else if (y<-5){
                    onControllerListener.down();
                    text="down";
                }else if (y>20){
                    onControllerListener.up();
                    text="up";
                }else {
                    onControllerListener.stop();
                    text="stop";
                }
                textView.setText("x="+x+"\ty="+y+"\tz="+z+"/nstate is "+text);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
