package com.pay.ioopos.trade;

import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_ERROR;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_FAIL;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_SUCCESS;
import static com.pay.ioopos.channel.card.CardUtils.crc;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_BALANCE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_ORDER_NO;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_ORDER_TIME;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_METHOD;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_TYPE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.enums.PayType;
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
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.sqlite.OrderStore;
import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.worker.WorkerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实体卡支付类
 * @author mo_yq5
 * @since 2021/11/04 15:19
 */
public class CardPayer {
    private static final String TAG = CardPayer.class.getSimpleName();
    private final AbstractFragment fragment;
    private final String cardUid;
    private final int money;
    private final byte[] uid;
    private final CardPayCallback callback;

    private CmdCase cmdCase;

    private CardBase base;
    private CardStat stat;
    private CardOrder order;

    public CardPayer(AbstractFragment fragment, String cardUid, String amount, CardPayCallback callback) {
        this.fragment = fragment;
        this.cardUid = cardUid;
        this.money = BigDecimalUtils.yuanToFen(new BigDecimal(amount));
        this.uid = HexUtils.toByteArray(cardUid);
        this.callback = callback;
    }

    public void bind() {

        Log.d(TAG, "黑名单卡号: " + CardRisk.getLockUidList());
        Log.d(TAG, "本交易卡号: " + cardUid);
        displayLog("卡号:%s", cardUid);
        if (CardRisk.getLockUidList().contains(cardUid)) {
            cardInvalid("卡已被挂失");
            CmdCase cmdCase = CardFactory.getCmdCase(createLockCmd("后台锁卡"));
            if (null != cmdCase) {
                cmdCase.bindToLifecycle(fragment);
            }
            return;
        }

        OrderStore store = StoreFactory.orderStore();
        int count = store.count(0);
        if ((0 == CardRisk.getLimitOffline() && !isNetworkAvailable())
                || (0 < count && count >= CardRisk.getLimitOffline())) {
            cardInvalid("设备离线交易笔数受限");
            return;
        }

        if (money > CardRisk.getLimitOrdAmount()) {
            cardInvalid("卡单笔交易金额受限");
            return;
        }

        Cmd baseRead = createBaseReadCmd();

        cmdCase = CardFactory.getCmdCase(baseRead);
        if (null != cmdCase) {
            cmdCase.bindToLifecycle(fragment);
        }

    }

    public void release() {
        if (null != cmdCase) {
            cmdCase.release();
        }
    }

    /**
     * 1.获取卡基本信息
     * @return cmd
     */
    private Cmd createBaseReadCmd() {
        return CardFactory.baseRead(new OnCardBaseListener() {

            @Override
            public Cmd onFail(String msg) {
                // 不能识别的卡，尝试通过网络同步交易
                callback.payNetwork();
                return null;
            }

            @Override
            public Cmd onSuccess(CardBase base) {
                return walletPayCheck(base);
            }
        });
    }

    /**
     * 2.获取卡最近交易记录
     * @return cmd
     */
    private Cmd createOrderReadCmd() {
        return CardFactory.orderRead(uid, 0, new OnCardOrderListener() {

            @Override
            public Cmd onFail(String msg) {
                callback.payFail("2->" + msg);
                return null;
            }

            @Override
            public Cmd onSuccess(CardOrder order) {
                return walletPayCheck(order);
            }
        });
    }

    /**
     * 3.获取卡统计信息
     * @return cmd
     */
    private Cmd createStatReadCmd() {
        return CardFactory.statRead(uid, new OnCardStatListener() {

            @Override
            public Cmd onFail(String msg) {
                callback.payFail("3->" + msg);
                return null;
            }

            @Override
            public Cmd onSuccess(CardStat stat) {
                return walletPayCheck(stat);
            }
        });
    }

