package com.pay.ioopos.sqlite;

import com.pay.ioopos.support.face.bean.BdFaceUser;

import java.util.List;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public interface BdFaceUserStore {

    /**
     * 添加/更新用户信息
     * @param bdFace 职员信息
     * @return 更新记录数
     */
    long mod(BdFaceUser bdFace);

    /**
     * 查询用户信息
     * @param bdFaceNo 用户号
     * @return 用户信息
     */
    BdFaceUser one(String bdFaceNo);

    /**
     *  获取商户职员记录
     * @param merchNo 商户号
     * @param afterTime  更新时间
     * @param page  页码
     * @return 职员列表
     */
    List<BdFaceUser> list(String merchNo, long afterTime, int page);

    /**
     * 统计用户总数
     * @return 用户数
     */
    int count();

    /**
     * 删除非指定商户用户信息
     * @param merchNo 保留商户号对应的用户信息
     * @return 返回值大于0说明有其它商户用户信息被删除，同时表示存在商户切换的情况，需要当前商户的用户信息需要重新加载
     */
    int delExcept(String merchNo);

    /**
     * 删除所有记录
     * @return 删除记录数
     */
    int delAll();
}
