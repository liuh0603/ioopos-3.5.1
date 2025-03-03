package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.App.DEV_IS_K12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.pay.ioopos.R;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.support.scan.cmd.CmdScan;
import com.pay.ioopos.support.scan.s1601.S1601Scan;
import com.pay.ioopos.support.scan.sp308.CameraScan;
import com.pay.ioopos.support.scan.weixin.WxOfflineQrcodeScan;
import com.pay.ioopos.support.scan.weixin.WxOnlineQrcodeScan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class AbstractScanFragment extends AbstractNetworkFragment implements Scheduled {
    private ScanListener listener;
    private final BarcodeFormat format;
    private Future<?> future;
    public AbstractScanFragment() {
        this(null);
    }
    public AbstractScanFragment(BarcodeFormat format) {
        this.format = format;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (null != future) {
            future.cancel(true);
        }
        future = ownSubmit(() -> {
            try {
                bindToLifecycle();
            } catch (Throwable e) {
                if (null != listener) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    public void setListener(ScanListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean useNetwork() {
        return false;
    }

    private void bindToLifecycle() {
        List<ScanCase> cases = new ArrayList<>();

        // 外接扫码器s1601
        cases.add(new S1601Scan());
        // 内置串口扫码
        cases.add(new CmdScan(2));
        // 内置相机扫码
        cases.add(new CameraScan(format));

        if (!DEV_IS_K12 && !DEV_IS_BDFACE) {
            // 内置微信刷脸摄像头扫码
            cases.add(new WxOfflineQrcodeScan());
            cases.add(new WxOnlineQrcodeScan());
        }

        ScanCase scanCase = new ScanLife(cases);
        scanCase.setScanListener(listener);
        scanCase.bindToLifecycle(this);
    }

}
