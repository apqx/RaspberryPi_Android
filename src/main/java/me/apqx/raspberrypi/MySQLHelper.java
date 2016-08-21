package me.apqx.raspberrypi;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chang on 2016/7/30.
 */
public class MySQLHelper extends SQLiteOpenHelper {
    private static final String CREATE_BOOK="create table raspberry(" +
            "id integer primary key autoincrement," +
            "ip1 integer," +
            "ip2 integer," +
            "ip3 integer," +
            "ip4 integer)";
    public MySQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
        ContentValues contentValues=new ContentValues();
        contentValues.put("ip1",192);
        contentValues.put("ip2",168);
        contentValues.put("ip3",0);
        contentValues.put("ip4",1);
        db.insert("raspberry",null,contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
