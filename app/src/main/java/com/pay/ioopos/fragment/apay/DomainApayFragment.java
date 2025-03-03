package com.pay.ioopos.fragment.apay;

import com.pay.ioopos.fragment.AbstractDomainFragment;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.worker.WorkerFactory;

/**
 * 支付宝云支付域名设置
 * @author    Moyq5
 * @since  2020/12/16 9:35
 */
public class DomainApayFragment extends AbstractDomainFragment {
    private static final String[] DOMAINS = {"https://ecogateway.alipay-eco.com/gateway.do", /* 有多域名请在这里添加 , */ };

    @Override
    public String[] domains() {
        return DOMAINS;
    }

    @Override
    public String curDomain() {
        return StoreFactory.apayStore().getServerUrl();
    }

    @Override
    public boolean onDomain(String domain) {
        StoreFactory.apayStore().setServerUrl(domain);
        WorkerFactory.enqueueSslCertLoadOneTime(true);
        return true;
    }

}
