package com.pay.ioopos.support.check;

import androidx.lifecycle.LifecycleOwner;

/**
 * 项目检查
 * @author    Moyq5
 * @since  2020/6/16 19:43
 */
public interface Check {

    /**
     * 指定语音播报系统
     * @param speech
     */
    void setSpeech(Speech speech);

    /**
     * 绑定生命周期
     * @author  Moyq5
     * @since    2020/6/19 10:45
     * @param
     * @return
     */
    void bindToLifecycle(LifecycleOwner lifecycleOwner);
    /**
     * 指定控制台
     * @author  Moyq5
     * @since    2020/6/17 18:17
     * @param
     * @return
     */
    void setConsole(Console console);
    /**
     * 检查并返回结果
     * @author  Moyq5
     * @since    2020/6/16 19:52
     * @param
     * @return  true 为检查通过，false 检查未通过
     */
    boolean check();
}
