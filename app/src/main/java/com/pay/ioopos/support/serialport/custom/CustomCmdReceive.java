package com.pay.ioopos.support.serialport.custom;

/**
 * 接收类的指令
 * @author moyq5
 * @since 2022/7/29
 */
public class CustomCmdReceive {
    private CustomCmdAckType ackType;

    private int cmdType;

    private int version;

    private int serialNo;

    private byte[] content;

    public CustomCmdAckType getAckType() {
        return ackType;
    }

    public void setAckType(CustomCmdAckType ackType) {
        this.ackType = ackType;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
