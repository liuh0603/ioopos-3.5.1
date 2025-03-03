package com.pay.ioopos.activity;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.content.Intent;
import android.os.Bundle;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractAmountFragment.OnAmountListener;
import com.pay.ioopos.fragment.RefundAmountFragment;
import com.pay.ioopos.fragment.RefundScanFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.ipay.RefundIngFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 退款
 * @author    Moyq5
 * @since  2020/3/26 19:09
 */
public class RefundActivity extends AbstractActivity implements ScanListener, OnAmountListener {
    private String amount;
    private String code;
    private SettingStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = StoreFactory.settingStore();
        onIntent(getIntent());
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_refund;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onIntent(intent);
    }

    private void onIntent(Intent intent) {
        if (!store.getSwitchRefund()) {
            setMainFragment(new TipVerticalFragment(WARN, "功能关闭，请联系主管"));
            return;
        }
        code = intent.getStringExtra(INTENT_PARAM_CODE);// 来自其它退款入口
        if (intent.hasExtra(INTENT_PARAM_AMOUNT)) {
            amount = intent.getStringExtra(INTENT_PARAM_AMOUNT);
            onAmountFinish(amount);
        } else {
            setMainFragment(new RefundAmountFragment(this));
        }

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_CANCEL || keyInfo == KEY_ENTER) {
            startActivity(new Intent(this, StatisticsActivity.class));
        }
        return true;
    }

    @Override
    public void onAmountFinish(String amount) {
        this.amount = amount;
        if (null == code) {
            showRefundScan();
        } else {
            showRefundIng();
        }
    }

    @Override
    public boolean onScan(Intent intent) {
        code = intent.getStringExtra(INTENT_PARAM_CODE);
        showRefundIng();
        return true;
    }

    private void showRefundScan() {
        setMainFragment(new RefundScanFragment(this));
    }

    private void showRefundIng() {
        if (null == code || code.length() > 32) {
            onError("退款码无效(32)");
            return;
        }
        setMainFragment(new RefundIngFragment(code, amount));
    }

}
