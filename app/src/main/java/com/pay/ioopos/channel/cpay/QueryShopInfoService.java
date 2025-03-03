package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.Account;
import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.exception.CPayResponseInvalid;
import com.tencent.cloudpay.service.AbstractService;
import com.tencent.cloudpay.utils.CommonUtils;
import com.tencent.cloudpay.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service("query_sub_mch_shop_info")
public class QueryShopInfoService extends AbstractService<QueryShopInfoRequest, QueryShopInfoResponse, QueryShopInfoBackendResponse>  {

    @Override
    public QueryShopInfoResponse onSuccess(QueryShopInfoBackendResponse backendResponse) throws CPayResponseInvalid {
        return backendResponse.getQuery_shop_info();
    }

    @Override
    public String makeRequestContent(QueryShopInfoRequest request) {
        Account account = InfoManager.getInstance().getAccount();
        Map<String, Object> data = new HashMap();
        data.put("out_mch_id", account.getOut_mch_id());
        data.put("out_sub_mch_id", account.getOut_sub_mch_id());
        data.put("nonce_str", CommonUtils.generateNonceStr());
        data.put("page_num", request.getPage_num());
        data.put("page_size", request.getPage_size());
        return JsonUtils.toJson(data);
    }
}
