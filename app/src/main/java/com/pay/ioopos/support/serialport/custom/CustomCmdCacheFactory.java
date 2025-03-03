package com.pay.ioopos.support.serialport.custom;

import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_HEAD;
import static com.pay.ioopos.support.serialport.custom.CustomCmdConstants.CMD_TAIL;

import android.util.Log;

import com.pay.ioopos.common.HexUtils;

/**
 * 串口数据粘包处理
 * @author moyq5
 * @since 2022/8/15
 */
abstract class CustomCmdCacheFactory {
    private static final String TAG = CustomCmdCacheFactory.class.getSimpleName();
    private static byte[] cache;
    private static final Object lock = new Object();

    private CustomCmdCacheFactory() {

    }

    static void cache(final byte[] data) {
        synchronized (lock) {
            if (null == data || data.length == 0) {
                return;
            }
            if (null != cache && cache.length > 0  && (cache[0] & CMD_HEAD) == CMD_HEAD && (cache.length == 1 || (cache[1] & CMD_HEAD) == CMD_HEAD)) {
                byte[] newCache = new byte[cache.length + data.length];
                System.arraycopy(cache, 0, newCache, 0, cache.length);
                System.arraycopy(data, 0, newCache, cache.length, data.length);
                cache = newCache;
            } else {
                cache = data;
            }
        }
    }

    static byte[] findCmd() {
        return findCmd(0);
    }

    private static byte[] findCmd(int skip) {
        synchronized (lock) {
            if (null == cache) {
                return null;
            }
            Log.d(TAG, "find in: " + HexUtils.toHexString(cache).toUpperCase());
            // 数据太小
            if (cache.length < skip + 2) {
                // 非法头，丢弃
                if (cache.length > skip && (cache[skip] & CMD_HEAD) != CMD_HEAD) {
                    cache = null;
                }
                return null;
            }

            int headIndex = -1;
            // 找指令包头
            for (int i = skip; i < cache.length; i++) {
                if ((cache[i] & CMD_HEAD) == CMD_HEAD && (i + 1 == cache.length || (cache[i + 1] & CMD_HEAD) == CMD_HEAD)) {
                    headIndex = i;
                    break;
                }
            }
            // 没找指令包头，整个丢弃
            if (headIndex == -1) {
                cache = null;
                return null;
            }

            // 数据长度不够，数据没包含指令包长度字段，等下个包一起
            if (cache.length < headIndex + 4) {//
                return null;
            }
            int pkLen = ((cache[headIndex + 2] << 8) | cache[headIndex + 3]) & 0xFFFF;
            int endIndex = headIndex + pkLen - 2;

            // 数据长度不够，数据没有指令包说明的长度那么长，等下个包一起
            if (endIndex + 2 > cache.length) {
                return null;
            }
            // 非预期包尾，找下一个指令包
            if ((cache[endIndex] & CMD_TAIL) != CMD_TAIL || (cache[endIndex + 1] & CMD_TAIL) != CMD_TAIL) {
                return findCmd(headIndex + 2);
            }

            byte[] dest = new byte[pkLen];
            System.arraycopy(cache, headIndex, dest, 0, dest.length);
            int rmLen = cache.length - headIndex - pkLen;// remain length
            if (rmLen > 0) {
                byte[] newCache = new byte[rmLen];
                System.arraycopy(cache, endIndex + 2, newCache, 0, rmLen);
                cache = newCache;
            } else {
                cache = null;
            }

            Log.d(TAG, "found: " + HexUtils.toHexString(dest).toUpperCase());
            return dest;
        }
    }
}
