package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.param.CloseOrderRequest;
import com.tencent.cloudpay.service.CloseOrderService;
import com.tencent.cloudpay.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service("close_order")
public class MyCloseOrderService extends CloseOrderService {

    @Override
    public String makeRequestContent(CloseOrderRequest request) {
        InfoManager infoManager = InfoManager.getInstance();
        Map<String, Object> orderClient = infoManager.setOrderClient();
        orderClient.put("sn_code", infoManager.getTerminal().getMachine_no());
        String json = super.makeRequestContent(request);
        MyHashMap data = JsonUtils.fromJson(json, MyHashMap.class);
        data.put("order_client", orderClient);
        //data.put("pay_platform", request.getPay_platform());
        return JsonUtils.toJson(data);

    }

    public static class MyHashMap extends HashMap<String, Object> {

    }
}
