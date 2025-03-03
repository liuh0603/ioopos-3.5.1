package com.pay.ioopos.fragment.ipay;

import com.pay.ioopos.fragment.AbstractDomainFragment;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.worker.WorkerFactory;

/**
 * 域名设置
 * @author    Moyq5
 * @since  2020/3/30 11:07
 */
public class DomainListFragment extends AbstractDomainFragment {
    private static final String[] DOMAINS = {"https://open.pay.ioopos.com", /* 有多域名请在这里添加 , */};

    @Override
    public String[] domains() {
        return DOMAINS;
    }

    @Override
    public String curDomain() {
        return StoreFactory.settingStore().getServerUrl();
    }

    @Override
    public boolean onDomain(String domain) {
        StoreFactory.settingStore().setServerUrl(domain);
        WorkerFactory.enqueueSslCertLoadOneTime(true);
        return true;
    }

}
