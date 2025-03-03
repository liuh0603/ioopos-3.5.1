package com.pay.ioopos.support.serialport.internal.mh1093;

import static com.pay.ioopos.channel.card.CardUtils.blockDataToHex;
import static com.pay.ioopos.channel.card.CardUtils.blockDataToMix;
import static com.pay.ioopos.channel.card.CardUtils.blockDataToString;
import static com.pay.ioopos.channel.card.CardUtils.dataToAmount;
import static com.pay.ioopos.channel.card.CardUtils.hexToBlockData;
import static com.pay.ioopos.channel.card.CardUtils.mixToBlockData;
import static com.pay.ioopos.channel.card.CardUtils.stringToBlockData;
import static com.pay.ioopos.channel.card.KeyType.A;
import static com.pay.ioopos.trade.CardRisk.getKeyA;
import static com.pay.ioopos.trade.CardRisk.getKeyB;

import com.pay.ioopos.channel.card.KeyType;
import com.pay.ioopos.channel.card.OnCardListener;
import com.pay.ioopos.support.serialport.internal.AbstractCmd;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdPackException;
import com.pay.ioopos.support.serialport.internal.CmdProvider;
import com.pay.ioopos.support.serialport.internal.OnByteListener;
import com.pay.ioopos.support.serialport.internal.OnCmdListener;
import com.pay.ioopos.support.serialport.internal.OnFailListener;
import com.pay.ioopos.support.serialport.internal.OnIntListener;
import com.pay.ioopos.support.serialport.internal.OnStringListener;

/**
 * @author mo_yq5
 * @since 2021/10/28
 */
public abstract class Mh1903Utils {


    protected static byte[] uid(byte[] data) {
        if (data.length < 2) {
            throw new CmdPackException("uid获取失败");
        }
        byte[] uid = new byte[data.length - 2];// 头尾两个字节是其它信息
        System.arraycopy(data, 1, uid, 0, uid.length);
        return uid;
    }

    protected static byte[] amountToBytes(int value) {
        byte[] data = new byte[6];
        for (int i = 0; i < 4; i++) {
            data[5 - i] = (byte) ( ( value >> (8 * (3 - i)) ) & 0xFF);// 按低字节在前，从高字节开始填
        }
        return data;
    }


    /**
     * 写字符串内容到块
     * @param uid 卡号
     * @param type 密钥类型
     * @param block 块号
     * @param info 要写入的信息
     * @param listener 回调
     * @return cmd
     */
    protected static AbstractCmd stringWrite(byte[] uid, KeyType type, int block, String info, OnCardListener listener) {
        return authWrite(uid, type, block, stringToBlockData(info), listener);
    }

