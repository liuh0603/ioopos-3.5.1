package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

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
 * 扫码绑定设备
 * @author    Moyq5
 * @since  2020/3/30 14:12
 */
public class ShopBindScanFragment extends AbstractScanFragment {

    public ShopBindScanFragment(ScanListener scanListener) {
        super(BarcodeFormat.QR_CODE);
        setListener(scanListener);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TipViewVertical tipView = new TipViewVertical(getContext());
        tipView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        tipView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        tipView.dispatch(WAIT, "请扫码绑定设备");
        return tipView;
    }

    @Override
    protected void execute() throws Exception {
        speak("请扫码绑定设备");
    }
}
