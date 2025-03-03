package com.pay.ioopos.display;

import com.pay.ioopos.support.scan.ScanListener;

/**
 * 刷脸可用状态
 * @author    Moyq5
 * @since  2020/3/3 19:23
 */
public interface ScanFace {

    void setScanListener(ScanListener listener);

    ScanListener getScanListener();

    /**
     * 指定客户界面
     * @param customer
     */
    void setCustomerPanel(CustomerFaceScan customer);

    /**
     * 刷脸功能是否正常
     * @author  Moyq5
     * @since    2020/3/3 19:24
     * @param
     * @return
     */
    boolean isAvailable();

    /**
     * 刷脸功能错误信息
     * @author  Moyq5
     * @since    2020/3/3 19:25
     * @param
     * @return
     */
    String message();

    /**
     * 开始刷脸
     * @author  Moyq5
     * @since    2020/3/4 11:10
     * @return
     */
    void verify();

    /**
     * 完成刷脸
     * @author  Moyq5
     * @since    2020/3/8 16:27
     * @param   mobile 手机号后4位
     * @return
     */
    void finish(String mobile);

    /**
     * 获取支付凭证支付, 并支付
     * @author  Moyq5
     * @since    2020/5/14 15:55
     * @param
     * @return
     */
    void credential();
    /**
     * 获取支付凭证支付, 不支付
     * @author  Moyq5
     * @since    2020/5/9 17:05
     */
    void credential(Runnable callback);
}
