package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializerAbstract;
import com.pay.ioopos.support.serialport.internal.CmdParamException;
import com.pay.ioopos.common.BigDecimalUtils;

import java.nio.charset.StandardCharsets;

/**
 * 反序列化输入指令：发起收款
 * @author moyq5
 * @since 2022/7/29
 * @see PayPostSerializer
 */
public class PayPostDeserializer extends CustomCmdDeserializerAbstract<PayPostReceive> {

    @Override
    public PayPostReceive deserialize(byte[] data) {
        PayPostReceive result = super.deserialize(data);
        byte[] content = result.getContent();
        // 金额，3字节，高位在前，单位：分
        int fen = (((content[0] & 0xFF) << 16) | ((content[1] & 0xFF) << 8) | (content[2] & 0xFF));
        if (fen > 0) {
            result.setAmount(BigDecimalUtils.fenToYuan(fen).toString());
        } else {
            throw new CmdParamException("金额无效：" + fen);
        }
        // 商品名长度，1字节
        int nameLength = content[3] & 0xFF;
        if (nameLength == 0) {
            return result;
        }
        // 商品名，nameLength字节
        byte[] nameData = new byte[nameLength];
        System.arraycopy(content, 4, nameData, 0, nameLength);
        result.setName(new String(nameData, StandardCharsets.UTF_8));
        return result;
    }

    @Override
    protected Class<PayPostReceive> resultClass() {
        return PayPostReceive.class;
    }
}
