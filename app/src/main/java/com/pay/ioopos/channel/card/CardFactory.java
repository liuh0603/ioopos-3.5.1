package com.pay.ioopos.channel.card;

import static com.pay.ioopos.App.DEV_IS_ICM522;
import static com.pay.ioopos.App.DEV_IS_MH1903;
import static com.pay.ioopos.App.DEV_IS_MH1903_MT1;
import static com.pay.ioopos.App.DEV_IS_MH1903_MT2;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_ERROR;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_FAIL;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_NEW;
import static com.pay.ioopos.channel.card.CardOrder.STATUS_SUCCESS;
import static com.pay.ioopos.channel.card.CardUtils.crc;
import static com.pay.ioopos.support.serialport.internal.mh1093.Mh1903CmdCase.DEV_MT2;

import android.annotation.SuppressLint;
import android.util.Log;

import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.support.scan.ScanBeater;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnFailListener;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.support.serialport.internal.OnMutipleListener;
import com.pay.ioopos.support.serialport.internal.OnStringListener;
import com.pay.ioopos.support.serialport.internal.icm522.Icm522CmdCase;
import com.pay.ioopos.support.serialport.internal.icm522.Icm522Factory;
import com.pay.ioopos.support.serialport.internal.mh1093.Mh1903CmdCase;
import com.pay.ioopos.support.serialport.internal.mh1093.Mh1903Factory;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.CardRisk;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 艾博世卡操作机制
 *
 * @author Moyq5
 * @since 2020/11/4 14:16
 */
public abstract class CardFactory {
    private static final String TAG = CardFactory.class.getSimpleName();
    /**
     * 默认密钥A
     */
    public static final byte[] DEF_KEY_A = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 默认密钥B
     */
    public static final byte[] DEF_KEY_B = DEF_KEY_A;
    /**
     * 默认控制块数据
     */
    public static final byte[] DEF_CTRL_BLOCK_DATA = createCtrlBlockData(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, DEF_KEY_A, DEF_KEY_B);

    /**
     * 钱包状态说明所在块。
     * A密钥读、写
     */
    public static final int BLOCK_WALLET_DESCR = 14;

    /**
     * 钱包状态所在块，钱包格式，小于等于0表示卡被锁定或者挂失。
     * A密钥读、减
     */
    public static final int BLOCK_WALLET_STATUS = 4;
    /**
     * 钱包余额所在块，钱包格式。
     * A密钥读、减
     */
    public static final int BLOCK_WALLET_BALANCE = 5;
    /**
     * 钱包交易统计数据所在块:
     * 状态和余额校验值（1字节前4位）、
     * 最近离线交易笔数(1字节后4位)，以交易记录扇区数量为一个计算周期、
     * 当日交易笔数（1字节）、
     * 当日交易金额（3字节，分）、
     * 最近交易时间（5字节，秒，16进制）、
     * 最近交易记录所在扇区索引(1字节前4位)
     * 最近成功交易记录所在扇区索引(1字节后4位)
     * 余额（3字节，16进制，单位：分）
     * A密钥读、写
     */
    public static final int BLOCK_WALLET_STAT = 6;
    /**
     * 钱包控制块。
     * B密钥读、写
     */
    public static final int BLOCK_WALLET_PERMIT = 7;
    /**
     * 用户姓名所在块:长度(1字节)、内容。
     * A密钥读
     */
    public static final int BLOCK_USER_NAME = 8;
    /**
     * 用户学号或者工号所在块:长度(1字节)、内容。
     * A密钥读
     */
    public static final int BLOCK_USER_NO = 9;
    /**
     * 用户班级或者部门名称所在块:长度(1字节)、内容。
     * A密钥读
     */
    public static final int BLOCK_USER_GROUP = 10;
    /**
     * 用户信息控制块。
     * B密钥读、写
     */
    public static final int BLOCK_USER_PERMIT = 11;
    /**
     * 平台卡号:长度(1字节)、内容（16进制）。
     * A密钥读
     */
    public static final int BLOCK_SYS_CARD_NO = 12;
    /**
     * 平台商户号:长度(1字节)、内容（16进制）。
     * A密钥读
     */
    public static final int BLOCK_SYS_MERCH_NO = 13;
    /**
     * 平台参数控制块。
     * B密钥读、写
     */
    public static final int BLOCK_SYS_PERMIT = 15;
    /**
     * 钱包扇区存取控制设定
     */
    private static final int[] CTRL_SETTING_WALLET = {1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1};
    /**
     * 用户信息扇区存取控制设定
     */
    private static final int[] CTRL_SETTING_USER = {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1};
    /**
     * 平台参数扇区存取控制设定
     */
    private static final int[] CTRL_SETTING_SYS = {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1};
    /**
     * 订单扇区存取控制设定
     */
    private static final int[] CTRL_SETTING_ORDER = {1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1};
    /**
     * 交易流水扇区。默认密钥可读，A密钥读写
     * 扇区块1：交易设备号:长度(1字节, 前4位为字节长度，第5~7位为字符串格式前缀字节数，第8位为16进制部分长度奇偶性：0奇，1偶)、内容（支持混合格式：前n字节是字符串，后面为16进制）
     * 扇区块2：交易流水号:长度(1字节, 前4位为字节长度，第5~7位为字符串格式前缀字节数，第8位为16进制部分长度奇偶性：0奇，1偶)、内容（支持混合格式：前n字节是字符串，后面为16进制）
     * 扇区块3：交易金额、时间:交易前余额(3字节，16进制，分)、交易金额(3字节，16进制，分)、交易时间(5字节，16进制，秒)、状态(1字节前4位)、检验值(1字节后4位)
     */
    public static final int[] SECTOR_ORDERS = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private CardFactory() {
    }

