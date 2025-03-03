package com.pay.ioopos.support.face;

import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateData;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateResult;

/**
 * @author: Administrator
 * @date: 2024/5/9
 */

public class BdFaceUserUpdate extends AbstractClient<BdFaceUserUpdateData, BdFaceUserUpdateResult> {

    public BdFaceUserUpdate(Merch merch) {
        super(merch);
    }

    protected String getServerPath() {
        //return /*Factory.getConfig().getServerPath()*/ "https://school-test.ncyunqi.com/scard/aiboshi" + "/baidu/face/user";
        return Factory.getConfig().getServerPath() + "/baidu/face/user";
    }

    protected Class<BdFaceUserUpdateResult> getResultClass() {
        return BdFaceUserUpdateResult.class;
    }
}