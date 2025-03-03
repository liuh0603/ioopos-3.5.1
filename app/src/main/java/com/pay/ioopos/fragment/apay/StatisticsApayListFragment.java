package com.pay.ioopos.fragment.apay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.apay.ApayHttp;
import com.pay.ioopos.fragment.AbstractListFragment;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.ApayHelper;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝云支付交易列表
 * @author    Moyq5
 * @since  2020/12/15 13:41
 */
public class StatisticsApayListFragment extends AbstractListFragment {
    private View view;
    private ListView listView;
    private int totalPage = -1;
    private int totalResult = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            listView.requestFocus();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_statistics_apay_list, container, false);
        listView = view.findViewById(R.id.pay_order_list);
        listView.setFocusable(true);
        listView.setFocusableInTouchMode(true);
        listView.requestFocus();
        listView.setOnKeyListener(new ViewKeyListener(this));
        listView.setAdapter(new StatisticsApayAdapter(getContext(), R.layout.fragment_statistics_apay_item, new ArrayList<>()));
        return view;
    }

    @Override
    protected List<Object> loadListData(int nowPage) throws Exception {
        ApayStore store = StoreFactory.apayStore();

        JSONObject bizContent = new JSONObject();
        bizContent.put("cp_mid", store.getMid());
        bizContent.put("page_num", nowPage);
        bizContent.put("terminal_id", DeviceUtils.sn());
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);

        Map<String, Object> map;
        try {
            map = ApayHttp.post("ant.antfin.eco.cloudpay.trade.batchquery", bizContent.toString());
        } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
            throw new LoadFailException("网络异常：" + e.getMessage());
        } catch (InterruptedIOException e) {
            throw new InterruptedException();
        } catch (Exception e) {
            throw new LoadFailException("查询异常：" + e.getMessage());
        }

        String code = (String)map.get("code");
        if (!"10000".equals(code)) {
            throw new LoadFailException("查询失败：["+ code +"]" + map.get("msg"));
        }
        String data = (String)map.get("data");
        map = JSON.toObject(data, new TypeReference<HashMap<String, Object>>(){});
        int total = (Integer)map.get("total");
        totalResult += total;
        if (totalResult == 0) {
            throw new NoRecordException();
        }
        totalPage++;

        return (List<Object>)map.get("data_list");
    }

    @Override
    protected ListView getListView() {
        return listView;
    }

    @Override
    protected int getTotalPage() {
        return totalPage;
    }

    @Override
    protected void back() {
        setMainFragment(new StatisticsMenuApayFragment());
    }

    @Override
    protected void showDetail(Object data) {
        setMainFragment(new StatisticsApayDetailFragment(data, this));
    }

}
