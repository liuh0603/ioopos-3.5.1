package com.pay.ioopos.support.serialport.custom;

/**
 * 串口操作工厂类
 * @author moyq5
 * @since 2022/7/27
 */
public class CustomSerialPortFactory {

    private static CustomSerialPortManager manager = new CustomSerialPortManagerDefault();

    private static CustomSerialPortListener defaultListener;
    public static void onUsbAttached() {
        manager.detect();
    }
    public static void onUsbDetached() {
        manager.detect();
    }
    public static void setManager(CustomSerialPortManager manager) {
        CustomSerialPortFactory.manager = manager;
    }

    public static CustomSerialPortManager getManager() {
        return manager;
    }

    public static void find(CustomSerialPortFinder finder) {
        manager.find(finder);
    }

    public static CustomSerialPortListener getDefaultListener() {
        return defaultListener;
    }

    public static void setDefaultListener(CustomSerialPortListener defaultListener) {
        CustomSerialPortFactory.defaultListener = defaultListener;
    }

}
