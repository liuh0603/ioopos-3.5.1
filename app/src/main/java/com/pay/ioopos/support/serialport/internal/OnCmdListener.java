package com.pay.ioopos.support.serialport.internal;

/**
 * 指令回调
 * @author    Moyq5
 * @since  2020/10/29 16:51
 */
public interface OnCmdListener {

    /**
     * 指令失败回调
     * @author  Moyq5
     * @since    2020/10/29 16:51
     * @param   code 错误码
     * @return 指定回调完成后要执行的指令
     */
    Cmd onFail(byte code);
    /**
     * 指令成功回调
     * @author  Moyq5
     * @since    2020/10/29 16:52
     * @param   data 业务数据
     * @return 指定回调完成后要执行的指令
     */
    Cmd onSuccess(byte[] data);
}
