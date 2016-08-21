package me.apqx.raspberrypi;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
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

import me.apqx.raspberrypi.View.ControllerView;
import me.apqx.raspberrypi.View.OnControllerListener;

/**
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
                imageView.setBackgroundColor(getResources().getColor(R.color.disConnect));
                close();
                break;
            case R.id.shutdown:
                sendText(RaspberryAction.SHUTDOWN);
                imageView.setBackgroundColor(getResources().getColor(R.color.disConnect));
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
        if (socket!=null){
            try {
                //延时0.1秒，防止命令未发送完成就关闭了连接
                Thread.currentThread().sleep(100);
                printStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
}
