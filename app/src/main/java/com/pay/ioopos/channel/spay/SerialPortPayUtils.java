package com.pay.ioopos.channel.spay;

import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;
import com.pay.ioopos.support.serialport.custom.CustomCmdSerializerResponse;
import com.pay.ioopos.support.serialport.custom.CustomCmdStatus;
import com.pay.ioopos.support.serialport.custom.CustomSerialPort;
import com.pay.ioopos.support.serialport.custom.CustomSerialPortFactory;
import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;

/**
 * 外部串口设备支付工具
 * @author moyq5
 * @since 2022/7/28
 */
public final class SerialPortPayUtils {
    private SerialPortPayUtils() {

    }

    public static void pay(String amount, PayProcess process) {
        SerialPortPayFactory.getPayer().pay(null, amount, process, null);
    }

    public static void pay(String orderNo, String amount, PayProcess process) {
        SerialPortPayFactory.getPayer().pay(orderNo, amount, process, null);
    }

    public static void pay(String orderNo, String amount, PayProcess process, String detail) {
        SerialPortPayFactory.getPayer().pay(orderNo, amount, process, detail);
    }

    public static void refund(String orderNo, String amount, RefundProcess process, String message) {
        SerialPortPayFactory.getPayer().refund(orderNo, amount, process, message);
    }

    public static void response(CustomCmdReceive receive, CustomCmdStatus status) {
        CustomSerialPortFactory.find(new SerialPortPayFinder() {
            @Override
            public void onFound(CustomSerialPort serialPort) {
                serialPort.send(new CustomCmdSerializerResponse(receive.getCmdType(), receive.getSerialNo(), status));
            }
        });
    }

    public static void response(CustomCmdReceive receive, CustomCmdStatus status, String message) {
        CustomSerialPortFactory.find(new SerialPortPayFinder() {
            @Override
            public void onFound(CustomSerialPort serialPort) {
                serialPort.send(new CustomCmdSerializerResponse(receive.getCmdType(), receive.getSerialNo(), status, message));
            }
        });
    }

}
