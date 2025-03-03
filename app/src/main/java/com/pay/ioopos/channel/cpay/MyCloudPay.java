package com.pay.ioopos.channel.cpay;

import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;
import com.tencent.cloudpay.CloudPay;
import com.tencent.cloudpay.CoreProcess;
import com.tencent.cloudpay.config.Account;
import com.tencent.cloudpay.config.HttpProxy;
import com.tencent.cloudpay.config.Terminal;
import com.tencent.cloudpay.param.CloseOrderBackendResponse;
import com.tencent.cloudpay.param.CloseOrderRequest;
import com.tencent.cloudpay.param.MicroPayBackendResponse;
import com.tencent.cloudpay.param.MicroPayRequest;
import com.tencent.cloudpay.param.MicroPayResponse;
import com.tencent.cloudpay.param.OfficialAccountPayRequest;
import com.tencent.cloudpay.param.OfficialAccountPayResponse;
import com.tencent.cloudpay.param.QueryOrderBackendResponse;
import com.tencent.cloudpay.param.QueryOrderRequest;
import com.tencent.cloudpay.param.QueryOrderResponse;
import com.tencent.cloudpay.param.QueryRefundBackendResponse;
import com.tencent.cloudpay.param.QueryRefundRequest;
import com.tencent.cloudpay.param.QueryRefundResponse;
import com.tencent.cloudpay.param.RefundBackendResponse;
import com.tencent.cloudpay.param.RefundRequest;
import com.tencent.cloudpay.param.ScanCodePayRequest;

public class MyCloudPay {
    private static MyCloudPay instance = null;
    private final UnbindDeviceService unbindDeviceService = new UnbindDeviceService();
    private final MyMicroPayService microPayService = new MyMicroPayService();
    private final MyQueryOrderService queryOrderService = new MyQueryOrderService();
    private final QueryOrderListOverviewService queryOrderListOverviewService = new QueryOrderListOverviewService();
    private final MyRefundService refundService = new MyRefundService();
    private final MyQueryRefundService queryRefundService = new MyQueryRefundService();
    private final QueryShopInfoService queryShopInfoService = new QueryShopInfoService();
    private final MyCloseOrderService closeOrderService = new MyCloseOrderService();

    static {
        CloudPay.disableLogger();
    }
    public static void init() {
        CpayStore store = StoreFactory.cpayStore();

        Account account = new Account();
        account.setDevice_id(store.getDeviceId());
        account.setOut_mch_id(store.getOutMchId());
        account.setOut_shop_id(store.getOutShopId());
        account.setOut_sub_mch_id(store.getOutSubMchId());
        account.setStaff_id(store.getStaffId());

        String authenKey = store.getAuthenKey();

        Terminal terminal = new Terminal();
        terminal.setMachine_no(DeviceUtils.sn());
        terminal.setSpbill_create_ip("127.0.0.1");
        terminal.setSub_terminal_type(50200);// 选取区间内的一个数即可 [50200,500300)
        terminal.setTerminal_type(2);

        instance = new MyCloudPay(account, authenKey, terminal);
        CloudPay.getInstance().setDomain(store.getServerUrl());
    }
    public static MyCloudPay getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private MyCloudPay(Account account, String authenKey, Terminal terminal) {
        CloudPay.init(account, authenKey, terminal);
    }

    public QueryShopInfoResponse queryShopInfo(QueryShopInfoRequest request) {
        return CoreProcess.process(this.queryShopInfoService, request, QueryShopInfoBackendResponse.class);
    }

    public QueryOrderListOverviewResponse queryOrderListOverview(QueryOrderListOverviewRequest request) {
        return CoreProcess.process(this.queryOrderListOverviewService, request, QueryOrderListOverviewBackendResponse.class);
    }

    public UnbindDeviceResponse unbindDevice(UnbindDeviceRequest request) {
        return CoreProcess.process(this.unbindDeviceService, request, UnbindDeviceBackendResponse.class);
    }

    public MicroPayResponse microPay(MicroPayRequest request) {
        return CoreProcess.process(this.microPayService, request, MicroPayBackendResponse.class);
        //return CloudPay.getInstance().microPay(request);
    }

    public String scanCodePay(ScanCodePayRequest request) {
        return CloudPay.getInstance().scanCodePay(request);
    }

    public OfficialAccountPayResponse officialAccountPay(OfficialAccountPayRequest request) {
        return CloudPay.getInstance().officialAccountPay(request);
    }

    public QueryOrderResponse queryOrder(QueryOrderRequest request) {
        return CoreProcess.process(this.queryOrderService, request, QueryOrderBackendResponse.class);
        //return CloudPay.getInstance().queryOrder(request);
    }

    public Boolean closeOrder(CloseOrderRequest request) {
        return CoreProcess.process(this.closeOrderService, request, CloseOrderBackendResponse.class);
        //return CloudPay.getInstance().closeOrder(request);
    }

    public int refund(RefundRequest request) {
        return CoreProcess.process(this.refundService, request, RefundBackendResponse.class);
        //return CloudPay.getInstance().refund(request);
    }

    public QueryRefundResponse queryRefund(QueryRefundRequest request) {
        return CoreProcess.process(this.queryRefundService, request, QueryRefundBackendResponse.class);
        //return CloudPay.getInstance().queryRefund(request);
    }

    public void setDomain(String domain) {
        CloudPay.getInstance().setDomain(domain);
    }

    public static int ping() {
        return CloudPay.ping();
    }

    public static int ping(String domain) {
        return CloudPay.ping(domain);
    }

    public static void setProxy(HttpProxy proxy) {
        CloudPay.setProxy(proxy);
    }

}
