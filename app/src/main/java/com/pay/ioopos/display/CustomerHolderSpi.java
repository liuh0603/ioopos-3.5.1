package com.pay.ioopos.display;

import static com.pay.ioopos.common.AppFactory.getColor;
import static com.pay.ioopos.common.AppFactory.getString;
import static com.pay.ioopos.common.AppFactory.iconTypeface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.CardUser;
import com.pay.ioopos.display.SpiScreenFactory.SpiScreenCanvas;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.StringUtils;
import com.pay.ioopos.trade.PayMode;
import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;
import com.pay.ioopos.widget.Tip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 双屏异显-客户屏显示控制类-旧屏实现
 * @author    Moyq5
 * @since  2020/3/3 13:49
 */
public class CustomerHolderSpi implements CustomerHolder {

    private final Context context;

    private String amount;

    public CustomerHolderSpi(Context context) {
        this.context = context;
    }

    @Override
    public CustomerPanel getPanel() {
        return null;
    }

    @Override
    public void setPanel(CustomerPanel panel) {

    }

    @Override
    public void showWelcome() {
        SpiScreenFactory.submit(() -> SpiScreenFactory.showWelcome(context));
    }

    @Override
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public void setScanFace(ScanFace scanFace) {

    }

    @Override
    public void payFace() {

    }

    @Override
    public void showPayProcess(PayProcess process, String amount) {
        showPayProcess(process, amount, null);
    }

    @Override
    public void showPayProcess(PayProcess process, String amount, String detail) {
        String msg = "未知";
        switch (process) {
            case PAYING:
                msg = "正在支付";
                break;
            case PAY_EXPIRED:
                msg = "等待超时";
                break;
            case PAY_ERROR:
                msg = "支付异常";
                break;
            case PAY_PWD:
                msg = "请输入密码";
                break;
            case PAY_FAIL:
                msg = "支付失败";
                break;
            case PAY_SUCCESS:
                msg = "支付成功";
                break;
            case REVOKE_FAIL:
                msg = "撤销失败";
                break;
            case REVOKE_SUCCESS:
                msg = "撤销成功";
                break;
            case REVOKING:
                msg = "正在撤销";
                break;
        }
        showPayProcess(process, amount, msg, detail);

    }

    @Override
    public void showPayProcess(PayProcess process, String amount, String msg, String detail) {
        SpiScreenFactory.submit(() -> {
            switch (process) {
                case PAY_WAIT:
                    showPayAmount(amount);
                    break;
                case PAY_PWD:
                case PAYING:
                    showPayStatus(R.color.colorWarning, R.string.glyphicon_exclamation_sign, msg, amount, detail);
                    break;
                case PAY_EXPIRED:
                case PAY_FAIL:
                case PAY_ERROR:
                    showPayStatus(R.color.colorDanger, R.string.glyphicon_remove_sign, msg, amount, detail);
                    break;
                case PAY_SUCCESS:
                    showPayStatus(R.color.colorSuccess, R.string.glyphicon_ok_sign, msg, amount, detail);
                    break;
                case REVOKE_FAIL:
                    showRevokeStatus(R.color.colorDanger, R.string.glyphicon_remove_sign, msg);
                    break;
                case REVOKE_SUCCESS:
                    showRevokeStatus(R.color.colorSuccess, R.string.glyphicon_ok_sign, msg);
                    break;
                case REVOKING:
                    showRevokeStatus(R.color.colorWarning, R.string.glyphicon_exclamation_sign, msg);
                    break;
            }
        });
    }

    @Override
    public void showRefundProcess(RefundProcess process) {
        showRefundProcess(process, null);
    }

    @Override
    public void showRefundProcess(RefundProcess process, String detail) {
        SpiScreenFactory.submit(() -> {
            switch (process) {
                case REFUND_INPUT_ORDER_NO:
                    showRefundScan();
                    break;
                case REFUND_FAIL:
                    showRefundStatus(R.color.colorDanger, R.string.glyphicon_remove_sign, R.string.refund_fail);
                    break;
                case REFUND_SUBMITTED:
                    showRefundStatus(R.color.colorSuccess, R.string.glyphicon_ok_sign, R.string.refund_submit);
                    break;
                case REFUNDING:
                    showRefundStatus(R.color.colorWarning, R.string.glyphicon_exclamation_sign, R.string.refund_refunding);
                    break;
                case REFUND_SUCCESS:
                    showRefundStatus(R.color.colorSuccess, R.string.glyphicon_ok_sign, R.string.refund_success);
                    break;
            }
        });
    }

