package com.pay.ioopos.receiver;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.support.usb.UsbFactory;

/**
 * USB设备接入通知接收器
 * @author    Moyq5
 * @since  2020/7/22 14:45
 */
public class UsbReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_USB_DEVICE_ATTACHED:
            case ACTION_USB_DEVICE_DETACHED:
                break;
            default:
                return;
        }
        new Task(goAsync()).executeOnExecutor(TaskFactory.pool(), intent);
    }

    private static class Task extends AsyncTask<Intent, Void, Void> {
        private final PendingResult pendingResult;

        private Task(PendingResult pendingResult) {
            this.pendingResult = pendingResult;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            if (intent.getAction().equals(ACTION_USB_DEVICE_ATTACHED)) {
                UsbFactory.onAttached();
            } else if (intent.getAction().equals(ACTION_USB_DEVICE_DETACHED)) {
                UsbFactory.onDetached();
            }
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
