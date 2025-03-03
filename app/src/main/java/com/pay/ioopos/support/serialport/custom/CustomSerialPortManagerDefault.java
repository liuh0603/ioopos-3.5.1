package com.pay.ioopos.support.serialport.custom;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.pay.ioopos.App;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 串口默认管理器
 * @author moyq5
 * @since 2022/7/27
 */
public class CustomSerialPortManagerDefault implements CustomSerialPortManager {
    private static final String TAG = CustomSerialPortManagerDefault.class.getSimpleName();

    private static final Map<String, CustomSerialPort> serialPorts = new ConcurrentHashMap<>();

    @Override
    public void find(CustomSerialPortFinder finder) {
        if (serialPorts.size() == 1) {
            finder.onFound(serialPorts.values().iterator().next());
            return;
        }
        for (Map.Entry<String, CustomSerialPort> entry: serialPorts.entrySet()) {
            CustomSerialPort serialPort = entry.getValue();
            CustomSerialPortListener listener = serialPort.getListener();
            serialPort.setListener(bytes -> {
                serialPort.setListener(listener);
                if (finder.ack(bytes)) {
                    finder.onFound(serialPort);
                }
            });
            serialPort.send(finder);
        }
    }

    @Override
    public void detect() {
        UsbManager manager = (UsbManager) App.getInstance().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> drivers = null;
        try {
            drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        } catch (RuntimeException ignored) {
            // android.os.DeadSystemException
        }
        if (null == drivers || drivers.isEmpty()) {
            clean();
            return;
        }
        UsbDevice device;
        String key;
        CustomSerialPortDefault serialPort;
        for (UsbSerialDriver driver: drivers) {
            device = driver.getDevice();
            key = device.getVendorId() + "-" + device.getProductId();
            Log.d(TAG, "detect: " + device);
            if (!serialPorts.containsKey(key)) {
                serialPort = CustomSerialPortDefault.open(driver);
                if (null != serialPort) {
                    serialPort.setListener(CustomSerialPortFactory.getDefaultListener());
                    serialPorts.put(key, serialPort);
                }
            } else {
                serialPort = (CustomSerialPortDefault) serialPorts.get(key);
                serialPort.living();
            }
        }
        clean();
    }

    private static void clean() {
        CustomSerialPortDefault serialPort;
        for (Map.Entry<String, CustomSerialPort> entry: serialPorts.entrySet()) {
            serialPort = (CustomSerialPortDefault)entry.getValue();
            if (serialPort.isExpired()) {
                serialPort.stop();
                serialPorts.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}