    @Deprecated
    @Override
    public void showCard(CardInfo info) {
        SpiScreenFactory.submit(() -> {
            showCardInfo(info, null);
            try {
                Thread.sleep(30000);
                SpiScreenFactory.showWelcome(context);
            } catch (InterruptedException ignored) {

            }
        });
    }

    @Override
    public void showCard(CardInfo info, CardOrder order) {
        SpiScreenFactory.submit(() -> {
            showCardInfo(info, order);
            try {
                Thread.sleep(30000);
                SpiScreenFactory.showWelcome(context);
            } catch (InterruptedException ignored) {

            }
        });
    }

    @Override
    public void showCard(List<CardOrder> orders) {
        SpiScreenFactory.submit(() -> {
            showCardInfo(orders);
            try {
                Thread.sleep(30000);
                SpiScreenFactory.showWelcome(context);
            } catch (InterruptedException ignored) {

            }
        });
    }

    @Override
    public void showMsg(Tip.TipType type, String msg, String detail) {
        SpiScreenFactory.submit(() -> {
            show(type, msg, detail);
            if (type != Tip.TipType.WAIT) {
                try {
                    Thread.sleep(10000);
                    SpiScreenFactory.showWelcome(context);
                } catch (InterruptedException ignored) {

                }
            }
        });
    }

    @Override
    public void dismiss() {

    }

    @Override
    public void showWelcomeLazy() {
        SpiScreenFactory.submit(() -> {
            try {
                Thread.sleep(3000);
                SettingStore store = StoreFactory.settingStore();
                if (store.getMode() != PayMode.FIXED) {
                    SpiScreenFactory.showWelcome(context);
                }
            } catch (InterruptedException ignored) {

            }
        }, true);
    }

    private static void showPayAmount(String amount) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(25f);

        String text = "支付金额(元)";
        int fontWidth = (int) paint.measureText(text);
        int startX = (canvas.getWidth() - fontWidth) / 2;
        canvas.drawText(text, startX, 130, paint);

        paint.setTextSize(80f);
        text = amount;
        fontWidth = (int) paint.measureText(text);
        startX = (canvas.getWidth() - fontWidth) / 2;
        canvas.drawText(text, startX, 220, paint);

