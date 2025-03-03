package com.pay.ioopos.channel.spay;

import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAYING;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_CANCEL;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_ERROR;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_EXPIRED;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_FAIL;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_PWD;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_SUCCESS;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.PAY_WAIT;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.REVOKE_FAIL;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.REVOKE_SUCCESS;
import static com.pay.ioopos.channel.spay.cmd.PayStateSerializer.State.REVOKING;
import static com.pay.ioopos.channel.spay.cmd.RefundStateSerializer.State.REFUND_FAIL;
import static com.pay.ioopos.channel.spay.cmd.RefundStateSerializer.State.REFUND_SUBMITTED;
import static com.pay.ioopos.channel.spay.cmd.RefundStateSerializer.State.REFUND_SUBMITTING;
import static com.pay.ioopos.channel.spay.cmd.RefundStateSerializer.State.REFUND_SUCCESS;

import com.pay.ioopos.channel.spay.cmd.PayStateSerializer;
import com.pay.ioopos.channel.spay.cmd.RefundStateSerializer;
import com.pay.ioopos.support.serialport.custom.CustomSerialPort;
import com.pay.ioopos.support.serialport.custom.CustomSerialPortFactory;
import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;

/**
 * 外接串口支付实现
 * @author moyq5
 * @since 2022/7/28
 */
public class SerialPortPayerDefault implements SerialPortPayer {

    @Override
    public void pay(String orderNo, String amount, PayProcess process, String message) {
        // 注意，以下枚举貌似一一对应，应做代码简化，实际不是的，这只是偶然情况，
        // PayProcess枚举不要求顺序，PayStateSerializer.State枚举有严格顺序要求，其值用于开放接口
        PayStateSerializer.State state;
        switch (process) {
            case PAY_WAIT:
                state = PAY_WAIT;
                break;
            case PAY_CANCEL:
                state = PAY_CANCEL;
                break;
            case PAYING:
                state = PAYING;
                break;
            case PAY_PWD:
                state = PAY_PWD;
                break;
            case PAY_FAIL:
                state = PAY_FAIL;
                break;
            case PAY_ERROR:
                state = PAY_ERROR;
                break;
            case PAY_EXPIRED:
                state = PAY_EXPIRED;
                break;
            case PAY_SUCCESS:
                state = PAY_SUCCESS;
                break;
            case REVOKING:
                state = REVOKING;
                break;
            case REVOKE_SUCCESS:
                state = REVOKE_SUCCESS;
                break;
            case REVOKE_FAIL:
                state = REVOKE_FAIL;
                break;
            default:
                throw new RuntimeException("当前支付流程无对应串口指令：" + process);
        }
        PayStateSerializer cmd  = new PayStateSerializer(amount, state);
        cmd.setOrderNo(orderNo);
        cmd.setMessage(message);

        CustomSerialPortFactory.find(new SerialPortPayFinder() {
            @Override
            public void onFound(CustomSerialPort serialPort) {
                serialPort.send(cmd);
            }
        });
    }

    @Override
    public void refund(String orderNo, String amount, RefundProcess process, String message) {
        // 注意，以下枚举貌似一一对应，应做代码简化，实际不是的，这只是偶然情况，
        // RefundProcess，RefundStateSerializer.State枚举有严格顺序要求，其值用于开放接口
        RefundStateSerializer.State state;
        switch (process) {
            case REFUND_SUCCESS:
                state = REFUND_SUCCESS;
                break;
            case REFUND_SUBMITTING:
                state = REFUND_SUBMITTING;
                break;
            case REFUND_SUBMITTED:
            case REFUNDING:
                state = REFUND_SUBMITTED;
                break;
            case REFUND_FAIL:
                state = REFUND_FAIL;
                break;
            default:
                throw new RuntimeException("当前退款流程无对应串口指令：" + process);
        }
        RefundStateSerializer cmd  = new RefundStateSerializer(orderNo, state);
        cmd.setAmount(amount);
        cmd.setMessage(message);

        CustomSerialPortFactory.find(new SerialPortPayFinder() {
            @Override
            public void onFound(CustomSerialPort serialPort) {
                serialPort.send(cmd);
            }
        });
    }
}
