package me.apqx.raspberrypi;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by chang on 2016/6/30.
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private Button connect;
    private Button disConnect;
    private Button shutdown;
    private EditText ip1;
    private EditText ip2;
    private EditText ip3;
    private EditText ip4;
    private ImageView imageView;
    private Button forward;
    private Button back;
    private Button right;
    private Button left;
    private PrintStream printStream;
    private Socket socket;
    private boolean upPressed;
    private boolean downPressed;
    private boolean rightPressed;
    private boolean leftPressed;
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
        ip1=(EditText)findViewById(R.id.ip_1);
        ip2=(EditText)findViewById(R.id.ip_2);
        ip3=(EditText)findViewById(R.id.ip_3);
        ip4=(EditText)findViewById(R.id.ip_4);
        imageView=(ImageView)findViewById(R.id.imageView);
        forward=(Button)findViewById(R.id.forward);
        back=(Button)findViewById(R.id.back);
        right=(Button)findViewById(R.id.turnRight);
        left=(Button)findViewById(R.id.turnLeft);
    }
    private void setListener(){
        connect.setOnClickListener(this);
        disConnect.setOnClickListener(this);
        shutdown.setOnClickListener(this);
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked()==MotionEvent.ACTION_DOWN){
                    upPressed=true;
                    sendText(RaspberryAction.FORWARD);
                }else if (event.getActionMasked()==MotionEvent.ACTION_UP){
                    upPressed=false;
                    sendText(RaspberryAction.STOP);
                    checkAndDo();
                }
                return false;
            }
        });
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked()==MotionEvent.ACTION_DOWN){
                    downPressed=true;
                    sendText(RaspberryAction.BACK);
                }else if (event.getActionMasked()==MotionEvent.ACTION_UP){
                    downPressed=false;
                    sendText(RaspberryAction.STOP);
                    checkAndDo();
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked()==MotionEvent.ACTION_DOWN){
                    leftPressed=true;
                    sendText(RaspberryAction.TURN_LEFT);
                }else if (event.getActionMasked()==MotionEvent.ACTION_UP){
                    leftPressed=false;
                    sendText(RaspberryAction.STOP);
                    checkAndDo();
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked()==MotionEvent.ACTION_DOWN){
                    rightPressed=true;
                    sendText(RaspberryAction.TURN_RIGHT);
                }else if (event.getActionMasked()==MotionEvent.ACTION_UP){
                    rightPressed=false;
                    sendText(RaspberryAction.STOP);
                    checkAndDo();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect:
                startCommunicate();
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
    private void sendText(String string){
        if (socket!=null&&socket.isConnected()){
            printStream.println(string);
        }
    }
    private void checkAndDo(){
        if (upPressed){
            sendText(RaspberryAction.FORWARD);
        }else if (downPressed){
            sendText(RaspberryAction.BACK);
        }else if (rightPressed){
            sendText(RaspberryAction.TURN_RIGHT);
        }else if (leftPressed){
            sendText(RaspberryAction.TURN_LEFT);
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
    }
}