        SpiScreenFactory.flush();
    }

    private void showPayStatus(int colorId, int iconId, String msg, String amount, String detail) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(80f);

        int moneyWidth = (int) paint.measureText(amount);
        int startX = (canvas.getWidth() - moneyWidth) / 2;
        canvas.drawText(amount, startX, 170, paint);

        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setTextSize(40f);
        iconPaint.setColor(getColor(colorId));
        iconPaint.setTypeface(iconTypeface());

        String icon = getString(iconId);
        int iconWidth = (int) iconPaint.measureText(icon);

        Paint msgPaint = new Paint();
        msgPaint.setAntiAlias(true);
        msgPaint.setTextSize(30f);

        int textWidth = (int) msgPaint.measureText(msg);

        startX = (canvas.getWidth() - iconWidth - textWidth) / 2;
        canvas.drawText(icon, startX, 230, iconPaint);

        startX = startX + iconWidth + 10;
        canvas.drawText(msg, startX, 220, msgPaint);

        if (null != detail) {
            Paint detailPaint = new Paint();
            detailPaint.setAntiAlias(true);
            detailPaint.setTextSize(20f);
            detailPaint.setColor(getColor(R.color.colorDanger));
            int detailWidth = (int) detailPaint.measureText(detail);
            startX = (canvas.getWidth() - detailWidth) / 2;
            canvas.drawText(detail, startX, 270, detailPaint);
        }

        SpiScreenFactory.flush();

    }

    private void showRevokeStatus(int colorId, int iconId, String text) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(80f);

        int moneyWidth = (int) paint.measureText(amount);
        int startX = (canvas.getWidth() - moneyWidth) / 2;
        canvas.drawText(amount, startX, 170, paint);

        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setTextSize(40f);
        iconPaint.setColor(getColor(colorId));
        iconPaint.setTypeface(iconTypeface());

        String icon = getString(iconId);
        int iconWidth = (int) iconPaint.measureText(icon);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(30f);

        int textWidth = (int) textPaint.measureText(text);

        startX = (canvas.getWidth() - iconWidth - textWidth) / 2;
        canvas.drawText(icon, startX, 230, iconPaint);

        startX = startX + iconWidth + 10;
        canvas.drawText(text, startX, 220, textPaint);

        SpiScreenFactory.flush();

    }

    private void showRefundScan() {
        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setTextSize(70f);
        iconPaint.setColor(getColor(R.color.colorWarning));
        iconPaint.setTypeface(iconTypeface());

        String icon = getString(R.string.glyphicon_exclamation_sign);
        int iconWidth = (int) iconPaint.measureText(icon);
        int startX = (canvas.getWidth() - iconWidth) / 2;
        canvas.drawText(icon, startX, 150, iconPaint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(30f);

        String text = "请扫码退款";
        int fontWidth = (int) paint.measureText(text);
        startX = (canvas.getWidth() - fontWidth) / 2;
        canvas.drawText(text, startX, 220, paint);

        SpiScreenFactory.flush();

    }

    private void showRefundStatus(int colorId, int iconId, int textId) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setTextSize(70f);
        iconPaint.setColor(getColor(colorId));
        iconPaint.setTypeface(iconTypeface());

        String icon = getString(iconId);
        int iconWidth = (int) iconPaint.measureText(icon);
        int startX = (canvas.getWidth() - iconWidth) / 2;
        canvas.drawText(icon, startX, 150, iconPaint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(30f);

        String text = getString(textId);
        int fontWidth = (int) paint.measureText(text);
        startX = (canvas.getWidth() - fontWidth) / 2;
        canvas.drawText(text, startX, 220, paint);

        SpiScreenFactory.flush();

    }

    @SuppressLint("SimpleDateFormat")
    private void showCardInfo(CardInfo info, CardOrder order) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(25f);

        CardUser user = info.getUser();
        if (null == user) {
            user = new CardUser();
        }

        String balance = BigDecimalUtils.fenToYuan(info.getBalance()).toPlainString();
        canvas.drawText("卡号：" + info.getCardUid(), 50, 40, paint);
        canvas.drawText("姓名：" + StringUtils.encode(user.getUserName(), 1), 50, 80, paint);
        canvas.drawText("学号：" + StringUtils.encode(user.getUserNo(), 3), 50, 120, paint);
        canvas.drawText("班级：" + user.getUserGroup(), 50, 160, paint);
        canvas.drawText("余额：" + balance + " 元", 50, 200, paint);
        if (null != order && order.isSuccess()) {
            String amount = BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString();
            canvas.drawText("最近消费：" + amount + " 元", 50, 240, paint);
            canvas.drawText("交易时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(order.getOrderTime() * 1000)), 50, 280, paint);
        }

        SpiScreenFactory.flush();

    }

    @SuppressLint("SimpleDateFormat")
    private void showCardInfo(List<CardOrder> orders) {
        if (null == orders) {
            show(Tip.TipType.WARN,"没有记录", null);
            return;
        }

        Iterator<CardOrder> iterator = orders.stream()
                .filter(CardOrder::isSuccess)
                .sorted((o1, o2) -> {
                    if (o1.getOrderTime() > o2.getOrderTime()) {
                        return -1;
                    }
                    return 0;
                })
                .limit(10)
                .iterator();

        if (!iterator.hasNext()) {
            show(Tip.TipType.WARN, "没有记录", null);
            return;
        }

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(20f);

        int i = 0;
        CardOrder order;
        String amount;
        String before;
        String after;
        while (iterator.hasNext()) {
            i++;
            order = iterator.next();
            amount = BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString();
            before = BigDecimalUtils.fenToYuan(order.getBalance()).toPlainString();
            after = BigDecimalUtils.fenToYuan(order.getBalance() - order.getAmount()).toPlainString();
            canvas.drawText((i < 10 ? "0" + i : i) + ".   " + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(order.getOrderTime() * 1000)) + "  " + before + "   -" + amount + "   =" + after, 40, 30 * i, paint);
        }

        SpiScreenFactory.flush();

    }

    private void show(Tip.TipType type, String msg, String detail) {

        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        Paint iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setTextSize(70f);

        iconPaint.setColor(getColor(tipColors.get(type)));
        iconPaint.setTypeface(iconTypeface());

        String icon = getString(tipIcons.get(type));
        int textWidth = (int) iconPaint.measureText(icon);

        int startX = (canvas.getWidth() - textWidth) / 2;
        canvas.drawText(icon, startX, 150, iconPaint);

        Paint msgPaint = new Paint();
        msgPaint.setAntiAlias(true);
        msgPaint.setTextSize(30f);

        textWidth = (int) msgPaint.measureText(msg);
        startX = (canvas.getWidth() - textWidth) / 2;
        canvas.drawText(msg, startX, 210, msgPaint);

        if (null != detail) {
            Paint detailPaint = new Paint();
            detailPaint.setAntiAlias(true);
            detailPaint.setTextSize(20f);
            detailPaint.setColor(getColor(tipColors.get(type)));
            textWidth = (int) detailPaint.measureText(detail);
            startX = (canvas.getWidth() - textWidth) / 2;
            canvas.drawText(detail, startX, 270, detailPaint);
        }

        SpiScreenFactory.flush();
    }

}
