package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.channel.ipay.ApiUtils.getCusOthers;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.PayOrderData;
import com.aggregate.pay.sanstar.bean.PayOrderResult;
import com.aggregate.pay.sanstar.bean.PayQueryData;
import com.aggregate.pay.sanstar.bean.PayQueryResult;
import com.aggregate.pay.sanstar.bean.PayUploadData;
import com.aggregate.pay.sanstar.bean.PayUploadDataItem;
import com.aggregate.pay.sanstar.bean.PayUploadResult;
import com.aggregate.pay.sanstar.bean.PayUploadResultItem;
import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayMode;
import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.OrderStore;
import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 离线支付数据上传
 * @author mo_yq5
 * @since 2021-10-25
 */
public class PayUploadWorker extends Worker {
    private static final Lock payLock = new ReentrantLock();
    private static final Lock uploadLock = new ReentrantLock();

    public PayUploadWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (!isNetworkAvailable() || !ApiUtils.isBound() || !ApiUtils.isChecked()) {
            return Result.success();
        }
        if (!payLock.tryLock()) {
            return Result.retry();
        }
        startUpload();
        try {
            try {
                StoreFactory.orderStore().query(0).forEach(order -> {
                    try {
                        apiPay(order);
                    } catch (Throwable e) {
                        LogUtils.error(e, "离线交易上传异常", Thread.currentThread());
                    }
                });
            } catch (Throwable e) {
                LogUtils.error(e, "离线交易上传异常", Thread.currentThread());
            }
        } finally {
            payLock.unlock();
        }
        return Result.success();
    }

    /**
     * 提交支付，提交成功，可移除本地记录
     * @param order
     */
    private void apiPay(ContentValues order) {

        PayOrderData apiData = createPayOrderData(order);

        Client<PayOrderData, PayOrderResult> client = SanstarApiFactory.payOrder(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<PayOrderResult> apiResult = client.execute(apiData);

        OrderStore store = StoreFactory.orderStore();

        if (apiResult.getStatus() == com.aggregate.pay.sanstar.Result.Status.OK) {
            PayOrderResult payResult = apiResult.getData();
            ApiUtils.bind(payResult.getOthers());

            store.delete(order.getAsString("order_id"));
            OrderUtils.broadcastCount();
            return;
        }

        order.put("pay_descr", apiResult.getMessage());
        store.update(order);
        LogUtils.error("离线交易上传失败：" + order.toString());
        apiQuery(order);

    }

    /**
     * 支付查询，可查询到说明已经上传成功，可移动本地记录
     * @param order
     */
    private void apiQuery(ContentValues order) {
        PayQueryData apiData = new PayQueryData();
        apiData.setCusOrderNo(order.getAsString("order_no"));

        Client<PayQueryData, PayQueryResult> client = SanstarApiFactory.payQuery(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<PayQueryResult> apiResult = client.execute(apiData);

        OrderStore store = StoreFactory.orderStore();
        if (apiResult.getStatus() == com.aggregate.pay.sanstar.Result.Status.OK) {
            PayQueryResult payResult = apiResult.getData();
            ApiUtils.bind(payResult.getOthers());

            store.delete(order.getAsString("order_id"));
            OrderUtils.broadcastCount();
        }
    }

    /**
     * 代上传：异步上传
     */
    private void startUpload() {
        TaskFactory.submit(() -> lockUpload());
    }

    /**
     * 代上传：加锁上传，串行上传
     */
    private void lockUpload() {
        if (!uploadLock.tryLock()) {
            return;
        }
        try {
            storeUpload();
        } catch (Throwable e) {
            LogUtils.error(e, "离线交易上传异常", Thread.currentThread());
        } finally {
            uploadLock.unlock();
        }
    }

    /**
     * 代上传：上传数据库数据
     */
    private void storeUpload() {
        List<ContentValues> contentValues = StoreFactory.orderStore().query(1);
        if (null == contentValues || contentValues.size() == 0) {
            return;
        }
        List<PayUploadDataItem> dataItems = new ArrayList<>();
        contentValues.forEach(order -> {
            PayOrderData payData = createPayOrderData(order);
            PayUploadDataItem item = new PayUploadDataItem();
            item.setDevSn(order.getAsString("dev_sn"));
            item.setPayData(payData);
            dataItems.add(item);
        });
        int size = dataItems.size();
        if (dataItems.size() == 0) {
            return;
        }

        int count = 3;// 每次提交笔数
        List<PayUploadDataItem> uploadItems;
        for (int i = 0; i < size; i +=count) {
            if (i + count < size) {
                uploadItems = dataItems.subList(i, i + count);
            } else {
                uploadItems = dataItems.subList(i, size);
            }
            if (uploadItems.size() > 0) {
                apiUpload(contentValues, uploadItems);
            }
        }

    }

    /**
     * 代上传：api调用
     * @param contentValues 数据库数据
     * @param dataItems 要上传的数据
     */
    private void apiUpload(List<ContentValues> contentValues, List<PayUploadDataItem> dataItems) {
        PayUploadData uploadData = new PayUploadData();
        uploadData.setOrders(dataItems);

        Client<PayUploadData, PayUploadResult> client = SanstarApiFactory.payUpload(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<PayUploadResult> apiResult = client.execute(uploadData);
        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            LogUtils.error("离线交易代上传失败：%s", apiResult.getMessage());
            return;
        }
        PayUploadResult uploadResult = apiResult.getData();
        if (null == uploadResult) {
            return;
        }
        List<PayUploadResultItem> resultItems = uploadResult.getOrders();
        if (null == resultItems) {
            return;
        }

        OrderStore store = StoreFactory.orderStore();

        resultItems.forEach(resultItem -> {
            PayOrderResult orderResult = resultItem.getPayResult();
            if (null == orderResult) {
                return;
            }
            contentValues.forEach(contentValue -> {
                if (orderResult.getCusOrderNo().equals(contentValue.getAsString("order_no"))) {
                    store.delete(contentValue.getAsString("order_id"));
                }
            });
        });

        OrderUtils.broadcastCount();
    }

    private PayOrderData createPayOrderData(ContentValues order) {
        PayType payType = PayType.values()[order.getAsInteger("pay_type")];
        PayMethod payMethod = PayMethod.values()[order.getAsInteger("pay_method")];
        String amount = order.getAsString("amount");

        // 元转分
        Integer apiAmount = BigDecimalUtils.yuanToFen(new BigDecimal(amount));

        PayOrderData orderData = new PayOrderData();
        orderData.setAmount(apiAmount);
        orderData.setAuthCode(order.getAsString("pay_code"));
        orderData.setCusOrderNo(order.getAsString("order_no"));
        orderData.setGoodsName("async" + DeviceUtils.sn());
        orderData.setOrderTime(new Date(order.getAsLong("add_time") * 1000));
        orderData.setPayMode(PayMode.LAZY_TIME);
        orderData.setPayMethod(payMethod);
        orderData.setPayType(payType);
        orderData.setRemark(order.getAsString("remark"));


        Map<String, Object> cusOthers = getCusOthers();
        if (payType == PayType.OTHER && payMethod == PayMethod.CARD) {// 实体卡支付上传卡余额
            cusOthers.put("cardBalance", order.getAsString("balance"));
        }
        if (payType == PayType.WEIXIN && payMethod == PayMethod.FACE) {// 微信刷脸传刷脸用户id
            cusOthers.put("faceOutUserId", order.getAsString("out_user_id"));
            cusOthers.put("faceUserId", order.getAsString("user_id"));
        }
        orderData.setCusOthers(JSON.toString(cusOthers));

        return orderData;
    }
}