    /**
     * 4.创建新的交易记录
     * @return cmd
     */
    private Cmd createOrderWriteCmd() {
        order = new CardOrder(DeviceUtils.sn(), ApiUtils.generateOrderNo(), base.getBalance(), money, System.currentTimeMillis()/1000, 0, 0, 0);
        return CardFactory.orderWrite(uid, order, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                callback.payFail("4->" + msg);
                return null;
            }

            @Override
            public Cmd onSuccess() {
                return createWalletPayCmd();
            }
        });
    }

    /**
     * 5.扣款
     * @return cmd
     */
    private Cmd createWalletPayCmd() {
        return CardFactory.walletPay(uid, money, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                return createBalanceReadCmd();
            }

            @Override
            public Cmd onSuccess() {
                return createBalanceReadCmd();
            }

        });
    }

    /**
     * 6.查询余额，确认扣款状态
     * @return cmd
     */
    private Cmd createBalanceReadCmd() {
        // 余额确认，发现有walletPay后卡内余额不变的情况，这里加强校验
        return CardFactory.balanceRead(uid, new OnIntListener() {

            @Override
            public Cmd onFail(String msg) {
                return createBalanceReadCmd();
            }

            @Override
            public Cmd onSuccess(int value) {
                // 扣款异常
                if (base.getBalance() - money != value) {
                    if (base.getBalance() == value) {// 余额没减
                        callback.payFail("扣款失败");
                        return createOrderUpdateCmd(STATUS_FAIL);
                    } else {// 扣款异常
                        callback.payError("扣款异常");
                        return createOrderUpdateCmd(STATUS_ERROR);
                    }
                }
                Intent params = new Intent();
                params.putExtra(INTENT_PARAM_PAY_TYPE, PayType.OTHER);
                params.putExtra(INTENT_PARAM_PAY_METHOD, PayMethod.CARD);
                params.putExtra(INTENT_PARAM_AMOUNT, BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString());
                params.putExtra(INTENT_PARAM_ORDER_NO, order.getOrderNo());
                params.putExtra(INTENT_PARAM_ORDER_TIME, order.getOrderTime());
                params.putExtra(INTENT_PARAM_CODE, base.getCardNo());
                params.putExtra(INTENT_PARAM_BALANCE, BigDecimalUtils.fenToYuan(base.getBalance()).toPlainString());

                base.setBalance(value);

                // 同步保存，确保数据保存成功
                try {
                    OrderUtils.asyncPay(params);
                } catch (Exception e) {
                    callback.payError(null);
                    return createOrderUpdateCmd(STATUS_ERROR);
                }

                callback.paySuccess();

                return createOrderUpdateCmd(STATUS_SUCCESS);
            }

        });
    }

    /**
     * 7.更新订单状态
     * @param status 目标状态
     * @return cmd
     */
    private Cmd createOrderUpdateCmd(int status) {
        order.setStatus(status);
        return CardFactory.orderWriteStatus(uid, order, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                return null;//createOrdersReadCmd(startSector(), new ArrayList<>());
            }

            @Override
            public Cmd onSuccess() {
                return createOrderMoveCmd();
            }
        });
    }

    /**
     * 8.移动订单
     * @return cmd
     */
    private Cmd createOrderMoveCmd() {
        return CardFactory.statUpdateByOrder(uid, order, stat, new OnCardListener() {
            @Override
            public Cmd onSuccess() {
                return null;//createOrdersReadCmd(startSector(), new ArrayList<>());
            }

            @Override
            public Cmd onFail(String msg) {
                return null;//createOrdersReadCmd(startSector(), new ArrayList<>());
            }
        });
    }

    /**
     * 锁卡
     * @param msg 锁卡原因
     * @return cmd
     */
    private Cmd createLockCmd(String msg) {
        return CardFactory.cardLock(uid, msg, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                Log.d("cardLock", "卡锁定失败：" + msg);
                return null;
            }

            @Override
            public Cmd onSuccess() {
                // 写了卡对卡本身进行锁定了之后就可以从列表中移除了，后续不用再从列表中检测
                // 以免后续因列表未跟后台同步最新，造成误判
                // 此做法以降低误判概率
                CardRisk.getLockUidList().remove(cardUid);
                if (null != base) {
                    WorkerFactory.enqueueCardLockReportOneTime(base.getCardNo(), cardUid);
                    return null;
                }
                return CardFactory.baseRead(new OnCardBaseListener() {
                    @Override
                    public Cmd onSuccess(CardBase base) {
                        WorkerFactory.enqueueCardLockReportOneTime(base.getCardNo(), cardUid);
                        return null;
                    }

                    @Override
                    public Cmd onFail(String msg) {
                        return null;
                    }
                });
            }

        });
    }

    /**
     * 卡基本信息校验
     * @param base 卡基本信息
     * @return 卡基本信息校验后的后续指令（即：获取最近交易记录并检校其交易状态）
     */
    private Cmd walletPayCheck(CardBase base) {

        if (null == base.getMerchNo()
                || base.getMerchNo().isEmpty()
                || !base.getMerchNo().equals(StoreFactory.settingStore().getMerchNo())) {
            cardInvalid("卡无效");
            return null;
        }

        if (base.getStatus() <= 0) {
            cardInvalid("卡已被挂失");
            return null;
        }

        if (money > base.getBalance()) {
            callback.payFail("卡余额不足");
            speak("余额不足");
            return null;
        }

        this.base = base;
        return createOrderReadCmd();
    }

    /**
     * 最近交易状态校验
     * @param order 最近交易流水
     * @return 最近流水校验后的后续指令（即：读取并校验统计信息）
     */
    private Cmd walletPayCheck(CardOrder order) {
        if (null != order && crc(order) == order.getCrc()) {
            this.order = order;
        }
        return CardFactory.orderUpdateByBalance(base, order, new OnCardOrderUpdateListener() {
            @Override
            public Cmd onNormal() {
                return createStatReadCmd();
            }

            @Override
            public Cmd onUpdate(CardOrder updatedOrder) {
                CardPayer.this.order = updatedOrder;
                return createStatReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                callback.payFail(msg);
                return null;
            }
        });
    }

    /**
     * 校验统计信息
     * @param stat 需要校验的统计信息
     * @return 校验后的后续指令（即：上传历史交流流水或者发起新的交易）
     */
    private Cmd walletPayCheck(CardStat stat) {
        // 卡首次交易的情况
        if (null == order || null == stat) {
            return createOrderWriteCmd();
        }

        // 上次执行不完整，或者最近交易没统计进去

        if (crc(stat, order) != stat.getCrc() || (order.isSuccess() && order.getOrderTime() > stat.getLastTime())) {
            return CardFactory.statUpdateByOrder(uid, order, stat, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return createStatReadCmd();// 再次按”一般情况“检查
                }

                @Override
                public Cmd onFail(String msg) {
                    callback.payFail(msg);
                    return null;
                }
            });
        }

        // 一般正常情况

        if (base.getBalance() != stat.getBalance()) {
            cardInvalid( "卡余额异常，请到管理处确认");
            LogUtils.warn("交易校验：卡余额异常，%s => %d<?>%d", base.getCardNo(), base.getBalance(), stat.getBalance());
            return null;
        }
        long lastTime = stat.getLastTime();
        if (lastTime > System.currentTimeMillis()/1000 - CardRisk.getLimitOrdPeriod()) {
            cardInvalid( "卡交易频率受限");
            return null;
        }
        if (stat.getOfflineCount() >= 3 && !isNetworkAvailable()) {
            cardInvalid( "卡离线交易笔数受限");
            return null;
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String curDay = format.format(new Date());
        String lastDay = format.format(new Date(lastTime * 1000));
        if (curDay.equals(lastDay)) {
            if (stat.getDayCount() >= CardRisk.getLimitDayCount()) {
                cardInvalid("卡当天交易笔数受限");
                return null;
            }
            if (stat.getDayAmount() >= CardRisk.getLimitDayAmount()) {
                cardInvalid( "卡当天交易额度受限");
                return null;
            }
        }

        this.stat = stat;
        return createOrdersUploadCmd(startSector(), new ArrayList<>());
    }

    /**
     * 上传历史流水
     * @param sector 当前要读取的流水所有扇区索引
     * @param orders 已读取流水列表
     * @return 流水上传后的后续指令
     */
    private Cmd createOrdersUploadCmd(Integer sector, List<CardOrder> orders) {
        if (null == sector) {
            if (null != orders && orders.size() > 0) {
                CardUtils.startUploadOrders(base, orders, stat.getLastSector() == SECTOR_ORDERS.length - 1);
            }
            return createOrderWriteCmd();
        }
        return CardFactory.orderRead(uid, sector, new OnCardOrderListener() {
            @Override
            public Cmd onSuccess(CardOrder order) {
                if (null != order) {
                    orders.add(order);
                }
                return next();
            }

            @Override
            public Cmd onFail(String msg) {
                return next();
            }

            private Cmd next() {
                if (orders.size() == 3) {// 分批上传，提高效率
                    CardUtils.startUploadOrders(base, orders, stat.getLastSector() == SECTOR_ORDERS.length - 1);
                    return createOrdersUploadCmd(nextSector(sector), new ArrayList<>());
                }
                return createOrdersUploadCmd(nextSector(sector), orders);
            }
        });

    }

    /**
     * 开始读取的流水扇区索引，从最近的流水开始
     * @return 开始读取的流水扇区索引
     */
    private Integer startSector() {
        return nextSector(null);
    }

    /**
     * 卡流水读取条件：1，统计信息不为空（即不是第一条流水），2，流水数据存储到最后一个流水扇区时必读，3，卡需要同步且有网络时必读
     * @param curSector 当前扇区索引
     * @return 返回下一个要读取卡流水的所在扇区索引，返回null，表示读取结束
     */
    private Integer nextSector(Integer curSector) {
        if (null == stat) {
            return null;
        }
        if (stat.getLastSector() < SECTOR_ORDERS.length - 1) {
            if (!CardRisk.getSyncUidList().contains(cardUid) || !isNetworkAvailable()) {
                return null;
            }
        }
        if (null == curSector) {
            return stat.getLastSector();
        }
        curSector--;
        if (curSector == 0) {
            curSector = SECTOR_ORDERS.length - 1;
        }
        if (curSector == stat.getLastSector()) {
            return null;
        }
        return curSector;
    }

    private void cardInvalid(String detail) {
        callback.payFail(detail);
    }

    public interface CardPayCallback {
        void payFail(String detail);
        void payNetwork();
        void payError(String detail);
        void paySuccess();
    }
}
