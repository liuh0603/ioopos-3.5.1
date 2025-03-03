package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardQueryData;
import com.aggregate.pay.sanstar.bean.CardQueryResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnStringListener;

/**
 * 刷卡获取建卡信息。即建卡前需要获取待建卡或者原卡(换卡的场景)信息
 * @author    Moyq5
 * @since  2020/11/10 18:47
 */
public class CardCreateScanFragment extends TipVerticalFragment implements BindState, Scheduled {

    @Override
    protected void execute() throws Exception {
        dispatch(WAIT, "请拍卡获取发卡信息");

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            dispatchFail("设备不支持");
        }

        Cmd uidWait = CardFactory.uidWait(new OnStringListener() {

            @Override
            public Cmd onSuccess(String uid) {
                dispatch(WAIT, "正在获取发卡信息");
                ownSubmit(() -> cardQuery(uid));
                return null;
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return null;
            }

        }, scanCase);

        scanCase.setRootCmd(uidWait);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    /**
     * 获取平台上建卡信息
     * @author  Moyq5
     * @since    2021/1/13 10:17
     * @param
     * @return
     */
    private void cardQuery(String cardUid) {
        CardQueryData apiData = new CardQueryData();
        apiData.setCardUid(cardUid);

        Client<CardQueryData, CardQueryResult> client = SanstarApiFactory.cardCreateQuery(ApiUtils.initApi());

        Result<CardQueryResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            dispatchFail("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        setMainFragment(new CardCreateInfoFragment(cardUid, apiResult.getData()));
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "发卡失败", detail);
        speak("发卡失败");
    }
}
