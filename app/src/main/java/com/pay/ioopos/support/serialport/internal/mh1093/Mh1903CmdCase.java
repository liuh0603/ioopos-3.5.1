package com.pay.ioopos.support.serialport.internal.mh1093;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.CmdFailException;
import com.pay.ioopos.support.serialport.internal.CmdPackException;
import com.pay.ioopos.support.serialport.internal.OnCmdListener;
import com.pay.ioopos.support.serialport.internal.SerialPortFactory;

/**
 * mh1903模块串口指令实现
 * @author    Moyq5
 * @since  2020/12/30 17:29
 */
public class Mh1903CmdCase extends CmdCase {
    public static final String DEV_MT1 = "/dev/ttyMT1";
    public static final String DEV_MT2 = "/dev/ttyMT2";
    private static final int DEV_BAUDRATE = 115200;
    private static boolean isOpened = false;

    public Mh1903CmdCase() {
        super(DEV_MT1, DEV_BAUDRATE);

    }

    public Mh1903CmdCase(Cmd cmd) {
        super(DEV_MT1, DEV_BAUDRATE);
        setRootCmd(cmd);
    }

    public Mh1903CmdCase(String path) {
        super(path, DEV_BAUDRATE);

    }

    public Mh1903CmdCase(String path, Cmd cmd) {
        super(path, DEV_BAUDRATE);
        setRootCmd(cmd);
    }

    @Override
    public void setRootCmd(final Cmd cmd) {
        if (null == cmd) {
            super.setRootCmd(null);
            return;
        }

        if (SerialPortFactory.isKeepOpen() && isOpened) {
            super.setRootCmd(cmd);
            return;
        }

        // 每次”打开“模块的卡功能后都需要重新寻卡，才能对卡进行其它操作

        final CmdUidOpen uidOpen = new CmdUidOpen();
        final UidObject obj = new UidObject();
        final CmdUidRead uidRead = new CmdUidRead();
        uidOpen.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                return uidOpen;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                isOpened = true;
                // 每次”打开“模块的卡功能后都需要重新寻卡，才能对卡进行其它操作
                if ((cmd instanceof CmdUidRead)) {
                    return cmd;
                }
                return uidRead;
            }
        });
        uidRead.setListener(new OnCmdListener() {
            @Override
            public Cmd onFail(byte code) {
                obj.hasComeIn = false;
                return uidRead;
            }

            @Override
            public Cmd onSuccess(byte[] data) {
                if (obj.hasComeIn) {
                    return uidRead;
                }
                obj.hasComeIn = true;
                return cmd;
            }
        });
        super.setRootCmd(uidOpen);
    }

    public byte[] analysis(byte[] data) {

        int len = data.length;

        if (len < 11) {// 数据长度不会少于11字节
            throw new CmdPackException("数据长度不会少于11字节");
        }

        if (data[0] != 0x02) {// 包头有误
            throw new CmdPackException("包头有误: " + data[0]);
        }

        if (data[len -1] != 0x40) {// 包尾有误
            throw new CmdPackException("包尾有误: " + data[len -1]);
        }

        if (data[6] != 0x00) {// 状态码为“非成功”
            throw new CmdFailException("状态码为失败：" + data[6]);
        }

        int pLen = data[2]<<8 | data[3];// 包长度
        if (len != pLen + 6) {// 包长度检查
            throw new CmdPackException("包长度有误: " + pLen);
        }

        int dLen = data[7]<<8 | data[8];// 业务数据长度字段值
        if (pLen != 5 + dLen) {// 业务数据长度字段值跟包长度核对
            throw new CmdFailException("业务数据长度有误：" + dLen);
        }

        int bcc = 0;
        for (int i = 1; i < len - 2; i++) {
            bcc ^= data[i];
        }
        if (bcc != data[len - 2]) {// 校验值错误
            throw new CmdFailException("校验值有误：" + bcc);
        }

        byte[] dData = new byte[dLen];// 值
        System.arraycopy(data, 9, dData, 0, dData.length);
        return dData;
    }

    private static class UidObject {
        private boolean hasComeIn = true;
    }
}
