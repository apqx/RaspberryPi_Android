package me.apqx.raspberrypi.view;

/**
 * Created by chang on 2016/8/24.
 */
public interface MyWebViewListener {
    void start();
    void stop();
    void takePicture();
    void refresh();
    void move(int offsetX,int offsetY);
}
