package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.common.AppFactory.serverType;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.check.Check;
import com.pay.ioopos.support.check.CheckBind;
import com.pay.ioopos.support.check.CheckBindApay;
import com.pay.ioopos.support.check.CheckBindCpay;
import com.pay.ioopos.support.check.CheckNetwork;
import com.pay.ioopos.support.check.CheckPant;
import com.pay.ioopos.support.check.CheckReport;
import com.pay.ioopos.support.check.CheckReportApay;
import com.pay.ioopos.support.check.CheckReportCpay;
import com.pay.ioopos.support.check.CheckScanFaceOffline;
import com.pay.ioopos.support.check.CheckScanFaceOnline;
import com.pay.ioopos.support.check.CheckScanNfc;
import com.pay.ioopos.support.check.CheckScanQrcode;
import com.pay.ioopos.support.check.CheckSpeech;
import com.pay.ioopos.support.check.CheckVolume;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;

/**
 * 306PRO帮助菜单
 * @author    Moyq5
 * @since  2020/7/13 16:23
 */
public class CheckMenuProFragment extends AbstractFragment implements KeyInfoListener {
    private final Check volume;
    private final Check speech;
    private final Check network;
    private final Check bind;
    private final Check report;
    private final Check wxFace;
    private final Check scanQrcode;
    private final Check scanNfc;
    private final Check pant;
    public CheckMenuProFragment() {
        volume = new CheckVolume();
        speech = new CheckSpeech();
        network = new CheckNetwork();
        bind = serverType() == SERVER_TYPE_C_PAY
                ? new CheckBindCpay(network): serverType() == SERVER_TYPE_A_PAY
                ? new CheckBindApay(network): new CheckBind(network);
        report = serverType() == SERVER_TYPE_C_PAY
                ? new CheckReportCpay(bind) : (serverType() == SERVER_TYPE_A_PAY)
                ? new CheckReportApay(bind) : new CheckReport(bind);
        wxFace = MyWxPayFace.IS_OFFLINE ? new CheckScanFaceOffline(bind): new CheckScanFaceOnline(bind);
        scanQrcode = new CheckScanQrcode();
        scanNfc = new CheckScanNfc();
        pant = new CheckPant(bind);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_menu_pro, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                setMainFragment(new CheckConsoleFragment(network, bind));
                return true;
            case KEY_NUM_2:
                setMainFragment(new CheckConsoleFragment(network));
                return true;
            case KEY_NUM_3:
                setMainFragment(new CheckConsoleFragment(bind));
                return true;
            case KEY_NUM_4:
                setMainFragment(new CheckConsoleFragment(report));
                return true;
            case KEY_NUM_5:
                setMainFragment(new CheckConsoleFragment(wxFace));
                return true;
            case KEY_NUM_6:
                setMainFragment(new CheckConsoleFragment(scanQrcode));
                return true;
            case KEY_NUM_7:
                setMainFragment(new CheckConsoleFragment(scanNfc));
                return true;
            case KEY_NUM_8:
                setMainFragment(new CheckConsoleFragment(volume));
                return true;
            case KEY_NUM_9:
                setMainFragment(new CheckConsoleFragment(speech));
                return true;
            case KEY_NUM_0:
                setMainFragment(new CheckConsoleFragment(pant));
                return true;
        }

        return false;
    }

}
