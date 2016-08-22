package me.apqx.raspberrypi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import me.apqx.raspberrypi.view.ControllerView;

/**为树莓派添加体感控制功能，使用加速度传感器判断手机姿态
 * Created by chang on 2016/8/21.
 */
public class UseSensor {
    //加速度传感器初始化数据
    private float initX;
    private float initY;
    //加速度传感器的实时数据
    private float sensorX;
    private float sensorY;
    //加速度传感器相对变化量
    private float offsetX;
    private float offsetY;
    //传递给ControllerView的模拟坐标
    private int setX;
    private int setY;
    private boolean isFirst=true;
    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textView;
    private SensorEventListener listener;
    private ControllerView controllerView;
    public UseSensor(ControllerView controllerView){
        this.controllerView=controllerView;
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
            isFirst=true;
            controllerView.setCenter();
        }
    }
    class SensorListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] sensorData=event.values;
            if (isFirst){
                initX=sensorData[0];
                initY=sensorData[1];
                isFirst=false;
            }else {
                sensorX=sensorData[0];
                sensorY=sensorData[1];
                offsetX=sensorX-initX;
                offsetY=sensorY-initY;
                controllerView.setPoint((int)(controllerView.getCenterX()+offsetX*10),(int)(controllerView.getCenterY()-offsetY*10));
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
