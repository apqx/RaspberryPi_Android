package me.apqx.raspberrypi;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by chang on 2016/7/30.
 */
public class IPSQLite {
    private SQLiteDatabase db;
    public IPSQLite(){
        MySQLHelper mySQLHelper=new MySQLHelper(MyApplication.getContext(),"raspberry.db",null,1);
        db=mySQLHelper.getWritableDatabase();
    }
    public int[] getIP(){
        Cursor cursor=db.query("raspberry",null,null,null,null,null,null);
        cursor.moveToFirst();
        int ip1=cursor.getInt(cursor.getColumnIndex("ip1"));
        int ip2=cursor.getInt(cursor.getColumnIndex("ip2"));
        int ip3=cursor.getInt(cursor.getColumnIndex("ip3"));
        int ip4=cursor.getInt(cursor.getColumnIndex("ip4"));
        cursor.close();
        return new int[]{ip1,ip2,ip3,ip4};
    }
    public void saveIP(int[] ip){
        ContentValues contentValues=new ContentValues();
        contentValues.put("ip1",ip[0]);
        contentValues.put("ip2",ip[1]);
        contentValues.put("ip3",ip[2]);
        contentValues.put("ip4",ip[3]);
        db.update("raspberry",contentValues,"id=?",new String[]{"1"});
    }
    public void close(){
        if (db!=null){
            db.close();
        }
    }
}
