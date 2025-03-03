package com.pay.ioopos.trade;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信离线刷脸风控配置类
 * @author mo_yq5
 * @since 2022-2-24
 */
public abstract class WxRisk {

    /**
     * 微信离线刷脸用户user_id黑名单列表
     */
    private static List<String> blackWxUids = new ArrayList<>();
    /**
     * 微信离线刷脸用户out_user_id黑名单列表
     */
    private static List<String> blackOuUids = new ArrayList<>();

    /**
     * 微信离线刷脸用户黑名单更新时间，单位：毫秒
     */
    private static Long settingTime = 0L;

    public static List<String> getBlackWxUids() {
        return blackWxUids;
    }

    public static void setBlackWxUids(List<String> blackWxUids) {
        if (null == blackWxUids) {
            blackWxUids = new ArrayList<>();
        }
        WxRisk.blackWxUids = blackWxUids;
    }

    public static List<String> getBlackOuUids() {
        return blackOuUids;
    }

    public static void setBlackOuUids(List<String> blackOuUids) {
        if (null == blackOuUids) {
            blackOuUids = new ArrayList<>();
        }
        WxRisk.blackOuUids = blackOuUids;
    }

    public static Long getSettingTime() {
        return settingTime;
    }

    public static void setSettingTime(Long settingTime) {
        if (null == settingTime) {
            settingTime = 0L;
        }
        WxRisk.settingTime = settingTime;
    }

}
