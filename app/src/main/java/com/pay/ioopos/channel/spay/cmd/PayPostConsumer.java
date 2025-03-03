package com.pay.ioopos.channel.spay.cmd;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.pay.ioopos.common.AppFactory.startActivity;

import android.app.Activity;
import android.content.Intent;

import com.pay.ioopos.App;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortConsumer;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.support.serialport.custom.CustomCmdStatus;

/**
 * 指令消费：发起收款指令
 * @author moyq5
 * @since 2022/8/3
 */
public class PayPostConsumer implements SerialPortConsumer {

    @Override
    public void consume(byte[] data) {
        PayPostReceive submit = (PayPostReceive) SerialPortBizType.PAY_POST.deserialize(data);
        if (null == submit) {
            return;
        }
        Activity activity = App.getInstance().getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity)activity;
            if (mainActivity.isPayFinished()) {
                startMainActivity(submit.getCmdType(), data);
            } else {
                SerialPortPayUtils.response(submit, CustomCmdStatus.FAIL, "支付进行中");
            }
        } else {
            startMainActivity(submit.getCmdType(), data);
        }
    }

    private void startMainActivity(int type, byte[] data) {
        Intent intent = new Intent(App.getInstance(), MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", type);
        intent.putExtra("data", data);
        startActivity(intent);
    }

}
