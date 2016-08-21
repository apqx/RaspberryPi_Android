package me.apqx.raspberrypi.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chang on 2016/8/19.
 */
public class ControllerView extends View {
    private Paint up=new Paint();
    private Paint down=new Paint();
    private Paint left=new Paint();
    private Paint right=new Paint();
    private Paint backGround=new Paint();
    private Paint centerPoint=new Paint();
    //中心控制圆圆心坐标偏移量
    private int offsetX;
    private int offsetY;
    private int lastX;
    private int lastY;
    private int currentX;
    private int currentY;
    //中心控制点圆心坐标
    private int x;
    private int y;
    //View中心绝对坐标
    private int centerX;
    private int centerY;
    //中心控制圆半径
    private int centerRadius;
    //中心控制圆圆心距原点的最大距离
    private int centerLength;
    private boolean isInit=true;
    //监听器
    private OnControllerListener listener;
    //命令状态
    private int whichIsOn;
    private final int UP=1;
    private final int DOWN=2;
    private final int RIGHT=3;
    private final int LEFT=4;
    private final int STOP=5;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec),measure(heightMeasureSpec));
    }

    private int measure(int measureSpec){
        int result=0;
        int specMod=MeasureSpec.getMode(measureSpec);
        int specSize=MeasureSpec.getSize(measureSpec);
        if (specMod==MeasureSpec.EXACTLY){
            result=specSize;
        }else {
            result=400;
            if (specMod==MeasureSpec.AT_MOST){
                result=Math.min(result,specSize);
            }
        }
        return result;
    }

    public ControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width=getMeasuredWidth();
        int height=getMeasuredHeight();
        centerX=width/2;
        centerY=height/2;
        //填充View的最大圆半径
        int radius=Math.min(width,height)/2;
        //斜矩形边长
        int length=(int)(radius/Math.sin(Math.PI/4));
        //箭头顶点距坐标轴距离
        int arrowPoint=length*3/8;
        //箭头单边长度
        int arrowLength=length/5;
        //箭头宽度
        int arrowWidth=length/14;
        //控制中心圆半径
        centerRadius=length/7;
        //中心控制圆圆心距原点的最大距离
        centerLength=length/6;
        //圆角矩形的圆角半径
        int roundRectRadius=length/10;


        backGround.setColor(Color.parseColor("#607d8b"));
        backGround.setStyle(Paint.Style.FILL);
        backGround.setAntiAlias(true);

        canvas.translate(width/2,height/2);
        canvas.save();
        canvas.rotate(45);
        //画斜圆角矩形
        canvas.drawRoundRect(-length/2,-length/2,length/2,length/2,roundRectRadius,roundRectRadius,backGround);

        //画四个指示方向的箭头
        Path arrow=new Path();
        if (whichIsOn==0||whichIsOn==STOP){
        up.setColor(Color.parseColor("#f5f5f5"));
        right.setColor(Color.parseColor("#f5f5f5"));
        down.setColor(Color.parseColor("#f5f5f5"));
        left.setColor(Color.parseColor("#f5f5f5"));
        }
        //上
        up.setStyle(Paint.Style.STROKE);
        up.setStrokeWidth(arrowWidth);
        arrow.moveTo(-arrowPoint,-arrowPoint+arrowLength);
        arrow.lineTo(-arrowPoint,-arrowPoint);
        arrow.lineTo(-arrowPoint+arrowLength,-arrowPoint);
        canvas.drawPath(arrow,up);
        //右
        arrow.reset();
        right.setStyle(Paint.Style.STROKE);
        right.setStrokeWidth(arrowWidth);
        arrow.moveTo(arrowPoint,-arrowPoint+arrowLength);
        arrow.lineTo(arrowPoint,-arrowPoint);
        arrow.lineTo(arrowPoint-arrowLength,-arrowPoint);
        canvas.drawPath(arrow,right);
        //下
        arrow.reset();
        down.setStyle(Paint.Style.STROKE);
        down.setStrokeWidth(arrowWidth);
        arrow.moveTo(arrowPoint,arrowPoint-arrowLength);
        arrow.lineTo(arrowPoint,arrowPoint);
        arrow.lineTo(arrowPoint-arrowLength,arrowPoint);
        canvas.drawPath(arrow,down);
        //左
        arrow.reset();
        left.setStyle(Paint.Style.STROKE);
        left.setStrokeWidth(arrowWidth);
        arrow.moveTo(-arrowPoint,arrowPoint-arrowLength);
        arrow.lineTo(-arrowPoint,arrowPoint);
        arrow.lineTo(-arrowPoint+arrowLength,arrowPoint);
        canvas.drawPath(arrow,left);
        arrow.close();
        canvas.restore();

        //画控制圆中心的运动边界圆
        Paint line=new Paint();
        line.setStyle(Paint.Style.STROKE);
        line.setColor(Color.WHITE);
        line.setAntiAlias(true);
        line.setStrokeWidth(10);
        canvas.drawCircle(0,0,centerLength,line);

        //画中心控制圆
        centerPoint.setAntiAlias(true);
        centerPoint.setColor(Color.parseColor("#78909c"));
        centerPoint.setStyle(Paint.Style.FILL);
        if (isInit){
            canvas.drawCircle(x,y,centerRadius,centerPoint);
        }else {
            canvas.drawCircle(x-width/2,y-height/2,centerRadius,centerPoint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX=(int)event.getX();
                lastY=(int)event.getY();
                if (getDistance(getMeasuredWidth()/2,getMeasuredHeight()/2,lastX,lastY)<=centerLength){
                    //如果点击在圈内，中心控制圆应该移动到点击位置
                    x=lastX;
                    y=lastY;
                    invalidate();
                }else {
                    //如果点击在圈外，控制圆移动到与圈接触的相应位置，并触发在相应区域内的点击操作，考虑按键冲突
                    setPointOnCircle(centerX,centerY,lastX,lastY);
                    invalidate();
                }
                isInit=false;
                break;
            case MotionEvent.ACTION_MOVE:
                currentX=(int)event.getX();
                currentY=(int)event.getY();
                if (getDistance(centerX,centerY,currentX,currentY)<=centerLength){
                    //如果移动在圈内，控制圆应随手指移动
                    x=currentX;
                    y=currentY;
                    invalidate();
                    if (listener!=null&&whichIsOn!=STOP){
                        listener.stop();
                        whichIsOn=STOP;
                    }
                }else {
                    //如果移动在圈外，控制圆应随手指移动，但是不能出圈
                    setPointOnCircle(centerX,centerY,currentX,currentY);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                x=centerX;
                y=centerY;
                invalidate();
                if (listener!=null){
                    listener.stop();
                    whichIsOn=STOP;
                }
                whichIsOn=0;
                break;
        }
        return true;
    }
    //获取两点间的距离
    private double getDistance(int startX,int startY,int endX,int endY){
        return Math.hypot(endX-startX,endY-startY);
    }
    //获取点与圆心连线与横轴的夹角
    private double getAngle(double centerX,double centerY,double startX,double startY){
        return Math.atan((startY-centerY)/(startX-centerX));
    }
    //获取并设置与圈外点相对应的控制圆边界上的点坐标,判断点击的行为
    private void setPointOnCircle(int centerX,int centerY,int currentX,int currentY){
        double tempLength=centerRadius*Math.sqrt(2)/2;
        double angle=getAngle(centerX,centerY,currentX,currentY);
        if (currentX>=centerX){
            x=centerX+(int)(Math.cos(angle)*centerLength);
            y=centerY+(int)(Math.sin(angle)*centerLength);
        }else {
            x=centerX-(int)(Math.cos(angle)*centerLength);
            y=centerY-(int)(Math.sin(angle)*centerLength);
        }
        //判断点击行为
        if (listener!=null){
            if (x>=centerX){
                if (y>=centerY){
                    if (x>=(centerX+tempLength)){
                        if (whichIsOn!=RIGHT){
                            listener.right();
                            whichIsOn=RIGHT;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#2ecc71"));
                        }
                    }else {
                        if (whichIsOn!=DOWN){
                            listener.down();
                            whichIsOn=DOWN;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#2ecc71"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }
                }else {
                    if (x>=(centerX+tempLength)){
                        if (whichIsOn!=RIGHT){
                            listener.right();
                            whichIsOn=RIGHT;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#2ecc71"));
                        }
                    }else {
                        if (whichIsOn!=UP){
                            listener.up();
                            whichIsOn=UP;
                            up.setColor(Color.parseColor("#2ecc71"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }
                }
            }else {
                if (y>=centerY){
                    if (x>=(centerX-tempLength)){
                        if (whichIsOn!=DOWN){
                            listener.down();
                            whichIsOn=DOWN;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#2ecc71"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }else {
                        if (whichIsOn!=LEFT){
                            listener.left();
                            whichIsOn=LEFT;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#2ecc71"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }
                }else {
                    if (x>=(centerX-tempLength)){
                        if (whichIsOn!=UP){
                            listener.up();
                            whichIsOn=UP;
                            up.setColor(Color.parseColor("#2ecc71"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#f5f5f5"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }else {
                        if (whichIsOn!=LEFT){
                            listener.left();
                            whichIsOn=LEFT;
                            up.setColor(Color.parseColor("#f5f5f5"));
                            down.setColor(Color.parseColor("#f5f5f5"));
                            left.setColor(Color.parseColor("#2ecc71"));
                            right.setColor(Color.parseColor("#f5f5f5"));
                        }
                    }
                }
            }
        }
    }
    //暴露监听器
    public void setOnControllerListener(OnControllerListener listener){
        this.listener=listener;
    }

}
