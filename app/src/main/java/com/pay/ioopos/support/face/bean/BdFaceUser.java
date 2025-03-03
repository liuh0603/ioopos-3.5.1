package com.pay.ioopos.support.face.bean;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public class BdFaceUser {
    private String faceFeature;
    private Object faceImg;
    private Object userInfo;
    private String userName;
    private String userId;

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public Object getFaceImg() {
        return faceImg;
    }

    public void setFaceImg(Object faceImg) {
        this.faceImg = faceImg;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
