package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.toast;

import android.hardware.usb.UsbDevice;

import com.pay.ioopos.channel.spay.SerialPortPayFactory;
import com.pay.ioopos.channel.spay.SerialPortPayFinder;
import com.pay.ioopos.channel.spay.cmd.VersionSerializer;
import com.pay.ioopos.support.serialport.custom.CustomSerialPort;
import com.pay.ioopos.support.serialport.custom.CustomSerialPortFactory;

/**
 * 自定义串口指令设备
 * @author moyq5
 * @since 2022/8/16
 */
public class Customer extends UsbDeviceManagerAbstract {
    static {
        CustomSerialPortFactory.setDefaultListener(SerialPortPayFactory.getListener());
    }
    @Override
    public boolean open(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("设备插入：" + device.getProductName());
        CustomSerialPortFactory.getManager().detect();
        CustomSerialPortFactory.find(new SerialPortPayFinder() {
            @Override
            public void onFound(CustomSerialPort serialPort) {
                serialPort.send(new VersionSerializer());
            }
        });
        return true;
    }

    @Override
    public boolean close(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("设备拔出：" + device.getProductName());
        CustomSerialPortFactory.getManager().detect();
        return true;
    }

    private boolean supported(UsbDevice device) {
        // 其它内置串口
        return device.getVendorId() != 2630 && device.getProductId() != 38433  // SP306+/SP308+
        && device.getVendorId() != 2965 && device.getProductId() != 30507      // SP306/SP308       AX88772C
        && device.getVendorId() != 11707 && device.getProductId() != 772       // SP306PRO_T        A200 3DSensor
        && device.getVendorId() != 11707 && device.getProductId() != 518       // SP306PRO_T        A200 HD Video device
        && device.getVendorId() != 4070 && device.getProductId() != 39168      // SP306PRO_T        USB 10/100 LAN
        && device.getVendorId() != 12881 && device.getProductId() != 6400      // SP810             USB Lite - U20L
        && device.getVendorId() != 7847 && device.getProductId() != 2322;      // SP810             USB-HID GamingKeyBoard

    }

}
