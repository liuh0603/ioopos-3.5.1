package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalInfoResult;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.Merch;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.AbstractNetworkFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

/**
 * 门店信息
 * @author    Moyq5
 * @since  2020/3/30 14:12
 */
public class ShopInfoFragment extends AbstractNetworkFragment implements BindState, CheckInState {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return view = inflater.inflate(R.layout.fragment_shop_info, container, false);
    }

    @Override
    protected void execute() throws Exception {
        SettingStore store = StoreFactory.settingStore();

        // 新版本支持：3.1.0以后已经支持在绑定的时候返回商户、门店名称等信息
        if (null != store.getMerchName() && !store.getMerchName().isEmpty()) {
            uiExecute(() -> {
                TextView merchName = view.findViewById(R.id.merch_name);
                merchName.setText(store.getMerchName());
                TextView merchNo = view.findViewById(R.id.merch_no);
                merchNo.setText(store.getMerchNo());
                TextView shopName = view.findViewById(R.id.shop_name);
                shopName.setText(store.getShopName());
                TextView shopSn = view.findViewById(R.id.shop_sn);
                shopSn.setText(String.valueOf(store.getShopNo()));
                TextView terminalSn = view.findViewById(R.id.terminal_sn);
                terminalSn.setText(String.valueOf(store.getTerminalNo()));
                TextView sn = view.findViewById(R.id.sn);
                sn.setText(DeviceUtils.sn());
            });
            return;
        }

        // 兼容旧版本：通过接口获取绑定信息

        Merch merch = ApiUtils.initApi();

        Client<Void, TerminalInfoResult> client = SanstarApiFactory.terminalInfo(merch);

        Result<TerminalInfoResult> apiResult = client.execute(null);

        if (apiResult.getStatus() != Result.Status.OK) {
            onError("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        TerminalInfoResult info = apiResult.getData();
        uiExecute(() -> {
            TextView merchName = view.findViewById(R.id.merch_name);
            merchName.setText(info.getMerchName());
            TextView merchNo = view.findViewById(R.id.merch_no);
            merchNo.setText(info.getMerchNo());
            TextView shopName = view.findViewById(R.id.shop_name);
            shopName.setText(info.getShopName());
            TextView shopSn = view.findViewById(R.id.shop_sn);
            shopSn.setText(String.valueOf(info.getShopNo()));
            TextView terminalSn = view.findViewById(R.id.terminal_sn);
            terminalSn.setText(String.valueOf(info.getTerminalNo()));
            TextView sn = view.findViewById(R.id.sn);
            sn.setText(DeviceUtils.sn());
        });
    }

}
