package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 支付宝云支付域名设置
 * @author    Moyq5
 * @since  2020/12/16 9:35
 */
public abstract class AbstractDomainFragment extends AbstractFragment implements KeyInfoListener, ScanListener {

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_domain_list, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        LinearLayout layout = view.findViewById(R.id.domain_list);
        for (int i = 0; i < domains().length; i++) {
            TextView domainView = new TextView(getContext());
            domainView.setTextSize(30);
            domainView.setText( (i + 1) + "." + domains()[i]);
            layout.addView(domainView);
        }

        TextView domainView = new TextView(getContext());
        domainView.setTextSize(30);
        domainView.setText( (domains().length + 1) + ".扫码配置");
        layout.addView(domainView);

        TextView curDomainView = view.findViewById(R.id.cur_comain);
        curDomainView.setText(curDomain());
        
        return view;
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

        int length = domains().length;
        if (num <= 0 || num > length + 1) {
            return false;
        }

        if (num == length + 1) {
            setMainFragment(new DomainScanFragment(this));
        } else {
            setDomain(domains()[num - 1]);
        }
        return true;

    }

    @Override
    public boolean onScan(Intent intent) {
        String content = intent.getStringExtra(INTENT_PARAM_CODE);
        setDomain(content);
        return true;
    }

    private void setDomain(String domain) {
        if (onDomain(domain)) {
            setMainFragment(new TipVerticalFragment(SUCCESS, "域名设置成功"));
        }
    }

    public abstract String[] domains();
    public abstract String curDomain();
    public abstract boolean onDomain(String domain);
}