    /**
     * 字符串内容块读取
     * @author  Moyq5
     * @since    2021/1/11 18:49
     * @param uid 卡号
     * @param type 密钥类型
     * @param block 块号
     * @param listener 回调
     * @return cmd
     */
    protected static AbstractCmd stringRead(byte[] uid, KeyType type, int block, OnStringListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter stringRead = new CmdBlockRead(block);
            stringRead.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("["+ type + block +"]卡读取失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess(blockDataToString(data));
                }
            });
            return stringRead;
        };

        return auth(uid, type, block, listener, next);
    }

    /**
     * 写16进制内容到块
     * @author  Moyq5
     * @since    2021/1/11 18:54
     */
    protected static AbstractCmd hexWrite(byte[] uid, KeyType type, int block, String hex, OnCardListener listener) {
        return authWrite(uid, type, block, hexToBlockData(hex), listener);
    }

    /**
     * 写16进制内容到块
     * @author  Moyq5
     * @since    2021/1/11 18:54
     */
    protected static AbstractCmd hexWrite(byte[] uid, KeyType type, byte[] key, int block, String hex, OnCardListener listener, CmdProvider dst) {
        return authWrite(uid, type, key, block, hexToBlockData(hex), listener, dst);
    }

    /**
     * 读取16进制的内容
     * @author  Moyq5
     * @since    2021/1/11 18:30
     */
    protected static AbstractCmd hexRead(byte[] uid, KeyType type, int block, OnStringListener listener) {
        return hexRead(uid, type, null, block, listener);
    }

    /**
     * 读取16进制的内容
     * @author  Moyq5
     * @since    2021/1/11 18:30
     */
    protected static AbstractCmd hexRead(byte[] uid, KeyType type, byte[] key, int block, OnStringListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter hexRead = new CmdBlockRead(block);
            hexRead.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("["+ type + block +"]卡读取失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess(blockDataToHex(data));
                }
            });
            return hexRead;
        };

        return auth(uid, type, key, block, listener, next);
    }

    /**
     * 读取混合(字符串+16进制)内容，使用内置密钥
     * @author  Moyq5
     * @since    2021/10/27 12:00
     */
    protected static AbstractCmd mixRead(byte[] uid, KeyType type, int block, OnStringListener listener) {
        return mixRead(uid, type, null, block, listener);
    }

    /**
     * 读取混合(字符串+16进制)内容，使用指定密钥
     * @author  Moyq5
     * @since    2021/10/27 12:00
     */
    protected static AbstractCmd mixRead(byte[] uid, KeyType type, byte[] key, int block, OnStringListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter mixRead = new CmdBlockRead(block);
            mixRead.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("["+ type + block +"]卡读取失败");
                }

                @Override
                public Cmd onSuccess(byte[] data) {
                    return listener.onSuccess(blockDataToMix(data));
                }
            });
            return mixRead;
        };

        return auth(uid, type, key, block, listener, next);
    }

    /**
     * 写混合内容到块
     * @author  Moyq5
     * @since    2021/10/27 13:56
     */
    protected static AbstractCmd mixWrite(byte[] uid, KeyType type, int block, String str, OnCardListener listener) {
        return mixWrite(uid, type, null, block, str, listener, null);
    }

    /**
     * 写混合内容到块
     * @author  Moyq5
     * @since    2021/10/27 13:56
     */
    protected static AbstractCmd mixWrite(byte[] uid, KeyType type, int block, String str, OnCardListener listener, CmdProvider dst) {
        return mixWrite(uid, type, null, block, str, listener, dst);
    }

    /**
     * 写混合内容到块
     * @author  Moyq5
     * @since    2021/10/27 13:56
     */
    protected static AbstractCmd mixWrite(byte[] uid, KeyType type, byte[] key, int block, String str, OnCardListener listener) {
        return mixWrite(uid, type, key, block, str, listener, null);
    }

    /**
     * 写混合内容到块
     * @author  Moyq5
     * @since    2021/10/27 13:56
     */
    protected static AbstractCmd mixWrite(byte[] uid, KeyType type, byte[] key, int block, String str, OnCardListener listener, CmdProvider dst) {
        return authWrite(uid, type, key, block, mixToBlockData(str, 5), listener, dst);
    }

    /**
     * 读取原始字节内容
     * @author  Moyq5
     * @since    2021/10/27 12:13
     */
    protected static AbstractCmd blockRead(byte[] uid, KeyType type, int block, OnByteListener listener) {
        return blockRead(uid, type, null, block, listener);
    }

    /**
     * 读取原始字节内容
     * @author  Moyq5
     * @since    2021/10/27 12:13
     */
    protected static AbstractCmd blockRead(byte[] uid, KeyType type, byte[] key, int block, OnByteListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter blockRead = new CmdBlockRead(block);
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
        };

        return auth(uid, type, key, block, listener, next);
    }


    /**
     * 写整数到块（初始化钱包，钱包模式）
     * @author  Moyq5
     * @since    2021/1/11 18:59
     */
    protected static AbstractCmd walletInit(byte[] uid, KeyType type, int block, int value, OnCardListener listener) {
        byte[] data = new byte[16];
        for (int i = 0; i < 4; i++) {
            data[i] = data[8 + i] = (byte) ( ( value >> (8 * i) ) & 0xFF);// 数字// 低字节在前
            data[4 + i] = (byte) ( ( ~data[i] ) & 0xFF );// 数字取反
        }
        data[12] = data[14] = (byte) (block & 0xFF);// 地址
        data[13] = data[15] = (byte) ~(block & 0xFF);// 地址取反

        return authWrite(uid, type, block, data, listener);
    }

    /**
     * 从块读整数（钱包模式）
     * @author  Moyq5
     * @since    2021/1/11 18:18
     */
    protected static AbstractCmd intRead(byte[] uid, KeyType type, int block, OnIntListener listener) {
        CmdProvider next = () -> {
            AbstractCmdAdapter intRead = new CmdBlockRead(block);
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
        };

        return auth(uid, type, block, listener, next);
    }

    protected static AbstractCmd authWrite(byte[] uid, KeyType type, int block, byte[] data, OnCardListener listener) {
        return authWrite(uid, type, null, block, data, listener, null);
    }

    protected static AbstractCmd authWrite(byte[] uid, KeyType type, byte[] key, int block, byte[] data, OnCardListener listener) {
        return authWrite(uid, type, key, block, data, listener, null);
    }

    protected static AbstractCmd authWrite(byte[] uid, KeyType type, byte[] key, int block, byte[] data, OnCardListener listener, CmdProvider dst) {
        CmdProvider next = () -> {
            AbstractCmdAdapter write = new CmdBlockWrite(block, data);

            write.setListener(new OnCmdListener() {
                @Override
                public Cmd onFail(byte code) {
                    return listener.onFail("[" + type + block + "]卡写入失败");
                }

                @Override
                public Cmd onSuccess(byte[] data1) {
                    if (null == dst) {
                        return listener.onSuccess();
                    }
                    return dst.getCmd();
                }
            });
            return write;
        };

        return auth(uid, type, key, block, listener, next);
    }

    protected static AbstractCmd auth(byte[] uid, KeyType type, int block, OnFailListener listener, CmdProvider dst) {
        return auth(uid, type, null, block, listener, dst);
    }

    protected static AbstractCmd auth(byte[] uid, KeyType type, byte[] key, int block, OnFailListener listener, CmdProvider dst) {
        if (null == key) {
            key = type == A ? getKeyA(): getKeyB();
        }
        CmdKeyAuth auth = new CmdKeyAuth(uid, type, key, block);//byte[] keyt = key;
        auth.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                //Log.e("authCard", "onFail: " + "[" + type + block + "]卡认证失败" + HexUtil.toHexString(keyt));
                Cmd next = listener.onFail("[" + type + block + "]卡认证失败");
                if (null == next) {
                    return null;
                }
                // 认证失败后需重新寻卡才能执行下一个指令
                CmdUidRead uidRead = new CmdUidRead();
                uidRead.setListener(new OnCmdListener() {
                    @Override
                    public Cmd onFail(byte code) {
                        return next;
                    }

                    @Override
                    public Cmd onSuccess(byte[] data) {
                        return next;
                    }
                });
                return uidRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                //Log.w("authCard", "onSuccess: "+ "[" + type + block + "]卡认证成功" + HexUtil.toHexString(keyt));
                if (null != dst) {
                    return dst.getCmd();
                }
                return null;
            }
        });
        return auth;
    }

}
