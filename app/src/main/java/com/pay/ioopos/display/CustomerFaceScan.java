package com.pay.ioopos.display;

import android.graphics.Bitmap;
import android.view.SurfaceView;

/**
 *
 * @author    Moyq5
 * @since  2020/4/22 17:25
 */
public interface CustomerFaceScan {

    /**
     * 刷脸预览
     * @author  Moyq5
     * @since    2020/4/22 17:28
     * @param
     * @return
     */
    SurfaceView getFaceSurface();

    SurfaceView getFaceIrSurface();

    /**
     * 刷脸提示
     * @author  Moyq5
     * @since    2020/4/22 17:29
     * @param
     * @return
     */
    void onFaceTip(String tip, boolean preview);

    /**
     * 显示刷脸后头像
     * @author  Moyq5
     * @since    2020/4/22 17:29
     * @param
     * @return
     */
    void onFaceBitmap(Bitmap bitmap, float left, float right, float top, float bottom);

    /**
     * 显示手机号输入面板
     * @author  Moyq5
     * @since    2020/4/22 17:30
     * @param
     * @return
     */
    void onFaceMobile();

    /**
     * 显示刷脸人名信息
     * @author  Moyq5
     * @since    2020/4/22 17:30
     * @param userInfo
     * @return
     */
    void onUser(Object userInfo);

    /**
     * 客户界面是否在显示
     * @author  Moyq5
     * @since    2020/4/22 17:34
     * @param
     * @return
     */
    boolean isShowing();

}
