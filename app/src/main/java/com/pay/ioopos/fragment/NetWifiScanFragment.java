package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.widget.TipViewVertical;

/**
 * 扫码配置wifi
 * @author    Moyq5
 * @since  2020/3/30 14:52
 */
public class NetWifiScanFragment extends AbstractScanFragment implements ScanListener {

    public NetWifiScanFragment() {
        super(BarcodeFormat.QR_CODE);
        setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        if (null == context) {
            return null;
        }
        TipViewVertical tipView = new TipViewVertical(getContext());
        tipView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        tipView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        tipView.dispatch(WAIT, "请扫码配置WIFI");
        return tipView;
    }

    @Override
    public boolean onScan(Intent intent) {
        String barcode = intent.getStringExtra(INTENT_PARAM_CODE);
        setMainFragment(new NetWifiApplyFragment(barcode));
        return  true;
    }

}
