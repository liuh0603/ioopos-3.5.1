package com.pay.ioopos.support.serialport.internal.icm522;

import static com.pay.ioopos.channel.card.CardUtils.blockDataToHex;
import static com.pay.ioopos.channel.card.CardUtils.blockDataToMix;
import static com.pay.ioopos.channel.card.CardUtils.blockDataToString;
import static com.pay.ioopos.channel.card.CardUtils.dataToAmount;
import static com.pay.ioopos.channel.card.KeyType.A;
import static com.pay.ioopos.trade.CardRisk.getKeyA;
import static com.pay.ioopos.trade.CardRisk.getKeyB;

import com.pay.ioopos.channel.card.KeyType;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.support.serialport.internal.AbstractCmd;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdProvider;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnCmdListener;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.support.serialport.internal.OnStringListener;

import java.util.List;

public class Icm522Utils {


    /**
     * 字符串内容块读取
     * @author  Moyq5
     * @since    2021/1/18 17:50
     * @param
     * @return
     */
    protected static AbstractCmd stringRead(KeyType type, int block, OnStringListener listener) {
        return blockRead(type, null, block, new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(blockDataToString(data));
            }

            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        });
    }

    /**
     * 从块读整数（钱包模式）
     * @author  Moyq5
     * @since    2021/1/18 17:46
     * @param
     * @return
     */
    protected static AbstractCmd walletRead(KeyType type, int block, OnIntListener listener) {
        AbstractCmdAdapter intRead = new CmdWalletRead(type, type == A ? getKeyA(): getKeyB(), block);
        intRead.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return listener.onFail("["+ type + block +"]卡读取失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(dataToAmount(data));
            }
        });

        return intRead;
    }

    /**
     * 读取16进制的内容
     * @author  Moyq5
     * @since    2021/1/18 17:42
     * @param
     * @return
     */
    protected static AbstractCmd hexRead(KeyType type, int block, OnStringListener listener) {
        return hexRead(type, null, block, listener);
    }

    /**
     * 读取16进制的内容
     * @author  Moyq5
     * @since    2021/1/18 17:42
     * @param
     * @return
     */
    protected static AbstractCmd hexRead(KeyType type, byte[] key, int block, OnStringListener listener) {
        return blockRead(type, key, block, new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(blockDataToHex(data));
            }

            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        });
    }

    /**
     * 读取混合(字符串+16进制)内容
     * @author  Moyq5
     * @since    2021/10/27 16:38
     * @param
     * @return
     */
    protected static AbstractCmd mixRead(KeyType type, int block, OnStringListener listener) {
        return mixRead(type, null, block, listener);
    }

    /**
     * 读取混合(字符串+16进制)内容
     * @author  Moyq5
     * @since    2021/10/27 16:38
     * @param
     * @return
     */
    protected static AbstractCmd mixRead(KeyType type, byte[] key, int block, OnStringListener listener) {
        return blockRead(type, key, block, new OnByteListener() {
            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(blockDataToMix(data));
            }

            @Override
            public Cmd onFail(String msg) {
                return listener.onFail(msg);
            }
        });
    }

    protected static AbstractCmd blockRead(KeyType type, byte[] key, int block, OnByteListener listener) {
        if (null == key) {
            key = type == A ? getKeyA(): getKeyB();
        }
        AbstractCmdAdapter blockRead = new CmdBlockRead(type, key, block);
        blockRead.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return listener.onFail("["+ type + block +"]卡读取失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                return listener.onSuccess(data);
            }
        });
        return blockRead;
    }

    protected static byte[] uid(byte[] data) {
        //byte[] uid = new byte[data.length - 2];// 头两个字节是其它信息
        //System.arraycopy(data, 2, uid, 0, uid.length);
        return data;
    }

    protected static AbstractCmdBlock createBlockWrite(KeyType type, int block, byte[] data) {
        return createBlockWrite(type, null, block, data);
    }

    protected static AbstractCmdBlock createBlockWrite(KeyType type, byte[] key, int block, byte[] data) {
        if (null == key) {
            key = type == A ? getKeyA(): getKeyB();
        }
        return new CmdBlockWrite(type, key, block, data);
    }

    protected static AbstractCmdBlock createWalletInit(KeyType type, int block, int amount) {
        return new CmdWalletInit(type, type == A ? getKeyA(): getKeyB(), block, amount);
    }

    protected static CmdProvider createWalletInitProvider(KeyType type, int block, int amount) {
        return new CmdProviderAbstract() {
            @Override
            public AbstractCmd getCmd() {
                AbstractCmd cmd = new CmdWalletInit(type, type == A ? getKeyA(): getKeyB(), block, amount);
                cmd.setListener(getListener());
                return cmd;
            }
        };
    }

    protected static CmdProvider createBlockWriteProvider(KeyType type, int block, byte[] data) {
        return createBlockWriteProvider(type, null, block, data);
    }

    protected static CmdProvider createBlockWriteProvider(KeyType type, byte[] key, int block, byte[] data) {
        if (null == key) {
            key = type == A ? getKeyA(): getKeyB();
        }
        byte[] dstKey = key;
        return new CmdProviderAbstract() {
            @Override
            public AbstractCmd getCmd() {
                AbstractCmd cmd = new CmdBlockWrite(type, dstKey, block, data);
                cmd.setListener(getListener());
                return cmd;
            }
        };
    }

    protected static void registerBlockWriteListener(List<CmdProvider> cmds, OnCardListener listener) {
        int size;
        if (null == cmds || (size = cmds.size()) == 0) {
            return;
        }
        for (int i = 0; i < size - 1; i++) {
            registerBlockWriteListener(cmds.get(i), listener, cmds.get(i + 1));
        }
        registerBlockWriteListener(cmds.get(size - 1), listener, null);
    }

    protected static void registerBlockWriteListener(CmdProvider cmd, OnCardListener listener, CmdProvider next) {
        ((CmdProviderAbstract)cmd).setListener(new OnCmdListener() {

            @Override
            public Cmd onFail(byte code) {
                AbstractCmdBlock dst = ((AbstractCmdBlock)cmd.getCmd());
                return listener.onFail("["+ dst.getType() + dst.getBlock() +"]卡写入失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if (null == next) {
                    return listener.onSuccess();
                }
                return next.getCmd();
            }
        });
    }

    protected static void registerBlockWriteListener(AbstractCmdBlock cmd, OnCardListener listener, AbstractCmdBlock next) {
        cmd.setListener(new OnCmdListener() {

            @Override
            public Cmd onFail(byte code) {
                return listener.onFail("["+ cmd.getType() + cmd.getBlock() +"]卡写入失败");
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if (null == next) {
                    return listener.onSuccess();
                }
                return next;
            }
        });
    }

    protected static void registerBlockWriteListener(AbstractCmdBlock cmd, OnCardListener listener) {
        registerBlockWriteListener(cmd, listener, null);
    }

}
