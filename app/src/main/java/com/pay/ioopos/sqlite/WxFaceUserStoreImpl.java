package com.pay.ioopos.sqlite;

import static com.pay.ioopos.sqlite.WxFaceUserHelper.TB_FACE_USER;
import static com.pay.ioopos.sqlite.WxFaceUserHelper.TB_SETTING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 离线刷脸用户信息
 * @author    Moyq5
 * @since  2020/5/12 18:39
 */
public class WxFaceUserStoreImpl implements WxFaceUserStore {
    private static WxFaceUserHelper helper;
    public WxFaceUserStoreImpl(Context context) {
        helper = new WxFaceUserHelper(context,  "wx_face_user.db", null, 2);
    }

    @Override
    public long modUser(ContentValues user) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        return wdb.replace(TB_FACE_USER, null, user);
    }

    @Override
    public ContentValues selUser(String wxUserId) {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select * from "+ TB_FACE_USER +" where wx_user_id=?", new String[]{wxUserId});
        ContentValues user = null;
        while (c.moveToNext()) {
            user = new ContentValues();
            int count = c.getColumnCount();
            for (int i = 0; i < count; i++) {
                user.put(c.getColumnName(i), c.getString(i));
            }
            break;
        }
        c.close();
        return user;
    }

    @Override
    public int delUserExclude(String notWxOrgId) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        return wdb.delete(TB_FACE_USER, "wx_org_id!=?", new String[]{notWxOrgId});
    }

    @Override
    public Long selModTime() {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select * from "+ TB_SETTING +" where name=?", new String[]{ "mod_time" });
        Long timestamp = null;
        while (c.moveToNext()) {
            timestamp = c.getLong(c.getColumnIndex("value"));
            break;
        }
        c.close();
        return  null == timestamp ? 0: timestamp;
    }

    @Override
    public void modModTime(Long modTime) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        ContentValues setting = new ContentValues();
        setting.put("name", "mod_time");
        setting.put("value", modTime);
        wdb.replace(TB_SETTING, null, setting);
    }
}
