package com.pay.ioopos.support.face.bean;

import java.util.Date;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public class BdFaceUserUpdateData {

    private String merchNo;
    private Date afterTime;

    public BdFaceUserUpdateData() {
    }

    public String getMerchNo() {
        return this.merchNo;
    }

    public void setMerchNo(String merchNo) {
        this.merchNo = merchNo;
    }

    public Date getAfterTime() {
        return this.afterTime;
    }

    public void setAfterTime(Date afterTime) {
        this.afterTime = afterTime;
    }
}
