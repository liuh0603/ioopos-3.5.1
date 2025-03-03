package com.pay.ioopos.common;

import static com.pay.ioopos.App.getInstance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DeviceUtils {

    @SuppressLint("MissingPermission")
    public static String sn() {
        //return "SP3082003231580";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return android.os.Build.getSerial();
        } else {
            return Build.SERIAL;
        }

    }

    public static String imei() {
        TelephonyManager tm = (TelephonyManager) getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (getInstance().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return tm.getDeviceId();
        }
        return null;
    }

    /**
     * 获取网络类型
     * @author  Moyq5
     * @since    2020/4/1 15:18
     */
    public static String net() {
        ConnectivityManager cm = (ConnectivityManager) getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (null == network) {
            return "网络未连";
        }
        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        if (null == nc) {
            return "网络未连";
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return "网络未连";
        }
        String valid = "";
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            valid = "（不可上网）";
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return "有线网络" + valid;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return "WIFI网络" + valid;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return "移动网络" + valid;
        }
        return "未知";
    }

    /**
     * 获取ip和子网掩码
     * @author  Moyq5
     * @since    2020/4/1 15:17
     */
    public static String[] ips() {
        String[] ips = new String[2];
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface ni;
            List<InterfaceAddress> ids;
            Iterator<InterfaceAddress> iid;
            InterfaceAddress id;
            InetAddress ia;
            String ip;
            while(interfaces.hasMoreElements()) {
                ni = interfaces.nextElement();
                if (!ni.isUp()) { // 判断网卡是否在使用
                    continue;
                }
                ids = ni.getInterfaceAddresses();
                iid = ids.iterator();
                while (iid.hasNext()) {
                    id = iid.next();
                    ia = id.getAddress();
                    if (ia.isLoopbackAddress()) {
                        continue;
                    }
                    ip = ia.getHostAddress();
                    if (ia.isSiteLocalAddress()) {
                        int index = ip.indexOf("%wlan0");
                        if (index != -1) {
                            ip = ip.substring(0, index);
                        }
                        ips[0] = ip;
                        ips[1] = calcMaskByPrefixLength(id.getNetworkPrefixLength());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ips;
    }

    /**
     * 获取dns
     * @author  Moyq5
     * @since    2020/4/1 15:18
     */
    public static String dns() {
        ConnectivityManager cm = (ConnectivityManager) getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (null == network) {
            return "";
        }
        LinkProperties lp = cm.getLinkProperties(network);
        if (null == lp) {
            return "";
        }
        for (InetAddress ia : lp.getDnsServers()) {
            return ia.getHostAddress();
        }
        return "";
    }

    /**
     * 计算出子网掩码
     * @author  Moyq5
     * @since    2020/4/1 15:18
     */
    private static String calcMaskByPrefixLength(int length) {

        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int[] maskParts = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        StringBuilder result = new StringBuilder();
        result.append(maskParts[0]);
        for (int i = 1; i < maskParts.length; i++) {
            result.append(".").append(maskParts[i]);
        }
        return result.toString();
    }

    /**
     * 获取mac地址（适配所有Android版本）
     */
    public static String getMac() {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     */
    private static String getMacDefault() {
        String mac;
        WifiManager wifi = (WifiManager) getInstance().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null || null == (mac = info.getMacAddress())) {
            return null;
        }
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    private static String getMacAddress() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();//去空格
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     */
    private static String getMacFromHardware() {
        try {
            Enumeration<NetworkInterface> all = NetworkInterface.getNetworkInterfaces();
            while(all.hasMoreElements()) {
                NetworkInterface nif = all.nextElement();
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder res1 = new StringBuilder();
                for (Byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
