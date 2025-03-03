package com.pay.ioopos.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 支付宝云支付相关配置数据库
 * @author    Moyq5
 * @since  2020/12/11 15:34
 */
public class ApayHelper extends SQLiteOpenHelper {
    public static final String TB_SETTING = "tb_setting";
    public static final String SUPPLIER_ID = "ioopos20201211";
    public ApayHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[] { TB_SETTING });
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " + TB_SETTING + "(name varchar(50) primary key, value varchar(2000))");
        }

        c = db.rawQuery("select value from " + TB_SETTING + " where name=?", new String[]{"serverUrl"});
        if (!c.moveToNext()) {
            db.execSQL("insert into " + TB_SETTING + " (name, value) values('serverUrl', 'https://ecogateway.alipay-eco.com/gateway.do')");
        }
    }
}
