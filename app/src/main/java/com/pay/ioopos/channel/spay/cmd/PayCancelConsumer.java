package com.pay.ioopos.channel.spay.cmd;

import static com.pay.ioopos.common.AppFactory.dispatchKeyEvent;

import android.app.Activity;
import android.view.KeyEvent;

import com.pay.ioopos.App;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortConsumer;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.support.serialport.custom.CustomCmdStatus;

/**
 * 指令消费：取消收款
 * @author moyq5
 * @since 2022/8/3
 */
public class PayCancelConsumer implements SerialPortConsumer {

    @Override
    public void consume(byte[] data) {
        PayCancelReceive cancel  = (PayCancelReceive) SerialPortBizType.PAY_CANCEL.deserialize(data);
        Activity activity = App.getInstance().getActivity();
        if (activity instanceof MainActivity) {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
            SerialPortPayUtils.response(cancel, CustomCmdStatus.SUCCESS);
            return;
        }
        SerialPortPayUtils.response(cancel, CustomCmdStatus.FAIL);
    }

}
