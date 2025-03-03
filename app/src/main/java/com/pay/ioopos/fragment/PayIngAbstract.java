package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.DEV_IS_MTSCAN;
import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_GOODS_NAME;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_ORDER_NO;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_METHOD;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_TYPE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_REMAIN_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_OUT_TRADE_NO;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_OUT_USER_ID;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_TRANSACTION_ID;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_USER_ID;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.trade.PayProcess.PAYING;
import static com.pay.ioopos.trade.PayProcess.PAY_ERROR;
import static com.pay.ioopos.trade.PayProcess.PAY_EXPIRED;
import static com.pay.ioopos.trade.PayProcess.PAY_FAIL;
import static com.pay.ioopos.trade.PayProcess.PAY_PWD;
import static com.pay.ioopos.trade.PayProcess.PAY_SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayType;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.receiver.WxReportReceiver;
import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.CardPayer;
import com.pay.ioopos.trade.CardPayer.CardPayCallback;
import com.pay.ioopos.trade.CardShower;
import com.pay.ioopos.trade.PayRecent;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 支付过程抽象类
 * @author    Moyq5
 * @since  2020/3/30 12:03
 */
public abstract class PayIngAbstract extends TipHorizontalFragment implements BindState, CheckInState, KeyInfoListener {
    private static final String TAG = PayIngAbstract.class.getSimpleName();
    private final Intent params;
    private final OnPayingListener listener;
    private final String authCase;
    private final String authCode;
    private final String amount;
    private PayType payType;
    private PayMethod payMethod = PayMethod.SCAN;

    private CardPayer cardPayer;

