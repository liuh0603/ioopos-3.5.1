package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardCancelData;
import com.aggregate.pay.sanstar.bean.CardCancelResult;
import com.aggregate.pay.sanstar.bean.CardCreateData;
import com.aggregate.pay.sanstar.bean.CardCreateResult;
import com.aggregate.pay.sanstar.enums.CardStatus;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.OnCardInfoListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.common.HexUtils;

/**
 * 换卡。卡注销再建卡
 * @author    Moyq5
 * @since  2020/11/16 10:47
 */
public class CardChangeFragment extends TipVerticalFragment implements BindState, Scheduled {
    private byte[] uid;
    private CmdCase scanCase;
    private CardInfo info;  // 实体卡信息
    /**
     * 读旧卡号回调
     */
    private final OnByteListener onUidReadListener = new OnByteListener() {

        @Override
        public Cmd onSuccess(byte[] id) {
            uid = id;
            dispatch(WAIT, "正在获取旧卡信息，卡片请勿离开");
            return CardFactory.cardRead(onCardReadListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("获取旧卡信息：" + msg);
            return null;
        }

    };

    /**
     * 读旧卡信息回调
     */
    private final OnCardInfoListener onCardReadListener = new OnCardInfoListener() {
        @Override
        public Cmd onSuccess(CardInfo info) {
            CardChangeFragment.this.info = info;
            dispatch(WAIT, "正在注销旧卡，卡片请勿离开");
            return CardFactory.cardReset(uid, onCardResetListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("获取旧卡信息：" + msg);
            return null;
        }
    };

    /**
     * 旧卡重置回调
     */
    private final OnCardListener onCardResetListener = new OnCardListener() {
        @Override
        public Cmd onSuccess() {
            if (!serverCancel()) {
                return null;
            }
            dispatch(SUCCESS, "旧卡注销成功");
            try {
                Thread.sleep(1000);
                dispatch(WAIT, "请拍新卡进行建卡");
                speak("请拍新卡");
                return CardFactory.uidWait(onNewUidReadListener, scanCase, uid);
            } catch (InterruptedException ignored) {
            }
            return null;
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("旧卡注销：" + msg);
            return null;
        }
    };

    /**
     * 读新卡号回调
     */
    private final OnByteListener onNewUidReadListener = new OnByteListener() {

        @Override
        public Cmd onSuccess(byte[] id) {
            // 旧卡信息调整后用作新卡信息
            info.setPrevUid(HexUtils.toHexString(uid));
            info.setCardUid(HexUtils.toHexString(id));
            info.setStatus(0);// 默认小于1为锁定状态
            uid = id;

            dispatch(WAIT, "正在建卡，卡片请勿离开");
            return CardFactory.cardInit(info, onCardInitListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("新卡建卡：" + msg);
            return null;
        }

    };

    /**
     * 新卡初始化回调
     */
    private final OnCardListener onCardInitListener = new OnCardListener() {
        @Override
        public Cmd onSuccess() {
            String cardNo = serverCreate();
            if (null == cardNo) {
                return null;
            }
            return CardFactory.cardNoWrite(info.getCardUid(), cardNo, onCardNoWriteListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("新卡建卡：" + msg);
            return null;
        }
    };

    /**
     * 新卡写平台卡号回调
     */
    private final OnCardListener onCardNoWriteListener = new OnCardListener() {

        @Override
        public Cmd onSuccess() {
            return CardFactory.cardActive(uid, onCardActiveListener);
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("新卡更新：" + msg);
            return null;
        }
    };

    /**
     * 新卡激活回调
     */
    private final OnCardListener onCardActiveListener = new OnCardListener() {

        @Override
        public Cmd onSuccess() {
            dispatch(SUCCESS, "建卡成功");
            speak("建卡成功");
            return null;
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail("新卡更新：" + msg);
            return null;
        }
    };

    @Override
    protected void execute() throws Exception {
        dispatch(WAIT, "请拍旧卡进行注销", "如果旧卡已经损坏或者丢失，请在平台上挂失并用新卡发卡");

        scanCase = CardFactory.getCmdCase();
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
     * 平台上卡信息注销
     * @author  Moyq5
     * @since    2020/11/17 13:34
     */
    private boolean serverCancel() {
        CardCancelData apiData = new CardCancelData();
        apiData.setCardNo(info.getCardNo());
        apiData.setCardUid(info.getCardUid());

        Client<CardCancelData, CardCancelResult> client = SanstarApiFactory.cardCancel(ApiUtils.initApi());

        Result<CardCancelResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            dispatchFail("旧卡注销：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 平台上建卡
     * @author  Moyq5
     * @since    2020/11/17 16:12
     */
    private String serverCreate() {

        CardCreateData apiData = new CardCreateData();
        apiData.setBalance(info.getBalance());
        apiData.setCardUid(info.getCardUid());
        apiData.setPrevUid(info.getPrevUid());
        apiData.setStatus(CardStatus.NORMAL);
        apiData.setUserGroup(info.getUser().getUserGroup());
        apiData.setUserName(info.getUser().getUserName());
        apiData.setUserNo(info.getUser().getUserNo());

        Client<CardCreateData, CardCreateResult> client = SanstarApiFactory.cardCreate(ApiUtils.initApi());

        Result<CardCreateResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            dispatchFail("新卡建卡：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return null;
        }
        return apiResult.getData().getCardNo();
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "换卡失败", detail);
        speak("换卡失败");
    }

}
