package me.apqx.raspberrypi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

import java.util.Calendar;


/**
 * 通过此WebView来接收mjpg-streamer发送的数据
 * Created by chang on 2016/8/24.
 */
public class MyWebView extends WebView {
    private Paint foreground;
    private Paint pic;
    private int height;
    private int width;
    private int radius;
    private int tempR;
    private int downTime;
    private int upTime;
    private boolean down;
    //判断是否开始
    private boolean start;
    //判断是否停止
    private boolean stop;
    private boolean motion;
    //判断是否正在绘制
    private boolean drawing;
    //点击坐标
    private int x;
    private int y;
    //滑动坐标
    private int currentX;
    private int currentY;
    //拍照按钮坐标
    private int picX;
    private int picY;
    //拍照按钮半径
    private int picRadius;
    //拍照按钮触控区域半径
    private int picTouchRadius;
    //监听器
    private MyWebViewListener listener;
    public MyWebView(Context context) {
        super(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width=getMeasuredWidth();
        height=getMeasuredHeight();
        picRadius=height/10;
        picTouchRadius=4*picRadius;
        picX=width/2;
        picY=height-picRadius;
        radius=(int)Math.hypot(width,height);
        foreground=new Paint();
        foreground.setStyle(Paint.Style.FILL);
        foreground.setColor(Color.parseColor("#2ecc71"));
        if (tempR<radius){
            //如果没有开始，绘制绿色前景
            canvas.drawRect(0,0,width,height,foreground);
        }
        foreground.setColor(Color.WHITE);
        //检测到点击，开始动画，动画播放完成，前景也消失
        if (start){
            if (tempR<radius){
                tempR=tempR+30;
                canvas.drawCircle(x,y,tempR,foreground);
                invalidate();
            }else {
                start=false;
                drawing=false;
                invalidate();
            }
        }
        if (stop){
            if (tempR>0){
                tempR=tempR-30;
                canvas.drawCircle(x,y,tempR,foreground);
                invalidate();
            }else {
                stop=false;
                drawing=false;
                invalidate();
            }
        }
        //绘制拍照按钮
        pic=new Paint();
        pic.setStyle(Paint.Style.FILL);
        pic.setColor(Color.WHITE);
        pic.setAlpha(60);
        canvas.drawCircle(picX,picY,picRadius,pic);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                down=true;
                //防止动画执行过程被干扰
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!drawing){
                    x=(int)event.getX();
                    y=(int)event.getY();
                    if(!motion){
                        //开始
                        start=true;
                        motion=true;
                        drawing=true;
                        tempR=0;
                        invalidate();
                        if (listener!=null){
                            listener.start();
                        }
//                        Log.d("apqx","start");
                    }else {
                        //结束
                        if (getDistance(x,y,picX,picY)<picTouchRadius){
                            //点击圆内区域为刷新
                            if (listener!=null){
                                listener.refresh();
                            }
//                            Log.d("apqx","refresh");
                            //纪录点击园内的时间，判断是否是长按
                            downTime=Calendar.getInstance().get(Calendar.SECOND);
                            //在手指没有抬起的时候，判断按下是否超过了1秒
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (down){
                                        upTime=Calendar.getInstance().get(Calendar.SECOND);
                                            if (isOverTime(downTime,upTime)){
                                                if (listener!=null){
                                                    listener.takePicture();
                                                }
//                                                Log.d("apqx","picture");

                                                //对于拍照的处理流程有较多问题
                                                //这里由于逻辑问题，picture执行前refresh一定会执行
                                                break;
                                            }

                                    }
                                }
                            },"Thread-isLongPress").start();

                        }else {
                            //点击园外区域为停止
                            if (listener!=null){
                                listener.stop();
                            }
                            stop=true;
                            motion=false;
                            drawing=true;
                            tempR=radius-10;
                            invalidate();
//                            Log.d("apqx","stop");
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                down=false;
                break;
        }
//        Log.d("apqx","x="+x);
//        Log.d("apqx","y="+y);
        return true;
    }
    //获取两点间的距离
    private double getDistance(int startX,int startY,int endX,int endY){
        return Math.hypot(endX-startX,endY-startY);
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
    //对外暴露监听器方法
    public void setOnMyWebViewListener(MyWebViewListener listener){
        this.listener=listener;
    }
}
