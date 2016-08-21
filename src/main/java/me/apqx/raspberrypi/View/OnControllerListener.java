package me.apqx.raspberrypi.View;

/**
 * Created by chang on 2016/8/21.
 * 监听ControllerView触摸事件的接口
 */
public interface OnControllerListener {
    void up();
    void down();
    void right();
    void left();
    void stop();
}
