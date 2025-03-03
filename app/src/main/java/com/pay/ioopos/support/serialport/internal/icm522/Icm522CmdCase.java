package com.pay.ioopos.support.serialport.internal.icm522;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.CmdFailException;
import com.pay.ioopos.support.serialport.internal.CmdPackException;

/**
 * icm522cpu模块串口指令实现
 * @author    Moyq5
 * @since  2020/10/27 16:40
 */
public class Icm522CmdCase  extends CmdCase {
    private static final String DEV_PATH = "/dev/ttyMT1";
    private static final int DEV_BAUDRATE = 9600;// 460800

    public Icm522CmdCase() {
        super(DEV_PATH, DEV_BAUDRATE);
    }

    public Icm522CmdCase(Cmd cmd) {
        super(DEV_PATH, DEV_BAUDRATE, cmd);
    }

    public Icm522CmdCase(int baudrate, Cmd cmd) {
        super(DEV_PATH, baudrate, cmd);
    }

    @Override
    public byte[] analysis(byte[] data) {
        int len = data.length;

        if (len < 4) {// 数据长度不会少于4字节
            throw new CmdPackException("数据长度不会少于4字节");
        }

        if (data[0] != (byte)0xFE) {// 包头字段有误
            throw new CmdPackException("包头有误: " + data[0]);
        }

        if (data[2] >= (byte)0xE0  &&  data[2] <= (byte)0xFF) {// 状态字段：0xE0---0xFF执行错误
            throw new CmdFailException("状态码为失败：" + data[2]);
        }

        int pLen = data[1];// 包长度字段的值
        if (len != pLen + 2) {// 包长度 = 长度字段的值 + 状态字段长度(1字节) + 校验位字段长度(1字节)
            throw new CmdPackException("包长度有误: " + pLen);
        }

        int dLen = pLen - 2;// 业务数据字段长度 = 长度字段的值 - 命令字段长度（1字节） - 长度字段长度（1字节）
        byte[] dData = new byte[dLen];// 业务数据字段内容
        System.arraycopy(data, 3, dData, 0, dData.length);// 前面三个字节分别是：命令字段、长度字段、状态字段

        int bcc = 0 ^ data[1] ^ data[2];// 校验值：长度(1Byte) + 命令(1Byte) + 数据(nByte)的异或和
        for (byte b: dData) {
            bcc ^= b;
        }
        if (data[len - 1] != (byte)bcc) {// 校验值错误
            throw new CmdFailException("校验值有误：" + bcc);
        }

        return dData;
    }

}
