package com.pay.ioopos.display;

import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.widget.Tip.TipType;

import java.util.List;

/**
 * 
 * @author    Moyq5
 * @since  2020/4/22 17:44
 */
public interface CustomerPanel {

    /**
     * 显示客户屏
     * @author  Moyq5
     * @since    2020/4/22 17:48
     */
    void show();

    /**
     * 显示欢迎界面
     * @author  Moyq5
     * @since    2020/4/22 17:49
     */
    void showWelcome();

    /**
     * 显示交易金额
     * @author  Moyq5
     * @since    2020/4/22 17:50
     * @param amount 金额 元
     */
    void onAmount(String amount);

    /**
     * 显示等待支付界面
     * @author  Moyq5
     * @since    2020/4/22 17:50
     */
    void showPayWait();

    /**
     * 指定刷脸实现
     * @author  Moyq5
     * @since    2020/4/22 17:51
     */
    void setScanFace(ScanFace scanFace);

    /**
     * 显示开始刷脸界面
     * @author  Moyq5
     * @since    2020/4/22 17:51
     */
    void showScanFace();

    /**
     * 更新显示交易状态
     * @author  Moyq5
     * @since    2020/4/22 17:52
     * @param type 状态类型
     * @param msg 状态信息
     * @param detail 详细描述
     */
    void showPay(TipType type, String msg, String detail);

    /**
     * 客户屏是否在显示
     * @author  Moyq5
     * @since    2020/4/22 17:53
     */
    boolean isShowing();

    /**
     * 关闭客户屏显示
     * @author  Moyq5
     * @since    2020/4/22 17:54
     */
    void hide();

    /**
     * 显示实体卡信息、余额等
     * @author  Moyq5
     * @since    2021/1/14 15:16
     * @param info 卡基本信息
     */
    void showCard(CardInfo info);

    /**
     * 显示实体卡信息、余额等
     * @author  Moyq5
     * @since    2021/11/1 16:30
     * @param info 卡基本信息
     * @param order 最近交易信息
     */
    void showCard(CardInfo info, CardOrder order);

    /**
     * 显示实体卡交易流水
     * @author  Moyq5
     * @since    2021/11/1 17:32
     * @param orders 最近交易列表
     */
    void showCard(List<CardOrder> orders);

    void showMsg(TipType type, String msg, String detail);
}
