package com.pay.ioopos.common;

/**
 * 字符串工具类
 * @author    Moyq5
 * @since  2020/11/5 15:28
 */
public class StringUtils {

    public static String encode(String src, int startIndex) {
        if (null == src) {
            return null;
        }
        int length = src.length();
        if (length <= startIndex) {
            return src;
        }
        int endIndex = length - startIndex;
        if (endIndex <= startIndex) {
            endIndex = startIndex + 1;
        }
        try {
            if (endIndex == length) {
                return src.substring(0, length - 1) + "*";
            }
            return src.substring(0, startIndex) + src.substring(startIndex, endIndex).replaceAll("\\w|\\W|[\\u4e00-\\u9fa5]{1}", "*") + src.substring(endIndex);
        } catch (Exception ignored) {

        }
        return src;
    }

    public static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }

}
