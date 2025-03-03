package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.SettingActivity;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.fragment.AbstractAmountFragment;
import com.pay.ioopos.fragment.AbstractAmountFragment.OnAmountListener;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.trade.CardShower;
import com.pay.ioopos.widget.Tip;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 卡管理菜单，当前界面可直接拍卡查询卡信息
 * @author    Moyq5
 * @since  2020/11/9 14:53
 */
public class CardMenuFragment extends AbstractFragment implements Scheduled, KeyInfoListener, OnAmountListener {
    private View view;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_card_menu, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        CardShower shower = new CardShower(this);
        shower.setListener(new CardShower.OnCardShowListener() {
            @Override
            public void onShow(CardInfo info, CardOrder order) {
                getCustomerHolder().showCard(info, order);
                setMainFragment(new CardInfoFragment(info, order));
            }

            @Override
            public void onShow(List<CardOrder> orders) {
            }

            @Override
            public Cmd onFail(String msg) {
                getCustomerHolder().showMsg(Tip.TipType.FAIL, "刷卡失败", msg);
                speak("刷卡失败");
                toast(msg);
                return null;
            }
        });
        shower.bind();

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
        case KEY_NUM_1:
            AbstractAmountFragment fragment = new CardChargeAmountFragment(this);
            //fragment.setForceAmount(true);
            setMainFragment(fragment);
            return true;
        case KEY_NUM_2:
            setMainFragment(new CardCreateScanFragment());
            return true;
        case KEY_NUM_3:
            setMainFragment(new CardActiveFragment());
            return true;
        case KEY_NUM_4:
            setMainFragment(new CardChangeFragment());
            return true;
        case KEY_NUM_5:
            setMainFragment(new CardCancelFragment());
            return true;
        case KEY_NUM_6:
            setMainFragment(new CardUidFragment());
            return true;
        case KEY_NUM_7:
            setMainFragment(new CardAdminSetFragment());
            return true;
        case KEY_NUM_8:
            startActivity(new Intent(App.getInstance(), SettingActivity.class));
            return true;
        }

        return true;
    }

    @Override
    public void onAmountFinish(String amount) {
        setMainFragment(new CardChargeScanFragment(amount));
    }
}
