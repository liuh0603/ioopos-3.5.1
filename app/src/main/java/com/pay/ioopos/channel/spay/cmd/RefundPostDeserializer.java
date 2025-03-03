package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializerAbstract;
import com.pay.ioopos.support.serialport.internal.CmdPackException;
import com.pay.ioopos.common.BigDecimalUtils;

import java.nio.charset.StandardCharsets;

/**
 * 反序列化输入指令：发起退款
 * @author moyq5
 * @since 2022/8/2
 * @see RefundPostSerializer
 */
public class RefundPostDeserializer extends CustomCmdDeserializerAbstract<RefundPostReceive> {

    @Override
    public RefundPostReceive deserialize(byte[] data) {
        RefundPostReceive result = super.deserialize(data);
        byte[] content = result.getContent();

        // 金额，3字节，高位在前，单位：分
        int fen = (((content[0] & 0xFF) << 16) | ((content[1] & 0xFF) << 8) | (content[2] & 0xFF));
        if (fen > 0) {
            result.setAmount(BigDecimalUtils.fenToYuan(fen).toString());
        }

        // 单号长度，1字节
        int length = content[3] & 0xFF;
        if (length == 0) {
            return result;
        }
        if (length > content.length - 3) {
            throw new CmdPackException("单号长度有误：" + length);
        }
        // 单号
        byte[] bytes = new byte[length];
        System.arraycopy(content, 4, bytes, 0, length);
        result.setOrderNo(new String(bytes, StandardCharsets.UTF_8));
        return result;
    }

    @Override
    protected Class<RefundPostReceive> resultClass() {
        return RefundPostReceive.class;
    }
}
