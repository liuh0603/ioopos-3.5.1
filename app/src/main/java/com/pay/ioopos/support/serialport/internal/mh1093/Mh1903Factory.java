package com.pay.ioopos.support.serialport.internal.mh1093;

import static com.pay.ioopos.channel.card.CardFactory.BLOCK_SYS_CARD_NO;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_SYS_MERCH_NO;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_SYS_PERMIT;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_USER_GROUP;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_USER_NAME;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_USER_NO;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_USER_PERMIT;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_WALLET_BALANCE;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_WALLET_DESCR;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_WALLET_PERMIT;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_WALLET_STAT;
import static com.pay.ioopos.channel.card.CardFactory.BLOCK_WALLET_STATUS;
import static com.pay.ioopos.channel.card.CardFactory.DEF_CTRL_BLOCK_DATA;
import static com.pay.ioopos.channel.card.CardFactory.DEF_KEY_A;
import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;
import static com.pay.ioopos.channel.card.CardFactory.getOrderCtrlData;
import static com.pay.ioopos.channel.card.CardFactory.getSysCtrlData;
import static com.pay.ioopos.channel.card.CardFactory.getUserCtrlData;
import static com.pay.ioopos.channel.card.CardFactory.getWalletCtrlData;
import static com.pay.ioopos.channel.card.CardUtils.blockDataToStat;
import static com.pay.ioopos.channel.card.CardUtils.createAmountBlockData;
import static com.pay.ioopos.channel.card.CardUtils.statToBlockData;
import static com.pay.ioopos.channel.card.KeyType.A;
import static com.pay.ioopos.channel.card.KeyType.B;
import static com.pay.ioopos.common.HexUtils.toHexString;

import com.pay.ioopos.channel.card.CardBase;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.CardStat;
import com.pay.ioopos.channel.card.CardUser;
import com.pay.ioopos.channel.card.OnCardBaseListener;
import com.pay.ioopos.channel.card.OnCardInfoListener;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.channel.card.OnCardOrderListener;
import com.pay.ioopos.channel.card.OnCardStatListener;
import com.pay.ioopos.support.scan.ScanBeater;
import com.pay.ioopos.support.serialport.internal.AbstractCmd;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdProvider;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnCmdBeatListener;
import com.pay.ioopos.support.serialport.internal.OnCmdListener;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.support.serialport.internal.OnMutipleListener;
import com.pay.ioopos.support.serialport.internal.OnStringListener;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.trade.CardRisk;

import java.util.Arrays;

/**
 * 艾博世卡操作机制
 *
 * @author Moyq5
 * @since 2020/11/4 14:16
 */
public abstract class Mh1903Factory {

    private Mh1903Factory() {
    }

    /**
     * 读卡号，忽略指定卡
     * @author  Moyq5
     * @since    2021/1/6 11:25
     * @param listener 回调
     * @param beater 音效
     * @param ignoredUid 忽略的卡号
     * @return cmd
     */
    public static Cmd uidWait(OnByteListener listener, ScanBeater beater, byte[] ignoredUid) {
        AbstractCmdAdapter idRead = new CmdUidRead();
        UidObject obj = new UidObject();
        idRead.setListener(new OnCmdBeatListener(beater) {
            @Override
            public Cmd onFail(byte code) {
                obj.hasComeIn = false;
                return idRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if (obj.hasComeIn) {
                    return idRead;
                }
                obj.hasComeIn = true;
                super.onSuccess(data);
                byte[] uid = Mh1903Utils.uid(data);
                if (null != ignoredUid && Arrays.equals(ignoredUid, uid)) {
                    return idRead;
                }
                return listener.onSuccess(uid);
            }
        });
        return idRead;
    }

