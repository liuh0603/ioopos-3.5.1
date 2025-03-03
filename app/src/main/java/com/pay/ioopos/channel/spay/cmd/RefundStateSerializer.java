package com.pay.ioopos.channel.spay.cmd;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;
import com.pay.ioopos.support.serialport.internal.CmdParamException;
import com.pay.ioopos.common.BigDecimalUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * 输出指令：退款状态通知
 * @author moyq5
 * @since 2022/8/10
 */
public class RefundStateSerializer extends SerialPortSerializer {
    private static int serialNo = 0;
    /**
     * 要退款的交易单号
     */
    private String orderNo;
    /**
     * 要退款的金额，单位：元。不填表示全额退款
     */
    private String amount;
    /**
     * 状态
     */
    private State state;
    /**
     * 状态描述
     */
    private String message;

    public RefundStateSerializer(@NonNull String orderNo, @NonNull State state) {
        this.orderNo = orderNo;
        this.state = state;

    }

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.REFUND_STATE;
    }

    @Override
    protected int serialNo() {
        if (serialNo > 0xFFFF) {
            serialNo = 0;
        }
        return serialNo++;
    }

    @Override
    protected byte[] content() {
        if (null == orderNo || orderNo.isEmpty()) {
            throw new CmdParamException("单号不能为空");
        }
        if (null == state) {
            throw new CmdParamException("状态不能为空");
        }

        byte[] noData = orderNo.getBytes(StandardCharsets.UTF_8);
        if (noData.length > 0xFF) {
            noData = new byte[0];// 单号过长，忽略
        }

        byte[] msgData = new byte[0];
        if (null != message && !message.isEmpty()) {
            msgData = message.getBytes(StandardCharsets.UTF_8);
        }
        if (msgData.length > 0xFF) {
            msgData = new byte[0];// 描述过长，忽略
        }
        int n = noData.length;
        int m = msgData.length;

        // 金额（3）+ 单号长度（1） + 单号内容（n）+ 状态（1） + 描述长度（1） + 描述内容（m）
        byte[] content = new byte[6 + n + m];

        int fen = 0;
        if (null != amount && !amount.isEmpty()) {
            fen = BigDecimalUtils.yuanToFen(new BigDecimal(amount));
            if (fen < 0 || fen > 0xFFFFFF) {
                throw new CmdParamException("金额无效");
            }
        }
        // 金额，3字节，高位在前，单位：分
        content[0] = (byte) ((fen >> 16) & 0xFF);
        content[1] = (byte) ((fen >> 8) & 0xFF);
        content[2] = (byte) (fen& 0xFF);

        // 单号长度，1字节
        content[3] = (byte) (n & 0xFF);
        // 单号，n字节
        if (n > 0) {
            System.arraycopy(noData, 0, content, 4, n);
        }
        // 状态，1字节
        content[4 + n] = (byte) (state.ordinal() & 0xFF);
        // 描述长度，1字节
        content[5 + n] = (byte) (m & 0xFF);
        // 描述内容，m字节
        if (m > 0) {
            System.arraycopy(msgData, 0, content, 6 + n, m);
        }

        return content;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 枚举顺序不可变
     */
    public enum State {
        /**
         * 正在申请退款
         */
        REFUND_SUBMITTING,
        /**
         * 退款申请成功（未到账）
         */
        REFUND_SUBMITTED,
        /**
         * 退款成功（已到账）
         */
        REFUND_SUCCESS,
        /**
         * 退款失败
         */
        REFUND_FAIL
    }
}
