package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

import com.hnykt.utils.MTReader;
import com.pay.ioopos.App;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.support.scan.mtscan.MTScan;
import com.szzt.zt698dev.SSCardCtrl;

import java.util.concurrent.Future;

/**
 * 明泰读卡器
 * @author: Administrator
 * @date: 2025/2/17
 */

public class MTScanner extends UsbDeviceManagerAbstract {

    private static final String TAG = MTScanner.class.getSimpleName();
    private Context mContext;
    private static final String ACTION_USB_PERMISSION = "com.mtscan.USB_PERMISSION";//可自定义

    private static UsbManager mUsbManager;
    private UsbDeviceConnection mUseDeviceConn = null;

    private BroadcastReceiver mUsbPermissionActionReceiver;
    private UsbCallback mUsbCallback;

    private static Future<?> future;

    private static final int VID = 0x23A4;
    private static final int PID = 0x020C;

    private static String sLogSavaPath = Environment.getExternalStorageDirectory() + "/SSCardDriver/";
    private static String sLogDirName = "Readder";

    int mfd;

    @Override
    protected boolean open(String key, UsbDevice device) {
        mContext = App.getInstance();

        Log.d(TAG, "明泰读卡器设备插入：" + device.getProductName());

        if(!tryGetUsbPermission(VID, PID)) {
            toast("明泰读卡器打开失败，请重启设备");
            return false;
        }

        MTReader.EnabledLog(sLogSavaPath.getBytes(), sLogDirName.getBytes());

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
        Log.d(TAG, "明泰读卡器设备已拔出：" + device.getProductName());
        toast("明泰读卡器设备已拔出：" + device.getProductName());
        MTReader.iCloseDevice();
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
        future = TaskFactory.submit(MTScanner::readScan);
    }

    public static void stopScan() {
        if (null != future) {
            future.cancel(true);
            future = null;
        }
    }

    private static void readScan() {
        while (!Thread.interrupted()) {

            byte[] pOutInfo = new byte[4096];
            long ret = MTReader.iReadCardBas(4, pOutInfo);
            String strCardInfo = new String(pOutInfo).trim();
            if (ret != 0) {
                @SuppressLint("DefaultLocale") String errInfo = String.format("错误码:[%d],提示信息:[%s]", ret, strCardInfo);
                Log.d(TAG, "read card err: " + errInfo);
            } else {
                String[] strCardMsg = strCardInfo.split("\\|");
                String strCardBaseInfo = String.format("%s|%s", strCardMsg[1], strCardMsg[2]);
                Log.d(TAG, "read card info : " + strCardBaseInfo);

                Intent intent = new Intent(MTScanner.class.getName());
                intent.putExtra(INTENT_PARAM_CODE, strCardBaseInfo);
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
        long result = MTReader.iOpenUsbDevice(usbDevice.getVendorId(), usbDevice.getProductId(), mfd, usbDevice.getDeviceName().getBytes());

        if (result > 0) {
            Log.d (TAG, "open " + dev_desc + " successed");
            //startScan();
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
                                Log.e(TAG, usbDevice.getDeviceName() + "已获取USB权限");
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
                Log.d(TAG,usbDevice.getDeviceName() + "已找到明泰读卡器USB") ;
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
            Log.e(TAG,"未找到明泰读卡器USB");
        }

        return ret;
    }

    public interface UsbCallback {
        void onPermissionConfirmed();
        void onPermissionDenied(String dev);
    };

}
