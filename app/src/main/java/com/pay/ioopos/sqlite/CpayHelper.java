package com.pay.ioopos.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CpayHelper extends SQLiteOpenHelper {

    public CpayHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        init(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        init(db);
    }

    private void init(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[] { "setting" });
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table setting(name varchar(50) primary key, value varchar(500))");
        }

        c = db.rawQuery("select value from setting where name=?", new String[]{"serverUrl"});
        if (!c.moveToNext()) {
            db.execSQL("insert into setting (name, value) values('serverUrl', 'https://pay.qcloud.com')");
        }
    }
}
