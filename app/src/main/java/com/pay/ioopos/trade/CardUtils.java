package com.pay.ioopos.trade;

import static com.pay.ioopos.channel.card.CardOrder.STATUS_UPLOADED;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardBalanceData;
import com.aggregate.pay.sanstar.bean.CardBalanceResult;
import com.aggregate.pay.sanstar.bean.PayOrderData;
import com.aggregate.pay.sanstar.bean.PayUploadData;
import com.aggregate.pay.sanstar.bean.PayUploadDataItem;
import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.card.CardBase;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mo_yq5
 * @since 2021/12/13
 */
public class CardUtils {

    /**
     * 异步上传，如果卡流水存储位置已经到达最后一个扇区，则”备份“到本地后再上传
     * @param base 卡基本信息
     * @param orders 要上传的卡流水
     * @param isLastSector 是否卡流水存储位置已经到达最后一个扇区
     */
    public static void startUploadOrders(final CardBase base, final List<CardOrder> orders, boolean isLastSector) {
        TaskFactory.execute(() -> {
            try {
                if (isLastSector) {
                    orders.stream().filter(order -> order.isSuccess() && order.getStatus() != STATUS_UPLOADED)
                            .forEach(order -> {
                                OrderUtils.proxyPay(base.getCardNo(), order);
                            });
                } else {
                    batchUploadOrders(base, orders);
                }
            } catch (Exception e) {
                LogUtils.error(e, "卡流水上传失败");
            }
        });
    }

    /**
     * 异步分批上传
     * @param base 卡基本信息
     * @param orders 要上传的卡流水
     */
    public static void startUploadOrders(final CardBase base, final List<CardOrder> orders) {
        TaskFactory.execute(() -> {
            try {
                batchUploadOrders(base, orders);
            } catch (Exception e) {
                LogUtils.error(e, "卡流水上传失败");
            }
        });
    }

    /**
     * 分批上传
     * @param base 卡基本信息
     * @param orders 要上传的卡流水
     */
    public static void batchUploadOrders(final CardBase base, final List<CardOrder> orders) {
        final int size = orders.size();
        final int batch = 3;
        AtomicInteger ai = new AtomicInteger(size/batch + (size % batch > 0 ? 1: 0));
        int num = ai.get();
        for (int i = 0; i < num; i++) {
            int index = i * batch;
            TaskFactory.execute(() -> {
                uploadOrders(base, orders.subList(index, Math.min(index + batch, size)));
                ai.getAndDecrement();
            });
        }
        try {
            while (ai.get() > 0) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {

        }
        CardBalanceData data = new CardBalanceData();
        data.setBalance(base.getBalance());
        data.setCardNo(base.getCardNo());
        data.setCardUid(base.getCardUid());
        Result<CardBalanceResult> apiResult = SanstarApiFactory.cardBalance(ApiUtils.initApi()).execute(data);
        if (null != apiResult.getData() && apiResult.getData().getBalance().intValue() == base.getBalance()) {
            CardRisk.getSyncUidList().remove(base.getCardUid());
        }

    }

    /**
     * 一次性上传
     * @param base 卡基本信息
     * @param orders 要上传的卡流水
     */
    private static void uploadOrders(final CardBase base, final List<CardOrder> orders) {
        List<PayUploadDataItem> items = new ArrayList<>();
        orders.stream()
                .filter(order -> order.isSuccess() && order.getStatus() != STATUS_UPLOADED)
                .forEach(order -> {
                    PayUploadDataItem item = new PayUploadDataItem();
                    item.setPayData(createPayOrderDataByCardOrder(base, order));
                    item.setDevSn(order.getDevSn());
                    items.add(item);
                });

        PayUploadData uploadData = new PayUploadData();
        uploadData.setOrders(items);

        SanstarApiFactory.payUpload(ApiUtils.initApi()).execute(uploadData);
    }

    private static PayOrderData createPayOrderDataByCardOrder(final CardBase base, CardOrder order) {
        PayOrderData data = new PayOrderData();
        data.setAmount(order.getAmount());
        data.setAuthCode(base.getCardNo());
        data.setCusOrderNo(order.getOrderNo());
        data.setGoodsName(base.getCardUid());
        data.setOrderTime(new Date(order.getOrderTime() * 1000));
        data.setPayMethod(PayMethod.CARD);
        data.setPayType(PayType.OTHER);
        data.setRemark(DeviceUtils.sn());

        String cusOthers = StoreFactory.settingStore().getOthers();
        Map<String, Object> map = JSON.toObject(cusOthers, new TypeReference<HashMap<String, Object>>() {});
        if (null == map) {
            map = new HashMap<>();
        }
        map.put("balance", order.getBalance());
        cusOthers = JSON.toString(map);
        data.setCusOthers(cusOthers);

        return data;
    }

}