    /**
     * 同时读卡或者扫码
     * @author  Moyq5
     * @since    2021/3/26 14:31
     * @param flags 扫码或者刷卡标志
     * @param listener 回调
     * @param beater 音效
     * @return cmd
     */
    public static Cmd uidOrQrcodeRead(int flags, OnMutipleListener listener, ScanBeater beater) {
        AbstractCmdAdapter uidOpen = new CmdUidOpen();
        AbstractCmdAdapter uidRead = new CmdUidRead();
        AbstractCmdAdapter qrcodeClose = new CmdQrcodeClose();
        AbstractCmdAdapter qrcodeOpen = new CmdQrcodeOpen();
        AbstractCmdAdapter qrcodeRead = new CmdQrcodeRead();
        UidObject obj = new UidObject();
        uidOpen.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return qrcodeClose;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return qrcodeClose;
            }
        });
        qrcodeClose.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return qrcodeOpen;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return qrcodeOpen;
            }
        });
        qrcodeOpen.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return uidRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return uidRead;
            }
        });
        uidRead.setListener(new OnCmdBeatListener(beater) {
            @Override
            public Cmd onFail(byte code) {
                obj.hasComeIn = false;
                return qrcodeRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if (obj.hasComeIn) {
                    return qrcodeRead;
                }
                obj.hasComeIn = true;
                if ((flags & 1) == 1) {
                    super.onSuccess(data);
                    String uid = toHexString(Mh1903Utils.uid(data));
                    return listener.onSuccess(1, uid);
                }
                return qrcodeRead;
            }
        });
        qrcodeRead.setListener(new OnCmdBeatListener(beater) {
            @Override
            public Cmd onFail(byte code) {
                return uidRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if ((flags & 2) == 2) {
                    super.onSuccess(data);
                    return listener.onSuccess(2, new String(data));
                }
                return uidRead;
            }
        });
        return uidOpen;
    }

    /**
     * 新卡建卡
     * @author  Moyq5
     * @since    2021/1/11 19:26
     * @param cardInfo 写入的卡信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardInit(CardInfo cardInfo, OnCardListener listener) {
        return new CardInit(HexUtils.toByteArray(cardInfo.getCardUid()), cardInfo, listener).getCmd();
    }

    /**
     * 写入平台卡号
     * @author  Moyq5
     * @since    2021/1/13 11:56
     * @param uid 卡号
     * @param cardNo 平台卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardNoWrite(byte[] uid, String cardNo, OnCardListener listener) {
        return Mh1903Utils.hexWrite(uid, B, BLOCK_SYS_CARD_NO, cardNo, listener);
    }

    /**
     * 卡激活，即从锁定状态更新为正常状态
     * @author  Moyq5
     * @since    2021/1/13 10:12
     * @param uid 卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardActive(byte[] uid, OnCardListener listener) {
        return Mh1903Utils.walletInit(uid, B, BLOCK_WALLET_STATUS, 1, listener);
    }

    /**
     * 查询钱包余额
     * @author  Moyq5
     * @since    2021/1/20 16:39
     * @param uid 卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd balanceRead(byte[] uid, OnIntListener listener) {
        return Mh1903Utils.intRead(uid, A, BLOCK_WALLET_BALANCE, listener);
    }

    /**
     * 钱包充值
     * @author  Moyq5
     * @since    2020/11/10 15:05
     * @param   amount 充值金额，单位：分
     * @return cmd
     */
    public static Cmd walletCharge(byte[] uid, int amount, OnCardListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter charge = new CmdBlockAdd(BLOCK_WALLET_BALANCE, Mh1903Utils.amountToBytes(amount));
            charge.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("钱包充值异常");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess();
                }
            });
            return charge;
        };

        return Mh1903Utils.auth(uid, B, BLOCK_WALLET_BALANCE, listener, next);
    }

    /**
     * 钱包扣款
     * @author  Moyq5
     * @since    2020/11/4 15:12
     * @param   amount 扣款金额，单位：分
     * @return cmd
     */
    public static Cmd walletPay(byte[] uid, int amount, OnCardListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter walletPay = new CmdBlockSub(BLOCK_WALLET_BALANCE, Mh1903Utils.amountToBytes(amount));
            walletPay.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("钱包扣款异常");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess();
                }
            });
            return walletPay;
        };

        return Mh1903Utils.auth(uid, A, BLOCK_WALLET_BALANCE, listener, next);
    }

    /**
     * 卡（钱包）锁定
     * @author  Moyq5
     * @since    2021/1/15 10:49
     * @param uid 卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardLock(byte[] uid, OnCardListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter walletPay = new CmdBlockSub(BLOCK_WALLET_STATUS, Mh1903Utils.amountToBytes(10));// 钱包状态减值，只要小于1此卡就为锁定状态
            walletPay.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("钱包锁定失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess();
                }
            });
            return walletPay;
        };

        return Mh1903Utils.auth(uid, A, BLOCK_WALLET_STATUS, listener, next);
    }

    /**
     * 卡重置，销户，恢复出厂值
     * @author    Moyq5
     * @since  2020/11/16 14:49
     */
    public static Cmd cardReset(byte[] uid, OnCardListener listener) {
        return new CardReset(uid, listener).getCmd();
    }

    /**
     * 卡基本信息获取
     * @param listener 回调
     * @return cmd
     */
    public static Cmd baseRead(OnCardBaseListener listener) {
        return new BaseRead(listener).getCmd();
    }

    /**
     * 写统计信息
     * @param uid 卡号
     * @param stat 统计信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd statWrite(byte[] uid, CardStat stat, OnCardListener listener) {
        return Mh1903Utils.authWrite(uid, A, BLOCK_WALLET_STAT, statToBlockData(stat), listener);
    }

    /**
     * 获取统计信息
     * @param uid 卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd statRead(byte[] uid, OnCardStatListener listener) {
        return Mh1903Utils.blockRead(uid, A, BLOCK_WALLET_STAT, new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(blockDataToStat(data));
            }

            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        });
    }

    /**
     * 获取交易记录
     * @param uid 卡号
     * @param sector 数据所在扇区索引
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderRead(byte[] uid, int sector, OnCardOrderListener listener) {
        return new OrderRead(uid, sector, listener).getCmd();
    }

    /**
     * 添加、更新交易记录
     * @param uid 卡号
     * @param order 订单
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderWrite(byte[] uid, CardOrder order, OnCardListener listener) {
        return new OrderWrite(uid, order, listener).getCmd();
    }

    /**
     * 更新状态
     * @param uid 卡号
     * @param order 订单信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderWriteStatus(byte[] uid, CardOrder order, OnCardListener listener) {
        return new OrderWriteStatus(uid, order, listener).getCmd();
    }

    /**
     * 读卡信息：余额、用户信息
     * @author  Moyq5
     * @since    2020/11/4 17:24
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardRead(OnCardInfoListener listener) {
        return new InfoRead(listener).getCmd();
    }

    private static class UidObject {
        private boolean hasComeIn = true;
    }

    public static class InfoRead implements CmdProvider {
        private final CardInfo info = new CardInfo();
        private final OnCardInfoListener listener;
        private CmdProvider userNameRead;
        private CmdProvider userNoRead;
        private CmdProvider userGroupRead;
        private CmdProvider merchNoRead;
        private CmdProvider cardNoRead;
        private CmdProvider balanceRead;
        private CmdProvider statusRead;
        private CmdProvider statRead;

        InfoRead(OnCardInfoListener listener) {
            this.listener = listener;
            info.setUser(new CardUser());
        }

        public AbstractCmd getCmd() {
            CmdUidRead uidRead = new CmdUidRead();
            uidRead.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("读卡失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    byte[] uid = Mh1903Utils.uid(data);
                    info.setCardUid(toHexString(uid));
                    createBizCmd(uid);
                    return userNameRead.getCmd();
                }
            });
            return uidRead;
        }

        private void createBizCmd(byte[] uid) {
            userNameRead = () -> Mh1903Utils.stringRead(uid, A, BLOCK_USER_NAME, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserName(value);
                    return userNoRead.getCmd();
                }
            });
            userNoRead = () -> Mh1903Utils.stringRead(uid, A, BLOCK_USER_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserNo(value);
                    return userGroupRead.getCmd();
                }
            });
            userGroupRead = () -> Mh1903Utils.stringRead(uid, A, BLOCK_USER_GROUP, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserGroup(value);
                    return merchNoRead.getCmd();
                }
            });
            merchNoRead = () -> Mh1903Utils.hexRead(uid, A, BLOCK_SYS_MERCH_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String merchNo) {
                    info.setMerchNo(merchNo);
                    return cardNoRead.getCmd();
                }
            });
            cardNoRead = () -> Mh1903Utils.hexRead(uid, A, BLOCK_SYS_CARD_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String cardNo) {
                    info.setCardNo(cardNo);
                    return balanceRead.getCmd();
                }
            });
            balanceRead = () -> Mh1903Utils.intRead(uid, A, BLOCK_WALLET_BALANCE, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int balance) {
                    info.setBalance(balance);
                    return statusRead.getCmd();
                }
            });
            statusRead = () -> Mh1903Utils.intRead(uid, A, BLOCK_WALLET_STATUS, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int status) {
                    info.setStatus(status);
                    return statRead.getCmd();
                }
            });
            statRead = () -> (AbstractCmd) statRead(uid, new OnCardStatListener() {

                @Override
                public Cmd onFail(String msg) {
                    return listener.onFail(msg);
                }

                @Override
                public Cmd onSuccess(CardStat stat) {
                    info.setStat(stat);
                    return listener.onSuccess(info);
                }
            });
        }

        private abstract class OnStringAdapter implements OnStringListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

        private abstract class OnIntAdapter implements OnIntListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }
    }

    public static class BaseRead implements CmdProvider {
        private final CardBase base = new CardBase();
        private final OnCardBaseListener listener;
        private CmdProvider merchNoRead;
        private CmdProvider cardNoRead;
        private CmdProvider balanceRead;
        private CmdProvider statusRead;
        private CmdProvider descrRead;

        BaseRead(OnCardBaseListener listener) {
            this.listener = listener;
        }

        public AbstractCmd getCmd() {
            CmdUidRead uidRead = new CmdUidRead();
            uidRead.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("读卡失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    byte[] uid = Mh1903Utils.uid(data);
                    base.setCardUid(toHexString(uid));
                    createBizCmd(uid);
                    return merchNoRead.getCmd();
                }
            });
            return uidRead;
        }

        private void createBizCmd(byte[] uid) {
            merchNoRead = () -> Mh1903Utils.hexRead(uid, A, BLOCK_SYS_MERCH_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String merchNo) {
                    base.setMerchNo(merchNo);
                    return cardNoRead.getCmd();
                }
            });
            cardNoRead = () -> Mh1903Utils.hexRead(uid, A, BLOCK_SYS_CARD_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String cardNo) {
                    base.setCardNo(cardNo);
                    return balanceRead.getCmd();
                }
            });
            balanceRead = () -> Mh1903Utils.intRead(uid, A, BLOCK_WALLET_BALANCE, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int balance) {
                    base.setBalance(balance);
                    return statusRead.getCmd();
                }
            });
            statusRead = () -> Mh1903Utils.intRead(uid, A, BLOCK_WALLET_STATUS, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int status) {
                    base.setStatus(status);
                    return descrRead.getCmd();
                }
            });
            descrRead = () -> Mh1903Utils.stringRead(uid, A, BLOCK_WALLET_DESCR, new OnStringListener() {
                @Override
                public Cmd onSuccess(String descr) {
                    base.setDescr(descr);
                    return listener.onSuccess(base);
                }

                @Override
                public Cmd onFail(String msg) {
                    return listener.onSuccess(base);// TODO 兼容旧卡未使用该数据块的情况
                    //return listener.onFail(msg);
                }
            });
        }

        private abstract class OnStringAdapter implements OnStringListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

        private abstract class OnIntAdapter implements OnIntListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }
    }

    public static class OrderRead implements CmdProvider {
        private final byte[] uid;
        private final int sector;
        private final OnCardOrderListener listener;

        private String devSn;
        private String orderNo;

        OrderRead(byte[] uid, int sector, OnCardOrderListener listener) {
            this.uid = uid;
            this.sector = sector;
            this.listener = listener;
        }

        public AbstractCmd getCmd() {
            return createDevSnReadCmd();
        }

        private AbstractCmd createDevSnReadCmd() {
            return Mh1903Utils.mixRead(uid, A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String devSn) {
                    if (null == devSn) {
                        return listener.onSuccess(null);
                    }
                    OrderRead.this.devSn = devSn;
                    return createOrderNoReadCmd();
                }
            });
        }

        private AbstractCmd createOrderNoReadCmd() {
            return Mh1903Utils.hexRead(uid, A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4 + 1, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String orderNo) {
                    if (null == orderNo) {
                        return listener.onSuccess(null);
                    }
                    OrderRead.this.orderNo = orderNo;
                    return createAmountReadCmd();
                }
            });
        }

        private AbstractCmd createAmountReadCmd() {
            return Mh1903Utils.blockRead(uid, A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4 + 2, new OnByteAdapter() {
                @Override
                public Cmd onSuccess(byte[] data) {
                    if (null == data || data.length != 16 || Arrays.equals(data, new byte[16])) {
                        return listener.onSuccess(null);
                    }
                    String hex = HexUtils.toHexString(data);
                    if (null == hex) {
                        return listener.onSuccess(null);
                    }
                    int balance = Integer.parseInt(hex.substring(0, 6));// 3节
                    int amount = Integer.parseInt(hex.substring(6, 12));// 3节
                    long time = Integer.parseInt(hex.substring(12, 22));// 5节
                    int status = data[11] & 0x0F;// 第12字节前4位
                    int crc = (data[11] & 0xF0) >> 4;// 第12字节后4位
                    CardOrder order = new CardOrder(devSn, orderNo, balance, amount, time, status, sector, crc);
                    return listener.onSuccess(order);
                }
            });
        }

        private abstract class OnStringAdapter implements OnStringListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

        private abstract class OnByteAdapter implements OnByteListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

    }

    public static class OrderWrite implements CmdProvider {
        private final byte[] uid;
        private final CardOrder order;
        private final OnCardListener listener;

        OrderWrite(byte[] uid, CardOrder order, OnCardListener listener) {
            this.uid = uid;
            this.order = order;
            this.listener = listener;
        }

        public AbstractCmd getCmd() {
            // TODO 旧卡兼容实现，检查订单扇区是否已经初始化，
            // 如果已经初始化（旧卡或者新卡），直接写入订单信息
            // 如果未初始化尝试初始化，成功后再写入订单信息，否则跳过订单信息写入。
            CmdProvider initOrderCtrl = () -> Mh1903Utils.authWrite(uid, A, DEF_KEY_A, SECTOR_ORDERS[order.getSector()] * 4 + 3, getOrderCtrlData(), new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return createDevSnWriteCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return listener.onSuccess();
                }
            });
            return Mh1903Utils.auth(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4, msg -> initOrderCtrl.getCmd(), this::createDevSnWriteCmd);

            // TODO 标准实现
            //return createDevSnWriteCmd();
        }

        private AbstractCmd createDevSnWriteCmd() {
            return Mh1903Utils.mixWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4, order.getDevSn(), listener, this::createOrderNoWriteCmd);
        }

        private AbstractCmd createOrderNoWriteCmd() {
            return Mh1903Utils.hexWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4 + 1, order.getOrderNo(), listener, this::createAmountWriteCmd);
        }

        private AbstractCmd createAmountWriteCmd() {
            return Mh1903Utils.authWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4 + 2, createAmountBlockData(order), listener);
        }

    }


    public static class OrderWriteStatus implements CmdProvider {
        private final byte[] uid;
        private final CardOrder order;
        private final OnCardListener listener;

        OrderWriteStatus(byte[] uid, CardOrder order, OnCardListener listener) {
            this.uid = uid;
            this.order = order;
            this.listener = listener;
        }

        public AbstractCmd getCmd() {
            return Mh1903Utils.authWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4 + 2, createAmountBlockData(order), listener);
        }

    }

    public static class InfoWrite implements CmdProvider {
        private final OnCardListener listener;
        private final CmdProvider userNameWrite;
        private final CmdProvider userNoWrite;
        private final CmdProvider userGroupWrite;
        private final CmdProvider merchNoWrite;
        private final CmdProvider cardNoWrite;
        private final CmdProvider statusWrite;
        private final CmdProvider statWrite;
        private final CmdProvider balanceWrite;

        InfoWrite(byte[] uid, CardInfo info, OnCardListener listener) {
            this.listener = listener;
            if (null == info.getUser()) {
                info.setUser(new CardUser());
            }
            CardUser user = info.getUser();

            userNameWrite = () -> Mh1903Utils.stringWrite(uid, B, BLOCK_USER_NAME, user.getUserName(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return userNoWrite.getCmd();
                }
            });

            userNoWrite = () -> Mh1903Utils.stringWrite(uid, B, BLOCK_USER_NO, user.getUserNo(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return userGroupWrite.getCmd();
                }
            });

            userGroupWrite = () -> Mh1903Utils.stringWrite(uid, B, BLOCK_USER_GROUP, user.getUserGroup(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return merchNoWrite.getCmd();
                }
            });

            merchNoWrite = () -> Mh1903Utils.hexWrite(uid, B, BLOCK_SYS_MERCH_NO, info.getMerchNo(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return cardNoWrite.getCmd();
                }
            });

            cardNoWrite = () -> Mh1903Utils.hexWrite(uid, B, BLOCK_SYS_CARD_NO, info.getCardNo(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return statWrite.getCmd();
                }
            });

            statWrite = () -> (AbstractCmd) statWrite(uid, info.getStat(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return statusWrite.getCmd();
                }
            });

            statusWrite = () -> Mh1903Utils.walletInit(uid, B, BLOCK_WALLET_STATUS, info.getStatus(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return balanceWrite.getCmd();
                }
            });

            balanceWrite = () -> Mh1903Utils.walletInit(uid, B, BLOCK_WALLET_BALANCE, info.getBalance(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return listener.onSuccess();
                }
            });

        }

        public AbstractCmd getCmd() {
            return userNameWrite.getCmd();
        }


        private abstract class OnCardAdapter implements OnCardListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

    }


    /**
     * 重置卡，重置卡数据内容成功与否不重要，重要的是能重置控制块密钥，使能重新写卡。
     * 为适应某些场景，支持在某控制块重置失败的情况下仍然可以重置其后续的控制块，即支持“重复”操作重置
     */
    public static class CardReset implements CmdProvider {
        private boolean hasSuccess = false;
        private final CmdProvider userCtrlReset;
        private final CmdProvider sysCtrlReset;
        private final CmdProvider walletCtrlReset;
        private final CmdProvider orderReset;
        private final CmdProvider infoWrite;

        CardReset(byte[] uid, OnCardListener listener) {
            userCtrlReset = () -> Mh1903Utils.authWrite(uid, B, BLOCK_USER_PERMIT, DEF_CTRL_BLOCK_DATA, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return listener.onSuccess();
                }

                @Override
                public Cmd onFail(String msg) {
                    if (hasSuccess) {
                        return listener.onSuccess();
                    }
                    return listener.onFail(msg);
                }
            });
            sysCtrlReset = () -> Mh1903Utils.authWrite(uid, B, BLOCK_SYS_PERMIT, DEF_CTRL_BLOCK_DATA, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    hasSuccess = true;
                    return userCtrlReset.getCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return userCtrlReset.getCmd();
                }
            });
            walletCtrlReset = () -> Mh1903Utils.authWrite(uid, B, BLOCK_WALLET_PERMIT, DEF_CTRL_BLOCK_DATA, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    hasSuccess = true;
                    return sysCtrlReset.getCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return sysCtrlReset.getCmd();
                }
            });
            orderReset = () -> createOrderReset(uid, 0, walletCtrlReset);
            infoWrite = new InfoWrite(uid, new CardInfo(), new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return orderReset.getCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return orderReset.getCmd();
                }
            });
        }

        public AbstractCmd getCmd() {
            return infoWrite.getCmd();
        }

        /**
         * 清理订单扇区数据和重置控制块
         * @param uid 卡号
         * @param sector 扇区索引
         * @param last 后续指令
         * @return cmd
         */
        private AbstractCmd createOrderReset(final byte[] uid, final int sector, final CmdProvider last) {
            AbstractCmd ctrlRest = Mh1903Utils.authWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[sector] * 4 + 3, DEF_CTRL_BLOCK_DATA, new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    hasSuccess = true;
                    return next();
                }

                @Override
                public Cmd onFail(String msg) {
                    return next();
                }

                private AbstractCmd next() {
                    return sector < SECTOR_ORDERS.length - 1 ? createOrderReset(uid, sector + 1, last): last.getCmd();
                }
            });
            return createOrderDataReset(uid, sector, 0, ctrlRest);
        }

        /**
         * 清理订单扇区数据
         * @param uid 卡号
         * @param sector 扇区索引
         * @param block 块号索引，相对于当前扇区
         * @param last 后续指令
         * @return cmd
         */
        private AbstractCmd createOrderDataReset(final byte[] uid, final int sector, final int block, final AbstractCmd last) {
            return Mh1903Utils.authWrite(uid, B, CardRisk.getKeyA(), SECTOR_ORDERS[sector] * 4 + block, new byte[16], new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return next();
                }

                @Override
                public Cmd onFail(String msg) {
                    return next();
                }

                private AbstractCmd next() {
                    return block < 2 ? createOrderDataReset(uid, sector, block + 1, last): last;
                }
            });
        }

    }

    /**
     * 卡扇区控制块初始化，使自定义密钥生效并配置数据块权限
     * @author mo_yq5
     * @since 2021/10/29
     */
    public static class CardInit implements CmdProvider {
        private final OnCardListener listener;
        private final CmdProvider walletCtrlInit;
        private final CmdProvider sysCtrlInit;
        private final CmdProvider userCtrlInit;
        private final CmdProvider orderCtrlInit;
        CardInit(byte[] uid, CardInfo info, OnCardListener listener) {
            this.listener = listener;
            walletCtrlInit = () -> Mh1903Utils.authWrite(uid, A, DEF_KEY_A, BLOCK_WALLET_PERMIT, getWalletCtrlData(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return sysCtrlInit.getCmd();
                }
            });
            sysCtrlInit = () -> Mh1903Utils.authWrite(uid, A, DEF_KEY_A, BLOCK_SYS_PERMIT, getSysCtrlData(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return userCtrlInit.getCmd();
                }
            });
            userCtrlInit = () -> Mh1903Utils.authWrite(uid, A, DEF_KEY_A, BLOCK_USER_PERMIT, getUserCtrlData(), new OnCardAdapter() {
                @Override
                public Cmd onSuccess() {
                    return orderCtrlInit.getCmd();
                }
            });
            orderCtrlInit = createOrderCtrlInitCmd(uid, 0, getOrderCtrlData(), listener, () -> new InfoWrite(uid, info, listener).getCmd());

        }

        public AbstractCmd getCmd() {
            return walletCtrlInit.getCmd();
        }

        /**
         * 递归初始化所有订单扇区控制块，兼容已经初始化过的扇区
         * @param uid 卡号
         * @param index 当前扇区索引
         * @param orderCtrlData 控制块数据
         * @param listener 回调
         * @param last 后续指令
         * @return cmd
         */
        public static CmdProvider createOrderCtrlInitCmd(byte[] uid, int index, byte[] orderCtrlData, OnCardListener listener, CmdProvider last) {
            CmdProvider next = index < SECTOR_ORDERS.length - 1 ? createOrderCtrlInitCmd(uid, index + 1, orderCtrlData, listener, last): last;
            return () -> Mh1903Utils.authWrite(uid, A, DEF_KEY_A, SECTOR_ORDERS[index] * 4 + 3, orderCtrlData, listener, next);
        }


        private abstract class OnCardAdapter implements OnCardListener {
            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }

    }

}
