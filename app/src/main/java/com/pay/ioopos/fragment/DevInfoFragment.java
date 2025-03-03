package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.appVersionName;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.R;
import com.pay.ioopos.common.DeviceUtils;

/**
 * 固件信息
 * @author    Moyq5
 * @since  2020/7/17 11:42
 */
public class DevInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_dev_info, container, false);
        TextView modelView = view.findViewById(R.id.model);
        modelView.setText(android.os.Build.MODEL);
        TextView sysVerView = view.findViewById(R.id.sys_ver);
        sysVerView.setText(android.os.Build.DISPLAY);
        TextView snView = view.findViewById(R.id.sn);
        snView.setText(DeviceUtils.sn());
        TextView imeiView = view.findViewById(R.id.imei);
        imeiView.setText(DeviceUtils.imei());
        TextView appVerView = view.findViewById(R.id.app_ver);
        appVerView.setText(appVersionName());
        return view;
    }

}
