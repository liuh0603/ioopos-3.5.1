package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;
import com.pay.ioopos.support.serialport.internal.CmdParamException;
import com.pay.ioopos.common.BigDecimalUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * 输出指令：发起收款
 * @author moyq5
 * @since 2022/7/26
 * @see PayPostDeserializer
 */
public class PayPostSerializer extends SerialPortSerializer {
    private static int serialNo = 0;

    /**
     * 收款金额，单位：元
     */
    private String amount;
    /**
     * 商品名称
     */
    private String name;

    public PayPostSerializer(String amount) {
        this.amount = amount;
    }

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.PAY_POST;
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
        if (null == amount || amount.isEmpty()) {
            throw new CmdParamException("金额不能为空");
        }
        int fen = BigDecimalUtils.yuanToFen(new BigDecimal(amount));
        if (fen <= 0 || fen > 0xFFFFFF) {
            throw new CmdParamException("金额无效");
        }
        byte[] nameData = new byte[0];
        if (null != name && !name.isEmpty()) {
            nameData = name.getBytes(StandardCharsets.UTF_8);
        }
        int n = nameData.length;
        if (n > 0xFF) {
            throw new CmdParamException("商品名过长：" + n);
        }

        // 金额（3） + 商品名长度值（1） + 商户名（n)
        byte[] content = new byte[4 + n];

        // 金额，分表示值，3个字节，高位在前
        content[0] = (byte) ((fen >> 16) & 0xFF);
        content[1] = (byte) ((fen >> 8) & 0xFF);
        content[2] = (byte) (fen & 0xFF);

        // 商品名长度，1字节
        content[3] = (byte) (n & 0xFF);

        if (n > 0) {
            System.arraycopy(nameData, 0, content, 4, n);
        }

        return content;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
