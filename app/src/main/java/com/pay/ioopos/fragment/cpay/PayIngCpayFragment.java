package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Intent;
import android.util.Log;

import com.aggregate.pay.sanstar.enums.PayType;
import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.channel.cpay.PayApiCloud;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.DeviceUtils;
import com.tencent.cloudpay.exception.CPayAuthenCodeCheckFail;
import com.tencent.cloudpay.exception.CPayNetworkError;
import com.tencent.cloudpay.exception.CPayNetworkTimeout;
import com.tencent.cloudpay.exception.CPayResponseInvalid;
import com.tencent.cloudpay.param.MicroPayRequest;
import com.tencent.cloudpay.param.MicroPayResponse;
import com.tencent.cloudpay.param.QueryOrderRequest;
import com.tencent.cloudpay.param.QueryOrderResponse;
import com.tencent.cloudpay.pojo.Order;
import com.tencent.cloudpay.pojo.State;
import com.tencent.cloudpay.utils.CommonUtils;

import java.math.BigDecimal;

/**
 * 腾讯云支付
 * @author    Moyq5
 * @since  2020/7/30 11:02
 */
public class PayIngCpayFragment extends PayIngAbstract {
    private static final String TAG = PayIngCpayFragment.class.getSimpleName();
    private final String orderNo;
    public PayIngCpayFragment(OnPayingListener listener, Intent intent) {
        super(listener, intent);
        orderNo = PayApiCloud.generateOrderNo();
    }

    public String getOrderNo() {
        return orderNo;
    }

    @Override
    protected void networkPay() throws Exception {

        final PayType payType = getPayType();
        final String amount = getAmount();
        final String authCode = getAuthCode();
        String goodsName = getGoodsName();
        if (null == goodsName || goodsName.isEmpty()) {
            goodsName = DeviceUtils.sn();
        }
        if (null == payType) {
            onPayFail(null, "不支持该付款码");
            return;
        }

        // 元转分
        int money = BigDecimalUtils.yuanToFen(new BigDecimal(amount));

        CpayStore store = StoreFactory.cpayStore();

        MicroPayRequest req = new MicroPayRequest();
        req.setAuthorCode(authCode);
        req.setBody(store.getShopName() + "-" + goodsName);
        req.setOutTradeNo(orderNo);
        if (payType == PayType.WEIXIN) {
            req.setPayPlatform(1);
        } else if (payType == PayType.ALIPAY) {
            req.setPayPlatform(2);
        } else {
            onPayFail(null, "不支持该付款码");
            return;
        }
        req.setTotalFee(money);
        Order order = null;
        try {
            MicroPayResponse res = MyCloudPay.getInstance().microPay(req);
            order = res.getOrder();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            toast("网络异常：" + e.getMessage());
            speak("网络异常");
            /*
            if (!isPayResultUnknown(e)) {
                Log.e(TAG, e.getMessage(), e);
                showPayError();
                return;
            }
            */
        }

        if (null == order || isUserPaying(order.getState())) {
            networkQuery();
        } else if (isPayFailed(order.getState())) {
            onPayFail(orderNo, "状态码：" + order.getState());
        } else {
            onPaySuccess();
            reportData(order);
        }
    }

    private void networkQuery() throws Exception {
        long startTime = CommonUtils.getNowTimestamp();
        int sleepTime = StoreFactory.settingStore().getQueryPeriod() * 1000;
        while(true) {
            Thread.sleep(sleepTime);
            long nowTime = CommonUtils.getNowTimestamp();
            long diff = nowTime - startTime;
            if (diff >= 120) {
                onPayExpired(orderNo);
                break;
            } else if (diff >= 60) {
                sleepTime = 6000;
            } else if (diff >= 30) {
                sleepTime = 5000;
            }

            try {
                QueryOrderRequest req = new QueryOrderRequest();
                req.setOut_trade_no(orderNo);
                QueryOrderResponse res = MyCloudPay.getInstance().queryOrder(req);
                Order order = res.getOrder();
                int state = order.getState();
                if (state == State.kCloudPaySdkLocalStateSuccess) {
                    onPaySuccess();
                    reportData(order);
                    return;
                } else if (isPayFailed(state)) {
                    onPayFail(orderNo, "状态码：" + state);
                    return;
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    throw e;
                }
                if (isPayResultUnknown(e)) {
                    toast("网络异常：" + e.getMessage());
                    speak("网络异常");
                    continue;
                }
                Log.e(TAG, e.getMessage(), e);
                toast("查询失败：" + e.getMessage());
                //showPayError();
                //return;
            }
        }
    }

    private boolean isPayResultUnknown(Exception e) {
        return e instanceof CPayNetworkTimeout
                || e instanceof CPayNetworkError
                || e instanceof CPayAuthenCodeCheckFail
                || e instanceof CPayResponseInvalid;
    }

    private boolean isUserPaying(int state) {
        return state == State.kCloudPaySdkLocalStateUserPaying || state == State.kCloudPaySdkLocalStateInit;
    }

    private boolean isPayFailed(int state) {
        return state == State.kCloudPaySdkLocalStateFail || state == State.kCloudPaySdkLocalStateClosed;
    }


    private void reportData(Order order) {
        reportData(order.getOut_trade_no(), order.getTransaction_id());
    }
}
