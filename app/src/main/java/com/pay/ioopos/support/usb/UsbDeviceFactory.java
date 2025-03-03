package com.pay.ioopos.support.usb;

/**
 * @author moyq5
 * @since 2022/8/17
 */
public final class UsbDeviceFactory {

    public static UsbDeviceManager manager;

    static  {
        UsbDeviceManagerAbstract custom = new Customer();
        UsbDeviceManagerAbstract keyboard = new GeekmakerKeyboard();
        UsbDeviceManagerAbstract s1601 = new S1601Scanner();
        UsbDeviceManagerAbstract ztScan = new ZTScanner();
        UsbDeviceManagerAbstract mtScan = new MTScanner();

        s1601.setNextManager(keyboard);
        keyboard.setNextManager(custom);
        custom.setNextManager(ztScan);
        ztScan.setNextManager(mtScan);

        manager = mtScan;
    }

    private UsbDeviceFactory() {

    }

    public static UsbDeviceManager getManager() {
        return manager;
    }
}
