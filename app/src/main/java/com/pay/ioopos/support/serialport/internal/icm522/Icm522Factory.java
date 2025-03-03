package com.pay.ioopos.support.serialport.internal.icm522;

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
import static com.pay.ioopos.channel.card.CardUtils.hexToBlockData;
import static com.pay.ioopos.channel.card.CardUtils.mixToBlockData;
import static com.pay.ioopos.channel.card.CardUtils.statToBlockData;
import static com.pay.ioopos.channel.card.CardUtils.stringToBlockData;
import static com.pay.ioopos.channel.card.KeyType.A;
import static com.pay.ioopos.channel.card.KeyType.B;
import static com.pay.ioopos.common.HexUtils.toHexString;
import static com.pay.ioopos.trade.CardRisk.getKeyA;
import static com.pay.ioopos.trade.CardRisk.getKeyB;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 艾博世卡操作机制
 *
 * @author Moyq5
 * @since 2020/11/4 14:16
 */
public abstract class Icm522Factory {

    private Icm522Factory() {
    }

    /**
     * 读卡号，忽略指定卡号
     * @author  Moyq5
     * @since    2021/1/6 11:25
     * @param listener 回调
     * @param beater 音效
     * @param ignoredUid 忽略的卡号
     * @return cmd
     */
    public static Cmd uidWait(OnByteListener listener, ScanBeater beater, byte[] ignoredUid) {
        AbstractCmdAdapter uidRead = new CmdUidWait();
        uidRead.setListener(new OnCmdBeatListener(beater) {
            @Override
            public Cmd onFail(byte code) {
                super.onFail(code);
                return listener.onFail("刷卡失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                super.onSuccess(data);
                byte[] uid = Icm522Utils.uid(data);
                if (null != ignoredUid && Arrays.equals(ignoredUid, uid)) {
                    return uidRead;
                }
                return listener.onSuccess(uid);
            }
        });
        return uidRead;
    }

    /**
     * 同时读卡或者扫码
     * @author  Moyq5
     * @since    2021/3/26 15:44
     * @param flags 功能标志
     * @param listener 回调
     * @param beater 音效
     * @return cmd
     */
    public static Cmd uidOrQrcodeRead(int flags, OnMutipleListener listener, ScanBeater beater) {
        if ((flags & 1) == 0) {// 该ICM522模块只支持读卡，且flag标志说不要读卡
            return null;
        }
        AbstractCmdAdapter uidRead = new CmdUidWait();
        uidRead.setListener(new OnCmdBeatListener(beater) {
            @Override
            public Cmd onFail(byte code) {
                super.onFail(code);
                return listener.onFail(1,"刷卡失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                super.onSuccess(data);
                String uid = toHexString(Icm522Utils.uid(data));
                return listener.onSuccess(1, uid);
            }
        });
        return uidRead;
    }

    /**
     * 查询余额
     * @author  Moyq5
     * @since    2021/1/20 16:40
     * @param listener 回调
     * @return cmd
     */
    public static Cmd balanceRead(OnIntListener listener) {
        return Icm522Utils.walletRead(A, BLOCK_WALLET_BALANCE, listener);
    }

    /**
     * 钱包充值:直接充值
     * @author  Moyq5
     * @since    2020/11/4 15:09
     * @param   amount 充值金额，单位：分
     * @param listener 回调
     * @return cmd
     */
    public static Cmd walletCharge(int amount, OnCardListener listener) {
        AbstractCmdAdapter walletCharge = new CmdWalletCharge(B, getKeyB(), BLOCK_WALLET_BALANCE, amount);
        walletCharge.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return listener.onFail("钱包充值异常");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess();
            }
        });
        return walletCharge;
    }

    /**
     * 钱包扣款
     * @author  Moyq5
     * @since    2020/11/4 15:12
     * @param   amount 扣款金额，单位：分
     * @param listener 回调
     * @return cmd
     */
    public static Cmd walletPay(int amount, OnCardListener listener) {
        AbstractCmdAdapter walletPay = new CmdWalletPay(A, getKeyA(), BLOCK_WALLET_BALANCE, amount);
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
    }

    /**
     * 卡重置，销户，恢复出厂值
     * @author    Moyq5
     * @since  2020/11/16 14:49
     */
    public static Cmd cardReset(OnCardListener listener) {
        return new CardReset(listener).getCmd();
    }

    public static Cmd cardActive(OnCardListener listener) {
        AbstractCmdBlock initWrite = Icm522Utils.createWalletInit(B, BLOCK_WALLET_STATUS, 1);
        Icm522Utils.registerBlockWriteListener(initWrite, listener);
        return initWrite;
    }

    public static Cmd cardNoWrite(String cardNo, OnCardListener listener) {
        AbstractCmdBlock cardNoWrite = Icm522Utils.createBlockWrite(B, BLOCK_SYS_CARD_NO, hexToBlockData(cardNo));
        Icm522Utils.registerBlockWriteListener(cardNoWrite, listener);
        return cardNoWrite;
    }

    /**
     * 卡（钱包）锁定
     * @author  Moyq5
     * @since    2021/1/18 17:57
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardLock(OnCardListener listener) {
        AbstractCmdAdapter walletPay = new CmdWalletPay(A, getKeyA(), BLOCK_WALLET_STATUS, 10);// 钱包状态减值，只要小于1此卡就为锁定状态
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
    }

    /**
     * 读卡信息：余额、用户信息
     * @author  Moyq5
     * @since    2021/1/18 18:24
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardRead(OnCardInfoListener listener) {
        return new InfoRead(listener).getCmd();
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
     * @param stat 统计信息
     * @param listener  回调
     * @return cmd
     */
    public static Cmd statWrite(CardStat stat, OnCardListener listener) {
        AbstractCmdBlock statWrite = Icm522Utils.createBlockWrite(A, BLOCK_WALLET_STAT, statToBlockData(stat));
        Icm522Utils.registerBlockWriteListener(statWrite, listener);
        return statWrite;
    }

    /**
     * 获取统计信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd statRead(OnCardStatListener listener) {
        return Icm522Utils.blockRead(A, getKeyA(), BLOCK_WALLET_STAT, new OnByteListener() {
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
     * @param sector 数据所在扇区索引
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderRead(int sector, OnCardOrderListener listener) {
        return new OrderRead(sector, listener).getCmd();
    }

    /**
     * 添加、更新交易记录
     * @param order 订单
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderWrite(CardOrder order, OnCardListener listener) {
        return new OrderWrite(order, listener).getCmd();
    }

    /**
     * 只更新状态
     * @param order 更新信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderWriteStatus(CardOrder order, OnCardListener listener) {
        return new OrderWriteStatus(order, listener).getCmd();
    }

    /**
     * 建卡、初始化，设置密钥、创建钱包、用户信息
     * @author  Moyq5
     * @since    2021/1/18 15:05
     * @param card 初始卡信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardInit(CardInfo card, OnCardListener listener) {
        return new CardInit(card, listener).getCmd();
    }

    public static class BaseRead implements CmdProvider {
        private final CardBase base = new CardBase();
        private final OnCardBaseListener listener;
        private final CmdProvider uidRead;
        private final CmdProvider merchNoRead;
        private final CmdProvider cardNoRead;
        private final CmdProvider balanceRead;
        private final CmdProvider statusRead;
        private final CmdProvider descrRead;

        BaseRead(OnCardBaseListener listener) {
            this.listener = listener;

            uidRead = () -> {
                CmdUidRead uidRead = new CmdUidRead();
                uidRead.setListener(new OnCmdListener() {

                    @Override
                    public Cmd onFail(byte code) {
                        return listener.onFail("读卡失败");
                    }

                    @Override
                    public Cmd onSuccess(byte[] data) {
                        base.setCardUid(toHexString(Icm522Utils.uid(data)));
                        return merchNoRead.getCmd();
                    }
                });
                return uidRead;
            };
            merchNoRead = () -> Icm522Utils.hexRead(A, BLOCK_SYS_MERCH_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String merchNo) {
                    base.setMerchNo(merchNo);
                    return cardNoRead.getCmd();
                }
            });
            cardNoRead = () -> Icm522Utils.hexRead(A, BLOCK_SYS_CARD_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String cardNo) {
                    base.setCardNo(cardNo);
                    return balanceRead.getCmd();
                }
            });
            balanceRead = () -> Icm522Utils.walletRead(A, BLOCK_WALLET_BALANCE, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int balance) {
                    base.setBalance(balance);
                    return statusRead.getCmd();
                }
            });
            statusRead = () -> Icm522Utils.walletRead(A, BLOCK_WALLET_STATUS, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int status) {
                    base.setStatus(status);
                    return descrRead.getCmd();
                }
            });
            descrRead = () -> Icm522Utils.stringRead(A, BLOCK_WALLET_DESCR, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String descr) {
                    base.setDescr(descr);
                    return listener.onSuccess(base);
                }

                @Override
                public Cmd onFail(String msg) {
                    return listener.onSuccess(base);// TODO 兼容旧卡
                    //return listener.onFail(msg);
                }
            });

        }

        public AbstractCmd getCmd() {
            return uidRead.getCmd();
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
        private final int sector;
        private final OnCardOrderListener listener;

        private String devSn;
        private String orderNo;

        OrderRead(int sector, OnCardOrderListener listener) {
            this.sector = sector;
            this.listener = listener;
        }

        public AbstractCmd getCmd() {
            return createDevSnReadCmd();
        }

        private AbstractCmd createDevSnReadCmd() {
            return Icm522Utils.mixRead(A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4, new OnStringAdapter() {
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
            return Icm522Utils.hexRead(A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4 + 1, new OnStringAdapter() {
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
            return Icm522Utils.blockRead(A, DEF_KEY_A, SECTOR_ORDERS[sector] * 4 + 2, new OnByteAdapter() {
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
        private final List<CmdProvider> providers = new ArrayList<>();

        OrderWrite(CardOrder order, OnCardListener listener) {

            providers.add(Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4, mixToBlockData(order.getDevSn(), 5)));
            providers.add(Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(),  SECTOR_ORDERS[order.getSector()] * 4 + 1, hexToBlockData(order.getOrderNo())));
            providers.add(Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4 + 2, createAmountBlockData(order)));

            Icm522Utils.registerBlockWriteListener(providers, listener);

            // TODO 兼容旧卡过渡的实现->start：如果写订单失败，尝试初始化订单扇区后再写入，旧卡过渡完成后可移除此块代码
            CmdProviderAbstract providerAbstract = (CmdProviderAbstract) Icm522Utils.createBlockWriteProvider(A, DEF_KEY_A, SECTOR_ORDERS[order.getSector()] * 4 + 3, getOrderCtrlData());
            providerAbstract.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onSuccess();
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return providers.get(0).getCmd();
                }
            });
            Icm522Utils.registerBlockWriteListener(providers.get(0), new OnCardListener() {
                @Override
                public Cmd onSuccess() {
                    return providers.get(1).getCmd();
                }

                @Override
                public Cmd onFail(String msg) {
                    return providerAbstract.getCmd();
                }
            }, null);
            // 兼容旧卡过渡的实现->end

        }

        public AbstractCmd getCmd() {
            return providers.get(0).getCmd();
        }

    }

    public static class OrderWriteStatus implements CmdProvider {
        private final AbstractCmd amountWrite;

        OrderWriteStatus(CardOrder order, OnCardListener listener) {
            amountWrite = Icm522Utils.createBlockWrite(B, CardRisk.getKeyA(), SECTOR_ORDERS[order.getSector()] * 4 + 2, createAmountBlockData(order));
            Icm522Utils.registerBlockWriteListener((AbstractCmdBlock) amountWrite, listener);
        }

        public AbstractCmd getCmd() {
            return amountWrite;
        }
    }

    public static class InfoRead implements CmdProvider {
        private final CardInfo info = new CardInfo();
        private final OnCardInfoListener listener;
        private final AbstractCmd uidRead;
        private final CmdProvider userNameRead;
        private final CmdProvider userNoRead;
        private final CmdProvider userGroupRead;
        private final CmdProvider merchNoRead;
        private final CmdProvider cardNoRead;
        private final CmdProvider balanceRead;
        private final CmdProvider statusRead;
        private final CmdProvider statRead;

        InfoRead(OnCardInfoListener listener) {
            this.listener = listener;
            info.setUser(new CardUser());

            uidRead = new CmdUidRead();
            uidRead.setListener(new OnCmdListener() {

                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("读卡失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    info.setCardUid(toHexString(Icm522Utils.uid(data)));
                    return userNameRead.getCmd();
                }
            });

            userNameRead = () -> Icm522Utils.stringRead(A, BLOCK_USER_NAME, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserName(value);
                    return userNoRead.getCmd();
                }
            });
            userNoRead = () -> Icm522Utils.stringRead(A, BLOCK_USER_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserNo(value);
                    return userGroupRead.getCmd();
                }
            });
            userGroupRead = () -> Icm522Utils.stringRead(A, BLOCK_USER_GROUP, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String value) {
                    info.getUser().setUserGroup(value);
                    return merchNoRead.getCmd();
                }
            });
            merchNoRead = () -> Icm522Utils.hexRead(A, BLOCK_SYS_MERCH_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String merchNo) {
                    info.setMerchNo(merchNo);
                    return cardNoRead.getCmd();
                }
            });
            cardNoRead = () -> Icm522Utils.hexRead(A, BLOCK_SYS_CARD_NO, new OnStringAdapter() {
                @Override
                public Cmd onSuccess(String cardNo) {
                    info.setCardNo(cardNo);
                    return balanceRead.getCmd();
                }
            });
            balanceRead = () -> Icm522Utils.walletRead(A, BLOCK_WALLET_BALANCE, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int balance) {
                    info.setBalance(balance);
                    return statusRead.getCmd();
                }
            });
            statusRead = () -> Icm522Utils.walletRead(A, BLOCK_WALLET_STATUS, new OnIntAdapter() {
                @Override
                public Cmd onSuccess(int status) {
                    info.setStatus(status);
                    return statRead.getCmd();
                }
            });
            statRead = () -> (AbstractCmd) statRead(new OnCardStatListener() {
                @Override
                public Cmd onSuccess(CardStat stat) {
                    info.setStat(stat);
                    return listener.onSuccess(info);
                }

                @Override
                public Cmd onFail(String msg) {
                    return listener.onFail(msg);
                }
            });
        }

        public AbstractCmd getCmd() {
            return uidRead;
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


    /**
     * 重置卡，重置卡数据内容成功与否不重要，重要的是能重置控制块密钥，使能重新写卡。
     * 为适应某些场景，支持在某控制块重置失败的情况下仍然可以重置其后续的控制块，即支持“重复”操作重置
     */
    public static class CardReset implements CmdProvider {
        private boolean hasSuccess = false;
        private final OnCardListener listener;
        private final List<CmdProvider> cleanCmds = new ArrayList<>();

        CardReset(OnCardListener listener) {
            this.listener = listener;

            byte[] defData = new byte[16];

            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_NAME, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_NO, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_GROUP, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_SYS_MERCH_NO, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_SYS_CARD_NO, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(A, BLOCK_WALLET_STAT, defData));

            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_WALLET_BALANCE, defData));
            cleanCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_WALLET_STATUS, defData));

            for (int sector : SECTOR_ORDERS) {
                cleanCmds.add((Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), sector * 4, defData)));
                cleanCmds.add((Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), sector * 4 + 1, defData)));
                cleanCmds.add((Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), sector * 4 + 2, defData)));
            }

            List<CmdProvider> ctrlCmds = new ArrayList<>();

            ctrlCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_WALLET_PERMIT, DEF_CTRL_BLOCK_DATA));
            ctrlCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_SYS_PERMIT, DEF_CTRL_BLOCK_DATA));
            ctrlCmds.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_PERMIT, DEF_CTRL_BLOCK_DATA));

            for (int sector : SECTOR_ORDERS) {
                ctrlCmds.add((Icm522Utils.createBlockWriteProvider(B, CardRisk.getKeyA(), sector * 4 + 3, DEF_CTRL_BLOCK_DATA)));
            }

            cleanCmds.add(ctrlCmds.get(0));
            registerDataResetListener(cleanCmds);
            registerCtrlResetListener(ctrlCmds);
        }

        public AbstractCmd getCmd() {
            return cleanCmds.get(0).getCmd();
        }

        private void registerDataResetListener(List<CmdProvider> cmds) {
            int size;
            if (null == cmds || (size = cmds.size()) == 0) {
                return;
            }
            for (int i = 0; i < size - 1; i++) {
                registerDataResetListener(cmds.get(i), cmds.get(i + 1));
            }
            registerDataResetListener(cmds.get(size - 1), null);
        }

        /**
         * 注册数据清理指令的回调，无论成功与否都会继续执行下一条指令
         * @param cur 当前指令
         * @param next 下一条指令
         */
        private void registerDataResetListener(CmdProvider cur, CmdProvider next) {
            ((CmdProviderAbstract)cur).setListener(new OnCmdListener() {

                @Override
                public Cmd onFail(byte code) {
                    if (null != next) {
                        return next.getCmd();
                    }
                    return null;
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    if (null != next) {
                        return next.getCmd();
                    }
                    return null;
                }
            });

        }

        private void registerCtrlResetListener(List<CmdProvider> cmds) {
            int size ;
            if (null == cmds || (size = cmds.size()) == 0) {
                return;
            }
            for (int i = 0; i < size - 1; i++) {
                registerCtrlResetListener(cmds.get(i), cmds.get(i + 1));
            }
            registerCtrlResetListener(cmds.get(size - 1), null);
        }

        /**
         * 注册扇区控制块重置指令的回调，无论成功与否都继续执行下一条指令，
         * 如果中间有一条指令成功，则最终指令也返回成功
         * @param cur 当前指令
         * @param next 下一条指令
         */
        private void registerCtrlResetListener(CmdProvider cur, CmdProvider next) {
            ((CmdProviderAbstract)cur).setListener(new OnCmdListener() {

                @Override
                public Cmd onFail(byte code) {
                    if (null == next) {
                        if (hasSuccess) {
                            return listener.onSuccess();
                        }
                        AbstractCmdBlock cmd = (AbstractCmdBlock)cur.getCmd();
                        return listener.onFail("["+ cmd.getType() + cmd.getBlock() +"]卡写入失败");
                    }
                    return next.getCmd();

                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    if (null == next) {
                        return listener.onSuccess();
                    }
                    hasSuccess = true;
                    return next.getCmd();
                }
            });

        }
    }


    /**
     * 卡扇区控制块初始化，使自定义密钥生效并配置数据块权限
     * @author mo_yq5
     * @since 2021/11/05
     */
    public static class CardInit implements CmdProvider {

        private final List<CmdProvider> providers = new ArrayList<>();

        public CardInit(CardInfo info, OnCardListener listener) {

            CardUser user = info.getUser();
            if (null == user) {
                user = new CardUser();
            }

            providers.add(Icm522Utils.createBlockWriteProvider(A, DEF_KEY_A, BLOCK_WALLET_PERMIT, getWalletCtrlData()));
            providers.add(Icm522Utils.createBlockWriteProvider(A, DEF_KEY_A, BLOCK_SYS_PERMIT, getSysCtrlData()));
            providers.add(Icm522Utils.createBlockWriteProvider(A, DEF_KEY_A, BLOCK_USER_PERMIT, getUserCtrlData()));

            for (int sector: SECTOR_ORDERS) {
                providers.add(Icm522Utils.createBlockWriteProvider(A, DEF_KEY_A, sector * 4 + 3, getOrderCtrlData()));
            }

            providers.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_NAME, stringToBlockData(user.getUserName())));
            providers.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_NO, stringToBlockData(user.getUserNo())));
            providers.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_USER_GROUP, stringToBlockData(user.getUserGroup())));
            providers.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_SYS_MERCH_NO, hexToBlockData(info.getMerchNo())));
            providers.add(Icm522Utils.createBlockWriteProvider(B, BLOCK_SYS_CARD_NO, hexToBlockData(info.getCardNo())));
            providers.add(Icm522Utils.createBlockWriteProvider(A, BLOCK_WALLET_STAT, statToBlockData(null)));
            providers.add(Icm522Utils.createWalletInitProvider(B, BLOCK_WALLET_BALANCE, info.getBalance()));
            providers.add(Icm522Utils.createWalletInitProvider(B, BLOCK_WALLET_STATUS, info.getStatus()));

            Icm522Utils.registerBlockWriteListener(providers, listener);
        }

        public AbstractCmd getCmd() {
            return providers.get(0).getCmd();
        }
    }

}
