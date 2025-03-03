package com.pay.ioopos.channel.spay.cmd;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.pay.ioopos.common.AppFactory.startActivity;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.app.Activity;
import android.content.Intent;

import com.pay.ioopos.App;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.activity.RefundActivity;
import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortConsumer;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.support.serialport.custom.CustomCmdStatus;

/**
 * 指令消费：发起退款
 * @author moyq5
 * @since 2022/8/3
 */
public class RefundPostConsumer implements SerialPortConsumer {

    @Override
    public void consume(byte[] data) {
        RefundPostReceive refund = (RefundPostReceive) SerialPortBizType.REFUND_POST.deserialize(data);
        if (null == refund) {
            return;
        }
        Activity activity = App.getInstance().getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity)activity;
            if (mainActivity.isPayFinished()) {
                startRefundActivity(refund);
            } else {
                SerialPortPayUtils.response(refund, CustomCmdStatus.FAIL, "支付进行中");
            }
        } else {
            startRefundActivity(refund);
        }
    }

    private void startRefundActivity(RefundPostReceive refund) {
        Intent intent = new Intent(App.getInstance(), RefundActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_PARAM_AMOUNT, refund.getAmount());
        intent.putExtra(INTENT_PARAM_CODE, refund.getOrderNo());
        startActivity(intent);
        SerialPortPayUtils.response(refund, CustomCmdStatus.SUCCESS);
    }

}
