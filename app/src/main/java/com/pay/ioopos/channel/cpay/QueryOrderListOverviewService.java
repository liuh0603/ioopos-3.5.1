package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.Account;
import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.exception.CPayResponseInvalid;
import com.tencent.cloudpay.service.AbstractService;
import com.tencent.cloudpay.utils.JsonUtils;
import com.tencent.cloudpay.utils.StrUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service("query_order_list_overview")
public class QueryOrderListOverviewService extends AbstractService<QueryOrderListOverviewRequest, QueryOrderListOverviewResponse, QueryOrderListOverviewBackendResponse>  {

    @Override
    public QueryOrderListOverviewResponse onSuccess(QueryOrderListOverviewBackendResponse backendResponse) throws CPayResponseInvalid {
        return backendResponse.getQuery_order_list_overview();
    }

    @Override
    public String makeRequestContent(QueryOrderListOverviewRequest request) {
        Account account = InfoManager.getInstance().getAccount();
        Map<String, Object> data = new HashMap();
        data.put("out_sub_mch_id", account.getOut_sub_mch_id());
        if (!StrUtils.isEmptyOrNull(request.getOut_shop_id())) {
            data.put("out_shop_id", request.getOut_shop_id());
        } else {
            data.put("out_shop_id", account.getOut_shop_id());
        }
        if (null != request.getSub_pay_platforms()) {
            data.put("sub_pay_platforms", request.getSub_pay_platforms());
        }
        if (null != request.getOrder_type()) {
            data.put("order_type", request.getOrder_type());
        } else {
            data.put("order_type", 3);
        }
        if (!StrUtils.isEmptyOrNull(request.getStaff_id())) {
            data.put("staff_id", request.getStaff_id());
        } else {
            data.put("staff_id", account.getStaff_id());
        }
        if (null != request.getStart_time()) {
            data.put("start_time", request.getStart_time());
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            data.put("start_time", cal.getTimeInMillis()/1000);
        }
        if (null != request.getEnd_time()) {
            data.put("end_time", request.getEnd_time());
        } else {
            Long startTime = (Long)data.get("start_time");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime * 1000);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            data.put("end_time", cal.getTimeInMillis()/1000);
        }
        return JsonUtils.toJson(data);
    }
}
