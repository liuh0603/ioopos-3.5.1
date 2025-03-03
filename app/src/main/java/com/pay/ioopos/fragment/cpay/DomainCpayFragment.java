package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.fragment.AbstractNetworkFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.widget.TipViewHorizontal;
import com.tencent.cloudpay.CloudPay;

import java.util.concurrent.Future;

/**
 * 腾讯云支付域名切换。
 * 开始检查当前域名，按数字检查对应域名，对应的不可用时自动切换下一个域名进行检查
 * @author    Moyq5
 * @since  2020/7/29 14:12
 */
public class DomainCpayFragment extends AbstractNetworkFragment implements KeyInfoListener {
    private static final String[] domains = {"https://pay.qcloud.com", "https://sz.pay.qcloud.com", "https://sh.pay.qcloud.com", "https://tj.pay.qcloud.com"};
    private View view;
    private TipViewHorizontal curDomain;
    private int endIndex = domains.length - 1;
    private Future<?> checkFuture;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_domain_list_cpay, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        String domain = StoreFactory.cpayStore().getServerUrl();
        LinearLayout layout = view.findViewById(R.id.domain_list);
        for (int i = 0; i < domains.length; i++) {
            if (domain.equals(domains[i])) {
                endIndex = i;
            }
            TextView domainView = new TextView(getContext());
            domainView.setTextSize(30);
            domainView.setText( (i + 1) + "." + domains[i]);
            layout.addView(domainView);
        }
        curDomain = view.findViewById(R.id.cur_comain);
        curDomain.dispatch(30f);
        return view;
    }

    @Override
    protected void execute() throws Exception {
        super.execute();
        checkFuture = TaskFactory.submit(this::check);
    }

    @Override
    public boolean useAuth() {
        return true;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        int num;
        try {
            num = Integer.parseInt(keyInfo.getValue());
        } catch (NumberFormatException e) {
            return false;
        }

        if (num <= 0 || num > domains.length) {
            return false;
        }

        if (num > 2) {
            endIndex = num - 2;
        }
        if (null != checkFuture) {
            checkFuture.cancel(true);
        }
        checkFuture = TaskFactory.submit(() -> check(num - 1));
        return true;
    }

    private void check(int index) {
        String domain = domains[index];
        curDomain.dispatch(WAIT, domain);
        try {
            int state = MyCloudPay.ping(domain);
            if (state != 0) {
                throw new Exception();
            }
            StoreFactory.cpayStore().setServerUrl(domain);
            CloudPay.getInstance().setDomain(domain);
            curDomain.dispatch(SUCCESS, domain);
            speak("域名切换成功");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            if (endIndex == index) {
                curDomain.dispatch(FAIL, "无可用域名");
                speak("无可用域名");
                return;
            }
            if ((index + 1) < domains.length) {
                check(index + 1);
            } else {
                check(0);
            }
        }
    }

    private void check() {
        String domain = StoreFactory.cpayStore().getServerUrl();
        int state = MyCloudPay.ping(domain);
        if (state == 0) {
            curDomain.dispatch(SUCCESS, domain);
        } else {
            curDomain.dispatch(FAIL, domain);
            speak("当前域名不可用");
        }
    }

}
