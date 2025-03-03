package com.pay.ioopos.sqlite;

import static com.pay.ioopos.common.Constants.NET_STATE_DEFAULT;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SettingHelper extends SQLiteOpenHelper {
    public static final String INIT_KEY = "0000000000000000000000000000000";
    private static final String DEF_CARD_ADMIN_PWD = "555555";
    public SettingHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[]{ "setting" });
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " +
                    "setting(name varchar(50) primary key," +
                    " value varchar(500))");

            db.execSQL("insert into setting (name, value) values('mode', '0')");
            db.execSQL("insert into setting (name, value) values('maxAmount', '100')");
            db.execSQL("insert into setting (name, value) values('fixAmount', '0.1')");
            db.execSQL("insert into setting (name, value) values('pwd', '000000')");
            db.execSQL("insert into setting (name, value) values('pwdAuth', 'false')");
            db.execSQL("insert into setting (name, value) values('transKey', '" + INIT_KEY + "')");
            db.execSQL("insert into setting (name, value) values('serverUrl', 'https://open.pay.ioopos.com')");
            db.execSQL("insert into setting (name, value) values('serverType', '0')");
            db.execSQL("insert into setting (name, value) values('queryPeriod', '5')");

            db.execSQL("insert into setting (name, value) values('synced', 'false')");
            db.execSQL("insert into setting (name, value) values('cardPwd', '" + DEF_CARD_ADMIN_PWD + "')");

            db.execSQL("insert into setting (name, value) values('switchScanPay', 'true')");
            db.execSQL("insert into setting (name, value) values('switchNfcPay', 'true')");
            db.execSQL("insert into setting (name, value) values('switchFacePay', 'true')");
            db.execSQL("insert into setting (name, value) values('switchTransQuery', 'true')");
            db.execSQL("insert into setting (name, value) values('switchRefund', 'true')");
            db.execSQL("insert into setting (name, value) values('switchAutoUpdate', 'true')");
            db.execSQL("insert into setting (name, value) values('switchFaceAutoScan', 'false')");
            db.execSQL("insert into setting (name, value) values('switchFaceAutoPay', 'false')");
            db.execSQL("insert into setting (name, value) values('switchFaceSyncPay', 'false')");

            db.execSQL("insert into setting (name, value) values('isCpay', 'false')");

            db.execSQL("insert into setting (name, value) values('netStates', '" + NET_STATE_DEFAULT + "')");

        } else {
            c = db.rawQuery("select value from setting where name=?", new String[]{"cardPwd"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('cardPwd', '" + DEF_CARD_ADMIN_PWD + "')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"queryPeriod"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('queryPeriod', '5')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"synced"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('synced', 'false')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchScanPay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchScanPay', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchNfcPay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchNfcPay', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchFacePay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchFacePay', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchTransQuery"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchTransQuery', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchRefund"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchRefund', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchAutoUpdate"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchAutoUpdate', 'true')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchFaceAutoScan"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchFaceAutoScan', 'false')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchFaceAutoPay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchFaceAutoPay', 'false')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"switchFaceSyncPay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('switchFaceSyncPay', 'false')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"isCpay"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('isCpay', 'false')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"netStates"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('netStates', '"+ NET_STATE_DEFAULT +"')");
            }
            c.close();
            c = db.rawQuery("select value from setting where name=?", new String[]{"serverType"});
            if (!c.moveToNext()) {
                db.execSQL("insert into setting (name, value) values('serverType', '0')");
            }
        }
        c.close();
    }
}
