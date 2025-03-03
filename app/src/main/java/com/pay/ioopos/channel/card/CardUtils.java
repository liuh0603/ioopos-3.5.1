package com.pay.ioopos.channel.card;

import static com.pay.ioopos.common.HexUtils.toHexString;

import com.pay.ioopos.support.serialport.internal.CmdException;
import com.pay.ioopos.support.serialport.internal.CmdFailException;
import com.pay.ioopos.common.HexUtils;

import java.nio.charset.StandardCharsets;

public class CardUtils {

    public static String blockDataToString(byte[] data) {
        if (null == data || data.length != 16) {
            return null;
        }
        byte len = data[0];// 第1个字节内容为有效数据长度
        byte[] dstData = new byte[len];
        System.arraycopy(data, 1, dstData, 0, len);
        return new String(dstData, StandardCharsets.UTF_8);
    }

    public static byte[] stringToBlockData(String str) {
        if (null == str || str.isEmpty()) {
            return new byte[16];// 填空，即清空块内容
        }
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        if (data.length > 15) {
            throw new CmdFailException("用户每项信息不能长于15字节");
        }
        byte[] dst = new byte[data.length + 1];
        dst[0] = (byte)data.length; // 写数据的第1个字节内容为有效数据长度
        System.arraycopy(data, 0, dst, 1, data.length);// 写数据剩下的字节为有效数据内容
        return dst;
    }

    public static byte[] hexToBlockData(String hex) {
        if (null == hex || hex.isEmpty()) {
            return new byte[16];
        }
        if (hex.length()%2 != 0) {
            throw new CmdException("内容格式错误(16)");
        }
        int len = hex.length()/2;
        if (len > 15) {
            throw new CmdException("内容长度错误(15)");
        }

        byte[] data = new byte[16];
        data[0] = (byte) (len & 0xFF);// 1字节表示有效内容长度

        byte[] bytes = HexUtils.toByteArray(hex);

        System.arraycopy(bytes, 0, data, 1, bytes.length);

        return data;
    }

    public static String blockDataToHex(byte[] data) {
        if (null == data || data.length != 16) {
            return null;
        }
        int len = data[0] & 0xFF;// 1字节表示有效内容长度

        byte[] value = new byte[len];
        System.arraycopy(data, 1, value, 0, len);

        return HexUtils.toHexString(value);
    }

    public static String blockDataToMix(byte[] data) {
        if (null == data || data.length != 16) {
            return null;
        }
        int byteLen = data[0] & 0x0F;// 前4位为有效字节长度（长度不包含当前字节）
        if (byteLen == 0) {
            return null;
        }

        int mixLen = (data[0] & 0x70) >> 4;// 第5~7位为字符串格式前缀长度
        if (byteLen < mixLen) {
            return null;
        }

        boolean isEven = (data[0] & 0x80) == 0x80; // 第8位为1，表示16进制部分内容长度为偶数，否则为奇数

        String prefix = "";
        if (mixLen > 0) {
            byte[] mixData = new byte[mixLen];
            System.arraycopy(data, 1, mixData, 0, mixData.length);
            prefix = new String(mixData);
        }

        String suffix = "";
        if (byteLen > mixLen) {
            byte[] hexData = new byte[byteLen - mixLen];
            System.arraycopy(data, (1 + mixLen), hexData, 0, hexData.length);
            suffix = HexUtils.toHexString(hexData);
            if (!isEven && suffix.length()%2 == 0) {// 16进制值是奇数长度，则从偶数长度内容中截取奇数长度内容
                suffix = suffix.substring(0, suffix.length() - 1);
            }
        }

        return prefix + suffix;
    }

    public static byte[] mixToBlockData(String str, int len) {
        if (null == str || str.isEmpty()) {
            return new byte[16];
        }
        if (str.length() < len) {
            throw new CmdFailException("mix参数错误(" + len + ")");
        }
        String mix = str.substring(0, len);
        byte[] mixData = mix.getBytes();
        String hex = str.substring(len);
        boolean isEven = true;
        if (hex.length()%2 != 0) {
            hex +="0";
            isEven = false;
        }
        byte[] hexData = HexUtils.toByteArray(hex);
        if (mixData.length + hexData.length > 15) {
            throw new CmdException("内容长度错误(15)");
        }

        byte[] data = new byte[16];
        data[0] |= (mixData.length + hexData.length) & 0x0F;// 前4位是有效内容字节数
        data[0] |= (mixData.length << 4) & 0x70;// 第5~7位为字符串格式前缀内容字节数
        data[0] |= (isEven ? 0x80: 0x00) & 0x80;// 第8位为16进制部分内容长度奇偶值

        System.arraycopy(mixData, 0, data, 1, mixData.length);
        System.arraycopy(hexData, 0, data, 1 + mixData.length, hexData.length);

        return data;
    }

