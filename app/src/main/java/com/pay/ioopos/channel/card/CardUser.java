package com.pay.ioopos.channel.card;

/**
 * 卡用户信息
 * @author    Moyq5
 * @since  2021/10/26 15:14
 */
public class CardUser {
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户学号、工号
     */
    private String userNo;
    /**
     * 用户班级、部门
     */
    private String userGroup;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }
}
