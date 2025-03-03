package com.hnykt.utils;

import android.util.Log;

public class MTReader {
	private static String	TAG	= "MTReader";
	
	static {
		try {
			System.loadLibrary("curl");
			System.loadLibrary("modcomm");
			System.loadLibrary("stdcomm");
			System.loadLibrary("SSCardDriverMT");
			Log.d(TAG, "loadLibrary SSCardDriverMT succeed!");	
		} catch (Throwable ex) {
			String mLoadErrMsg = ex.toString();
			Log.e(TAG, mLoadErrMsg);
		}
	}
	
	public static native long iOpenUsbDevice(int iVid,int iPid,int iUsbFD,byte[] pUsbPath);
	public static native long iOpenSerialDevice(int devid,int proid,int iBuad,byte[] pSerialPath);
	public static native long iCloseDevice();

	public static native long iReadCardBas(int iType,byte[] pOutInfo);
	public static native long iReadCardBas_HSM_Step1(int iType,byte[] pOutInfo);
	public static native long iReadCardBas_HSM_Step1tostep2(byte[] indata, byte[] outdata);
	public static native long iReadCardBas_HSM_Step2(byte[] pKey,byte[] pOutInfo);

	public static native long iReadCard(int iType, int iAuthType,byte[] pCardInfo,byte[] pFileAddr,byte[] pOutInfo);
	public static native long iReadCard_HSM_Step1(int iType,byte[] pCardInfo,byte[] pFileAddr,byte[] pOutInfo);
	public static native long iReadCard_HSM_Step1tostep2(byte[] indata, byte[] outdata);
	public static native long iReadCard_HSM_Step2(byte[] pKey,byte[] pOutInfo);

	public static native long iWriteCard(int iType,byte[] pCardInfo,byte[] pFileAddr,byte[] pWriteData,byte[] pOutInfo);
	public static native long iWriteCard_HSM_Step1(int iType,byte[] pCardInfo,byte[] pFileAddr,byte[] pOutInfo);
	public static native long iWriteCard_HSM_Step1tostep2(byte[] indata, byte[] outdata);
	public static native long iWriteCard_HSM_Step2(byte[] pKey,byte[] pWriteData,byte[] pOutInfo);

	public static native long iVerifyPIN(int iType,byte[] pOutInfo);
	public static native long iChangePIN(int iType,byte[] pOutInfo);

	public static native long iReloadPIN(int iType,byte[] pCardInfo,byte[] pOutInfo);
	public static native long iReloadPIN_HSM_Step1(int iType,byte[] pCardInfo,byte[] pOutInfo);
	public static native long iReloadPIN_HSM_Step1tostep2(byte[] indata, byte[] outdata);
	public static native long iReloadPIN_HSM_Step2(byte[] pKey,byte[] pOutInfo);
	public static native long iReloadPIN_HSM_Step2tostep3(byte[] indata, byte[] outdata);
	public static native long iReloadPIN_HSM_Step3(byte[] pKey,byte[] pOutInfo);

	public static native long iUnblockPIN(int iType,byte[] pCardInfo,byte[] pOutInfo);
	public static native long iUnblockPIN_HSM_Step1(int iType,byte[] pCardInfo,byte[] pOutInfo);
	public static native long iUnblockPIN_HSM_Step1tostep2(byte[] indata, byte[] outdata);
	public static native long iUnblockPIN_HSM_Step2(byte[] pKey,byte[] pOutInfo);
	public static native long iUnblockPIN_HSM_Step2tostep3(byte[] indata, byte[] outdata);
	public static native long iUnblockPIN_HSM_Step3(byte[] pKey,byte[] pOutInfo);

	public static native long iDoDebit(int iType,byte[] pCardInfo,byte[] pPayInfo,byte[] pOutInfo);
	public static native long iDoDebit_HSM_Step1(int iType,byte[] pCardInfo,byte[] pPayInfo,byte[] pOutInfo);
	public static native long iDoDebit_HSM_Step1toStep2(byte[] indata, byte[] outdata);
	public static native long iDoDebit_HSM_Step2(byte[] pKey,byte[] pOutInfo);
	
	public static native long iReadDebitRecord(int iType,byte[] pOutInfo);
	//读取金融ic卡号
	//public static native long iRPbocAccount(int iType,int slotno,byte[] Account,byte[] pOutInfo);
	
	public static native int EnabledLog(byte[] sLogSavaPath,byte[] sLogDirName);
	public static native int DisEnabledLog();
		
	//public static native retCardInfo  iReadCardYkt(int iType,retCardInfo obj,String cardAddr);
	//public static native retCardImage iReadCardYktImage(int iType,retCardImage obj,String ckkPostAdd);

}

