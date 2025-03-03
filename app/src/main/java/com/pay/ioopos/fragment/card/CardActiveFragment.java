package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardActiveData;
import com.aggregate.pay.sanstar.bean.CardActiveResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardBase;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.OnCardBaseListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.card.OnCardOrderListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.trade.CardRisk;
import com.pay.ioopos.trade.CardUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡激活、挂失解挂
 * @author    Moyq5
 * @since  2021/1/13 16:33
 */
public class CardActiveFragment extends TipVerticalFragment implements BindState, Scheduled {
    private byte[] uid;// 卡号
    private CardBase base;  // 实体卡信息
    /**
     * 交易数据是否已经上传
     */
    private boolean isUploaded = false;
    /**
     * 读卡号回调
     */
    private final OnByteListener onUidReadListener = new OnByteListener() {

        @Override
        public Cmd onSuccess(byte[] uid) {
            CardActiveFragment.this.uid = uid;
            dispatch(WAIT, "正在解挂，卡片请勿离开");
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
            return serverActive();
        }

        @Override
        public Cmd onFail(String msg) {
            dispatchFail(msg);
            return null;
        }
    };

    /**
     * 激活、解挂回调
     */
    private final OnCardListener onCardActiveListener = new OnCardListener() {

        @Override
        public Cmd onSuccess() {
            dispatch(SUCCESS, "解挂成功");
            speak("解挂成功");
            CardRisk.getLockUidList().remove(base.getCardUid());
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
        dispatch(WAIT, "请拍卡解挂");

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            dispatchFail("设备不支付");
            return;
        }

        Cmd uidWait = CardFactory.uidWait(onUidReadListener, scanCase);

        scanCase.setRootCmd(uidWait);

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
    private Cmd serverActive() {
        CardActiveData apiData = new CardActiveData();
        apiData.setCardUid(base.getCardUid());
        apiData.setCardNo(base.getCardNo());
        apiData.setBalance(base.getBalance());

        Client<CardActiveData, CardActiveResult> client = SanstarApiFactory.cardActive(ApiUtils.initApi());

        Result<CardActiveResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            // 首次后端余额异常，尝试将卡本地流水上传后再试
            if (!isUploaded && "C0208".equals(apiResult.getCode())) {
                isUploaded = true;
                dispatch("正在校验，卡片请勿离开");
                return createOrdersReadCmd(0, new ArrayList<>());
            }
            dispatchFail("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return null;
        }
        return CardFactory.cardActive(uid, onCardActiveListener);
    }

    /**
     * 后台余额异常，尝试上传卡内流水
     * @param sector 流水所在扇区索引
     * @return cmd
     */
    private Cmd createOrdersReadCmd(int sector, List<CardOrder> orders) {
        return CardFactory.orderRead(uid, sector, new OnCardOrderListener() {
            @Override
            public Cmd onSuccess(CardOrder order) {
                if (null != order && order.isSuccess()) {
                    orders.add(order);
                }
                if (sector < SECTOR_ORDERS.length - 1) {
                    return createOrdersReadCmd(sector + 1, orders);
                }
                CardUtils.batchUploadOrders(base, orders);
                return serverActive();

            }

            @Override
            public Cmd onFail(String msg) {
                CardUtils.batchUploadOrders(base, orders);
                return serverActive();
            }
        });
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "解挂失败", detail);
        speak("解挂失败");
    }

}
