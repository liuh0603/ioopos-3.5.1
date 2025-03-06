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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.pay.ioopos.support.face.bean.BdFaceUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */


public class BdFaceUserStoreImpl implements BdFaceUserStore {
    private static BdFaceUserHelper helper;

    public BdFaceUserStoreImpl(Context context) {
        helper = new BdFaceUserHelper(context,  "bdFace_sanstar.db", null, 1);
    }

    @Override
    public long mod(BdFaceUser bdFace) {
        ContentValues cv = new ContentValues();
        Log.d("liuh", "BdFaceUserStoreImpl bdFace =" + bdFace + ", userID=" + bdFace.getUserId() +
                ", Name=" + bdFace.getUserName() + ", feature=" + bdFace.getFaceFeature());
        cv.put(FACE_COL_BDFACE_NO, bdFace.getUserId());
        cv.put(FACE_COL_MERCH_NO, StoreFactory.settingStore().getMerchNo());
        //cv.put(FACE_COL_GROUP_NO, "null");
        cv.put(FACE_COL_NAME, bdFace.getUserName());
        //cv.put(FACE_COL_UNIT, bdFace.getUnit());
        //cv.put(FACE_COL_NUM, bdFace.getNum());
        //cv.put(FACE_COL_STATE, "null");
        //cv.put(FACE_COL_SETTING, JSON.toString(bdFace.getSetting()));
        cv.put(FACE_COL_FEATURE, bdFace.getFaceFeature());
        cv.put(FACE_COL_ADD_TIME, new Date().getTime());
        SQLiteDatabase wdb = helper.getWritableDatabase();
        return wdb.replace(TB_FACE, null, cv);
    }

    @Override
    public BdFaceUser one(String bdFaceNo) {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select * from "+ TB_FACE +" where " + FACE_COL_BDFACE_NO + "=?", new String[]{ bdFaceNo });
        if (!c.moveToNext()) {
            c.close();
            return null;
        }
        BdFaceUser bdFace = convert(c);
        c.close();
        return bdFace;
    }

    @Override
    public List<BdFaceUser> list(String merchNo, long afterTime, int page) {
        String sql = "select * from " +
                TB_FACE +
                " where " +
                FACE_COL_MERCH_NO +
                "=? and " +
                FACE_COL_ADD_TIME +
                ">? order by " +
                FACE_COL_ADD_TIME +
                " desc limit ?, ?";

        int pageSize = 500;
        List<String> values = new ArrayList<>();
        values.add(merchNo);
        values.add(afterTime + "");
        values.add((page - 1) * pageSize + "");
        values.add(pageSize + "");

        int size = values.size();
        List<BdFaceUser> list = new ArrayList<>();
        SQLiteDatabase rdb = helper.getReadableDatabase();
        try (Cursor c = rdb.rawQuery(sql, values.toArray(new String[size]))) {
            while (c.moveToNext()) {
                list.add(convert(c));
            }
        }
        return list;
    }

    @Override
    public int count() {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        try (Cursor c = rdb.rawQuery("select count(*) from " + TB_FACE, new String[]{})) {
            if (c.moveToNext()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    @Override
    public int delExcept(String merchNo) {
        SQLiteDatabase wdb = helper.getWritableDatabase();
        return wdb.delete(TB_FACE, FACE_COL_MERCH_NO + "!=?", new String[]{merchNo});
    }

    @Override
    public int delAll() {
        SQLiteDatabase wdb = helper.getWritableDatabase();
        return wdb.delete(TB_FACE, null, null);
    }

    private static BdFaceUser convert(Cursor c) {
        //Log.d("liuh", "BdFaceUserStoreImpl convert userID=" + c.getString(c.getColumnIndex(FACE_COL_BDFACE_NO)));
        //Log.d("liuh", "BdFaceUserStoreImpl convert feature=" + c.getString(c.getColumnIndex(FACE_COL_FEATURE)));
        BdFaceUser bdFace = new BdFaceUser();
        bdFace.setUserId(c.getString(c.getColumnIndex(FACE_COL_BDFACE_NO)));
        //bdFace.setFaceImg(c.getString(c.getColumnIndex(FACE_COL_GROUP_NO)));
        bdFace.setUserName(c.getString(c.getColumnIndex(FACE_COL_NAME)));
        //bdFace.setNum(c.getString(c.getColumnIndex(FACE_COL_NUM)));
        //bdFace.setUnit(c.getString(c.getColumnIndex(FACE_COL_UNIT)));
        //bdFace.setUserInfo(c.getString(c.getColumnIndex(FACE_COL_STATE)));
        bdFace.setFaceFeature(c.getString(c.getColumnIndex(FACE_COL_FEATURE)));
        return bdFace;
    }
}
