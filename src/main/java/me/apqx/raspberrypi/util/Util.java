package me.apqx.raspberrypi.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by chang on 2016/8/27.
 */
public class Util {
    //判断时间是否大于指定的时间
    public static boolean isOverTime(int time1,int time2,int time){
        if (time2>=time1&&time2<60){
            if ((time2-time1)<time){
                return false;
            }else {
                return true;
            }
        }else {
            if ((time2+60-time1)<time){
                return false;
            }else {
                return true;
            }
        }
    }
    //获取Android IP地址
    public static String getHostAddress(Context context){
        //获取wifi服务
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }
    private static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
    //获取两点间的距离
    public static double getDistance(int startX,int startY,int endX,int endY){
        return Math.hypot(endX-startX,endY-startY);
    }
}
