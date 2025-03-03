package com.pay.ioopos.sqlite;

import static com.pay.ioopos.sqlite.Constants.FACE_COL_ADD_TIME;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_FEATURE;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_GROUP_NO;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_MERCH_NO;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_NAME;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_NUM;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_SETTING;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_BDFACE_NO;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_STATE;
import static com.pay.ioopos.sqlite.Constants.FACE_COL_UNIT;
import static com.pay.ioopos.sqlite.Constants.TB_FACE;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public class BdFaceUserHelper extends SQLiteOpenHelper {
    public BdFaceUserHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 1, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name=?", new String[]{TB_FACE});
        if (!c.moveToNext() || c.getInt(0) == 0) {
            db.execSQL("create table " + TB_FACE + "(" +
                    FACE_COL_BDFACE_NO + " varchar(50) primary key, " +         // 编号
                    FACE_COL_MERCH_NO + " varchar(32), " +                       // 商户号
                    FACE_COL_GROUP_NO + " varchar(32), " +                       // 配置组号
                    FACE_COL_NAME + " varchar(32), " +                           // 职员名称
                    FACE_COL_UNIT + " varchar(32), " +                           // 部门
                    FACE_COL_NUM + " varchar(32), " +                            // 工号
                    FACE_COL_STATE + " int(8)," +                                // 状态
                    FACE_COL_FEATURE + " varchar(1000)," +                       // 人脸特征值
                    FACE_COL_SETTING + " varchar(5000)," +                       // 配置信息
                    FACE_COL_ADD_TIME + " bigint" +                              // 添加时间
                    ")"
            );
        }
        c.close();
    }

}
