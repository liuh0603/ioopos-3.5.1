package com.pay.ioopos.trade;

import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;
import static com.pay.ioopos.channel.card.CardUtils.crc;

import android.util.Log;

import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.channel.card.CardFactory.OnCardOrderUpdateListener;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.CardStat;
import com.pay.ioopos.channel.card.OnCardInfoListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.card.OnCardOrderListener;
import com.pay.ioopos.channel.card.OnCardStatListener;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnFailListener;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.widget.Tip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实体卡信息显示类
 * @author    Moyq5
 * @since  2021/11/1 16:19
 */
public class CardShower {
    private static final String TAG = CardShower.class.getSimpleName();
    private final AbstractFragment fragment;
    private boolean isToggle = false;
    private byte[] uid;
    private CardInfo info;
    private CardOrder order;
    private Cmd uidWait;
    private OnCardShowListener listener = new OnCardShowListener() {
        @Override
        public void onShow(CardInfo info, CardOrder order) {
            displayLog("卡余额查询成功");
            fragment.getCustomerHolder().showCard(info, order);
            String amount = BigDecimalUtils.fenToYuan(info.getBalance()).toPlainString();
            toast("余额：%s元", amount);
            fragment.hideLoading();
        }

        @Override
        public void onShow(List<CardOrder> orders) {
            displayLog("卡流水查询成功");
            fragment.getCustomerHolder().showCard(orders);
            toast("交易查询，请看客屏显示");
            fragment.hideLoading();
        }

        @Override
        public Cmd onFail(String msg) {
            fragment.getCustomerHolder().showMsg(Tip.TipType.FAIL,"刷卡失败", msg);
            speak("刷卡失败");fragment.hideLoading();
            toast(msg);
            return null;
        }
    };

    public CardShower(AbstractFragment fragment) {
        this.fragment = fragment;
    }

    public void setUid(byte[] uid) {
        this.uid = uid;
    }

    public void bind() {
        if (!ApiUtils.walletPaySupport()) {
            return;
        }

        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            return;
        }

