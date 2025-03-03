package com.pay.ioopos.sqlite;

import android.content.ContentValues;

import java.util.List;

/**
 * 离线订单
 * @author    Moyq5
 * @since  2020/5/12 18:08
 */
public interface OrderStore {
    String TABLE = "pay_order";

    /**
     * 添加订单
     * @param order
     * @return
     */
    long add(ContentValues order);

    /**
     * 更新记录
     * @param order
     */
    void update(ContentValues order);

    /**
     * 查询未支付订单
     * @return
     */
    List<ContentValues> query(int type);

    /**
     * 删除订单
     * @param orderId
     */
    void delete(String orderId);

    /**
     * 记录数
     * @return
     */
    int count(int type);
}
