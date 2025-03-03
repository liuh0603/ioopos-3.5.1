package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.channel.ipay.ApiUtils.getCusOthers;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_REMAIN_AMOUNT;

import android.content.Intent;
import android.util.Log;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.PayOrderData;
import com.aggregate.pay.sanstar.bean.PayOrderResult;
import com.aggregate.pay.sanstar.bean.PayQueryData;
import com.aggregate.pay.sanstar.bean.PayQueryResult;
import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayMode;
import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.Merch;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.HttpUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 艾博世支付过程
 * @author    Moyq5
 * @since  2020/3/30 12:03
 */
public class PayIngFragment extends PayIngAbstract {
    private final String orderNo;
    private Merch merch;
    private PayType payType;
    private PayMethod payMethod;
    private String channelOrderNo;

    public PayIngFragment(OnPayingListener listener, Intent params) {
        super(listener, params);
        this.orderNo = ApiUtils.generateOrderNo();
    }

    public String getOrderNo() {
        return orderNo;
    }

    /**
     * 网络支付
     * @author    Moyq5
     */
    protected void networkPay() throws Exception {
        merch = ApiUtils.initApi();

        payType = getPayType();
        if (null == payType) {
            payType = PayType.OTHER;
        }
        payMethod = getPayMethod();

        String authCode = getAuthCode();
        String amount = getAmount();
        String goodsName = getGoodsName();
        if (null == goodsName || goodsName.isEmpty()) {
            goodsName = DeviceUtils.sn();
        }


        // 元转分
        final int money = BigDecimalUtils.yuanToFen(new BigDecimal(amount));

        PayOrderData apiData = new PayOrderData();
        apiData.setAmount(money);
        apiData.setAuthCode(authCode);
        apiData.setCusOrderNo(orderNo);
        apiData.setGoodsName(goodsName);
        apiData.setOrderTime(new Date());
        apiData.setPayMethod(payMethod);
        apiData.setPayType(payType);
        apiData.setPayMode(PayMode.REAL_TIME);
        apiData.setRemark("");

        Map<String, Object> cusOthers = getCusOthers();
        if (payType == PayType.WEIXIN && payMethod == PayMethod.FACE) {// 微信刷脸传刷脸用户id
            cusOthers.put("faceOutUserId", getWxOutUserId());
            cusOthers.put("faceUserId", getWxUserId());
        }

        apiData.setCusOthers(JSON.toString(cusOthers));

        Client<PayOrderData, PayOrderResult> client = SanstarApiFactory.payOrder(merch);

        Result<PayOrderResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            if ("C9997".equals(apiResult.getCode())) {// InterruptedIOException
                // 主动退出
                return;
            } else if (apiResult.getCode().equals("C9998")) {
                displayLog("网络异常 %s", apiResult.getMessage());
                toast("网络异常：%s", apiResult.getMessage());
                speak("网络异常");
                networkQuery();
            } else if (apiResult.getCode().equals("C9999")) {
                displayLog("支付异常%s", apiResult.getMessage());
                toast("支付异常：%s", apiResult.getMessage());
                networkQuery();
            } else {
                onPayFail(orderNo, "支付返回：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            }
            return;
        }

        PayOrderResult payResult = apiResult.getData();
        ApiUtils.bind(payResult.getOthers());

        channelOrderNo = payResult.getOrderNo();
        switch (payResult.getPayStatus()) {
            case FAIL:
                onPayFail(orderNo, payResult.getPayDesc());
                break;
            case SUCCESS:
                if(payMethod == PayMethod.FACE){
                    HttpUtils https= new HttpUtils();
                    String OrderNo =https.getOrderNo();
                    if(OrderNo.equals(payResult.getCusOrderNo())){
                        Integer remainAmount = Integer.valueOf(https.getRemainAmount());
                        onPaySuccess(BigDecimalUtils.fenToYuan(remainAmount).toString() +"元");
                    }else{
                        onPaySuccess();
                    }
                }else{
                    onPaySuccess();
                }
                reportData(payResult.getSupOrderNo());
                break;
            case PAYING:
                onPayPwd();
                networkQuery();
                break;
            default:
                toast(payResult.getPayDesc());
                networkQuery();
                break;
        }

    }

    /**
     * 支付结果查询
     * @author  Moyq5
     */
    private void networkQuery() throws Exception {

        PayQueryData apiData = new PayQueryData();
        apiData.setCusOrderNo(orderNo);
        apiData.setOrderNo(channelOrderNo);

        Client<PayQueryData, PayQueryResult> client = SanstarApiFactory.payQuery(merch);
        Result<PayQueryResult> apiResult;
        PayQueryResult queryResult;

        long startTime = System.currentTimeMillis();
        int sleepTime = StoreFactory.settingStore().getQueryPeriod() * 1000;
        long diff;

        query: while(true) {

            //noinspection BusyWait
            Thread.sleep(sleepTime);

            diff = (System.currentTimeMillis() - startTime)/1000;
            if (diff >= 120) {
                onPayExpired(orderNo);
                break;
            } else if (diff >= 60) {
                sleepTime = 7000;
            } else if (diff >= 30) {
                sleepTime = 6000;
            }

            displayLog("支付查询...");
            apiResult = client.execute(apiData);

            if (apiResult.getStatus() != Result.Status.OK) {
                if (apiResult.getCode().equals("C0008")) {// 订单不存在
                    onPayFail(orderNo, "查询返回：[" + apiResult.getCode() + "]" + apiResult.getMessage());
                    return;
                }
                if (apiResult.getCode().equals("C9998")) {
                    displayLog("网络异常 %s", apiResult.getMessage());
                    toast("网络异常：%s", apiResult.getMessage());
                    speak("网络异常");
                } else if (apiResult.getCode().equals("C9999")) {
                    displayLog("查询异常 %s", apiResult.getMessage());
                    toast("查询异常：%s", apiResult.getMessage());
                } else {
                    displayLog("查询返回 [%s]%s", apiResult.getCode(), apiResult.getMessage());
                    toast("查询返回：[%s]%s", apiResult.getCode(), apiResult.getMessage());
                }
                continue;
            }

            queryResult = apiResult.getData();

            //ApiFactory.bindOthers(queryResult.getOthers());

            switch (queryResult.getPayStatus()) {
                case PAYING:
                    onPayPwd();
                    break;
                case FAIL:
                    onPayFail(orderNo, queryResult.getPayDesc());
                    break query;
                case SUCCESS:
                    onPaySuccess();
                    reportData(queryResult.getSupOrderNo());
                    break query;
            }
        }
    }

    /**
     * 微信上报
     * @author  Moyq5
     */
    private void reportData(List<String> supOrderNo) {
        if (payType != PayType.WEIXIN || payMethod != PayMethod.SCAN) {
            return;
        }
        if (null == supOrderNo || supOrderNo.isEmpty()) {
            reportData(channelOrderNo, null);
            return;
        }
        int size;
        if ( (size = supOrderNo.size()) == 1) {
            reportData(channelOrderNo, supOrderNo.get(0));
        } else {
            reportData(supOrderNo.get(size - 2), supOrderNo.get(size - 1));
        }
    }
}
