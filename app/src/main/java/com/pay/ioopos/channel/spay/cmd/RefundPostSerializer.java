package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;
import com.pay.ioopos.support.serialport.internal.CmdParamException;
import com.pay.ioopos.common.BigDecimalUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * 输出指令：退款
 * @author moyq5
 * @since 2022/8/2
 * @see PayPostDeserializer
 */
public class RefundPostSerializer extends SerialPortSerializer {
    private static int serialNo = 0;
    /**
     * 要退款的交易单号
     */
    private String orderNo;
    /**
     * 要退款的金额，单位：元。不填表示全额退款
     */
    private String amount;
    public RefundPostSerializer(String amount) {
        this.amount = amount;
    }

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.REFUND_POST;
    }

    @Override
    protected int serialNo() {
        if (serialNo > 0xFFFF) {
            serialNo = 0;
        }
        return serialNo++;
    }

    @Override
    protected byte[] content() {
        byte[] noData = new byte[0];
        if (null != orderNo && !orderNo.isEmpty()) {
            noData = orderNo.getBytes(StandardCharsets.UTF_8);
        }
        if (noData.length > 0xFF) {
            noData = new byte[0];// 单号过长，忽略
        }
        int n = noData.length;

        // 金额（3）+ 单号长度（1） + 单号（n）
        byte[] content = new byte[4 + n];

        int fen = 0;
        if (null != amount && !amount.isEmpty()) {
            fen = BigDecimalUtils.yuanToFen(new BigDecimal(amount));
            if (fen < 0 || fen > 0xFFFFFF) {
                throw new CmdParamException("金额无效");
            }
        }
        // 金额，3字节，高位在前，单位：分
        content[0] = (byte) ((fen >> 16) & 0xFF);
        content[1] = (byte) ((fen >> 8) & 0xFF);
        content[2] = (byte) (fen& 0xFF);

        // 单号长度，1字节
        content[3] = (byte) (n & 0xFF);
        // 单号，n字节
        if (n > 0) {
            System.arraycopy(noData, 0, content, 4, n);
        }
        return content;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
