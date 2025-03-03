package com.pay.ioopos.support.serialport.custom;

import static com.hoho.android.usbserial.driver.UsbSerialPort.DATABITS_8;
import static com.hoho.android.usbserial.driver.UsbSerialPort.PARITY_EVEN;
import static com.hoho.android.usbserial.driver.UsbSerialPort.STOPBITS_1;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener;
import com.pay.ioopos.App;

/**
 * 串口实例
 * @author moyq5
 * @since 2022/7/28
 */
public class CustomSerialPortDefault implements CustomSerialPort {
    private static final String TAG = CustomSerialPortDefault.class.getSimpleName();
    private final UsbSerialDriver driver;
    private SerialInputOutputManager serialManager;

    private CustomSerialPortListener listener;

    private long livingTime = System.currentTimeMillis();

    private final Listener internalListener = new Listener() {
        @Override
        public void onNewData(byte[] bytes) {
            if (null != listener) {
                CustomCmdCacheFactory.cache(bytes);
                byte[] cmd;
                while (null != (cmd = CustomCmdCacheFactory.findCmd())) {
                    listener.onReceive(cmd);
                }
            }
        }

        @Override
        public void onRunError(Exception e) {
            Log.e(TAG, "onRunError: ", e);
            toast("外接串口异常：%s", e.getMessage());
        }
    };

    private CustomSerialPortDefault(UsbSerialDriver driver) {
        this.driver = driver;
    }

    public static CustomSerialPortDefault open(UsbSerialDriver driver) {
        CustomSerialPortDefault serialPort = new CustomSerialPortDefault(driver);
        if (serialPort.open()) {
            return serialPort;
        }
        return null;
    }

    private boolean open() {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        UsbDevice device = driver.getDevice();
        UsbSerialPort port = driver.getPorts().get(0);
        UsbDeviceConnection conn = manager.openDevice(device);

        try {
            port.open(conn);
            port.setParameters(9600, DATABITS_8, STOPBITS_1, PARITY_EVEN);
        } catch (Exception e) {
            // java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.hardware.usb.UsbDeviceConnection.claimInterface(android.hardware.usb.UsbInterface, boolean)' on a null object reference
            Log.e(TAG, "open: ", e);
            return false;
        }
        serialManager = new SerialInputOutputManager(port, internalListener);
        new Thread(serialManager).start();
        return true;
    }

    @Override
    public CustomSerialPortListener getListener() {
        return listener;
    }

    @Override
    public void setListener(CustomSerialPortListener listener) {
        this.listener = listener;
    }

    @Override
    public void send(CustomCmdSerializer serializer) {
        serialManager.writeAsync(serializer.serialize());
    }

    public void living() {
        livingTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return livingTime + 10000 < System.currentTimeMillis();
    }

    public void stop() {
        serialManager.stop();
    }
}