        uidWait = CardFactory.uidWait(new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] uid) {
                displayLog("刷卡:%s", HexUtils.toHexString(uid));
                if (!ApiUtils.isChecked()) {
                    listener.onFail("设备未签到");
                    return uidWait;
                }
                fragment.showLoading();
                if (Arrays.equals(CardShower.this.uid, uid) && (isToggle = !isToggle)) {
                    displayLog("查询卡流水...");
                    speak("交易查询");
                    return createOrdersReadCmd(1, new ArrayList<>());// 0索引扇区的数据不算，因为它本身会存在于其它扇区，避免显示重复
                }
                displayLog("查询卡余额...");
                speak("余额查询");
                CardShower.this.uid = uid;
                CardShower.this.isToggle = false;
                return createInfoReadCmd();
            }

            @Override
            public Cmd onFail(String msg) {
                fragment.hideLoading();
                listener.onFail(msg);
                return uidWait;
            }
        }, scanCase);

        scanCase.setRootCmd(uidWait);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(fragment);
    }

    public void setListener(OnCardShowListener listener) {
        this.listener = new OnCardShowListener() {
            @Override
            public void onShow(CardInfo info, CardOrder order) {
                displayLog("卡余额查询成功");
                fragment.hideLoading();
                listener.onShow(info, order);
            }

            @Override
            public void onShow(List<CardOrder> orders) {
                displayLog("卡流水查询成功");
                fragment.hideLoading();
                listener.onShow(orders);
            }

            @Override
            public Cmd onFail(String msg) {
                displayLog(msg);
                fragment.hideLoading();
                return listener.onFail(msg);
            }
        };
    }

    /**
     * 1.获取卡基本信息，展示
     * @return cmd
     */
    private Cmd createInfoReadCmd() {
        return CardFactory.cardRead(new OnCardInfoListener() {

            @Override
            public Cmd onFail(String msg) {
                listener.onFail(msg);
                return uidWait;
            }

            @Override
            public Cmd onSuccess(CardInfo info) {
                CardShower.this.info = info;
                return createOrderReadLastCmd();
            }
        });
    }

    /**
     * 2.获取卡流水，展示
     * @param sector 扇区索引
     * @return cmd
     */
    private Cmd createOrdersReadCmd(int sector, List<CardOrder> orders) {
        return CardFactory.orderRead(uid, sector, new OnCardOrderListener() {
            @Override
            public Cmd onSuccess(CardOrder order) {
                if (null != order) {
                    orders.add(order);
                }
                if (sector < SECTOR_ORDERS.length - 1) {
                    return createOrdersReadCmd(sector + 1, orders);
                }
                listener.onShow(orders);
                if (isNetworkAvailable() && null != info && CardRisk.getSyncUidList().contains(info.getCardUid())) {
                    CardUtils.startUploadOrders(info, orders);
                }
                return uidWait;
            }

            @Override
            public Cmd onFail(String msg) {
                listener.onFail(msg);
                return uidWait;
            }
        });
    }

    /**
     * 1.1.获取卡最后一条交易信息
     * @return cmd
     */
    private Cmd createOrderReadLastCmd() {
        return CardFactory.orderRead(uid, 0, new OnCardOrderListener() {

            @Override
            public Cmd onFail(String msg) {
                Log.d(TAG, "卡最近交易获取失败：" + msg);
                listener.onShow(info, order);
                return uidWait;
            }

            @Override
            public Cmd onSuccess(CardOrder order) {
                CardShower.this.order = order;
                return createOrderUpdateCmd();
            }
        });
    }

    /**
     * 1.2.检查并更近一条交易记录状态
     * @return cmd
     */
    private Cmd createOrderUpdateCmd() {
        return CardFactory.orderUpdateByBalance(info, order, new OnCardOrderUpdateListener() {
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
                Log.d(TAG, "卡最近交易更新失败：" + msg);
                listener.onShow(info, order);
                return uidWait;
            }
        });
    }

    /**
     * 1.3.获取统计信息
     * @return cmd
     */
    private Cmd createStatReadCmd() {
        return CardFactory.statRead(uid, new OnCardStatListener() {

            @Override
            public Cmd onFail(String msg) {
                Log.d(TAG, "卡统计获取失败：" + msg);
                listener.onShow(info, order);
                return uidWait;
            }

            @Override
            public Cmd onSuccess(CardStat stat) {
                if (null != stat && (null == order || !order.isSuccess())) {
                    return createOrderReadSuccCmd(stat);
                }
                listener.onShow(info, order);
                return createStatUpdateCmd(stat);
            }
        });
    }

    /**
     * 1.4.根据统计信息获取最近一条成功交易记录
     * @param stat 统计信息
     * @return cmd
     */
    private Cmd createOrderReadSuccCmd(CardStat stat) {
        return CardFactory.orderRead(uid, stat.getSuccSector(), new OnCardOrderListener() {

            @Override
            public Cmd onFail(String msg) {
                Log.d(TAG, "卡最近成功交易获取失败：" + msg);
                listener.onShow(info, order);
                return createStatUpdateCmd(stat);
            }

            @Override
            public Cmd onSuccess(CardOrder order) {
                listener.onShow(info, order);
                return createStatUpdateCmd(stat);
            }
        });
    }

    /**
     * 1.5.更新统计信息
     * @param stat 统计信息
     * @return cmd
     */
    private Cmd createStatUpdateCmd(CardStat stat) {
        // 卡首次交易的情况

        if (null == order || null == stat) {
            return createOrdersUploadCmd(stat);
        }

        // 上次执行不完整，或者最近交易没统计进去

        if (crc(stat, order) != stat.getCrc() || (order.isSuccess() && order.getOrderTime() > stat.getLastTime())) {
            return CardFactory.statUpdateByOrder(uid, order, stat, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return createOrdersUploadCmd(stat);
                }

                @Override
                public Cmd onFail(String msg) {
                    Log.d(TAG, "卡统计更新失败：" + msg);
                    return createOrdersUploadCmd(stat);
                }
            });
        }

        // 一般正常情况
        return createOrdersUploadCmd(stat);

    }

    /**
     * 1.6.附加动作，检查并上传流水
     * @return cmd
     */
    private Cmd createOrdersUploadCmd(CardStat stat) {
        return createOrdersUploadCmd(startSector(stat), stat, new ArrayList<>());
    }

    /**
     * 1.7.获取所有交易记录并上传
     * @param sector 扇区索引
     * @param orders 交易记录列表
     * @return cmd
     */
    private Cmd createOrdersUploadCmd(Integer sector, CardStat stat, List<CardOrder> orders) {
        if (null == sector) {
            if (null != orders && orders.size() > 0) {
                CardUtils.startUploadOrders(info, orders);
            }
            return uidWait;
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
                    CardUtils.startUploadOrders(info, orders);
                    return createOrdersUploadCmd(nextSector(sector, stat), stat, new ArrayList<>());
                }
                return createOrdersUploadCmd(nextSector(sector, stat), stat, orders);
            }
        });

    }

    private Integer startSector(CardStat stat) {
        return nextSector(null, stat);
    }

    private Integer nextSector(Integer curSector, CardStat stat) {
        if (null == stat || !isNetworkAvailable() || !CardRisk.getSyncUidList().contains(info.getCardUid())) {
            return null;
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

    public interface OnCardShowListener extends OnFailListener {
        void onShow(CardInfo info, CardOrder order);
        void onShow(List<CardOrder> orders);
    }
}
