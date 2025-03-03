package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;
import static com.pay.ioopos.channel.card.CardUtils.crc;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;
import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardChargeConfirmData;
import com.aggregate.pay.sanstar.bean.CardChargeConfirmResult;
import com.aggregate.pay.sanstar.bean.CardChargeData;
import com.aggregate.pay.sanstar.bean.CardChargeResult;
import com.aggregate.pay.sanstar.enums.CardTradeStatus;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.card.CardBase;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.CardFactory.OnCardOrderUpdateListener;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.CardStat;
import com.pay.ioopos.channel.card.OnCardBaseListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.card.OnCardOrderListener;
import com.pay.ioopos.channel.card.OnCardStatListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.CardRisk;
import com.pay.ioopos.trade.CardUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 读待充值卡信息并充值
 * @author    Moyq5
 * @since  2020/11/9 16:51
 */
public class CardChargeScanFragment extends TipVerticalFragment implements BindState, Scheduled, View.OnKeyListener {
    /**
     * 是否现金充值
     */
    private final boolean isCashCharge;
    /**
     * 发起充值(指令)
     */
    private Cmd uidWait;
    /**
     * 充值金额，元
     */
    private String yuan;
    /**
     * 充值金额，分
     */
    private Integer fen;
    /**
     * 充值卡号
     */
    private byte[] uid;
    /**
     * 实体卡信息
     */
    private CardBase base;
    /**
     * 最近交易
     */
    private CardOrder order;
    /**
     * 交易统计
     */
    private CardStat stat;
    /**
     * 交易数据是否已经上传
     */
    private boolean isUploaded = false;
    /**
     * 预充值信息
     */
    private CardChargeResult chargeInfo;

    private Future<?> delayFuture;

    public CardChargeScanFragment(String yuan) {
        if (null == yuan || yuan.isEmpty()) {
            isCashCharge = false;
        } else {
            this.yuan = yuan;
            this.fen = BigDecimalUtils.yuanToFen(new BigDecimal(yuan));
            isCashCharge = true;
        }
    }

    public CardChargeScanFragment() {
        isCashCharge = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (null == view) {
            return null;
        }
        view.setFocusable(true);
        view.setOnKeyListener(this);
        view.setKeepScreenOn(true);
        view.requestFocus();
        return view;
    }

    @Override
    protected void execute() throws Exception {

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            dispatch(FAIL, "充值失败", "设备不支持");
            return;
        }

        if (isCashCharge) {
            dispatch(WAIT, "请拍卡充值", "即将充值"+ yuan +"元");
        } else {
            super.dispatch(WARN, "...预充充值挂机中...", "可直接拍卡根据预充信息充值");
        }