    public PayIngAbstract(OnPayingListener listener, Intent params) {
        super(WAIT, "正在支付");
        this.listener = proxyListener(listener);
        this.params = params;
        this.authCase = params.getStringExtra(INTENT_PARAM_CASE);
        this.authCode = params.getStringExtra(INTENT_PARAM_CODE);
        this.amount = params.getStringExtra(INTENT_PARAM_AMOUNT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected final void execute() throws Exception {
        try {
            if (null == amount || new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            onPayFail("金额错误：" + amount);
            return;
        }

        // 根据付款场景和付款凭证内容分析出支付类型和支付方式
        analysePayType();

        params.putExtra(INTENT_PARAM_ORDER_NO, getOrderNo());
        params.putExtra(INTENT_PARAM_PAY_TYPE, payType);
        params.putExtra(INTENT_PARAM_PAY_METHOD, payMethod);

        /*
          通知界面互动当前的支付方式
          @see com.pay.ioopos.display.CustomerView
         */
        Intent intent = new Intent(PayIngAbstract.class.getName());
        intent.putExtra(INTENT_PARAM_CASE, authCase);
        localSend(intent);

        SerialPortPayUtils.pay(getOrderNo(), amount, PAYING);
        getCustomerHolder().showPayProcess(PAYING, amount);

        // 默认支持刷卡电子包离线交易
        if (payType == PayType.OTHER && payMethod == PayMethod.CARD) {
            displayLog("卡钱包支付...");
            myWalletPay();
            return;
        }

        // 默认支持微信离线刷脸交易
        SettingStore store = StoreFactory.settingStore();
        if (payType == PayType.WEIXIN
                && payMethod == PayMethod.FACE
                && MyWxPayFace.IS_OFFLINE
                && (!store.getSwitchFaceSyncPay() || !isNetworkAvailable())) {
            displayLog("异步扣款");
            // 同步保存，确保数据保存成功
            try {
                OrderUtils.asyncPay(params);
            } catch (Exception e) {
                onPayFail(e.getMessage());
                return;
            }
            onScanSuccess();
            return;
        }

        // 网络支付
        displayLog("通过网络支付...");
        myNetworkPay();
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_MENU:
            case KEY_SEARCH:
                return false;
        }

        TipType type = getType();
        if (type == WAIT && keyInfo == KEY_CANCEL) {
            listener.onPayCancel();
        } else if (type == FAIL && keyInfo == KEY_ENTER) {
            listener.onPayScan();
        } else if (type != WAIT) {
            listener.onPayInput(keyInfo);
        }
        return  true;
    }

    @Override
    public void onError(String msg) {
        toast(msg);
        onPayError();
    }

    /**
     * 网络支付(即，自定义通道支付)
     * @author    Moyq5
     */
    protected abstract void networkPay() throws Exception;

    public abstract String getOrderNo();

    public final String getAmount() {
        return amount;
    }

    protected final String getGoodsName() {
        return params.getStringExtra(INTENT_PARAM_GOODS_NAME);
    }

    protected final PayType getPayType() {
        return (PayType) params.getSerializableExtra(INTENT_PARAM_PAY_TYPE);
    }

    protected final PayMethod getPayMethod() {
        return (PayMethod) params.getSerializableExtra(INTENT_PARAM_PAY_METHOD);
    }

    protected final String getAuthCode() {
        return params.getStringExtra(INTENT_PARAM_CODE);
    }

    protected final String getWxUserId() {
        return params.getStringExtra(INTENT_PARAM_WX_USER_ID);
    }

    protected final String getWxOutUserId() {
        return params.getStringExtra(INTENT_PARAM_WX_OUT_USER_ID);
    }

    protected final void onPayPwd() {
        dispatch(WAIT, R.string.pay_pwd);
        speak(R.string.please_type_in_pwd);
        SerialPortPayUtils.pay(getOrderNo(), amount, PAY_PWD);
        getCustomerHolder().showPayProcess(PAY_PWD, amount);
    }

    protected final void onPaySuccess() {
        PayRecent.instance().setLastTime(System.currentTimeMillis());
        dispatch(SUCCESS, R.string.pay_success);
        speak(R.string.pay_success);
        SerialPortPayUtils.pay(getOrderNo(), amount, PAY_SUCCESS);
        String remainAmount =params.getStringExtra(INTENT_PARAM_REMAIN_AMOUNT);
        getCustomerHolder().showPayProcess(PAY_SUCCESS, amount, "余额: "+remainAmount);
        listener.onPayFinish();
    }
    protected final void onPaySuccess(String detail) {
        PayRecent.instance().setLastTime(System.currentTimeMillis());
        dispatch(SUCCESS, R.string.pay_success);
        speak(R.string.pay_success);
        SerialPortPayUtils.pay(getOrderNo(), amount, PAY_SUCCESS);
        getCustomerHolder().showPayProcess(PAY_SUCCESS, amount, "余额: "+detail);
        listener.onPayFinish();
    }

    protected final void onPayFail(String detail) {
        onPayFail(null, detail);
    }

    protected final void onPayFail(String orderNo, String detail) {
        if (null != orderNo) {
            ApiUtils.repeal(orderNo);
        }
        toast(detail);
        dispatch(FAIL, R.string.pay_fail);
        speak(R.string.pay_fail);
        SerialPortPayUtils.pay(orderNo, amount, PAY_FAIL, detail);
        getCustomerHolder().showPayProcess(PAY_FAIL, amount, detail);
        listener.onPayFinish();
    }

    protected final void onPayExpired(String orderNo) {
        if (null != orderNo) {
            ApiUtils.repeal(orderNo);
        }
        dispatch(FAIL, R.string.pay_expired);
        speak(R.string.pay_expired);
        SerialPortPayUtils.pay(orderNo, amount, PAY_EXPIRED);
        getCustomerHolder().showPayProcess(PAY_EXPIRED, amount);
        listener.onPayFinish();
    }

    protected final void onPayError() {
        if (null != getOrderNo()) {
            ApiUtils.repeal(getOrderNo());
        }
        dispatch(FAIL, R.string.pay_error);
        speak(R.string.pay_error);
        SerialPortPayUtils.pay(getOrderNo(), amount, PAY_ERROR);
        getCustomerHolder().showPayProcess(PAY_ERROR, amount);
        listener.onPayFinish();
    }

    /**
     * 离线扫码、刷卡、刷脸提示
     * @author  Moyq5
     * @since    2020/5/12 14:38
     */
    protected final void onScanSuccess() {
        String msg = "支付成功";
        /**
        if (payType == PayType.OTHER) {// NFC
            msg = "刷卡成功";
        } else if (payMethod == PayMethod.FACE) {// 刷脸
            msg = "刷脸成功";
        } else { // 扫码 PayMethod.SCAN
            msg = "扫码成功";
        }
        */
        PayRecent.instance().setLastTime(System.currentTimeMillis());
        speak(msg);
        dispatch(SUCCESS, msg);
        SerialPortPayUtils.pay(getOrderNo(), amount, PAY_SUCCESS);
        getCustomerHolder().showPayProcess(PAY_SUCCESS, amount, msg);
        listener.onPayFinish();
    }

    private void myNetworkPay() {
        if (!isNetworkAvailable()) {
            onPayFail("不支持离线交易，请换其它方式");
            return;
        }
        ownSubmit(() -> {
            try {
                networkPay();
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                Log.e(TAG, "myNetworkPay: ", e);
                onError(e.getMessage());
                LogUtils.log("myNetworkPay", Thread.currentThread(), e);
            }
        });
    }

    /**
     * 卡钱包支付
     * @author  Moyq5
     * @since    2020/11/5 18:10
     */
    private void myWalletPay() {
        if (!ApiUtils.walletPaySupport() || DEV_IS_MTSCAN) {
            displayLog("不支持钱包支付，通过网络支付...");
            myNetworkPay();
            return;
        }
        cardPayer = new CardPayer(this, authCode, amount, new CardPayCallback() {
            @Override
            public void payFail(String detail) {
                ownSubmit(() -> onPayFail(detail));
            }

            @Override

            public void payNetwork() {
                displayLog("钱包支付失败，尝试通过网络支付...");
                ownSubmit(() -> {
                    try {
                        params.putExtra(INTENT_PARAM_PAY_METHOD, PayMethod.SCAN);// 兼容处理，转为后台渠道支持卡的支付方式
                        myNetworkPay();
                    } catch (Exception e) {
                        onError(e.getMessage());
                    }
                });
            }

            @Override
            public void payError(String detail) {
                ownSubmit(() -> onPayError());
            }

            @Override
            public void paySuccess() {
                ownSubmit(() -> onPaySuccess());
            }
        });
        cardPayer.bind();

    }

    /**
     * 代理支付流程监听，增加特定状态下可以刷卡读卡信息（用户、余额等）支持
     * @author  Moyq5
     * @since    2020/11/6 11:55
     */
    private OnPayingListener proxyListener(OnPayingListener preListener) {
        return new OnPayingListener() {

            @Override
            public boolean onPayFinish() {
                boolean paused = preListener.onPayFinish();
                // 如果是交易暂停状态则打开余额查询
                if (paused) {
                    startCardShowerLater();
                }
                return paused;
            }

            @Override
            public void onPayInput(KeyInfo keyInfo) {
                preListener.onPayInput(keyInfo);
            }

            @Override
            public void onPayScan() {
                preListener.onPayScan();
            }

            @Override
            public void onPayCancel() {
                preListener.onPayCancel();
            }
        };
    }

    /**
     * 可能是执行附加卡操作，或者正在播报支付结果语音，约延时4秒再开户读余额
     */
    private void startCardShowerLater() {
        ownSchedule(() -> {
            try {
                if (null != cardPayer) {
                    cardPayer.release();
                    Thread.sleep(100);
                }
                new CardShower(PayIngAbstract.this).bind();
            } catch (InterruptedException  ignored) {

            }

        }, 2, TimeUnit.SECONDS);
    }

    private void analysePayType() {
        // nfc刷卡卡号
        if (ScanCase.NFC.equals(authCase)) {
            payType = PayType.OTHER;
            payMethod = PayMethod.CARD;
            displayLog("刷卡支付...");
            return;
        }

        // 微信刷脸扣款凭证
        if (ScanCase.WX_FACE.equals(authCase)) {
            payType = PayType.WEIXIN;
            payMethod = PayMethod.FACE;
            displayLog("微信刷脸支付...");
            return;
        }

        // 百度刷脸扣款
        if (ScanCase.BD_FACE.equals(authCase)) {
            payType = PayType.OTHER;
            payMethod = PayMethod.FACE;
            displayLog("百度刷脸支付...");
            return;
        }

        payMethod = PayMethod.SCAN;

        int length = authCode.length();

        // 社保码:固定22位纯数字
        if (length == 22 && authCode.matches("^\\d{22}$")) {
            payType = PayType.OTHER;
            return;
        }

        // 支付宝付款码
        if (length >= 16 && length <=24) {
            try {
                int pre = Integer.parseInt(authCode.substring(0, 2));
                if (pre >= 25 && pre <= 30) {
                    payType = PayType.ALIPAY;
                    displayLog("支付宝付款码支付...");
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // 微信付款码
        if (length == 18) {
            try {
                int pre = Integer.parseInt(authCode.substring(0, 2));
                if (pre >= 10 && pre <=15) {
                    payType = PayType.WEIXIN;
                    displayLog("微信付款码支付...");
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // 云闪付、京东付款码
        if (length == 19 && authCode.startsWith("62")) {
            payType = PayType.UNIONPAY;
            displayLog("云闪付付款码支付...");
            return;
        }

        displayLog("普通扫码支付...");

    }

    protected static void reportData(String outTradeNo, String transactionId) {
        PayRecent.instance().setWxReportCount(PayRecent.instance().getWxReportCount() + 1);
        if (null == transactionId) {
            PayRecent.instance().setWxReportError("微信交易单号为空");
            return;
        }
        PayRecent.instance().setLastWxOutTradeNo(outTradeNo);
        PayRecent.instance().setLastWxTransactionId(transactionId);
        Intent intent = new Intent(WxReportReceiver.class.getName());
        intent.putExtra(INTENT_PARAM_WX_OUT_TRADE_NO, outTradeNo);
        intent.putExtra(INTENT_PARAM_WX_TRANSACTION_ID, transactionId);
        localSend(intent);
    }

    public interface OnPayingListener {
        boolean onPayFinish();
        void onPayInput(KeyInfo keyInfo);
        void onPayScan();
        void onPayCancel();
    }

}
