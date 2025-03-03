package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.CardShower;
import com.pay.ioopos.widget.Tip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 展示卡信息，并支持循环拍卡获取所拍卡信息
 * @author    Moyq5
 * @since  2020/11/9 18:34
 */
public class CardInfoFragment extends AbstractFragment implements KeyInfoListener {
    private View view;
    private ListView listView;
    private ArrayAdapter<CardOrder> adapter;
    private CardInfo info;
    private final CardOrder order;
    public CardInfoFragment(CardInfo info, CardOrder order) {
        this.info = info;
        this.order = order;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_card_info, container, false);
        adapter = new CardInfoAdapter(getContext(), R.layout.fragment_card_info_adapter, new ArrayList<>());
        listView = view.findViewById(R.id.pay_order_list);
        listView.setAdapter(adapter);

        showCardInfo(info, order);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getCustomerHolder().showWelcome();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        CardShower shower = new CardShower(this);
        shower.setListener(new CardShower.OnCardShowListener() {
            @Override
            public void onShow(CardInfo info, CardOrder order) {
                CardInfoFragment.this.info = info;
                getCustomerHolder().showCard(info, order);
                showCardInfo(info, order);
            }

            @Override
            public void onShow(List<CardOrder> orders) {
                getCustomerHolder().showCard(orders);
                showOrders(orders);
                // 上报日志
                TaskFactory.submit(() -> {
                    String infoJson = JSON.toString(info);
                    String ordersJson = JSON.toString(orders);
                    LogUtils.info("info = %s ; orders = %s", infoJson, ordersJson);
                });
            }

            @Override
            public Cmd onFail(String msg) {
                getCustomerHolder().showMsg(Tip.TipType.FAIL,"刷卡失败", msg);
                speak("刷卡失败");
                toast(msg);
                return null;
            }
        });
        shower.setUid(HexUtils.toByteArray(info.getCardUid()));
        shower.bind();

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KeyInfo.KEY_UP) {
            listView.onKeyDown(KeyEvent.KEYCODE_DPAD_UP, new KeyEvent(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN));
            return true;
        } else if (keyInfo == KeyInfo.KEY_DOWN) {
            listView.onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, new KeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.ACTION_DOWN));
            return true;
        }
        return false;
    }

    private void showCardInfo(CardInfo info, CardOrder order) {
        uiExecute(() -> {
            listView.setFocusable(false);
            listView.setFocusableInTouchMode(false);
            listView.requestFocus();
            listView.setOnKeyListener(null);

            view.findViewById(R.id.info).setVisibility(View.VISIBLE);
            view.findViewById(R.id.list).setVisibility(View.INVISIBLE);

            TextView cardNoView = view.findViewById(R.id.card_uid);
            cardNoView.setText(info.getCardUid());
            TextView userNameView = view.findViewById(R.id.user_name);
            userNameView.setText(info.getUser().getUserName());
            TextView userNoView = view.findViewById(R.id.user_no);
            userNoView.setText(info.getUser().getUserNo());
            TextView userGroupView = view.findViewById(R.id.user_group);
            userGroupView.setText(info.getUser().getUserGroup());
            TextView balanceView = view.findViewById(R.id.balance);
            balanceView.setText(BigDecimalUtils.fenToYuan(info.getBalance()).toPlainString());

            TextView amountView = view.findViewById(R.id.last_amount);
            TextView timeView = view.findViewById(R.id.last_time);
            if (null != order) {
                amountView.setText(BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString());
                timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(order.getOrderTime() * 1000)));
            } else {
                amountView.setText("(无)");
                timeView.setText("(无)");
            }

        });
    }

    private void showOrders(List<CardOrder> orders) {

        if (null == orders) {
            toast("交易查询：没有记录");
            return;
        }

        Object[] array = orders.stream().filter(CardOrder::isSuccess).sorted((o1, o2) -> o1.getOrderTime() > o2.getOrderTime() ? -1: 0).limit(10).toArray();

        if (array.length == 0) {
            toast("交易查询：没有记录");
            return;
        }

        uiExecute(() -> {
            listView.setFocusable(true);
            listView.setFocusableInTouchMode(true);
            listView.requestFocus();
            listView.setOnKeyListener(new ViewKeyListener(this));

            view.findViewById(R.id.info).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.list).setVisibility(View.VISIBLE);
            adapter.clear();
            adapter.addAll((CardOrder[]) array);
            listView.setAdapter(adapter);
        });
    }
}
