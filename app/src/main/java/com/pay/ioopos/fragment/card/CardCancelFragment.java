package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.util.Log;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardCancelData;
import com.aggregate.pay.sanstar.bean.CardCancelResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardBase;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.OnCardBaseListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;

/**
 * 卡注销，重置卡片、后台卡信息注销
 * @author    Moyq5
 * @since  2021/1/6 18:11
 */
public class CardCancelFragment extends TipVerticalFragment implements BindState, Scheduled {
    private static final String TAG = CardCancelFragment.class.getSimpleName();
    private byte[] uid;
    private CardBase base;  // 实体卡信息

    /**
     * 读卡号回调
     */
    private final OnByteListener onUidReadListener = new OnByteListener() {
        @Override
        public Cmd onSuccess(byte[] id) {
            uid = id;
            dispatch(WAIT, "正在获取信息，卡片请勿离开");
            return CardFactory.baseRead(onCardReadListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail(msg);
            return null;
        }
    };

    /**
     * 调卡信息回调
     */
    private final OnCardBaseListener onCardReadListener = new OnCardBaseListener() {
        @Override
        public Cmd onSuccess(CardBase info) {
            base = info;
            dispatch(WAIT, "正在销卡，卡片请勿离开");
            return CardFactory.cardReset(uid, onCardResetListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatch(WAIT, "正在销卡，卡片请勿离开");
            return CardFactory.cardReset(uid, onCardResetListener);
        }
    };

    /**
     * 卡重置回调
     */
    private final OnCardListener onCardResetListener = new OnCardListener() {
        @Override
        public Cmd onSuccess() {
            ownSubmit(CardCancelFragment.this::serverCancel);
            dispatch(SUCCESS, "销卡成功", "卡已恢复出厂状态");
            speak("销卡成功");
            return null;
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail(msg);
            return null;
        }
    };

    @Override
    protected void execute() throws Exception {
        dispatch(WAIT, "请拍卡进行注销", "如果卡已经损坏或者丢失，请在平台上挂失");

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            dispatchFail("设备不支付");
            return;
        }

        Cmd uidWait =  CardFactory.uidWait(onUidReadListener, scanCase);

        scanCase.setRootCmd(uidWait);
        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    /**
     * 平台卡注销
     * @author  Moyq5
     * @since    2020/11/17 13:34
     */
    private void serverCancel() {
        if (null == base) {
            return;
        }
        CardCancelData apiData = new CardCancelData();
        apiData.setCardNo(base.getCardNo());
        apiData.setCardUid(base.getCardUid());

        Client<CardCancelData, CardCancelResult> client = SanstarApiFactory.cardCancel(ApiUtils.initApi());

        Result<CardCancelResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            Log.d(TAG, "销卡失败: [" + apiResult.getCode() + "]" + apiResult.getMessage());
            toast("[" + apiResult.getCode() + "]" + apiResult.getMessage());
        }
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "销卡失败", detail);
        speak("销卡失败");
    }

}
