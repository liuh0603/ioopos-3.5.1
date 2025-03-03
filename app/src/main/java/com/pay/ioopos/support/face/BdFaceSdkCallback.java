package com.pay.ioopos.support.face;

public interface BdFaceSdkCallback<T extends BdFaceSdkStatus> {

    void call(T data);

}
