package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import static java.lang.Thread.sleep;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

import com.pay.ioopos.App;
import com.pay.ioopos.common.HexUtils;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.szzt.zt698dev.SSCardCtrl;

import java.util.concurrent.Future;

/**
 * 政通读卡器
 * @author: Administrator
 * @date: 2025/2/14
 */

public class ZTScanner extends UsbDeviceManagerAbstract implements SSCardCtrl.PinCallback {

    private static final String TAG = ZTScanner.class.getSimpleName();
    private Context mContext;
    private static final String ACTION_USB_PERMISSION = "com.ztscan.USB_PERMISSION";//可自定义

    private static UsbManager mUsbManager;
    private UsbDeviceConnection mUseDeviceConn = null;

    private BroadcastReceiver mUsbPermissionActionReceiver;
    private UsbCallback mUsbCallback;

    private static Future<?> future;

    private static final int VID = 0x23AB;
    private static final int PID = 0x2005;

    int mfd;

    @Override
    protected boolean open(String key, UsbDevice device) {
        mContext = App.getInstance();

        Log.d(TAG, "政通读卡器设备插入：" + device.getProductName());

        String ext_dir = mContext.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS).toString();
        SSCardCtrl.ZTLogConfig(0, ext_dir);
        SSCardCtrl.setPinLister(this);

        if(!tryGetUsbPermission(VID, PID)) {
            toast("政通读卡器打开失败，请重启设备");
            return false;
        }

        mUsbCallback = new UsbCallback() {
            @Override
            public void onPermissionConfirmed() {
                Log.d(TAG, "onPermissionConfirmed");
            }

            @Override
            public void onPermissionDenied(String dev) {
                Log.d(TAG, "onPermissionDenied : " + dev);
            }
        };

        return true;
    }

    @Override
    protected boolean close(String key, UsbDevice device) {
        Log.d(TAG, "政通读卡器设备已拔出：" + device.getProductName());
        toast("政通读卡器设备已拔出：" + device.getProductName());
        SSCardCtrl.dev_close(mfd);
        if(mUsbPermissionActionReceiver != null) {
            mContext.unregisterReceiver(mUsbPermissionActionReceiver);
        }
        stopScan();
        return true;
    }

    public static void startScan() {
        if (null != future) {
            future.cancel(true);
        }
        future = TaskFactory.submit(ZTScanner::readScan);
    }

    public static void stopScan() {
        if (null != future) {
            future.cancel(true);
            future = null;
        }
    }

    @Override
    public long onPinLister(String title, byte[] input, int len) {
        //TODO:处理回调提示
        return 6;
    }

    private static void readScan() {
        while (!Thread.interrupted()) {
            byte[] result = new byte[512];
            long ret = SSCardCtrl.iReadCardBas(2, result);

            if (ret != 0) {
                String errInfo = String.format("get card info err[%d]", ret);
                Log.d(TAG, "read card err: " + errInfo);
            } else {
                String[] mBasicInfo = HexUtils.bcd2Strs(result);
                String info = String.format("DistCode[%s], SS_No[%s], CardNo[%s], CanIdenCode[%s], " +
                                "Name[%s], AtrHistory[%s], CanVer[%s], IssuingDate[%s], ExpireDate[%s]," +
                                " TermNum[%s], TermSN[%s]", mBasicInfo[0], mBasicInfo[1], mBasicInfo[2], mBasicInfo[3], mBasicInfo[4],
                        mBasicInfo[5], mBasicInfo[6], mBasicInfo[7], mBasicInfo[8], mBasicInfo[9], mBasicInfo[10]);
                Log.d(TAG, info);

                Intent intent = new Intent(ZTScanner.class.getName());
                intent.putExtra(INTENT_PARAM_CODE, mBasicInfo[1] + "|" + mBasicInfo[2]);
                localSend(intent);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void tryOpenDevice(UsbDevice usbDevice) {

        mUseDeviceConn = mUsbManager.openDevice(usbDevice);
        mfd = mUseDeviceConn.getFileDescriptor();
        String dev_desc = String.format("VID_%04X&PID_%04X", usbDevice.getVendorId(),
                usbDevice.getProductId());
        int result = SSCardCtrl.dev_open(dev_desc, mfd);

        if (result == 0) {
            Log.d (TAG, "open " + dev_desc + " successed");
            startScan();
        } else {
            Log.d (TAG, "open " + dev_desc + " failed: " + result);
        }
    }

    //获取USB权限
    public Boolean tryGetUsbPermission(int vid, int pid) {

        boolean ret = false;

        mUsbPermissionActionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    context.unregisterReceiver(this);//解注册
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(null != usbDevice){
                                Log.e(TAG,usbDevice.getDeviceName() + "已获取USB权限");
                                if (mUsbCallback != null) {
                                    mUsbCallback.onPermissionConfirmed();
                                }
                                tryOpenDevice(usbDevice);
                            }
                        }
                        else {
                            //user choose NO for your previously popup window asking for grant perssion for this usb device
                            if (mUsbCallback != null) {
                                mUsbCallback.onPermissionDenied("USB权限已被拒绝，Permission denied for device " + usbDevice);
                            }
                        }
                    }

                }
            }
        };

        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        if(mUsbPermissionActionReceiver != null) {
            mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
        }

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        boolean has_usb = false;

        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            if ((usbDevice.getVendorId() == vid) &&
                    (usbDevice.getProductId() == pid)) {
                has_usb = true;
                Log.d(TAG,usbDevice.getDeviceName() + "已找到政通读卡器USB") ;
                if(mUsbManager.hasPermission(usbDevice)){
                    ret = true;
                    if (mUsbCallback != null) {
                        mUsbCallback.onPermissionConfirmed();
                    }
                    tryOpenDevice(usbDevice);
                } else {
                    Log.d(TAG,usbDevice.getDeviceName() + "请求获取USB权限");
                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                    ret = false;
                }
            }
        }

        if(!has_usb) {
            Log.e(TAG,"未找到政通读卡器USB");
        }

        return ret;
    }

    public interface UsbCallback {
        void onPermissionConfirmed();
        void onPermissionDenied(String dev);
    };

}
