package com.pay.ioopos.activity;

import static android.os.Process.myPid;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.pay.ioopos.App.DEV_IS_801;
import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.common.AppFactory.appVersionName;
import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.isDebug;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.trade.PayProcess.PAY_EXPIRED;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.channel.spay.cmd.PayPostReceive;
import com.pay.ioopos.display.CustomerActivity;
import com.pay.ioopos.display.SubScreenLoader;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.AbstractTipFragment;
import com.pay.ioopos.fragment.PayExpressionFragment;
import com.pay.ioopos.fragment.PayExpressionFragment.OnPayExpressionListener;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.fragment.PayIngAbstract.OnPayingListener;
import com.pay.ioopos.fragment.PayRevokeAbstract;
import com.pay.ioopos.fragment.PayRevokeAbstract.OnPayCancelListener;
import com.pay.ioopos.fragment.PayScanFragment;
import com.pay.ioopos.fragment.TipHorizontalFragment;
import com.pay.ioopos.fragment.apay.PayIngApayFragment;
import com.pay.ioopos.fragment.cpay.PayIngCpayFragment;
import com.pay.ioopos.fragment.cpay.PayRevokeCpayFragment;
import com.pay.ioopos.fragment.ipay.PayIngFragment;
import com.pay.ioopos.fragment.ipay.PayRevokeFragment;
import com.pay.ioopos.fragment.support.StatusListener;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.receiver.WxReportReceiver;
import com.pay.ioopos.sqlite.OrderStore;
import com.pay.ioopos.sqlite.OrderUtils;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.keyboard.KeyboardFactory;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;
import com.pay.ioopos.support.serialport.custom.CustomCmdStatus;
import com.pay.ioopos.support.serialport.internal.CmdException;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.trade.CardShower;
import com.pay.ioopos.trade.PayMode;
import com.pay.ioopos.widget.Tip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * 收款主界面
 * @author    Moyq5
 * @since  2020/3/31 16:12
 */
