package com.pay.ioopos.sqlite;

import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_BALANCE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_DEV_SN;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_DEV_TYPE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_ORDER_NO;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_ORDER_TIME;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_METHOD;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_TYPE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_USER_NAME;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_OUT_USER_ID;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_USER_ID;

import android.content.ContentValues;
import android.content.Intent;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayStatus;
import com.aggregate.pay.sanstar.enums.PayType;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.worker.WorkerFactory;

/**
 * @author  Moyq5
 * @since    2021/10/25 14:14
 */
public class OrderUtils {
    private static final String TAG = OrderUtils.class.getName();

    public static final String ACTION_COUNT = TAG + ".COUNT";

    /**
     * 添加异步支付订单
     * @author  Moyq5
     * @since    2020/7/9 10:07
     */
    public static void asyncPay(Intent intent) {
        try {
            add(intent);
        } catch (Throwable e) {
            LogUtils.error(e, "离线交易保存异常", Thread.currentThread());
            throw  e;
        } finally {
            WorkerFactory.enqueuePayUploadOneTime();
        }

    }

    /**
     * 添加代上传订单
     * @param cardNo （平台）卡号
     * @param order 待上传订单
     */
    public static void proxyPay(String cardNo, CardOrder order) {
        try {
            Intent intent = new Intent();
            intent.putExtra(INTENT_PARAM_PAY_TYPE, PayType.OTHER);
            intent.putExtra(INTENT_PARAM_PAY_METHOD, PayMethod.CARD);
            intent.putExtra(INTENT_PARAM_CODE, cardNo);
            intent.putExtra(INTENT_PARAM_AMOUNT, BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString());
            intent.putExtra(INTENT_PARAM_BALANCE, BigDecimalUtils.fenToYuan(order.getBalance()).toPlainString());
            intent.putExtra(INTENT_PARAM_ORDER_NO, order.getOrderNo());
            intent.putExtra(INTENT_PARAM_ORDER_TIME, order.getOrderTime());
            intent.putExtra(INTENT_PARAM_DEV_TYPE, 1);
            intent.putExtra(INTENT_PARAM_DEV_SN, order.getDevSn());
            add(intent);
        } catch (Throwable e) {
            LogUtils.error(e, "离线交易代保存异常", Thread.currentThread());
            throw  e;
        } finally {
            WorkerFactory.enqueuePayUploadOneTime();
        }
    }

    public static void broadcastCount() {
        localSend(new Intent(ACTION_COUNT));
    }

    private static void add(Intent intent) {
        ContentValues order = new ContentValues();
        order.put("pay_type", ((PayType) intent.getSerializableExtra(INTENT_PARAM_PAY_TYPE)).ordinal());
        order.put("pay_method", ((PayMethod) intent.getSerializableExtra(INTENT_PARAM_PAY_METHOD)).ordinal());
        order.put("pay_code", intent.getStringExtra(INTENT_PARAM_CODE));
        order.put("amount", intent.getStringExtra(INTENT_PARAM_AMOUNT));// 元
        order.put("balance", intent.getStringExtra(INTENT_PARAM_BALANCE));// 元
        order.put("order_no", intent.getStringExtra(INTENT_PARAM_ORDER_NO));
        order.put("pay_status", PayStatus.NEW.ordinal());
        order.put("pay_descr", "未支付");
        order.put("add_time", intent.getLongExtra(INTENT_PARAM_ORDER_TIME, System.currentTimeMillis()/1000));// 秒
        order.put("query_time", System.currentTimeMillis()/1000);
        order.put("query_times", 0);
        order.put("out_user_id", intent.getStringExtra(INTENT_PARAM_WX_OUT_USER_ID));
        order.put("user_id", intent.getStringExtra(INTENT_PARAM_WX_USER_ID));
        order.put("user_name", intent.getStringExtra(INTENT_PARAM_USER_NAME));
        order.put("dev_type", intent.getIntExtra(INTENT_PARAM_DEV_TYPE, 0));
        order.put("dev_sn", intent.getStringExtra(INTENT_PARAM_DEV_SN));

        OrderStore store = StoreFactory.orderStore();
        store.add(order);
        broadcastCount();
    }

}
