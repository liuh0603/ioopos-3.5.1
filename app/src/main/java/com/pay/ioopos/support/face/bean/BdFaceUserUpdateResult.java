package com.pay.ioopos.support.face.bean;

import com.aggregate.pay.sanstar.ListResult;

import java.util.Date;

/**
 * @author: Administrator
 * @date: 2024/5/8
 */

public class BdFaceUserUpdateResult extends ListResult<BdFaceUser> {
    private Date beforeTime;

    public BdFaceUserUpdateResult() {
    }

    public Date getBeforeTime() {
        return this.beforeTime;
    }

    public void setBeforeTime(Date beforeTime) {
        this.beforeTime = beforeTime;
    }
}
