package com.pay.ioopos.fragment.ipay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.AbstractListFragment;
import com.pay.ioopos.keyboard.ViewKeyListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收款记录
 * @author    Moyq5
 * @since  2020/3/30 14:18
 */
public class StatisticsPayListFragment extends AbstractListFragment {
    private View view;
    private ListView listView;
    private Client<String, String> client;
    private int totalPage = -1;
    private int totalResult = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            listView.requestFocus();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_statistics_pay_list, container, false);
        listView = view.findViewById(R.id.pay_order_list);
        listView.setOnKeyListener(new ViewKeyListener(this));
        listView.setAdapter(new StatisticsPayAdapter(getContext(), R.layout.fragment_statistics_pay_item, new ArrayList<>()));
        client = SanstarApiFactory.statisticsPay(ApiUtils.initApi());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected List<Object> loadListData(int nowPage) throws Exception {
        Result<String> apiResult = client.execute("{ \"basicSearch\": {\"nowPage\": "+ nowPage +", \"totalResult\": "+ totalResult +"}}");
        if (apiResult.getStatus() != Result.Status.OK) {
            if ("C9997".equals(apiResult.getCode())) {// InterruptedIOException
                throw new InterruptedException();
            }
            throw new LoadFailException("[" + apiResult.getCode() + "]" + apiResult.getMessage());
        }
        String page = apiResult.getData();
        Map<String, Object> map = JSON.toObject(page, new TypeReference<HashMap<String, Object>>() {});
        Map<String, Object> paging = (Map<String, Object>)map.get("paging");
        totalPage = Integer.parseInt(String.valueOf(paging.get("totalPage")));
        totalResult = Integer.parseInt(String.valueOf(paging.get("totalResult")));
        if (totalResult == 0) {
            throw new NoRecordException();
        }
        listView.postDelayed(() -> {
            listView.setFocusable(true);
            listView.setFocusableInTouchMode(true);
            listView.requestFocus();
        }, 100);
        return (List<Object>)map.get("list");
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
        setMainFragment(new StatisticsMenuFragment());
    }

    @Override
    protected void showDetail(Object data) {
        setMainFragment(new StatisticsPayDetailFragment(data, this));
    }

}
