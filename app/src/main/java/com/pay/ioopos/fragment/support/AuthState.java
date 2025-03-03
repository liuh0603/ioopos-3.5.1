package com.pay.ioopos.fragment.support;

/**
 * 权限类型检测
 * @author    Moyq5
 * @since  2020/3/26 15:25
 */
public interface AuthState {

    /**
     * 是否需要权限
     * @author  Moyq5
     * @since    2020/3/26 15:25
     * @param
     * @return
     */
    boolean useAuth();

    /**
     * 标志为“已授权”
     * @author  Moyq5
     * @since    2020/3/26 15:49
     * @param
     * @return
     */
    void auth(boolean bool);

    /**
     * 是否“已授权”
     * @author  Moyq5
     * @since    2020/3/26 15:52
     * @param
     * @return
     */
    boolean isAuth();
}
