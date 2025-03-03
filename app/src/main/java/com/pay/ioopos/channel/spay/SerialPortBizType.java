package com.pay.ioopos.channel.spay;

import com.pay.ioopos.channel.spay.cmd.KeyEventConsumer;
import com.pay.ioopos.channel.spay.cmd.KeyEventDeserializer;
import com.pay.ioopos.channel.spay.cmd.PayCancelConsumer;
import com.pay.ioopos.channel.spay.cmd.PayCancelDeserializer;
import com.pay.ioopos.channel.spay.cmd.PayPostConsumer;
import com.pay.ioopos.channel.spay.cmd.PayPostDeserializer;
import com.pay.ioopos.channel.spay.cmd.RefundPostConsumer;
import com.pay.ioopos.channel.spay.cmd.RefundPostDeserializer;
import com.pay.ioopos.channel.spay.cmd.VersionDeserializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializer;

/**
 * 指令业务类型
 * @author moyq5
 * @since 2022/7/26
 */
public enum SerialPortBizType {
    /**
     * 设备侦测
     */
    VERSION(new VersionDeserializer()),
    /**
     * 键盘输入
     */
    KEY_EVENT(new KeyEventDeserializer(), new KeyEventConsumer()),
    /**
     * 发起收款
     */
    PAY_POST(new PayPostDeserializer(), new PayPostConsumer()),
    /**
     * 取消收款
     */
    PAY_CANCEL(new PayCancelDeserializer(), new PayCancelConsumer()),
    /**
     * 收款状态
     */
    PAY_STATE(),
    /**
     * 发起退款
     */
    REFUND_POST(new RefundPostDeserializer(), new RefundPostConsumer()),
    /**
     * 退款状态
     */
    REFUND_STATE();

    private CustomCmdDeserializer<?> deserializer;
    private SerialPortConsumer consumer;

    SerialPortBizType() {
    }

    SerialPortBizType(CustomCmdDeserializer<?> deserializer) {
        this.deserializer = deserializer;
    }

    SerialPortBizType(SerialPortConsumer consumer) {
        this.consumer = consumer;
    }

    SerialPortBizType(CustomCmdDeserializer<?> deserializer, SerialPortConsumer consumer) {
        this.deserializer = deserializer;
        this.consumer = consumer;
    }

    public Object deserialize(byte[] data) {
        if (null == deserializer) {
            return null;
        }
        return deserializer.deserialize(data);
    }

    public void consume(byte[] data) {
        if (null == consumer) {
            return;
        }
        consumer.consume(data);
    }
}
