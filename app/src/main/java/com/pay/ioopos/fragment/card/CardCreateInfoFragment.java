package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggregate.pay.sanstar.bean.CardInfo;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.AbstractTipFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnStringListener;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.StringUtils;

/**
 * 换卡、建卡卡信息确认
 * @author    Moyq5
 * @since  2020/11/9 18:34
 */
public class CardCreateInfoFragment extends AbstractFragment implements Scheduled {

    private final CardInfo cardInfo;
    private final String queryUid;
    private Cmd uidWait;
    private String newUid;// 指定发卡的新卡号
    public CardCreateInfoFragment(String queryUid, CardInfo cardInfo) {
        this.queryUid = queryUid;
        this.cardInfo = cardInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_card_create_info, container, false);
        String typeText;
        String uid = queryUid + "(查)";
        if (!StringUtils.isEmpty(cardInfo.getCardUid())) {
            newUid = cardInfo.getCardUid();
            uid = newUid + "(新)";
            typeText = "“指定新卡号”";
        } else if (!StringUtils.isEmpty(cardInfo.getPrevUid())) {
            uid = cardInfo.getPrevUid() + "(旧)";
            typeText = "“指定旧卡号”";
        } else {
            typeText = "“未指定卡号”";
        }
        TextView descrView = view.findViewById(R.id.type_descr);
        descrView.setText("来自" + typeText + "的发卡信息：");
        TextView cardNoView = view.findViewById(R.id.card_uid);
        cardNoView.setText(uid);
        TextView userNameView = view.findViewById(R.id.user_name);
        userNameView.setText(cardInfo.getUserName());
        TextView userNoView = view.findViewById(R.id.user_no);
        userNoView.setText(cardInfo.getUserNo());
        TextView userGroupView = view.findViewById(R.id.user_group);
        userGroupView.setText(cardInfo.getUserGroup());
        TextView balaceView = view.findViewById(R.id.balance);
        balaceView.setText(BigDecimalUtils.fenToYuan(cardInfo.getBalance()).toPlainString());

        bindCmdCaseToLifecycle();

        return view;
    }

    private void bindCmdCaseToLifecycle() {
        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            return;
        }

        uidWait = CardFactory.uidWait(new OnStringListener() {

            @Override
            public Cmd onSuccess(String uid) {
                if (null != newUid && !uid.equalsIgnoreCase(newUid)) {
                    toast("请拍指定卡发卡：" + newUid);
                    speak("请拍指定卡发卡");
                    return uidWait;
                }
                cardInfo.setCardUid(uid);
                setMainFragment(new CardCreateIngFragment(cardInfo));
                return null;
            }

            @Override
            public Cmd onFail(String msg) {
                AbstractTipFragment fragment = new TipVerticalFragment();
                fragment.dispatch(FAIL, "发卡失败", msg);
                setMainFragment(fragment);
                speak("发卡失败");
                return null;
            }

        }, scanCase);

        scanCase.setRootCmd(uidWait);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }
}
