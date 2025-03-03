package com.pay.ioopos.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 微信刷脸用户信息数据库
 * @author    Moyq5
 * @since  2020/12/9 15:59
 */
public class WxFaceUserHelper extends SQLiteOpenHelper {
    public static final String TB_FACE_USER = "tb_face_user"; // 刷脸用户信息表
    public static final String TB_SETTING = "tb_setting";   // 相关设置参数，比如人脸信息最近更新时间
    public WxFaceUserHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 1, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[]{ TB_FACE_USER });
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " + TB_FACE_USER + "(" +
                    "wx_user_id varchar(50) primary key, " +    // 微信刷脸用户id
                    "wx_org_id varchar(32), " +                 // 微信刷脸机构id
                    "wx_out_id varchar(32), " +                 // 微信刷脸用户商户自定义id，即微信字段out_user_id
                    "wx_user_name varchar(32), " +              // 微信刷脸用户姓名
                    "wx_user_info varchar(32)" +                // 微信刷脸用户其它信息，如班级名称、职务名称
                    ")"
            );

        }
        c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[]{ TB_SETTING });
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " + TB_SETTING + "(" +
                    "name varchar(32) primary key, " +   // 参数名
                    "value varchar(500)" +               // 参数值
                    ")"
            );
        }
    }

}
