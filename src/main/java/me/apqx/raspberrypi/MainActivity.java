package me.apqx.raspberrypi;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Calendar;

import me.apqx.raspberrypi.view.ControllerView;
import me.apqx.raspberrypi.view.MyWebView;
import me.apqx.raspberrypi.view.MyWebViewListener;
import me.apqx.raspberrypi.view.OnControllerListener;
import me.apqx.raspberrypi.view.ServoView;
import me.apqx.raspberrypi.view.ServoViewListener;

/**树莓派控制主类
 * Created by chang on 2016/6/30.
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private ControllerView handControllerView;
    private OnControllerListener handControllerListener;
    private ServoView servoView;
    private ServoViewListener servoViewListener;
    private MyWebView webView;
    private WebViewListener webViewListener;
    private boolean check;
    private Thread checkThread;
    private ControllerView directionControllerView;
    protected OnControllerListener directionControllerListener;
    private UseSensor useSensor;
    private ToggleButton toggleSensor;
    protected TextView sensorData;
    private Button connect;
    private Button disConnect;
    private Button shutdown;
    private EditText ip1;
    private EditText ip2;
    private EditText ip3;
    private EditText ip4;
    private ImageView imageView;
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private Socket socket;
    private IPSQLite ipsqLite;
    private boolean upIsOn;
    private boolean downIsOn;
    private boolean leftIsOn;
    private boolean rightIsOn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        inite();
        setListener();
    }
    private void inite(){
        connect=(Button)findViewById(R.id.connect);
        disConnect=(Button)findViewById(R.id.disConnect);
        shutdown=(Button)findViewById(R.id.shutdown);
        ipsqLite=new IPSQLite();
        ip1=(EditText)findViewById(R.id.ip_1);
        ip2=(EditText)findViewById(R.id.ip_2);
        ip3=(EditText)findViewById(R.id.ip_3);
        ip4=(EditText)findViewById(R.id.ip_4);
        imageView=(ImageView)findViewById(R.id.imageView);
        directionControllerView=(ControllerView)findViewById(R.id.drectionControllerView);
        toggleSensor=(ToggleButton)findViewById(R.id.toggleSensor);
        sensorData=(TextView)findViewById(R.id.sensorData);
        directionControllerListener=new DirectionControllerListener();
        useSensor=new UseSensor(directionControllerView);
        webView=(MyWebView)findViewById(R.id.webView);
//        webSettings.setSupportZoom(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setUseWideViewPort(true);
        //缩放百分之四百,和输出视频尺寸相配合使之占满屏幕
        webView.setInitialScale(400);
        webViewListener=new WebViewListener();
        servoView=(ServoView)findViewById(R.id.servoView);
        servoViewListener=new ServoListener();
        handControllerView=(ControllerView)findViewById(R.id.handControllerView);
        handControllerListener=new HandControllerListener();
        int[] ip=ipsqLite.getIP();
        ip1.setText(ip[0]+"");
        ip2.setText(ip[1]+"");
        ip3.setText(ip[2]+"");
        ip4.setText(ip[3]+"");
    }
    private void setListener(){
        connect.setOnClickListener(this);
        disConnect.setOnClickListener(this);
        shutdown.setOnClickListener(this);
        directionControllerView.setOnControllerListener(directionControllerListener);
        webView.setOnMyWebViewListener(webViewListener);
        toggleSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    useSensor.start(sensorData);
                }else {
                    useSensor.stop();
                }
            }
        });
        servoView.setServoViewListener(servoViewListener);
        handControllerView.setOnControllerListener(handControllerListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect:
                startCommunicate();
                ipsqLite.saveIP(new int[]{Integer.parseInt(ip1.getText().toString()),Integer.parseInt(ip2.getText().toString()),Integer.parseInt(ip3.getText().toString()),Integer.parseInt(ip4.getText().toString())});
                break;
            case R.id.disConnect:
                sendText(RaspberryAction.EXIT);
                close();
                break;
            case R.id.shutdown:
                sendText(RaspberryAction.SHUTDOWN);
                close();
                break;
        }
    }
    private void startCommunicate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip=ip1.getText()+"."+ip2.getText()+"."+ip3.getText()+"."+ip4.getText();
                    socket=new Socket(InetAddress.getByName(ip),1335);
                    printStream=new PrintStream(socket.getOutputStream());
                    bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setBackgroundColor(getResources().getColor(R.color.connect));
                            Toast.makeText(MainActivity.this,"Connected Successfull",Toast.LENGTH_SHORT).show();
                        }
                    });
                    String string;
                    checkConnection();
                    while ((string=bufferedReader.readLine())!=null) {
                        switch (string) {
                            case RaspberryAction.CHECK:
                                sendText(RaspberryAction.CHECK_BACK);
                                break;
                            case RaspberryAction.CHECK_BACK:
                                check=true;
                                break;
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Connected Failed",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        },"Thread-startCommunicate").start();

    }
    protected void sendText(String string){
        if (socket!=null&&socket.isConnected()){
            printStream.println(string);
        }else {
            imageView.setBackgroundColor(Color.RED);
            //Toast.makeText(this,"No Connection",Toast.LENGTH_SHORT).show();
        }
    }

    private void close(){
        imageView.setBackgroundColor(getResources().getColor(R.color.disConnect));
        if (checkThread!=null){
            checkThread.interrupt();
        }
        if (socket!=null){
            try {
                //延时0.1秒，防止命令未发送完成就关闭了连接
                Thread.currentThread().sleep(100);
                printStream.close();
                bufferedReader.close();
                socket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket!=null){
            try {
                printStream.close();
                bufferedReader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ipsqLite.close();
    }
    //检查连接是否中断
    private void checkConnection(){
        new Thread(new Runnable() {
            private int time;
            private int currentTime;
            @Override
            public void run() {
                checkThread=Thread.currentThread();
                try {
                    while (true){
                        check=false;
                        sendText(RaspberryAction.CHECK);
                        time=Calendar.getInstance().get(Calendar.SECOND);
                        //Log.d("apqx","心跳");
                        while (!check){
                            currentTime=Calendar.getInstance().get(Calendar.SECOND);
                            if (isOverTime(time,currentTime)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        close();
                                    }
                                });
                                //Log.d("apqx","心跳线程退出");
                                return;
                            }
                        }
                        //Log.d("apqx","sleep");
                        Thread.currentThread().sleep(5000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //Log.d("apqx","checkThread异常");
                }
            }
        },"Thread-checkConnection").start();
    }
    //判断时间是否大于2秒
    private boolean isOverTime(int time1,int time2){
        if (time2>=time1&&time2<60){
            if ((time2-time1)<2){
                return false;
            }else {
                return true;
            }
        }else {
            if ((time2+60-time1)<2){
                return false;
            }else {
                return true;
            }
        }
    }
    class DirectionControllerListener implements OnControllerListener{
        @Override
        public void up() {
            sendText(RaspberryAction.FORWARD);
        }

        @Override
        public void down() {
            sendText(RaspberryAction.BACK);
        }

        @Override
        public void right() {
            sendText(RaspberryAction.TURN_RIGHT);
        }

        @Override
        public void left() {
            sendText(RaspberryAction.TURN_LEFT);
        }

        @Override
        public void stop() {
            sendText(RaspberryAction.STOP);
        }
    }
    class WebViewListener implements MyWebViewListener{
        @Override
        public void start() {
            //开始使用摄像头
            sendText(RaspberryAction.CAMERA_ON);
            webView.loadUrl("http://192.168.0.1:8080/?action=stream");
//            Log.d("apqx","start");
        }

        @Override
        public void stop() {
            //关闭摄像头
            sendText(RaspberryAction.CAMERA_OFF);
            webView.stopLoading();
//            Log.d("apqx","stop");
        }

        @Override
        public void takePicture() {
            //拍照
        }

        @Override
        public void refresh() {
            //刷新
            webView.reload();
//            Log.d("apqx","refresh");
        }
    }
    class HandControllerListener implements OnControllerListener{
        @Override
        public void up() {
            //向上
            sendText(RaspberryAction.SERVO_DS3218_CW);
//            Log.d("apqx","hand up");
        }

        @Override
        public void down() {
            //向下
            sendText(RaspberryAction.SERVO_DS3218_CCW);
//            Log.d("apqx","hand down");
        }

        @Override
        public void right() {
            //夹紧
            sendText(RaspberryAction.SERVO_MG995_HAND_CCW);
//            Log.d("apqx","hand tight");
        }

        @Override
        public void left() {
            //松开
            sendText(RaspberryAction.SERVO_MG995_HAND_CW);
//            Log.d("apqx","hand unTight");
        }

        @Override
        public void stop() {
            //停止
            sendText(RaspberryAction.SERVO_DS3218_STOP);
            sendText(RaspberryAction.SERVO_MG995_HAND_STOP);
//            Log.d("apqx","hand stop");
        }
    }
    class ServoListener implements ServoViewListener{
        @Override
        public void up() {
            sendText(RaspberryAction.SERVO_MG995_CAMERA_CW);
//            Log.d("apqx","camera up");
        }

        @Override
        public void down() {
            sendText(RaspberryAction.SERVO_MG995_CAMERA_CCW);
//            Log.d("apqx","camera down");
        }

        @Override
        public void stop() {
            sendText(RaspberryAction.SERVO_MG995_CAMERA_STOP);
//            Log.d("apqx","camera stop");
        }
    }

    //支持手柄按键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //北通手柄||蓝牙键盘
        switch (keyCode){
            case KeyEvent.KEYCODE_GRAVE:
            case KeyEvent.KEYCODE_BACK:
                //返回键
                this.finish();
                break;
            case KeyEvent.KEYCODE_Q:
            case KeyEvent.KEYCODE_BUTTON_START:
                //开始
                startCommunicate();
                ipsqLite.saveIP(new int[]{Integer.parseInt(ip1.getText().toString()),Integer.parseInt(ip2.getText().toString()),Integer.parseInt(ip3.getText().toString()),Integer.parseInt(ip4.getText().toString())});
                break;
            case KeyEvent.KEYCODE_E:
                close();
                break;
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_DPAD_UP:
                //左摇杆上
                directionControllerView.setUp();
                upIsOn=true;
                break;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //左摇杆下
                directionControllerView.setDown();
                downIsOn=true;
                break;
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //左摇杆右
                directionControllerView.setRight();
                rightIsOn=true;
                break;
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //左摇杆左
                directionControllerView.setLeft();
                leftIsOn=true;
                break;
            case KeyEvent.KEYCODE_K:
            case KeyEvent. KEYCODE_BUTTON_A:
                //A
                break;
            case KeyEvent.KEYCODE_L:
            case KeyEvent.KEYCODE_BUTTON_B:
                //B
                break;
            case KeyEvent.KEYCODE_I:
            case KeyEvent.KEYCODE_BUTTON_Y:
                //Y
                break;
            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_BUTTON_X:
                //X
                break;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                //左摇杆按下
                break;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                //右摇杆按下
                break;
            case KeyEvent.KEYCODE_BUTTON_L1:
                //LB
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                //RB
                break;
            default:break;
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        directionControllerView.setCenter();
        switch (keyCode) {
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_DPAD_UP:
                //左摇杆上
                upIsOn=false;
                checkAndDo();
                break;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //左摇杆下
                downIsOn=false;
                checkAndDo();
                break;
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //左摇杆右
                rightIsOn=false;
                checkAndDo();
                break;
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //左摇杆左
                leftIsOn=false;
                checkAndDo();
                break;
        }
        return true;
    }
    //解决按键冲突
    private void checkAndDo(){
        if (upIsOn){
            directionControllerView.setUp();
        }
        if (downIsOn){
            directionControllerView.setDown();
        }
        if (rightIsOn){
            directionControllerView.setRight();
        }
        if (leftIsOn){
            directionControllerView.setLeft();
        }
    }
}
