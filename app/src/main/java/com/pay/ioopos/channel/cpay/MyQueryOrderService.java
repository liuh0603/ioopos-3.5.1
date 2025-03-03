package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.param.QueryOrderRequest;
import com.tencent.cloudpay.service.QueryOrderService;
import com.tencent.cloudpay.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service("query_order")
public class MyQueryOrderService extends QueryOrderService {

    public String makeRequestContent(QueryOrderRequest request) {
        InfoManager infoManager = InfoManager.getInstance();
        Map<String, Object> orderClient = infoManager.setOrderClient();
        orderClient.put("sn_code", infoManager.getTerminal().getMachine_no());
        String json = super.makeRequestContent(request);
        MyHashMap data = JsonUtils.fromJson(json, MyHashMap.class);
        data.put("order_client", orderClient);
        return JsonUtils.toJson(data);
    }

    public static class MyHashMap extends HashMap<String, Object> {

    }
}
