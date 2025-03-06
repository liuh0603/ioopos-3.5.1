package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.App.DEV_IS_FACE;
import static com.pay.ioopos.App.DEV_IS_K12;
import static com.pay.ioopos.App.DEV_IS_MTSCAN;
import static com.pay.ioopos.App.DEV_IS_ZTSCAN;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_GOODS_NAME;
import static com.pay.ioopos.support.scan.weixin.MyWxPayFace.IS_OFFLINE;
import static com.pay.ioopos.trade.PayProcess.PAY_CANCEL;
import static com.pay.ioopos.trade.PayProcess.PAY_FAIL;
import static com.pay.ioopos.trade.PayProcess.PAY_WAIT;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.support.scan.baidu.BdFaceScan;
import com.pay.ioopos.support.scan.cmd.CmdScan;
import com.pay.ioopos.support.scan.mtscan.MTScan;
import com.pay.ioopos.support.scan.s1601.S1601Scan;
import com.pay.ioopos.support.scan.sp308.CameraScan;
import com.pay.ioopos.support.scan.weixin.WxOfflineFaceScan;
import com.pay.ioopos.support.scan.weixin.WxOfflineQrcodeScan;
import com.pay.ioopos.support.scan.weixin.WxOnlineFaceScan;
import com.pay.ioopos.support.scan.weixin.WxOnlineQrcodeScan;
import com.pay.ioopos.support.scan.ztscan.ZTScan;
import com.pay.ioopos.trade.PayProcess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 等待用户支付，扫码、刷Id卡、刷脸
 * @author    Moyq5
 * @since  2020/3/30 12:05
 */
public class PayScanFragment extends TipHorizontalFragment implements BindState, CheckInState, Scheduled, KeyInfoListener {
    private final String amount;
    private final String goodsName;
    private final ScanListener listener;
    public PayScanFragment() {
        super(WAIT, R.string.pay_wait);
        amount = null;
        goodsName = null;
        listener = new ScanListener() {
            @Override
            public boolean onScan(Intent intent) {
                return false;
            }

            @Override
            public void onError(String msg) {

            }
        };
    }

    public PayScanFragment(String amount, String goodsName, ScanListener listener) {
        super(WAIT, R.string.pay_wait);
        this.amount = amount;
        this.goodsName = goodsName;
        this.listener = proxyListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.setKeepScreenOn(true);
        view.requestFocus();
        return view;
    }

    @Override
    protected void execute() throws Exception {
        try {
            if (null == amount || new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            listener.onError("金额错误：" + amount);
            return;
        }

        ScanLife life = new ScanLife(createScanCases());
        life.setScanListener(listener);
        life.bindToLifecycle(this);

        speak("请支付" + amount + "元");
        SerialPortPayUtils.pay(amount, PAY_WAIT);
        getCustomerHolder().setAmount(amount);

        SettingStore store = StoreFactory.settingStore();

        if (DEV_IS_FACE || DEV_IS_BDFACE && store.getSwitchFacePay() && store.getSwitchFaceAutoScan()) {
            getCustomerHolder().payFace();
            return;
        }

        getCustomerHolder().showPayProcess(PAY_WAIT, amount);
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_ENTER:
            case KEY_CANCEL:
            case KEY_MENU:
            case KEY_SEARCH:
                SerialPortPayUtils.pay(amount, PAY_CANCEL);
                return false;
        }
        return true;
    }

    public ScanListener proxyListener(ScanListener listener) {
        return new ScanListener() {

            @Override
            public void onError(String detail) {
                speak("支付失败");
                toast(detail);
                SerialPortPayUtils.pay(null, amount, PAY_FAIL, detail);
                getCustomerHolder().showPayProcess(PayProcess.PAY_FAIL, amount, detail);
                listener.onError("支付失败");
            }

            @Override
            public boolean onScan(Intent intent) {
                intent.putExtra(INTENT_PARAM_AMOUNT, amount);
                intent.putExtra(INTENT_PARAM_GOODS_NAME, goodsName);
                return listener.onScan(intent);
            }
        };
    }

    private List<ScanCase> createScanCases() {
        SettingStore store = StoreFactory.settingStore();

        List<ScanCase> cases = new ArrayList<>();

        // 串口刷卡或者扫码
        int flag = 0;
        if (store.getSwitchNfcPay()) {
            flag |= 1;
        }
        if (store.getSwitchScanPay()) {
            flag |= 2;
        }

        cases.add(new CmdScan(flag));

        // 扫码
        if (store.getSwitchScanPay()) {
            // 外接扫码器s1601
            cases.add(new S1601Scan());
            // 内置相机扫码
            cases.add(new CameraScan());
            if (!DEV_IS_K12 && !DEV_IS_BDFACE) {// 影响刷脸效率, 所以仅限于非306pro的其它设备
                // 微信离线刷脸摄像头扫码
                cases.add(new WxOfflineQrcodeScan());
                // 微信在线刷脸摄像头扫码
                cases.add(new WxOnlineQrcodeScan());
            }
        }

        // 内置政通读卡器
        if (store.getSwitchNfcPay()) {
            if (DEV_IS_ZTSCAN) {
                cases.add(new ZTScan());
            }
            if (DEV_IS_MTSCAN) {
                cases.add(new MTScan());
            }
        }

        // 刷脸
        if (store.getSwitchFacePay()) {
            if (DEV_IS_FACE) {
                // 微信离线刷脸
                WxOfflineFaceScan wxOfflineFaceScan = new WxOfflineFaceScan(amount);
                cases.add(wxOfflineFaceScan);
                // 微信在线刷脸
                WxOnlineFaceScan wxOnlineFaceScan = new WxOnlineFaceScan(amount);
                cases.add(wxOnlineFaceScan);

                getCustomerHolder().setScanFace(IS_OFFLINE ? wxOfflineFaceScan : wxOnlineFaceScan);
            } else if (DEV_IS_BDFACE) {
                BdFaceScan bdFaceScan = new BdFaceScan();
                cases.add(bdFaceScan);
                getCustomerHolder().setScanFace(bdFaceScan);
            }
        }

        return  cases;

    }

}
