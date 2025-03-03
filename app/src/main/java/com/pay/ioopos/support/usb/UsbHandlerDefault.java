package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.toast;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.pay.ioopos.App;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * USB设备管理
 * @author moyq5
 * @since 2022/8/16
 */
class UsbHandlerDefault implements UsbHandler {
    private static final String ACTION_USR_PERMISSION = "com.ioopos.USB_PERMISSION";
    private static final Map<String, UsbDevice> devices = new ConcurrentHashMap<>();

    private final UsbDeviceManager manager;

    public UsbHandlerDefault(UsbDeviceManager manager) {
        this.manager = manager;
    }

    @Override
    public final void onAttached() {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> map = manager.getDeviceList();
        for (Map.Entry<String, UsbDevice> entry: map.entrySet()) {
            requestPermission(entry.getValue(), device_ -> {
                if (this.manager.tryOpen(entry.getKey(), entry.getValue())) {
                    synchronized (devices) {
                        devices.put(entry.getKey(), entry.getValue());
                    }
                }
            });
        }
    }

    @Override
    public final void onDetached() {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> map = manager.getDeviceList();
        synchronized (devices) {
            boolean contained;
            for (Map.Entry<String, UsbDevice> pre: devices.entrySet()) {
                contained = false;
                for (Map.Entry<String, UsbDevice> cur: map.entrySet()) {
                    if (cur.getKey().equals(pre.getKey())
                            && cur.getValue().getVendorId() == pre.getValue().getVendorId()
                            && cur.getValue().getProductId() == pre.getValue().getProductId()) {
                        contained = true;
                        break;
                    }
                }
                if (!contained) {
                    this.manager.tryClose(pre.getKey(), pre.getValue());
                    devices.remove(pre.getKey(), pre.getValue());
                }
            }
        }
    }

    private void requestPermission(UsbDevice device, Consumer<UsbDevice> consumer) {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        if (manager.hasPermission(device)) {
            consumer.accept(device);
            return;
        }
        PendingIntent intent = PendingIntent.getBroadcast(App.getInstance(), 0, new Intent(ACTION_USR_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USR_PERMISSION);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!ACTION_USR_PERMISSION.equals(action)) {
                    return;
                }
                App.getInstance().unregisterReceiver(this);
                UsbDevice curDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && null != curDevice && curDevice.equals(device)) {
                    consumer.accept(device);
                    return;
                }
                toast("设备已被拒绝");
            }
        };
        App.getInstance().registerReceiver(receiver, filter);
        manager.requestPermission(device, intent);
    }

}
