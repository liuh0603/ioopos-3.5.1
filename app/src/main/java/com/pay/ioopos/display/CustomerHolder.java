package com.pay.ioopos.display;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;
import com.pay.ioopos.widget.Tip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 双屏异显-客户屏显示控制类
 * @author    Moyq5
 * @since  2020/3/3 13:38
 */
public interface CustomerHolder {

    Map<Tip.TipType, Integer> tipColors = ((Supplier<Map<Tip.TipType, Integer>>) () -> {
        Map<Tip.TipType, Integer> tipColors = new HashMap<>();
        tipColors.put(Tip.TipType.FAIL, R.color.colorDanger);
        tipColors.put(Tip.TipType.NONE, R.color.colorInfo);
        tipColors.put(Tip.TipType.SUCCESS, R.color.colorSuccess);
        tipColors.put(Tip.TipType.WAIT, R.color.colorWarning);
        tipColors.put(Tip.TipType.WARN, R.color.colorWarning);
        return tipColors;
    }).get();

    Map<Tip.TipType, Integer> tipIcons = ((Supplier<Map<Tip.TipType, Integer>>) () -> {
        Map<Tip.TipType, Integer> tipIcons = new HashMap<>();
        tipIcons.put(Tip.TipType.FAIL, R.string.glyphicon_remove_sign);
        tipIcons.put(Tip.TipType.NONE, R.string.glyphicon_ok_sign);
        tipIcons.put(Tip.TipType.SUCCESS, R.string.glyphicon_ok_sign);
        tipIcons.put(Tip.TipType.WAIT, R.string.glyphicon_exclamation_sign);
        tipIcons.put(Tip.TipType.WARN, R.string.glyphicon_exclamation_sign);
        return tipIcons;
    }).get();

    CustomerPanel getPanel();

    void setPanel(CustomerPanel panel);

    /**
     * 欢迎
     * @author  Moyq5
     * @since    2020/3/3 13:48
     */
    void showWelcome();

    /**
     * 延时显示 欢迎
     * @author  Moyq5
     * @since    2020/3/4 14:36
     */
    void showWelcomeLazy();

    /**
     * 指定要显示的付款金额
     * @author  Moyq5
     * @since    2020/3/17 14:30
     * @param amount 显示支付金额
     */
    void setAmount(String amount);

    /**
     * 指定刷脸实现
     * @author  Moyq5
     * @since    2020/3/4 15:22
     * @param scanFace 刷脸实现
     */
    void setScanFace(ScanFace scanFace);

    /**
     * 开始刷脸
     * @author  Moyq5
     * @since    2020/3/4 15:22
     */
    void payFace();

    /**
     * 显示支付状态进程
     * @author  Moyq5
     * @since    2020/3/4 15:42
     * @param process 支付状态
     * @param amount 支付金额
     */
    void showPayProcess(PayProcess process, String amount);

    /**
     * 显示支付状态进程
     * @author  Moyq5
     * @since    2020/3/4 15:42
     * @param   process 状态
     * @param detail 状态描述
     * @param  amount 交易金额
     */
    void showPayProcess(PayProcess process, String amount, String detail);

    /**
     * 显示支付状态进程
     * @author  Moyq5
     * @since    2022/1/19 11:42
     * @param   process 状态
     * @param   msg 状态描述
     * @param   detail 状态描述
     * @param  amount 交易金额
     */
    void showPayProcess(PayProcess process, String amount, String msg, String detail);

    /**
     * 显示退款进程状态
     * @author  Moyq5
     * @since    2020/3/30 13:40
     * @param   process 状态
     */
    void showRefundProcess(RefundProcess process);

    /**
     * 显示退款进程状态
     * @author  Moyq5
     * @since    2020/3/30 13:40
     * @param   process 状态
     * @param detail 状态描述
     */
    void showRefundProcess(RefundProcess process, String detail);

    /**
     * 显示卡信息：用户、余额等
     * @author  Moyq5
     * @since    2020/11/5 10:53
     * @param info 卡信息
     */
    void showCard(CardInfo info);

    /**
     * 显示卡信息：用户、余额等
     * @author  Moyq5
     * @since    2021/11/1 16:29
     * @param info 卡信息
     * @param order 最近交易
     */
    void showCard(CardInfo info, CardOrder order);

    /**
     * 显示卡最近交易流水
     * @author Moyq5
     * @since    2021/11/1 17:18
     * @param orders 交易流水列表
     */
    void showCard(List<CardOrder> orders);

    /**
     * 显示普通信息
     * @author Moyq5
     * @since 2021/12/6 14:32
     * @param type 消息类型
     * @param msg 提示信息
     * @param detail 详细描述
     */
    void showMsg(Tip.TipType type, String msg, String detail);

    /**
     * 关闭客户屏显示
     * @author  Moyq5
     * @since    2020/3/9 16:20
     */
    void dismiss();

}
