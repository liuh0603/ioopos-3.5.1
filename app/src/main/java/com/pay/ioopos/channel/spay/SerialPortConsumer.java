package com.pay.ioopos.channel.spay;

/**
 * 指令消费
 * @author moyq5
 * @since 2022/8/3
 */
public interface SerialPortConsumer {

    void consume(byte[] data);
}
