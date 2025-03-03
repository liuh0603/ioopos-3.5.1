package com.pay.ioopos.receiver;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_SHUTDOWN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_BOOT_COMPLETED:
            case ACTION_SHUTDOWN:
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
            if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {

            } else if (intent.getAction().equals(ACTION_SHUTDOWN)) {
                ApiUtils.pant(false);
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