    public static int dataToAmount(byte[] data) {
        int balance = 0;
        for (int i = 0; i < 4; i++) {// 金额 4字节, 低字节在前
            balance |= ((data[i] & 0xFF) << (8 * i));
        }
        return balance;
    }

    public static byte[] statToBlockData(CardStat stat) {
        if (null == stat) {
            return new byte[16];
        }
        byte[] data = new byte[16];

        data[0] |= stat.getCrc() & 0x0F;

        data[0] |= (stat.getOfflineCount() << 4) & 0xF0;

        // 笔数 1字节
        data[1] =  (byte) (stat.getDayCount() & 0xFF);

        // 交易金额 3字节
        for (int i = 0; i < 3; i++) {
            data[2 + i] = (byte) ( ( stat.getDayAmount() >> (8 * i) ) & 0xFF);// 低字节在前
        }

        // 交易时间 5字节
        byte[] timeData = HexUtils.toByteArray("" + stat.getLastTime());//
        System.arraycopy(timeData, 0, data, 5, timeData.length);

        // 最近订单所在扇区索引 1字节前4位
        data[10] |= stat.getLastSector() & 0x0F;
        // 最近成功所在扇区索引 1字节后4位
        data[10] |= (stat.getSuccSector() << 4) & 0xF0;

        // 余额
        String balanceHex = String.valueOf(stat.getBalance());
        if (balanceHex.length()%2 != 0) {
            balanceHex = "0" + balanceHex;
        }
        byte[] balanceData = HexUtils.toByteArray(balanceHex);
        System.arraycopy(balanceData, 0, data, 11 + 3 - balanceData.length, balanceData.length);
        return data;
    };

    public static CardStat blockDataToStat(byte[] data) {
        if (null == data || data.length != 16) {
            return null;
        }
        CardStat stat = new CardStat();
        stat.setCrc(data[0] & 0x0F);
        stat.setOfflineCount((data[0] & 0xF0) >> 4);
        stat.setDayCount(data[1] & 0xFF);
        int amount = 0;
        for (int i = 0; i < 3; i++) {
            amount |= (data[2 + i] & 0xFF) << (8 * i);// 低字节在前
        }
        stat.setDayAmount(amount);
        byte[] time = new byte[5];
        System.arraycopy(data, 5, time, 0, time.length);
        stat.setLastTime(Integer.parseInt(toHexString(time)));
        stat.setLastSector(data[10] & 0x0F);
        stat.setSuccSector((data[10] & 0xF0) >> 4);
        byte[] balance = new byte[3];
        System.arraycopy(data, 11, balance, 0, balance.length);
        stat.setBalance(Integer.parseInt(toHexString(balance)));
        return stat;
    }

    public static int crc(CardStat stat, CardOrder order) {
        if (null == stat) {
            return 0;
        }
        byte[] bytes = (stat.toString() + (null != order ? order.toString(): "")).getBytes(StandardCharsets.UTF_8);
        int crc = 0;
        for (byte b: bytes) {
            crc ^= b;
        }
        return crc & 0x0F;
    }

    public static int crc(CardOrder order) {
        if (null == order) {
            return 0;
        }
        byte[] bytes = order.toString().getBytes(StandardCharsets.UTF_8);
        int crc = 0;
        for (byte b: bytes) {
            crc ^= b;
        }
        return crc & 0x0F;
    }

    public static byte[] createAmountBlockData(CardOrder order) {
        if (null == order) {
            return new byte[16];
        }
        int balance = order.getBalance();
        String balanceHex = String.valueOf(balance);
        if (balanceHex.length()%2 != 0) {
            balanceHex = "0" + balanceHex;
        }
        int amount = order.getAmount();
        String amountHex = String.valueOf(amount);
        if (amountHex.length()%2 != 0) {
            amountHex = "0" + amountHex;
        }
        long time = order.getOrderTime();
        String timeHex = String.valueOf(time);
        if (timeHex.length()%2 != 0) {
            timeHex = "0" + timeHex;
        }
        byte[] data = new byte[16];
        byte[] balanceData = HexUtils.toByteArray(balanceHex);
        System.arraycopy(balanceData, 0, data, 3 - balanceData.length, balanceData.length);
        byte[] amountData = HexUtils.toByteArray(amountHex);
        System.arraycopy(amountData, 0, data, 3 + 3 - amountData.length, amountData.length);
        byte[] timeData = HexUtils.toByteArray(timeHex);
        System.arraycopy(timeData, 0, data, 3 + 3 + 5 - timeData.length, timeData.length);
        data[11] |= order.getStatus() & 0x0F;
        data[11] |= (crc(order) << 4) & 0xF0;

        return data;
    }
}
