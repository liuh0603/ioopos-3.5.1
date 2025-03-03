package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardActiveData;
import com.aggregate.pay.sanstar.bean.CardActiveResult;
import com.aggregate.pay.sanstar.bean.CardInfo;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.CardUser;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.common.HexUtils;

/**
 * 开始建卡
 * @author    Moyq5
 * @since  2020/11/12 17:54
 */
public class CardCreateIngFragment extends TipVerticalFragment implements BindState, Scheduled {

    private final CardInfo cardInfo;
    public CardCreateIngFragment(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    @Override
    protected void execute() throws Exception {
        dispatch(WAIT, "正在发卡，卡片请勿离开");

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            dispatchFail("设备不支持");
            return;
        }

        com.pay.ioopos.channel.card.CardInfo info = new com.pay.ioopos.channel.card.CardInfo();
        info.setStatus(0);// 默认小于1为锁定状态
        info.setMerchNo(StoreFactory.settingStore().getMerchNo());
        info.setCardUid(cardInfo.getCardUid());
        info.setPrevUid(cardInfo.getPrevUid());
        info.setCardNo(cardInfo.getCardNo());
        info.setBalance(cardInfo.getBalance());

        CardUser user = new CardUser();
        user.setUserNo(cardInfo.getUserNo());
        user.setUserName(cardInfo.getUserName());
        user.setUserGroup(cardInfo.getUserGroup());

        info.setUser(user);

        Cmd cardActive = CardFactory.cardActive(HexUtils.toByteArray(cardInfo.getCardUid()), new OnCardListener() {

            @Override
            public Cmd onSuccess() {
                dispatch(SUCCESS, "发卡成功");
                speak("发卡成功");
                return null;
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return null;
            }
        });

        Cmd cardInit = CardFactory.cardInit(info, new OnCardListener() {

            @Override
            public Cmd onSuccess() {
                return serverActive() ? cardActive: null;
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return null;
            }
        });

        scanCase.setRootCmd(cardInit);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    /**
     * 平台上卡信息激活
     * @author  Moyq5
     * @since    2021/1/13 10:02
     */
    private boolean serverActive() {
        CardActiveData apiData = new CardActiveData();
        apiData.setCardUid(cardInfo.getCardUid());
        apiData.setCardNo(cardInfo.getCardNo());
        apiData.setBalance(cardInfo.getBalance());

        Client<CardActiveData, CardActiveResult> client = SanstarApiFactory.cardActive(ApiUtils.initApi());

        Result<CardActiveResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            dispatchFail("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return false;
        }
        return true;
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "发卡失败", detail);
        speak("发卡失败");
    }
}
