package com.pay.ioopos.receiver;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_OUT_TRADE_NO;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_TRANSACTION_ID;
import static com.pay.ioopos.worker.WxReportIniWorker.PARAMS_STATUS;
import static com.pay.ioopos.worker.WxReportIniWorker.STATUS_NEW;
import static com.pay.ioopos.worker.WxReportIniWorker.STATUS_SUCCESS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.worker.WorkerFactory;
import com.pay.ioopos.worker.WxReportIniWorker;

import java.util.HashMap;

/**
 * 上报广播接收器
 * @author    Moyq5
 * @since  2020/6/18 15:53
 */
public class WxReportReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(WxReportReceiver.class.getName())) {
            return;
        }
        new Task(goAsync()).executeOnExecutor(TaskFactory.pool(), intent);
    }

    private static class Task extends AsyncTask<Intent, Void, Void> {
        private final PendingResult pendingResult;
        private HashMap<String, Object> reportData;
        private final BroadcastReceiver initReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context sdkInitContext, Intent sdkInitIntent) {
                int status = sdkInitIntent.getIntExtra(PARAMS_STATUS, STATUS_NEW);
                if (status == STATUS_NEW) {// 初始化过程中
                    return;
                }
                localUnregister(initReceiver);
                if (status != STATUS_SUCCESS) {
                    return;
                }
                WorkerFactory.enqueueWxReportExeOneTime(reportData);
            }
        };

        private Task(PendingResult pendingResult) {
            this.pendingResult = pendingResult;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            reportData = new HashMap<>();
            reportData.put("out_trade_no", intent.getStringExtra(INTENT_PARAM_WX_OUT_TRADE_NO));
            reportData.put("transaction_id", intent.getStringExtra(INTENT_PARAM_WX_TRANSACTION_ID));

            localRegister(initReceiver, new IntentFilter(WxReportIniWorker.ACTION_STATUS));
            WorkerFactory.enqueueWxReportIniOneTime(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if (null != pendingResult) {
                pendingResult.finish();
            }
        }

    }
}
