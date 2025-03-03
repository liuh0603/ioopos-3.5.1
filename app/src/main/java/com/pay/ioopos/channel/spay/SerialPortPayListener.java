package com.pay.ioopos.channel.spay;

import android.util.Log;
import android.view.KeyEvent;

import com.pay.ioopos.channel.spay.cmd.KeyEventDeserializer;
import com.pay.ioopos.channel.spay.cmd.KeyEventSerializer;
import com.pay.ioopos.channel.spay.cmd.PayCancelSerializer;
import com.pay.ioopos.channel.spay.cmd.PayPostSerializer;
import com.pay.ioopos.channel.spay.cmd.RefundPostSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;
import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;
import com.pay.ioopos.support.serialport.custom.CustomCmdUtils;
import com.pay.ioopos.support.serialport.custom.CustomSerialPortListener;
import com.pay.ioopos.support.serialport.internal.CmdException;
import com.pay.ioopos.common.HexUtils;

/**
 * 串口支付监听
 * @author moyq5
 * @since 2022/7/28
 */
public class SerialPortPayListener implements CustomSerialPortListener {
    private static final String TAG = SerialPortPayListener.class.getSimpleName();
    private static final CustomSerialPortListener listener = new SerialPortPayListener();

    private SerialPortPayListener() {

    }

    public static CustomSerialPortListener getInstance() {
        return listener;
    }

    @Override
    public void onReceive(byte[] data) {
        Log.d(TAG, "onReceive: " + HexUtils.toHexString(data));
        CustomCmdReceive cmd;
        try {
            cmd = CustomCmdUtils.deserialize(data, CustomCmdReceive.class);
        } catch (CmdException e) {
            Log.d(TAG, "onReceive: " + e.getMessage());
            if (data.length != 1 && (data[0] & 0xFF) != 0x00) {
                return;
            }
            KeyEventSerializer eventCmd;
            KeyEvent keyEvent;
            for (int keyCode: KeyEventDeserializer.keyCodes) {
                keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                eventCmd = new KeyEventSerializer(keyEvent);
                Log.d(TAG, "action_down: " + HexUtils.toHexString(eventCmd.serialize()).toUpperCase());
            }
            for (int keyCode: KeyEventDeserializer.keyCodes) {
                keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
                eventCmd = new KeyEventSerializer(keyEvent);
                Log.d(TAG, "action_up: " + HexUtils.toHexString(eventCmd.serialize()).toUpperCase());
            }
            String orderNo = "T" + System.currentTimeMillis();
            String amount = "1.23";
            String name = "心灵鸡汤";
            PayPostSerializer submit = new PayPostSerializer(amount);
            Log.d(TAG, "submit: amount=" + amount + ",name=null cmd=" + HexUtils.toHexString(submit.serialize()).toUpperCase());
            PayPostSerializer submit2 = new PayPostSerializer(amount);
            submit2.setName(name);
            Log.d(TAG, "submit: amount=" + amount + ",name=" + name + " cmd=" + HexUtils.toHexString(submit2.serialize()).toUpperCase());
            PayCancelSerializer cancel = new PayCancelSerializer();
            Log.d(TAG, "cancel: cmd=" + HexUtils.toHexString(cancel.serialize()).toUpperCase());
            RefundPostSerializer refund = new RefundPostSerializer(amount);
            refund.setOrderNo(orderNo);
            Log.d(TAG, "refund: orderNo=" + orderNo + ",amount=" + amount + ", cmd=" + HexUtils.toHexString(refund.serialize()).toUpperCase());
            RefundPostSerializer refund2 = new RefundPostSerializer(amount);
            Log.d(TAG, "refund: orderNo=null,amount=" + amount + ", cmd=" + HexUtils.toHexString(refund2.serialize()).toUpperCase());
            RefundPostSerializer refund3 = new RefundPostSerializer(null);
            Log.d(TAG, "refund: orderNo=null,amount=null, cmd=" + HexUtils.toHexString(refund3.serialize()).toUpperCase());
            return;
        }
        if (cmd.getAckType() != CustomCmdAckType.REQ) {
            Log.d(TAG, "onReceive: 非请求指令，忽略");
            return;
        }
        int type = cmd.getCmdType();
        if (type > SerialPortBizType.values().length - 1) {
            Log.d(TAG, "onReceive: 非法指令类型(" +type+ ")");
            return;
        }
        SerialPortBizType cmdType = SerialPortBizType.values()[type];
        cmdType.consume(data);
    }

}
