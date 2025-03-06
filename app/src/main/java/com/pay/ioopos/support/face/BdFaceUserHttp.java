package com.pay.ioopos.support.face;

import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateData;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateResult;

/**
 * @author: Administrator
 * @date: 2024/5/8
 */

public class BdFaceUserHttp {

    private static final String TAG = BdFaceUserHttp.class.getSimpleName();

    public static Client<BdFaceUserUpdateData, BdFaceUserUpdateResult> updateBdFaceUser(Merch merch) {
        return (Client<BdFaceUserUpdateData, BdFaceUserUpdateResult>) Factory.getClient(BdFaceUserUpdate.class, merch);
    }

    public static void uploadBdFaceUser() {

    }
}
