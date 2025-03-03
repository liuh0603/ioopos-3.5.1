package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.aill.androidserialport.SerialPort;
import com.pay.ioopos.common.TaskFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * S1601外接扫码器设备
 * @author moyq5
 * @since 2022/8/16
 */
public class S1601Scanner extends UsbDeviceManagerAbstract {
    private static final String TAG = S1601Scanner.class.getSimpleName();
    private static final String DEV_PATH = "/dev/ttyACM0";
    private static final int DEV_RATE = 9600;
    private static Future<?> future;

    @Override
    public boolean open(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("扫码器插入：" + device.getProductName());
        startService();
        return true;
    }

    @Override
    public boolean close(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("扫码器拔出：" + device.getProductName());
        stopService();
        return true;
    }

    private boolean supported(UsbDevice device) {
        return (device.getVendorId() == 0x0525 && device.getProductId() == 0xA4A7)
                || (device.getVendorId() == 0x0103 && device.getProductId() == 0x6061)
                || (device.getVendorId() == 0x152A && device.getProductId() == 0x880F);
    }

    private void startService() {
        if (null != future) {
            future.cancel(true);
        }
        future = TaskFactory.submit(this::startSerialPort);
    }

    private void stopService() {
        if (null != future) {
            future.cancel(true);
            future = null;
        }
    }

    private void startSerialPort() {
        File file = new File(DEV_PATH);
        if (!file.exists()) {
            toast("S1601扫码器不可用，请重启设备");
            return;
        }
        SerialPort serialPort = null;
        try {
            serialPort = new SerialPort(file, DEV_RATE, 0);
            try (InputStream is = serialPort.getInputStream()) {
                reading(is);
            }
        } catch (Throwable e) {
            if (e instanceof InterruptedException // InterruptedException 可能是取消，
                    || e instanceof SecurityException // SecurityException 可能是没接上设备
                    || e instanceof IOException) {// IOException 可能是在使用过程中插掉设备
            } else {
                Log.e(TAG, "S1601扫码器异常：", e);
                toast("S1601扫码器异常: %s", e.getMessage());
            }
        } finally {
            if (null != serialPort) {
                serialPort.close();
            }
        }
    }


    private void reading(InputStream is) throws InterruptedException, IOException {
        byte[] buffer = new byte[255];
        while (!Thread.interrupted()) {
            if (is.available() == 0) {
                Thread.sleep(50);
                continue;
            }
            int len = is.read(buffer);
            byte[] data = new byte[len];
            System.arraycopy(buffer, 0, data, 0, data.length);
            String code = new String(data).replaceAll("[\r\n]", "");
            Intent intent = new Intent(S1601Scanner.class.getName());
            intent.putExtra(INTENT_PARAM_CODE, code);
            localSend(intent);
        }
    }

}
