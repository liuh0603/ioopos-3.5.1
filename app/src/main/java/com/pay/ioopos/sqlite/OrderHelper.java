package com.pay.ioopos.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 离线订单数据库
 * @author    Moyq5
 * @since  2020/5/12 18:38
 */
public class OrderHelper extends SQLiteOpenHelper {

    public OrderHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[] {"pay_order"});
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " +
                    "pay_order(" +
                    "order_id integer primary key autoincrement, " +
                    "order_no varchar(50)," +
                    "pay_type int(1)," +
                    "pay_method int(1)," +
                    "amount varchar(20)," +
                    "balance varchar(20)," +
                    "pay_code text," +
                    "pay_status int(1)," +
                    "pay_descr varchar(200)," +
                    "add_time bigint," +
                    "query_time bigint," +
                    "query_times int(2)," +
                    "out_user_id varchar(200)," +
                    "user_id varchar(200)," +
                    "user_name varchar(200)," +
                    "dev_type int(1) default 0," +
                    "dev_sn varchar(100)" +
                    ")");
        } else {
            c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=? and sql like '%balance%'", new String[] {"pay_order"});
            if (!c.moveToNext() || c.getInt(0) == 0) {
                db.execSQL("alter table pay_order add column balance varchar(20)");
            }
            c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=? and sql like '%dev_type%'", new String[] {"pay_order"});
            if (!c.moveToNext() || c.getInt(0) == 0) {
                db.execSQL("alter table pay_order add column dev_type int(1) default 0");
            }
            c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=? and sql like '%dev_sn%'", new String[] {"pay_order"});
            if (!c.moveToNext() || c.getInt(0) == 0) {
                db.execSQL("alter table pay_order add column dev_sn varchar(100)");
            }
        }
    }
}
