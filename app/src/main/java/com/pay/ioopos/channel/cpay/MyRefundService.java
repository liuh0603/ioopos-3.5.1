package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.EncryptType;
import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.param.RefundRequest;
import com.tencent.cloudpay.service.RefundService;
import com.tencent.cloudpay.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service(
        path = "refund",
        encrypt = EncryptType.kAuthen_HMAC_SHA256
)
public class MyRefundService extends RefundService {

    public String makeRequestContent(RefundRequest request) {
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
