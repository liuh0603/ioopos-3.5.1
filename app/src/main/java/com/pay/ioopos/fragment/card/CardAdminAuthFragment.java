package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardAuthResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.trade.CardRisk;

/**
 * 检查当前设备卡管理权限
 * @author    Moyq5
 * @since  2021/1/14 14:30
 */
public class CardAdminAuthFragment extends TipVerticalFragment implements BindState, Scheduled, KeyInfoListener {

    public CardAdminAuthFragment() {
        super(WAIT, "正在检查权限");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.setKeepScreenOn(true);
        view.requestFocus();
        return view;
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    protected void execute() throws Exception {
        Client<Void, CardAuthResult> client = SanstarApiFactory.cardAuth(ApiUtils.initApi());

        Result<CardAuthResult> apiResult = client.execute(null);

        if (apiResult.getStatus() != Result.Status.OK) {
            dispatch(FAIL, "授权失败", "[" + apiResult.getCode() + "]" + apiResult.getMessage());
            speak("授权失败");
            return;
        }
        CardRisk.setKeyB(apiResult.getData().getKeyB().getBytes());
        setMainFragment(new CardMenuFragment());
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_ENTER:
            case KEY_CANCEL:
            case KEY_MENU:
            case KEY_SEARCH:
                return false;
        }
        return true;
    }
}
