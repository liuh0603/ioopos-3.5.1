package com.pay.ioopos.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线订单
 * @author    Moyq5
 * @since  2020/5/12 18:39
 */
public class OrderStoreImpl implements OrderStore {
    private static OrderHelper helper;
    public OrderStoreImpl(Context context) {
        helper = new OrderHelper(context, "pay_order.db", null, 9);
    }

    @Override
    public long add(ContentValues order) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        return wdb.insert(TABLE, null, order);
    }

    @Override
    public void update(ContentValues order) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        String orderId = order.getAsString("order_id");
        order.remove("order_id");
        wdb.update(TABLE, order, "order_id=?", new String[]{orderId});
        order.put("order_id", orderId);
    }

    @Override
    public List<ContentValues> query(int type) {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        List<ContentValues> orders = new ArrayList<>();
        Cursor c = rdb.rawQuery("select * from "+ TABLE +" where dev_type=? and pay_status=0 and query_times < 10", new String[]{""+ type});
        while (c.moveToNext()) {
            ContentValues order = new ContentValues();
            int count = c.getColumnCount();
            for (int i = 0; i < count; i++) {
                order.put(c.getColumnName(i), c.getString(i));
            }
            orders.add(order);
        }
        c.close();
        return  orders;
    }

    @Override
    public void delete(String orderId) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        wdb.delete(TABLE, "order_id=?", new String[]{orderId});
    }

    @Override
    public int count(int type) {
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select count(*) from "+ TABLE + " where dev_type=?", new String[]{"" + type});
        int count = 0;
        while (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
}
