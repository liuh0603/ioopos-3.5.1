package com.pay.ioopos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.R;
import com.pay.ioopos.common.DeviceUtils;

/**
 * 网络信息
 * @author    Moyq5
 * @since  2020/4/1 9:33
 */
public class NetInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_net_info, container, false);
        TextView netView = view.findViewById(R.id.net);
        netView.setText(DeviceUtils.net());
        TextView macView = view.findViewById(R.id.mac);
        macView.setText(DeviceUtils.getMac());
        String[] ips = DeviceUtils.ips();
        TextView ipView = view.findViewById(R.id.ip);
        ipView.setText(ips[0]);
        TextView maskView = view.findViewById(R.id.mask);
        maskView.setText(ips[1]);
        TextView dnsView = view.findViewById(R.id.dns);
        dnsView.setText(DeviceUtils.dns());
        return view;

    }

}
