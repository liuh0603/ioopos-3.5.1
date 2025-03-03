package com.pay.ioopos.support.check;

import android.graphics.Bitmap;
import android.view.View.OnKeyListener;

/**
 * 项目检查日志输出
 * @author    Moyq5
 * @since  2020/6/16 16:54
 */
public interface Console {
    /**
     * 输出普通日志
     * @author  Moyq5
     * @since    2020/6/19 14:15
     * @param
     * @return
     */
    void info(String format, Object... args);
    /**
     * 输出警告日志
     * @author  Moyq5
     * @since    2020/6/19 14:15
     * @param
     * @return
     */
    void warn(String format, Object... args);
    /**
     * 输出错误日志
     * @author  Moyq5
     * @since    2020/6/19 14:15
     * @param
     * @return
     */
    void error(String format, Object... args);
    /**
     * 更新最后一条提示
     * @author  Moyq5
     * @since    2020/6/20 10:17
     * @param
     * @return
     */
    void replace(String format, Object... args);
    /**
     * 输出图片
     * @author  Moyq5
     * @since    2020/6/19 14:14
     * @param
     * @return
     */
    void bitmap(Bitmap bitmap);
    /**
     * 指定键盘监听
     * @author  Moyq5
     * @since    2020/6/19 14:16
     * @param
     * @return
     */
    void setOnKeyListener(OnKeyListener listener);
}
