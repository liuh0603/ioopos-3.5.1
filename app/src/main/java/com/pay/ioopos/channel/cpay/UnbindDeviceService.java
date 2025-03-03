package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.config.Account;
import com.tencent.cloudpay.config.InfoManager;
import com.tencent.cloudpay.config.Service;
import com.tencent.cloudpay.config.Terminal;
import com.tencent.cloudpay.exception.CPayResponseInvalid;
import com.tencent.cloudpay.service.AbstractService;
import com.tencent.cloudpay.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service("unbind_device_sn_code")
public class UnbindDeviceService extends AbstractService<UnbindDeviceRequest, UnbindDeviceResponse, UnbindDeviceBackendResponse>  {

    @Override
    public UnbindDeviceResponse onSuccess(UnbindDeviceBackendResponse backendResponse) throws CPayResponseInvalid {
        return new UnbindDeviceResponse();
    }

    @Override
    public String makeRequestContent(UnbindDeviceRequest unbindDeviceRequest) {
        Account account = InfoManager.getInstance().getAccount();
        Terminal terminal = InfoManager.getInstance().getTerminal();
        Map<String, Object> data = new HashMap();
        data.put("out_sub_mch_id", account.getOut_sub_mch_id());
        data.put("out_shop_id", account.getOut_shop_id());
        data.put("device_id", account.getDevice_id());
        data.put("sn_code", terminal.getMachine_no());
        return JsonUtils.toJson(data);
    }
}
