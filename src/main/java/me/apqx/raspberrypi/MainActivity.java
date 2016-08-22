package me.apqx.raspberrypi;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import me.apqx.raspberrypi.view.ControllerView;
import me.apqx.raspberrypi.view.OnControllerListener;

/**树莓派控制主类
 * Created by chang on 2016/6/30.
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private ControllerView controllerView;
    protected OnControllerListener controllerListener;
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
        controllerView=(ControllerView)findViewById(R.id.controllerView);
        toggleSensor=(ToggleButton)findViewById(R.id.toggleSensor);
        sensorData=(TextView)findViewById(R.id.sensorData);
        controllerListener=new ControllerListener();
        useSensor=new UseSensor(controllerView);
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
        controllerView.setOnControllerListener(controllerListener);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setBackgroundColor(getResources().getColor(R.color.connect));
                            Toast.makeText(MainActivity.this,"Connected Successfull",Toast.LENGTH_SHORT).show();
                        }
                    });
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
        }).start();
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
        if (socket!=null){
            try {
                //延时0.1秒，防止命令未发送完成就关闭了连接
                Thread.currentThread().sleep(100);
                printStream.close();
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
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ipsqLite.close();
    }
    class ControllerListener implements OnControllerListener{
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
                controllerView.setUp();
                upIsOn=true;
                break;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //左摇杆下
                controllerView.setDown();
                downIsOn=true;
                break;
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //左摇杆右
                controllerView.setRight();
                rightIsOn=true;
                break;
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //左摇杆左
                controllerView.setLeft();
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

        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        controllerView.setCenter();
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
            controllerView.setUp();
        }
        if (downIsOn){
            controllerView.setDown();
        }
        if (rightIsOn){
            controllerView.setRight();
        }
        if (leftIsOn){
            controllerView.setLeft();
        }
    }
}