        uidWait = CardFactory.uidWait(new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] uid) {
                CardChargeScanFragment.this.uid = uid;
                dispatch(WAIT, "正在校验，卡片请勿离开");
                return createBaseReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        }, scanCase);

        scanCase.setRootCmd(uidWait);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }

    /**
     * 1.查询卡基本信息
     * @return cmd
     */
    private Cmd createBaseReadCmd() {
        return CardFactory.baseRead(new OnCardBaseListener() {
            @Override
            public Cmd onSuccess(CardBase base) {
                CardChargeScanFragment.this.base = base;
                if (isCashCharge && CardChargeScanFragment.this.base.getBalance() + fen > CardRisk.getLimitMaxBalance()) {
                    dispatchFail("卡充值额度受限");
                    return null;
                }
                return createOrderReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        });
    }

    /**
     * 2.查询最近交易
     * @return cmd
     */
    private Cmd createOrderReadCmd() {
        return CardFactory.orderRead(uid, 0, new OnCardOrderListener() {
            @Override
            public Cmd onSuccess(CardOrder order) {
                CardChargeScanFragment.this.order = order;
                return createOrderUpdateCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        });
    }

    /**
     * 3.更新订单状态
     * @return cmd
     */
    private Cmd createOrderUpdateCmd() {
        return CardFactory.orderUpdateByBalance(base, order, new OnCardOrderUpdateListener() {
            @Override
            public Cmd onNormal() {
                return createStatReadCmd();
            }

            @Override
            public Cmd onUpdate(CardOrder updatedOrder) {
                order = updatedOrder;
                return createStatReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        });
    }

    /**
     * 4.查询统计信息
     * @return cmd
     */
    private Cmd createStatReadCmd() {
        return CardFactory.statRead(uid, new OnCardStatListener() {
            @Override
            public Cmd onSuccess(CardStat stat) {
                CardChargeScanFragment.this.stat = stat;
                return createOrderMoveCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        });
    }

    /**
     * 5.检查并更新统计
     * @return cmd
     */
    private Cmd createOrderMoveCmd() {
        // 新卡，无交易的情况
        if (null == order || null == stat) {
            return serverCharge();
        }

        // 上次执行不完整，或者最近交易没统计进去

        if (crc(stat, order) != stat.getCrc() || (order.isSuccess() && order.getOrderTime() > stat.getLastTime())) {
            return CardFactory.statUpdateByOrder(uid, order, stat, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return createStatReadCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    dispatchFail(msg);
                    return resetCmd();
                }
            });
        }

        // 一般正常情况
        if (base.getBalance() != stat.getBalance()) {
            dispatchFail("卡余额异常，不允许充值");
            LogUtils.warn("充值校验：卡余额异常，%s => %d<?>%d", base.getCardNo(), base.getBalance(), stat.getBalance());
            return CardFactory.cardLock(uid, "余额异常锁卡", new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return resetCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return resetCmd();
                }
            });
        }
        return serverCharge();
    }

    /**
     * 6.向后台进行预充
     * @return cmd
     */
    private Cmd serverCharge() {
        dispatch("正在充值，卡片请勿离开");
        CardChargeData apiData = new CardChargeData();
        apiData.setCardUid(base.getCardUid());
        apiData.setCardNo(base.getCardNo());
        apiData.setAmount(fen);
        apiData.setBalance(base.getBalance());
        apiData.setBizNo(base.getCardUid() + System.currentTimeMillis());
        apiData.setBizTime(new Date());
        apiData.setRemark(BigDecimalUtils.fenToYuan(base.getBalance()).toPlainString());

        Client<CardChargeData, CardChargeResult> client = SanstarApiFactory.cardCharge(ApiUtils.initApi());

        Result<CardChargeResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            // 首次后端余额异常，尝试将卡本地流水上传后再试
            if (!isUploaded && "C0208".equals(apiResult.getCode())) {
                isUploaded = true;
                dispatch("正在校验，卡片请勿离开");
                return createOrdersReadCmd(0, new ArrayList<>());
            }
            dispatchFail("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return resetCmd();
        }
        chargeInfo = apiResult.getData();
        // 提交的金额和返回的金额不一致
        if (null == chargeInfo || (null != fen && fen.intValue() != chargeInfo.getAmount())) {
            dispatchFail("金额有误，未知原因");
            return resetCmd();
        }
        fen = chargeInfo.getAmount();
        yuan = BigDecimalUtils.fenToYuan(fen).toPlainString();
        return createChargeLockCmd();
    }

    /**
     * 7.充值前锁卡
     * @return cmd
     */
    private Cmd createChargeLockCmd() {
        return CardFactory.cardLock(uid, "充值前锁卡", new OnCardListener() {
            @Override
            public Cmd onSuccess() {
                return createChargeCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                dispatchFail(msg);
                return resetCmd();
            }
        });
    }


    /**
     * 8.后台余额异常，尝试上传卡内流水
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
                return serverCharge();
            }

            @Override
            public Cmd onFail(String msg) {
                return serverCharge();
            }
        });
    }

    /**
     * 7.充值
     * @return cmd
     */
    private Cmd createChargeCmd() {
        return CardFactory.charge(uid, fen, new OnCardListener() {
            @Override
            public Cmd onSuccess() {
                return createBalanceReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                return createBalanceReadCmd();
            }
        });
    }

    /**
     * 9.充值后余额确认
     * @return cmd
     */
    private Cmd createBalanceReadCmd() {
        return CardFactory.balanceRead(uid, new OnIntListener() {
            @Override
            public Cmd onSuccess(int balance) {
                if (base.getBalance() == balance) {
                    LogUtils.info("充值后卡余额没变化：%s", base.getCardNo());
                    dispatchFail("余额未变化");
                    serverConfirm(false);
                    return resetCmd();
                }
                if (base.getBalance() + fen != balance) {
                    LogUtils.info("充值后卡余额异常，卡号：%s，充值前：%d，充值：%d，充值后：%d", base.getCardNo(), base.getBalance(), fen, balance);
                    dispatchError("余额异常");
                    return resetCmd();
                }
                if (!serverConfirm(true)) {
                    dispatchError("后台同步异常，请确认");
                    return resetCmd();
                }
                base.setBalance(balance);
                return createUpdateStatCmd(new int[]{0});
            }

            @Override
            public Cmd onFail(String msg) {
                return createBalanceReadCmd();
            }
        });
    }


    /**
     * 10.向后台充值确认
     */
    private boolean serverConfirm(boolean isSuccess) {

        CardChargeConfirmData apiData = new CardChargeConfirmData();
        apiData.setCardUid(base.getCardUid());
        apiData.setCardNo(base.getCardNo());
        apiData.setAmount(chargeInfo.getAmount());
        apiData.setBizNo(chargeInfo.getBizNo());
        apiData.setTradeNo(chargeInfo.getTradeNo());
        apiData.setStatus(isSuccess ? CardTradeStatus.SUCCESS : CardTradeStatus.FAIL);

        Client<CardChargeConfirmData, CardChargeConfirmResult> client = SanstarApiFactory.cardChargeConfirm(ApiUtils.initApi());

        Result<CardChargeConfirmResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            toast("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 11.更新统计（余额备份）
     * @return cmd
     */
    private Cmd createUpdateStatCmd(int[] failTimes) {
        stat.setBalance(base.getBalance());
        stat.setLastTime(System.currentTimeMillis()/1000);
        stat.setCrc(crc(stat, order));
        return CardFactory.statWrite(uid, stat, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                failTimes[0]++;
                if (failTimes[0] > 2) {// 防止死循环
                    return createActiveCmd();
                }
                return createUpdateStatCmd(failTimes);
            }

            @Override
            public Cmd onSuccess() {
                return createActiveCmd();
            }
        });
    }

    /**
     * 12.卡解锁
     * @return cmd
     */
    private Cmd createActiveCmd() {
        return CardFactory.cardActive(uid, new OnCardListener() {
            @Override
            public Cmd onSuccess() {
                String amount = BigDecimalUtils.fenToYuan(base.getBalance()).toPlainString();
                getCustomerHolder().showMsg(SUCCESS,"余额：" + amount + "元", null);
                dispatch(SUCCESS, "充值成功，余额：" + amount + "元");
                speak("充值成功");
                return resetCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                onSuccess();
                return resetCmd();
            }
        });
    }

    private void dispatchFail(String detail) {
        dispatch(FAIL, "充值失败", detail);
        speak("充值失败");
    }

    private void dispatchError(String detail) {
        dispatch(FAIL, "充值异常", detail);
        speak("充值异常");
    }

    private Cmd resetCmd() {
        yuan = null;
        fen = null;
        uid = null;
        stat = null;
        order = null;
        isUploaded = false;
        chargeInfo = null;
        if (null != delayFuture)  {
            delayFuture.cancel(true);
        }
        if (isCashCharge) {
            return null;
        }
        delayFuture = ownSchedule(() -> super.dispatch(WARN, "...预充充值挂机中...", "可直接拍卡根据预充信息充值"), 5, TimeUnit.SECONDS);
        return uidWait;
    }

    @Override
    public void dispatch(TipType type, String msg, String detail) {
        super.dispatch(type, msg, detail);
        getCustomerHolder().showMsg(type, msg, detail);
        getCustomerHolder().showWelcomeLazy();
        if (null != delayFuture)  {
            delayFuture.cancel(true);
        }
    }

    @Override
    public void dispatch(String msg, String detail) {
        super.dispatch(msg, detail);
        getCustomerHolder().showMsg(getType(), msg, detail);
        getCustomerHolder().showWelcomeLazy();
        if (null != delayFuture)  {
            delayFuture.cancel(true);
        }
    }

    @Override
    public void dispatch(String msg) {
        super.dispatch(msg);
        getCustomerHolder().showMsg(getType(), msg, null);
        getCustomerHolder().showWelcomeLazy();
        if (null != delayFuture)  {
            delayFuture.cancel(true);
        }
    }

    /**
     * 退出预充值模式要重新校验权限
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (isCashCharge) {
            return false;
        }
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return true;
        }

        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo) {
            return true;
        }

        switch (keyInfo) {
            case KEY_ENTER:
            case KEY_MENU:
            case KEY_CANCEL:
            case KEY_SEARCH:
            case KEY_DELETE:
                setMainFragment(new MyCardAdminValidFragment());
                return true;
        }

        return true;
    }

    public static class MyCardAdminValidFragment extends CardAdminValidFragment {
        @Override
        public boolean onKeyUp(KeyInfo keyInfo) {
            if (keyInfo == KeyInfo.KEY_CANCEL) {
                setMainFragment(new CardChargeScanFragment());
                return true;
            }
            super.onKeyUp(keyInfo);
            return true;
        }
    }
}