    public static CmdCase getCmdCase() {
        return getCmdCase(null);
    }

    public static CmdCase getCmdCase(Cmd cmd) {
        if (DEV_IS_ICM522) {
            return new Icm522CmdCase(cmd);
        }
        if (DEV_IS_MH1903_MT1) {
            return new Mh1903CmdCase(cmd);
        }
        if (DEV_IS_MH1903_MT2) {
            return new Mh1903CmdCase(DEV_MT2, cmd);
        }
        return null;
    }


    /**
     * 读卡号，带读卡音效
     * @author  Moyq5
     * @since    2020/11/9 17:56
     */
    public static Cmd uidWait(OnStringListener listener, ScanBeater beater) {
        return uidWait(new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] uid) {
                return listener.onSuccess(HexUtils.toHexString(uid));
            }

            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        }, beater, null);
    }

    /**
     * 读卡号，字节格式，带音效
     * @author  Moyq5
     * @since    2021/10/28 13:47
     */
    public static Cmd uidWait(OnByteListener listener, ScanBeater beater) {
        return uidWait(listener, beater, null);
    }

    /**
     * 读卡号，带读卡音效
     * @author  Moyq5
     * @since    2020/11/9 17:56
     * @param   ignoredUid 忽略卡号
     * @return cmd
     */
    public static Cmd uidWait(OnByteListener listener, ScanBeater beater, byte[] ignoredUid) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.uidWait(listener, beater, ignoredUid);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.uidWait(listener, beater, ignoredUid);
            }
        });
    }

    /**
     * 查询余额
     * @author  Moyq5
     * @since    2021/1/20 16:36
     */
    public static Cmd balanceRead(byte[] uid, OnIntListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.balanceRead(uid, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.balanceRead(listener);
            }
        });
    }

    /**
     * 钱包充值:先读到卡再充值
     * @author  Moyq5
     * @since    2020/11/10 15:05
     * @param   amount 充值金额，单位：分
     * @return cmd
     */
    public static Cmd charge(byte[] uid, int amount, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.walletCharge(uid, amount, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.walletCharge(amount, listener);
            }
        });
    }

    /**
     * 钱包扣款
     * @author  Moyq5
     * @since    2020/11/4 15:12
     * @param   amount 扣款金额，单位：分
     * @return
     */
    public static Cmd walletPay(byte[] uid, int amount, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.walletPay(uid, amount, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.walletPay(amount, listener);
            }
        });
    }

    /**
     * 建卡、初始化，设置密钥、创建钱包、用户信息
     * @param cardInfo 建卡信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardInit(CardInfo cardInfo, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardInit(cardInfo, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardInit(cardInfo, listener);
            }
        });
    }

    /**
     * 写入平台卡号
     * @author  Moyq5
     * @since    2021/1/13 11:53
     */
    public static Cmd cardNoWrite(String hexUid, String cardNo, OnCardListener listener) {
        byte[] uid = HexUtils.toByteArray(hexUid);
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardNoWrite(uid, cardNo, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardNoWrite(cardNo, listener);
            }
        });
    }

    /**
     * 卡激活，即从锁定状态更新为正常状态
     * @author  Moyq5
     * @since    2021/1/13 10:04
     * @param uid 卡号
     * @param listener 回调
     * @return cmd
     */
    public static Cmd cardActive(byte[] uid, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardActive(uid, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardActive(listener);
            }
        });
    }

    /**
     * 读卡信息：余额、用户信息
     * @author  Moyq5
     * @since    2020/11/4 17:24
     */
    public static Cmd cardRead(OnCardInfoListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardRead(listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardRead(listener);
            }
        });
    }

    /**
     * 卡基本信息获取
     * @param listener 回调
     * @return
     */
    public static Cmd baseRead(OnCardBaseListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.baseRead(listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.baseRead(listener);
            }
        });
    }

    /**
     * 更新统计
     * @param uid 卡号
     * @param stat 要写入统计信息
     * @param listener 回调
     * @return
     */
    public static Cmd statWrite(byte[] uid, CardStat stat, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.statWrite(uid, stat, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.statWrite(stat, listener);
            }
        });
    }

    /**
     * 获取统计信息
     * @param uid 卡号
     * @param listener 回调
     * @return
     */
    public static Cmd statRead(byte[] uid, OnCardStatListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.statRead(uid, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.statRead(listener);
            }
        });
    }

    /**
     * 获取交易记录
     * @param uid 卡号
     * @param sector 数据所在扇区索引
     * @param listener 回调
     * @return
     */
    public static Cmd orderRead(byte[] uid, int sector, OnCardOrderListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.orderRead(uid, sector, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.orderRead(sector, listener);
            }
        });
    }

    /**
     * 写交易记录
     * @param uid 卡号
     * @param order 要写的信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd orderWrite(byte[] uid, CardOrder order, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.orderWrite(uid, order, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.orderWrite(order, listener);
            }
        });
    }

    /**
     * 更新状态
     * @param uid 卡号
     * @param order 订单信息
     * @param listener 回调
     * @return
     */
    public static Cmd orderWriteStatus(byte[] uid, CardOrder order, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.orderWriteStatus(uid, order, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.orderWriteStatus(order, listener);
            }
        });
    }

    /**
     * 卡重置，销户，恢复出厂值
     * @author    Moyq5
     * @since  2020/11/16 14:49
     */
    public static Cmd cardReset(byte[] uid, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardReset(uid, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardReset(listener);
            }
        });
    }

    /**
     * 卡（钱包）锁定
     * @author  Moyq5
     * @since    2021/1/15 10:45
     */
    public static Cmd cardLock(byte[] uid, String msg, OnCardListener listener) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.cardLock(uid, listener);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.cardLock(listener);
            }
        });
    }

    /**
     * 读卡号或者扫二维码
     * @author  Moyq5
     * @since    2021/3/26 15:36
     */
    public static Cmd uidOrQrcodeRead(int flags, OnMutipleListener listener, ScanBeater beater) {
        return modeCmd(new OnModelListener() {
            @Override
            public Cmd onMh1903() {
                return Mh1903Factory.uidOrQrcodeRead(flags, listener, beater);
            }

            @Override
            public Cmd onIcm522() {
                return Icm522Factory.uidOrQrcodeRead(flags, listener, beater);
            }
        });
    }

    private static Cmd modeCmd(OnModelListener listener) {
        if (DEV_IS_MH1903) {
            return listener.onMh1903();
        }
        return listener.onIcm522();
    }

    public static byte[] getWalletCtrlData() {
        return createCtrlBlockData(CTRL_SETTING_WALLET, CardRisk.getKeyA(), CardRisk.getKeyB());
    }

    public static byte[] getSysCtrlData() {
        return createCtrlBlockData(CTRL_SETTING_SYS, CardRisk.getKeyA(), CardRisk.getKeyB());
    }

    public static byte[] getUserCtrlData() {
        return createCtrlBlockData(CTRL_SETTING_USER, CardRisk.getKeyA(), CardRisk.getKeyB());
    }

    public static byte[] getOrderCtrlData() {
        return createCtrlBlockData(CTRL_SETTING_ORDER, DEF_KEY_A, CardRisk.getKeyA());
    }

    private static byte[] createCtrlBlockData(int[] ctrls, byte[] keyA, byte[] keyB) {
        byte[] data = new byte[16];
        System.arraycopy(keyA, 0, data, 0, 6);// A密钥
        System.arraycopy(keyB, 0, data, 10, 6);// B密钥

        // 控制位最后一个字节没用到，固定写0x69
        data[9] = 0x69;

        // 某扇区块0控制位
        data[7] |= ctrls[0] << 4;   // C10
        data[8] |= ctrls[1] << 0;   // C20
        data[8] |= ctrls[2] << 4;   // C30
        // 某扇区块1控制位
        data[7] |= ctrls[3] << 5;   // C11
        data[8] |= ctrls[4] << 1;   // C21
        data[8] |= ctrls[5] << 5;   // C31
        // 某扇区块2控制位
        data[7] |= ctrls[6] << 6;   // C12
        data[8] |= ctrls[7] << 2;   // C22
        data[8] |= ctrls[8] << 6;   // C32
        // 某扇区块3控制位
        data[7] |= ctrls[9] << 7;   // C13
        data[8] |= ctrls[10] << 3;  // C23
        data[8] |= ctrls[11] << 7;  // C33


        data[6] = (byte) ( (data[7] & (1 << 4) ) > 0 ? ( data[6] & ( ~(1 << 0) & 0xFF ) ): ( data[6] | (1 << 0) ) );    // C10_b
        data[6] = (byte) ( (data[7] & (1 << 5) ) > 0 ? ( data[6] & ( ~(1 << 1) & 0xFF ) ): ( data[6] | (1 << 1) ) );    // C11_b
        data[6] = (byte) ( (data[7] & (1 << 6) ) > 0 ? ( data[6] & ( ~(1 << 2) & 0xFF ) ): ( data[6] | (1 << 2) ) );    // C12_b
        data[6] = (byte) ( (data[7] & (1 << 7) ) > 0 ? ( data[6] & ( ~(1 << 3) & 0xFF ) ): ( data[6] | (1 << 3) ) );    // C13_b

        data[6] = (byte) ( (data[8] & (1 << 0) ) > 0 ? ( data[6] & ( ~(1 << 4) & 0xFF ) ): ( data[6] | (1 << 4) ) );    // C20_b
        data[6] = (byte) ( (data[8] & (1 << 1) ) > 0 ? ( data[6] & ( ~(1 << 5) & 0xFF ) ): ( data[6] | (1 << 5) ) );    // C21_b
        data[6] = (byte) ( (data[8] & (1 << 2) ) > 0 ? ( data[6] & ( ~(1 << 6) & 0xFF ) ): ( data[6] | (1 << 6) ) );    // C22_b
        data[6] = (byte) ( (data[8] & (1 << 3) ) > 0 ? ( data[6] & ( ~(1 << 7) & 0xFF ) ): ( data[6] | (1 << 7) ) );    // C23_b

        data[7] = (byte) ( (data[8] & (1 << 4) ) > 0 ? ( data[7] & ( ~(1 << 0) & 0xFF ) ): ( data[7] | (1 << 0) ) );    // C30_b
        data[7] = (byte) ( (data[8] & (1 << 5) ) > 0 ? ( data[7] & ( ~(1 << 1) & 0xFF ) ): ( data[7] | (1 << 1) ) );    // C31_b
        data[7] = (byte) ( (data[8] & (1 << 6) ) > 0 ? ( data[7] & ( ~(1 << 2) & 0xFF ) ): ( data[7] | (1 << 2) ) );    // C32_b
        data[7] = (byte) ( (data[8] & (1 << 7) ) > 0 ? ( data[7] & ( ~(1 << 3) & 0xFF ) ): ( data[7] | (1 << 3) ) );    // C33_b

        return data;
    }

    public static Cmd orderUpdateByBalance(CardBase base, CardOrder order, OnCardOrderUpdateListener listener) {
        // 非法信息，忽略
        if (null == order || crc(order) != order.getCrc()) {
            Log.d(TAG, "卡最近交易无效：" + order);
            return listener.onNormal();
        }
        // 正常状态
        if (order.getStatus() != STATUS_NEW && order.getStatus() != STATUS_ERROR) {
            Log.d(TAG, "卡最近交易正常");
            return listener.onNormal();
        }
        // 异常状态，尝试修复
        if (base.getBalance() == order.getBalance()) {// 确实未支付，可纠正
            Log.d(TAG, "卡最近交易更新：" + order.getStatus() + "->" + STATUS_FAIL);
            order.setStatus(STATUS_FAIL);
        } else if (base.getBalance() == order.getBalance() - order.getAmount()) {// 实际已支付，可纠正
            Log.d(TAG, "卡最近交易更新：" + order.getStatus() + "->" + STATUS_SUCCESS);
            order.setStatus(STATUS_SUCCESS);
            OrderUtils.proxyPay(base.getCardNo(), order);
        } else {
            Log.w(TAG, "卡最近交易异常：" + base.getBalance() + "<?>" + order.getBalance());
            LogUtils.warn("状态校验：卡余额异常：%s => %d<?>%d, state=%d", base.getCardNo(), base.getBalance(), order.getBalance(), order.getStatus());
            return listener.onFail("卡余额异常，请到管理处确认");
        }
        return CardFactory.orderWriteStatus(HexUtils.toByteArray(base.getCardUid()), order, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                Log.w(TAG, "卡最近交易更新失败：" + msg);
                return listener.onFail(msg);
            }

            @Override
            public Cmd onSuccess() {
                return listener.onUpdate(order);
            }
        });
    }

    /**
     * 移动订单并更新统计
     * @param uid 卡号
     * @param order 原订单
     * @param stat 统计信息
     * @param listener 回调
     * @return cmd
     */
    public static Cmd statUpdateByOrder(byte[] uid, CardOrder order, CardStat stat, OnCardListener listener) {
        if (null == stat) {
            stat = new CardStat();
        }
        boolean sectorRecycle = stat.getLastSector() >= SECTOR_ORDERS.length - 1;
        stat.setLastSector(1 + (sectorRecycle ? 0: stat.getLastSector()));// 0索引扇区固定用于保存最新记录
        if (order.isSuccess()) {
            int raiseOffline = isNetworkAvailable() ? 0: 1;
            int raiseAmount = order.getAmount();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            boolean isNewDay = !format.format(new Date(stat.getLastTime() * 1000))
                    .equals(format.format(new Date(System.currentTimeMillis())));

            stat.setOfflineCount(raiseOffline + (sectorRecycle ? 0: stat.getOfflineCount()));
            stat.setDayAmount(raiseAmount + (isNewDay ? 0: stat.getDayAmount()));
            stat.setDayCount(1 + (isNewDay ? 0: stat.getDayCount()));
            stat.setSuccSector(stat.getLastSector());
            if (order.getOrderTime() >= stat.getLastTime()) {
                stat.setBalance(order.getBalance() - order.getAmount());
            }
        } else {
            // TODO 存量卡兼容处理：失败的交易，时间比较新，且余额备份不正常的情况下，余额备份以交易前余额为准
            if (order.getOrderTime() >= stat.getLastTime() && stat.getBalance() == 0) {
                stat.setBalance(order.getBalance());
            }
        }
        stat.setLastTime(order.getOrderTime());
        stat.setCrc(crc(stat, order));
        final CardOrder fOrder = new CardOrder(order.getDevSn(), order.getOrderNo(), order.getBalance(), order.getAmount(), order.getOrderTime(), order.getStatus(), stat.getLastSector(), 0);
        final CardStat fStat = stat;
        return CardFactory.orderWrite(uid, fOrder, new OnCardListener() {

            @Override
            public Cmd onFail(String msg) {
                return null != listener ? listener.onFail(msg): null;
            }

            @Override
            public Cmd onSuccess() {
                return CardFactory.statWrite(uid, fStat, new OnCardListener() {

                    @Override
                    public Cmd onFail(String msg) {
                        return null != listener ? listener.onFail(msg): null;
                    }

                    @Override
                    public Cmd onSuccess() {
                        return null != listener ? listener.onSuccess(): null;
                    }

                });
            }
        });
    }

    public interface OnCardOrderUpdateListener extends OnFailListener {
        Cmd onNormal();
        Cmd onUpdate(CardOrder order);
    }
}