public class MainActivity extends AbstractActivity implements ScanListener, StatusListener, OnPayExpressionListener, OnPayingListener, OnPayCancelListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver reportReceiver = new WxReportReceiver();
    private ScheduledFuture<?> timeoutFuture;
    private boolean isPayFinished = true;

    private final BroadcastReceiver orderCountReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            OrderStore store = StoreFactory.orderStore();
            int count0 = store.count(0);
            int count1 = 0;//store.count(1);
            boolean show = count0 > 0 || count1 > 0;
            String text = "";
            if (show) {
                if (count0 > 0) {
                    text += count0;
                }
                if (count1 > 0) {
                    text += "+" + count1;
                }
            }
            String finalText = text;
            TextView view = findViewById(R.id.text_offline_count);
            mainHandler.post(() -> {
               try {
                   view.setText("("+ finalText +")");
                   view.setVisibility(show ? VISIBLE: GONE);
               } catch (Throwable ignored) {

               }
            });
        }

    };

    private final Runnable offlineTagToggle = () -> mainHandler.post(() -> {
        try {
            findViewById(R.id.text_offline_tag).setVisibility(isNetworkAvailable() ? GONE: VISIBLE);
        } catch (Throwable ignored) {

        }
    });

    private final NetworkRequest networkRequest = new NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();

    private final NetworkCallback networkCallback = new NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            offlineTagToggle.run();
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            offlineTagToggle.run();
        }

        @Override
        public void onLost(@NonNull Network network) {
            offlineTagToggle.run();
        }

    };
    private ConnectivityManager connectivityManager;

    private boolean fixedPayPause = true;
    private final Handler fixedPayHandler = new Handler();
    private final Runnable fixedPayRunnable = () -> {
        if (!fixedPayPause) {
            onPayScan();
        }
    };
    private PayMode mode;
    private String maxAmount;
    private TextView amountView;
    private TextView modeView;
    private SettingStore store;

    private String curAmount;   // pay amount
    private String curGoodsName;     // goods name


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if (null != message) {
            toast(message);
        }

        localRegister(reportReceiver, new IntentFilter(WxReportReceiver.class.getName()));
        localRegister(orderCountReceiver, new IntentFilter(OrderUtils.ACTION_COUNT));

        orderCountReceiver.onReceive(null, null);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        if (DEV_IS_SPI) {// 准备SP306/308副屏背景图
            SubScreenLoader.getInstance().load(this);
        }

        TextView snView = findViewById(R.id.sn);
        snView.setText(DeviceUtils.sn());

        TextView versionView = findViewById(R.id.version);
        versionView.setText(appVersionName());

        ImageView logoView = findViewById(R.id.logo);
        switch (App.getInstance().serverType()) {
            case SERVER_TYPE_C_PAY:
                logoView.setImageResource(R.drawable.ic_channel_c_pay);
                logoView.setVisibility(VISIBLE);
                break;
            case SERVER_TYPE_A_PAY:
                logoView.setImageResource(R.drawable.ic_channel_a_pay);
                logoView.setVisibility(VISIBLE);
                break;
        }

        amountView = findViewById(R.id.text_pay_amount);
        modeView = findViewById(R.id.text_mode_name);

        store = StoreFactory.settingStore();
        mode = store.getMode();
        maxAmount = store.getMaxAmount();

        modeView.setText(mode.getText());
        if (mode == PayMode.FIXED) {
            fixedPayPause(true);
            onCardRead();
        } else {
            onPayExpression(null);
        }

        if (isDebug()) {
            toggleMemInfo();
        }
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean resume = intent.getBooleanExtra("resume", false);
        if (resume) {
            return;
        }

        mode = store.getMode();
        maxAmount = store.getMaxAmount();
        modeView.setText(mode.getText());

        try {
            if (deserializeCmd(intent)) {
                return;
            }
        } catch (CmdException e) {
            Log.d(TAG, "" + e.getMessage());
        }

        if (mode == PayMode.FIXED) {
            fixedPayPause(true);
            onCardRead();
        } else {
            onPayExpression(null);
        }

    }

    private boolean deserializeCmd(Intent intent) {
        int type = intent.getIntExtra("type", -1);
        if (type == -1) {
            return false;
        }
        byte[] data = intent.getByteArrayExtra("data");
        SerialPortBizType cmdType = SerialPortBizType.values()[type];
        switch (cmdType) {
            case PAY_POST:
                PayPostReceive submit = (PayPostReceive) cmdType.deserialize(data);
                assert submit != null;
                BigDecimal amount = new BigDecimal(submit.getAmount());
                setMainFragment(null);
                onAmountChange(amount);
                if (onAmountFinish(amount, submit.getName())) {
                    SerialPortPayUtils.response(submit, CustomCmdStatus.SUCCESS);
                } else {
                    SerialPortPayUtils.response(submit, CustomCmdStatus.FAIL);
                }
                return true;
            case PAY_CANCEL:
                CustomCmdReceive cancel = (CustomCmdReceive) cmdType.deserialize(data);
                onPayCancel();
                SerialPortPayUtils.response(cancel, CustomCmdStatus.SUCCESS);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != timeoutFuture) {
            timeoutFuture.cancel(true);
        }
        try {
            fixedPayHandler.removeCallbacks(fixedPayRunnable);
        } catch (Exception ignored) {

        }
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception ignored) {

        }
        try {
            localUnregister(reportReceiver);
        } catch (Exception ignored) {

        }
        try {
            localUnregister(orderCountReceiver);
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void myDestroy() {
        if (mode == PayMode.FIXED) {
            fixedPayPause(true);
        } else {
            //onPayExpression(null);
        }
        onPayEmpty();
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (mode == PayMode.FIXED) {
            if (keyInfo != KEY_ENTER &&  keyInfo != KEY_CANCEL) {
                return true;
            }
            displayLog("定额模式");
            if (fixedPayPause) {
                fixedPayPause(false);
                onPayScan();
            } else if (mainFragment() instanceof PayScanFragment ||
                        (mainFragment() instanceof AbstractTipFragment && mainFragment().isDetached() && ((AbstractTipFragment)mainFragment()).getType() == FAIL)) {
                displayLog("暂停收款");
                fixedPayPause(true);
                onCardRead();
            }
            return true;
        }
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            onPayExpression(null);
        }

        return true;
    }

    @Override
    public boolean onScan(Intent intent) {
        int serverType = StoreFactory.settingStore().getServerType();
        Fragment fragment;
        switch (serverType) {
            case SERVER_TYPE_C_PAY:
                fragment = new PayIngCpayFragment(this, intent);
                break;
            case SERVER_TYPE_A_PAY:
                fragment = new PayIngApayFragment(this, intent);
                break;
            default:
                fragment = new PayIngFragment(this, intent);
                break;
        }
        setMainFragment(fragment);
        return true;
    }

    @Override
    public boolean onPayFinish() {
        isPayFinished = true;
        if (mode == PayMode.FIXED) {
            fixedPayHandler.removeCallbacks(fixedPayRunnable);
            if (!fixedPayPause) {
                Fragment fragment = mainFragment();
                int delay = fragment instanceof Tip && fragment.isAdded() && ((Tip) fragment).getType() == SUCCESS ? 1500 : 3000;
                fixedPayHandler.postDelayed(fixedPayRunnable, delay);
                return false;
            }
        } else {
            getCustomerHolder().showWelcomeLazy();
        }
        return true;
    }

    @Override
    public void setMainFragment(Fragment fragment) {
        if (null != timeoutFuture) {
            timeoutFuture.cancel(true);
        }
        if (fragment instanceof PayScanFragment || fragment instanceof PayIngAbstract ) {
            timeoutFuture = TaskFactory.schedule(() -> {
                if (!fragment.isDetached() && ((Tip) fragment).getType() == Tip.TipType.WAIT) {
                    displayLog("1分钟内没完成支付");
                    speak("等待超时");
                    SerialPortPayUtils.pay(curAmount, PAY_EXPIRED);
                    getCustomerHolder().showPayProcess(PAY_EXPIRED, curAmount);
                    onError("等待超时");
                }
            }, 1, TimeUnit.MINUTES);// 1分钟没交易自动暂停
        }
        super.setMainFragment(fragment);
    }

    @Override
    public void onPayInput(KeyInfo keyInfo) {
        if (mode == PayMode.FIXED) {
            onKeyUp(keyInfo);
        } else {
            onPayExpression(keyInfo);
        }
    }

    @Override
    public void onPayScan() {
        isPayFinished = false;
        displayLog("发起%s元收款...", curAmount);
        setMainFragment(new PayScanFragment(curAmount, curGoodsName, this));
        if (DEV_IS_801) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startActivity(new Intent(this, CustomerActivity.class), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
            }
        }
    }

    @Override
    public void onPayCancel() {
        if (mainFragment() instanceof PayIngAbstract) {
            PayIngAbstract fragment = (PayIngAbstract)mainFragment();
            switch (App.getInstance().serverType()) {
                case SERVER_TYPE_I_PAY:
                    setMainFragment(new PayRevokeFragment(this, fragment.getOrderNo(), fragment.getAmount()));
                    break;
                case SERVER_TYPE_C_PAY:
                    setMainFragment(new PayRevokeCpayFragment(this, fragment.getOrderNo(), fragment.getAmount()));
                    break;
                case SERVER_TYPE_A_PAY:
                    onPayExpression(null);
                    break;
            }

        } else if (mainFragment() instanceof PayRevokeAbstract) {
            PayRevokeAbstract fragment = (PayRevokeAbstract) mainFragment();
            switch (App.getInstance().serverType()) {
                case SERVER_TYPE_I_PAY:
                    setMainFragment(new PayRevokeFragment(this, fragment.getOrderNo(), fragment.getAmount()));
                    break;
                case SERVER_TYPE_C_PAY:
                    setMainFragment(new PayRevokeCpayFragment(this, fragment.getOrderNo(), fragment.getAmount()));
                    break;
                case SERVER_TYPE_A_PAY:
                    onPayExpression(null);
                    break;
            }
        }
    }

    @Override
    public void onError(String msg) {
        setMainFragment(new TipHorizontalFragment(FAIL, msg));
        onPayFinish();
        KeyboardFactory.getKeyboard().showMessage("fail");
    }

    @Override
    public void onSuccess(String msg) {
        setMainFragment(new TipHorizontalFragment(SUCCESS, msg));
    }

    @Override
    public void onAmountChange(BigDecimal amount) {
        curAmount = amount.setScale(2, RoundingMode.DOWN).toPlainString();
        amountView.setText(curAmount);
        KeyboardFactory.getKeyboard().showMessage(curAmount);
    }

    @Override
    public boolean onAmountFinish(BigDecimal amount) {
        return onAmountFinish(amount, null);
    }

    private boolean onAmountFinish(BigDecimal amount, String goodsName) {
        curGoodsName = goodsName;
        if (amount.setScale(2, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) < 1) {
            curAmount = "0.00";
            amountView.setText(curAmount);
            speak("金额无效");
            toast("金额无效");
            return false;
        }

        curAmount = amount.setScale(2, RoundingMode.DOWN).toPlainString();
        amountView.setText(curAmount);
        if (Float.parseFloat(curAmount) > Float.parseFloat(maxAmount)) {
            speak("金额受限");
            toast("金额上限为：%s元", maxAmount);
            return false;
        }

        onPayScan();
        return true;
    }

    public boolean isPayFinished() {
        return isPayFinished;
    }

    private void fixedPayPause(boolean bool) {
        if (mode != PayMode.FIXED) {
            return;
        }
        curAmount = store.getFixAmount();
        curGoodsName = null;
        fixedPayPause = bool;
        fixedPayHandler.post(() -> {
            amountView.setText(curAmount);
            modeView.setText(mode.getText() + (bool ? "(暂停)": ""));
        });
    }

    private void onPayEmpty() {
        isPayFinished = true;
        setMainFragment(null);
        getCustomerHolder().showWelcome();
    }

    private void onPayExpression(KeyInfo keyInfo) {
        isPayFinished = true;
        setMainFragment(new PayExpressionFragment(keyInfo, this));
    }

    private void onCardRead() {
        isPayFinished = true;
        setMainFragment(new CardReadFragemnt());
        getCustomerHolder().showWelcome();
    }

    public static class CardReadFragemnt extends AbstractFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            new CardShower(this).bind();
        }

    }

    private static Future<?> memFuture;
    @SuppressLint("DefaultLocale")
    public void toggleMemInfo() {
        if (null != memFuture) {
            memFuture.cancel(true);
            memFuture = null;
            uiExecute(() -> {
                TextView view = findViewById(R.id.text_mem_log);
                view.setVisibility(GONE);
            });
            return;
        }
        memFuture = TaskFactory.schedule(() -> {
            int max = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
            int tot = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
            int free = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
            int totalPss = -1;
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{myPid()});
            if (null != memInfo && memInfo.length > 0) {
                totalPss = memInfo[0].getTotalPss()/1024;
            }
            int pss = totalPss;
            MemoryInfo mi = new MemoryInfo();
            activityManager.getMemoryInfo(mi);
            uiExecute(() -> {
                TextView view = findViewById(R.id.text_mem_log);
                view.setVisibility(VISIBLE);
                view.setText(String.format("f:%d t:%d m:%d p:%d st:%d sa:%d l:%d",
                        free, tot, max, pss, mi.totalMem/1024/1024, mi.availMem/1024/1024, mi.lowMemory ? 1: 0));
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
}
