package com.szzt.zt698dev;

import android.util.Log;

public class SSCardCtrl {

    private static final String TAG = "SSCardCtrl";
    private static OnEventListener eventListener;

    static {
        try{
            System.loadLibrary("zt698dev");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface OnEventListener {
        void onEvent(int event_id, byte[][] data);
    }

    public void setEventListener(OnEventListener listener) {
        eventListener = listener;
    }

    public static void dev_event_callback(int event_id, byte[][] data) {
        if(eventListener != null) {
            eventListener.onEvent(event_id, data);
        }
    }

    /**
     *      Serial(such as "COM1:9600,N,8,1", "ttyS1:9600,N,8,1")
     *      HID(such as "VID_23AB&PID_1003")， 测试接口
     */

    public static native void ZTLogConfig(int nLevel, String strLogPath, int nSaveMode, int nExtend);

    public static void ZTLogConfig(int nLevel, String strLogPath)
    {
        ZTLogConfig(nLevel, strLogPath, -2, 0);
    }

    public static native int dev_open(String dev, int baud);
    public static native int dev_close(long handle);
    public static native int dev_reset();

    public static native int get_version(int len, byte[] ver);

    public static native long NICC_PowerOn(byte[] Response, long[] o_len);
    public static native long NICC_PowerOff();
    public static native long NICC_Apdu(byte[] apdu, long[] length, byte[] resp);

    public static native long IC_Select(int slot_no);
    public static native long IC_PowerOn(byte[] resp, long[] o_len);
    public static native long IC_PowerOff();
    public static native long IC_GetStatus(int slot_no);
    public static native long IC_SendAPDU(byte[] apdu, long[] length, byte[] resp);

    /**
     *  社保卡操作接口
     * **/
    public static native long iSendCpuApdu(int iType, byte[] apdu, long[] length, byte[] resp);
    public static native long iReadCardNo(int iType, byte[] pOutInfo);
    public static native long iReadCardBas(int iType, byte[] pOutInfo);
    public static native long iReadCardBas_HSM_Step1(int iType, byte[] pOutInfo);
    public static native long iReadCardBas_HSM_Step2(byte[] pKey, byte[] pOutInfo);
    public static native long iReadCardBas_HSM_Step1_to_2(byte[] pInInfo, byte[] pOutInfo);

    public static native long iReadCard(int iType, int iAuthType, byte[] pCardInfo,
                                        byte[] pFileAddr, byte[] pOutInfo);
    public static native long iReadCard_HSM_Step1(int iType, byte[] pCardInfo, byte[] pFileAddr, byte[] pOutInfo);
    public static native long iReadCard_HSM_Step2(byte[] pKey, byte[] pOutInfo);
    public static native long iReadCard_HSM_Step1_to_2(byte[] pInInfo, byte[] pOutInfo);

    public static native long iWriteCard(int iType, byte[] pCardInfo, byte[] pFileAddr,
                                         byte[] pWriteData, byte[] pOutInfo);
    public static native long iWriteCard_HSM_Step1(int iType, byte[] pCardInfo, byte[] pFileAddr, byte[] pOutInfo);
    public static native long iWriteCard_HSM_Step2(byte[]  pKey, byte[] pWriteData, byte[] pOutInfo);
    public static native long iWriteCard_HSM_Step1_to_2(byte[] pInInfo, byte[] pOutInfo);

    public static native long iVerifyPIN(int iType, byte[] pOutInfo);
    public static native long iChangePIN(int iType, byte[] pOutInfo);
    public static native long iReloadPIN(int iType, byte[] pCardInfo, byte[] pOutInfo);
    public static native long iReloadPIN_HSM_Step1(int iType, byte[] pCardInfo, byte[] pOutInfo);
    public static native long iReloadPIN_HSM_Step2(byte[] pKey, byte[] pOutInfo);
    public static native long iReloadPIN_HSM_Step3(byte[] pKey, byte[] pOutInfo);
    public static native long iReloadPIN_HSM_Step1_to_2(byte[] pInInfo, byte[] pOutInfo);
    public static native long iReloadPIN_HSM_Step2_to_3(byte[] pInInfo, byte[] pOutInfo);

    public static native long iUnblockPIN(int iType, byte[] pCardInfo, byte[] pOutInfo);
    public static native long iUnblockPIN_HSM_Step1(int iType, byte[] pCardInfo, byte[] pOutInfo);
    public static native long iUnblockPIN_HSM_Step2(byte[] pKey, byte[] pOutInfo);
    public static native long iUnblockPIN_HSM_Step3(byte[] pKey, byte[] pOutInfo);
    public static native long iUnblockPIN_HSM_Step1_to_2(byte[] pInInfo, byte[] pOutInfo);
    public static native long iUnblockPIN_HSM_Step2_to_3(byte[] pInInfo, byte[] pOutInfo);

    public static native long iDoDebit(int iType, byte[] pCardInfo, byte[] pPayInfo, byte[] pOutInfo);
    public static native long iReadDebitRecord(int iType, byte[] pOutInfo);

    public static native long iWriteCaStep1(int iType, byte[] i_user_pin, byte[] pOutInfo);
    public static native long iWriteCaStep2(byte[] pUserPin, byte[] pPrivateKey, byte[] pEnCert, byte[] pSignCert, byte[] pOldDevKey,
                                            byte[] pNewDevKey, byte[] pOldAdminPIN, byte[] pNewAdminPIN, byte[] pOutInfo);

    public interface PinCallback {
        long onPinLister(String title, byte[] input, int len);
    }

    private static PinCallback mPinCallback;

    public static void setPinLister(PinCallback lister) {
        mPinCallback = lister;
    }

    private static long getPinCallback(String title, byte[] input, int len) {
        long ret = 0;
        Log.d (TAG, "getPinCallback, mPinCallback: " + mPinCallback);
        if (mPinCallback != null) {
            ret = mPinCallback.onPinLister(title, input, len);
        }
        return ret;
    }



}
