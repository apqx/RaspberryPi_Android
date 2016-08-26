package me.apqx.raspberrypi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chang on 2016/8/26.
 */
public class ServoView extends View {
    private int width;
    private int height;
    private Paint line;
    private Paint circle;
    //控制圆的坐标
    private int x;
    private int y;
    //控制圆的半径
    private int circleRadius;
    //线宽
    private int strokeWidth;
    //线半长
    private int lineLength;
    private ServoViewListener listener;
    private boolean upIsOn;
    private boolean downIsOn;
    private boolean motion;

    public ServoView(Context context) {
        super(context);
    }

    public ServoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ServoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ServoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measurewidth(widthMeasureSpec),measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int measureSpec){
        int result;
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
    private int measurewidth(int measureSpec){
        int result;
        int specMod=MeasureSpec.getMode(measureSpec);
        int specSize=MeasureSpec.getSize(measureSpec);
        if (specMod==MeasureSpec.EXACTLY){
            result=specSize;
        }else {
            result=100;
            if (specMod==MeasureSpec.AT_MOST){
                result=Math.min(result,specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width=getMeasuredWidth();
        height=getMeasuredHeight();
        strokeWidth=20;
        if (!motion){
            y=height/2;
        }
        x=width/2;
        circleRadius=strokeWidth*2;
        lineLength=height/2*3/4;
        canvas.save();
        canvas.translate(width/2,height/2);

        //划线
        line=new Paint();
        line.setStyle(Paint.Style.FILL);
        line.setColor(Color.parseColor("#2ecc71"));
        line.setStrokeWidth(strokeWidth);
        canvas.drawLine(0,lineLength,0,-lineLength,line);
        canvas.drawCircle(0,-lineLength,strokeWidth/2,line);
        canvas.drawCircle(0,lineLength,strokeWidth/2,line);

        //画控制圆
        canvas.restore();
        circle=new Paint();
        circle.setStyle(Paint.Style.FILL);
        circle.setColor(Color.parseColor("#78909c"));
        circle.setAlpha(60);
        canvas.drawCircle(x,y,circleRadius,circle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                y=(int)event.getY();
                if (y>height/2-lineLength&&y<height/2+lineLength){
                    motion=true;
                    invalidate();
                    if (y>height/2){
                        if (listener!=null){
                            listener.down();
                        }
                        downIsOn=true;
//                        Log.d("apqx","down");
                    }else {
                        if (listener!=null){
                            listener.up();
                        }
                        upIsOn=true;
//                        Log.d("apqx","up");
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                y=(int)event.getY();
                if (y>height/2-lineLength&&y<height/2+lineLength){
                    invalidate();
                    if (y>height/2&&!downIsOn){
                        downIsOn=true;
                        upIsOn=false;
                        if (listener!=null){
                            listener.down();
                        }
//                        Log.d("apqx","down");
                    }else if (y<height/2&&!upIsOn){
                        upIsOn=true;
                        downIsOn=false;
                        if (listener!=null){
                            listener.up();
                        }
//                        Log.d("apqx","up");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                upIsOn=false;
                downIsOn=false;
                motion=false;
                invalidate();
                if (listener!=null){
                    listener.stop();
                }
//                Log.d("apqx","stop");
                break;
        }
        return true;
    }
    //对外暴露监听器方法
    public void setServoViewListener(ServoViewListener listener){
        this.listener=listener;
    }
}
