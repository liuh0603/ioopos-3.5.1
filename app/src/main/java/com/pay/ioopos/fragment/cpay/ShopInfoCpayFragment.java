package com.pay.ioopos.fragment.cpay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 腾讯云支付门店信息
 * @author    Moyq5
 * @since  2020/7/29 19:30
 */
public class ShopInfoCpayFragment extends AbstractFragment implements BindState {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_shop_info_cpay, container, false);
        CpayStore store = StoreFactory.cpayStore();
        TextView deviceIdView = view.findViewById(R.id.device_id);
        deviceIdView.setText(store.getDeviceId());
        TextView deviceNameView = view.findViewById(R.id.device_name);
        deviceNameView.setText(store.getDeviceName());
        TextView shopIdView = view.findViewById(R.id.shop_id);
        shopIdView.setText(store.getOutShopId());
        TextView shopNameView = view.findViewById(R.id.shop_name);
        shopNameView.setText(store.getShopName());
        TextView staffIdView = view.findViewById(R.id.staff_id);
        staffIdView.setText(store.getStaffId());
        TextView staffNameView = view.findViewById(R.id.staff_name);
        staffNameView.setText(store.getStaffName());
        return view;

    }

}
