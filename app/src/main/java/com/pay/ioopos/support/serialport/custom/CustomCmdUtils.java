package com.pay.ioopos.support.serialport.custom;

import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_CONTENT_MAX;
import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_HEAD;
import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_MAX;
import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_MIN;
import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_TAIL;

import com.pay.ioopos.support.serialport.internal.CmdBccException;
import com.pay.ioopos.support.serialport.internal.CmdException;
import com.pay.ioopos.support.serialport.internal.CmdPackException;
import com.pay.ioopos.support.serialport.internal.CmdParamException;

/**
 * 自定义指令工具类
 * @author moyq5
 * @since 2022/7/28
 */
public final class CustomCmdUtils {

    private CustomCmdUtils() {

    }

    public static byte[] serialize(CustomCmdAckType ackType, int cmdType, int serialNo, byte[] contents) throws CmdException {
        if (null == ackType) {
            throw new CmdParamException("请求类型不能为空");
        }
        if (null == contents) {
            contents = new byte[0];
        }
        if (contents.length > CMD_CONTENT_MAX) {
            throw new CmdParamException("指令内容长度超过上限：" + CMD_CONTENT_MAX);
        }
        int len = CMD_MIN + contents.length;
        byte[] cmd = new byte[len];

        // 包结构：以固定0xFBFB 开头 2字节
        cmd[0] = (byte) CMD_HEAD;
        cmd[1] = (byte) CMD_HEAD;

        // 包结构：整个数据包长度，2字节，高位在前，最大值65535，约为65K
        cmd[2] = (byte)(( len >> 8 ) & 0xFF);
        cmd[3] = (byte)(len & 0xFF);

        // 包结构：指令类型，1字节，第8位是响应类型，前7位是指令类型，最多支持127种指令
        cmd[4] |= (byte)((ackType.ordinal() << 7) & 0x80);
        cmd[4] |= (byte)(cmdType & 0x7F);
        // 包结构：版本号
        cmd[5] = 0;
        // 包结构：指令序列号，2字节，高位在前，最大值65535
        cmd[6] = (byte)(( serialNo >> 8 ) & 0xFF);
        cmd[7] = (byte)(serialNo & 0xFF);

        // 包结构：业务内容，自定义长度
        System.arraycopy(contents, 0, cmd, 8, contents.length);

        // 包结构：倒数第3字节为检验值，1字节，校验内容为长度值到校验值之前的内容
        int bcc = 0;
        for (int i = 2; i < len - 3; i++) {
            bcc ^= cmd[i];
        }
        cmd[len - 3] = (byte) (bcc & 0xFF);

        // 包结构：以固定0xEDED 结尾 2字节
        cmd[len - 2] = (byte) CMD_TAIL;
        cmd[len - 1] = (byte) CMD_TAIL;
        return cmd;
    }

    public static <T extends CustomCmdReceive> T deserialize(byte[] cmd, Class<T> clazz) throws CmdException {
        if (null == cmd) {
            throw new CmdPackException("空包");
        }

        int len = cmd.length;
        if (len < CMD_MIN) {
            throw new CmdPackException("包太小");
        }

        if (len > CMD_MAX) {
            throw new CmdPackException("包太大");
        }

        if ((cmd[0] & CMD_HEAD) != CMD_HEAD || (cmd[1] & CMD_HEAD) != CMD_HEAD) {
            throw new CmdPackException("非法包头");
        }

        if ((cmd[len - 1] & CMD_TAIL) != CMD_TAIL || (cmd[len - 2] & CMD_TAIL) != CMD_TAIL) {
            throw new CmdPackException("非法包尾");
        }

        int pkLen = ((cmd[2] << 8) | cmd[3]) & 0xFFFF;
        if (pkLen != len) {
            throw new CmdPackException("包实际长度与对应字段值不一致");
        }

        int bcc = 0;
        for (int i = 2; i < len - 3; i++) {
            bcc ^= cmd[i];
        }
        if ((cmd[len - 3] & 0xFF) != (bcc & 0xFF)) {
            throw new CmdBccException("BCC校验未通过：" + bcc);
        }

        int ackType = (cmd[4] >> 7) & 0x01;
        if (ackType > CustomCmdAckType.values().length - 1) {
            throw new CmdPackException("请求类型有误：" + ackType);
        }

        int cmdType = cmd[4] & 0x7F;

        int version = cmd[5] & 0xFF;

        int serialNo = (( cmd[6] << 8 ) | cmd[7]) & 0xFFFF;
        byte[] content = new byte[cmd.length - CMD_MIN];
        System.arraycopy(cmd, 8, content, 0, content.length);

        T t;
        try {
            t = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CmdException("解码失败", e);
        }

        t.setAckType(CustomCmdAckType.values()[ackType]);
        t.setCmdType(cmdType);
        t.setVersion(version);
        t.setSerialNo(serialNo);
        t.setContent(content);
        return t;
    }
}
