package com.pay.ioopos.support.serialport.custom;


import androidx.annotation.CallSuper;

import com.pay.ioopos.support.serialport.internal.CmdParamException;

import java.nio.charset.StandardCharsets;

/**
 * 自定义串指令-通用回复指令
 * @author    Moyq5
 * @since  2022/8/3
 */
public class CustomCmdSerializerResponse extends CustomCmdSerializerAbstract {
    private int cmdType;
    private int serialNo;
    private CustomCmdStatus status;
    private String message;

    public CustomCmdSerializerResponse(int cmdType, int serialNo, CustomCmdStatus status) {
        this.cmdType = cmdType;
        this.serialNo = serialNo;
        this.status = status;
    }

    public CustomCmdSerializerResponse(int cmdType, int serialNo, CustomCmdStatus status, String message) {
        this.cmdType = cmdType;
        this.serialNo = serialNo;
        this.status = status;
        this.message = message;
    }

    @Override
    protected final CustomCmdAckType ackType() {
        return CustomCmdAckType.RES;
    }

    @Override
    protected final int cmdType() {
        return cmdType;
    }

    @Override
    protected final int serialNo() {
        return serialNo;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    @CallSuper
    @Override
    protected byte[] content() {
        if (null == status) {
            throw new CmdParamException("状态不能为空");
        }
        byte[] messages = new byte[0];
        if (null != message && !message.isEmpty()) {
            messages = message.getBytes(StandardCharsets.UTF_8);
        }
        if (messages.length > 0x7F) {
            throw new CmdParamException("状态描述长度大于最大允许长度：" + 0x7F);
        }
        byte[] content = new byte[1 + messages.length];
        // 状态，第1字节高1位
        content[0] = (byte) ((status.ordinal() & 0x01) << 7);
        // 描述内容长度，第1字节低1~7位，即支持最大长度127(0x7F)
        content[0] |= (byte) (messages.length & 0x7F);
        // 描述内容
        if (messages.length > 0) {
            System.arraycopy(messages, 0, content, 1, messages.length);
        }
        return content;
    }


    public CustomCmdStatus getStatus() {
        return status;
    }

    public void setStatus(CustomCmdStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
