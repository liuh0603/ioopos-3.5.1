package com.pay.ioopos.common;

import java.nio.charset.Charset;

/**
 * 16进制工具
 * @author Moyq5
 * @since 2019年2月27日
 */
public class HexUtils {


	/**
	 * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。   
	 * @param byteArray byte[] data
	 * @return hex string   
	 */      
	public static String toHexString(byte[] byteArray){
	    StringBuilder stringBuilder = new StringBuilder();
	    if (byteArray == null || byteArray.length <= 0) {
	        return null;   
	    }
		for (byte b : byteArray) {
			int v = b & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
	    return stringBuilder.toString();   
	} 
	
	/**  
	 * Convert hex string to byte[]  
	 * @param hexString the hex string  
	 * @return byte[]  
	 */  
	public static byte[] toByteArray(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toLowerCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (toByte(hexChars[pos]) << 4 | toByte(hexChars[pos + 1]));   
	    }   
	    return d;   
	}   
	
	/**  
	 * Convert char to byte  
	 * @param c char  
	 * @return byte  
	 */  
	 private static byte toByte(char c) {   
	    return (byte) "0123456789abcdef".indexOf(c);
	 }

	public static String[] bcd2Strs(byte[] datas) {
		String[] strings = new String[11];
		int len = 0;
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < datas.length; i++) {
			if (datas[i] == 0) {
				break;
			}
			if (datas[i] == 0x7C) {
				strings[len ++] = tmp.toString();
				tmp = new StringBuilder();
				continue;
			}

			if (datas[i] > 0) {
				tmp.append(String.format("%c", datas[i]));
			} else {
				byte[] utf8Bytes = new byte[2];
				utf8Bytes[0] = datas[i];
				utf8Bytes[1] = datas[i + 1];
				tmp.append(new String(utf8Bytes, Charset.forName("GBK")));
				i++;
			}
		}
		return strings;

	}
}
