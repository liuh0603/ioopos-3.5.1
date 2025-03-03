package com.pay.ioopos.sqlite;

import android.content.ContentValues;

/**
 * 微信刷脸用户信息
 * @author    Moyq5
 * @since  2020/12/9 16:25
 */
public interface WxFaceUserStore {

    /**
     * 添加/更新用户
     * @param user
     * @return
     */
    long modUser(ContentValues user);

    /**
     * 查询用户
     * @return
     */
    ContentValues selUser(String wxUserId);

    /**
     * 删除非指定机构下所有用户信息
     * @param notWxOrgId
     * @return 返回值大于0说明有其它机构用户信息被删除，同时表示存在机构切换的情况，需要当前机构的用户信息需要重新加载
     */
    int delUserExclude(String notWxOrgId);

    /**
     * 获取本地用户信息最近变动时间，下次需要时将拉取服务端该时间之后有变动的用户信息
     * @return
     */
    Long selModTime();

    /**
     * 记录本地用户信息的最近变动时间
     * @param modTime
     */
    void modModTime(Long modTime);
}
