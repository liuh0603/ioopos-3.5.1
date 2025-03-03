package com.pay.ioopos.fragment.apay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 支付宝云支付门店信息
 * @author    Moyq5
 * @since  2020/12/15 18:09
 */
public class ShopInfoApayFragment extends AbstractFragment implements BindState {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_shop_info_apay, container, false);
        ApayStore store = StoreFactory.apayStore();
        TextView midView = view.findViewById(R.id.cp_mid);
        midView.setText(store.getMid());
        TextView storeIdView = view.findViewById(R.id.cp_store_id);
        storeIdView.setText(store.getStoreId());
        return view;

    }

}
