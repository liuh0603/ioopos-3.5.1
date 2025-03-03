package com.pay.ioopos.display;

import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.os.Handler;
import android.os.Looper;

import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;
import com.pay.ioopos.widget.Tip;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * 双屏异显-客屏显示控制类-通用屏实现
 * @author    Moyq5
 * @since  2020/3/3 13:46
 */
public class CustomerHolderDef implements CustomerHolder {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Vector<Queue> queues = new Vector<>();
    private CustomerPanel panel;
    private final Runnable welcomeInvoke = () -> panel.showWelcome();
    private final Runnable welcomeLazy = () -> handler.postDelayed(welcomeInvoke, 10000);

    public CustomerPanel getPanel() {
        return panel;
    }

    public void setPanel(CustomerPanel panel) {
        this.panel = panel;
        showPanel();
    }

    @Override
    public void showWelcome() {
        showPanel(() -> panel.showWelcome());
    }

    @Override
    public void showWelcomeLazy() {
        showPanel(welcomeLazy);
    }

    @Override
    public void setAmount(String amount) {
        showPanel(() -> panel.onAmount(amount));
    }

    @Override
    public void setScanFace(ScanFace scanFace) {
        showPanel(new Queue() {
            @Override
            public boolean isRequired() {
                return true;
            }

            @Override
            public void run() {
                panel.setScanFace(scanFace);
            }
        });
    }

    @Override
    public void payFace() {
        showPanel(() -> panel.showScanFace());
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
            msg = "支付成功";   //baidu face success
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

        switch (process) {
            case PAY_WAIT:
                showPanel(() -> panel.showPayWait());
                break;
            case PAY_PWD:
            case PAYING:
            case REVOKING:
                showPanel(() -> panel.showPay(WAIT, msg, detail));
                break;
            case PAY_FAIL:
            case PAY_ERROR:
            case PAY_EXPIRED:
            case REVOKE_FAIL:
                showPanel(() -> panel.showPay(FAIL, msg, detail));
                break;
            case PAY_SUCCESS:
            case REVOKE_SUCCESS:
                showPanel(() -> panel.showPay(SUCCESS, msg, detail));
                break;
        }
    }

    @Override
    public void showRefundProcess(RefundProcess process) {

    }

    @Override
    public void showRefundProcess(RefundProcess process, String detail) {

    }

    @Override
    public void showCard(CardInfo info) {
        showPanel(() -> panel.showCard(info));
        showPanel(() -> handler.postDelayed(welcomeInvoke, 30000));
    }

    @Override
    public void showCard(CardInfo info, CardOrder order) {
        showPanel(() -> panel.showCard(info, order));
        showPanel(() -> handler.postDelayed(welcomeInvoke, 30000));
    }

    @Override
    public void showCard(List<CardOrder> orders) {
        showPanel(() -> panel.showCard(orders));
        showPanel(() -> handler.postDelayed(welcomeInvoke, 30000));
    }

    @Override
    public void showMsg(Tip.TipType type, String msg, String detail) {
        showPanel(() -> panel.showMsg(type, msg, detail));
        showPanel(welcomeLazy);
    }

    @Override
    public void dismiss() {
        if (null != panel && panel.isShowing()) {
            panel.hide();
            panel.setScanFace(null);
        }
    }

    private void showPanel() {
        if (null == panel) {
            return;
        }
        if (!panel.isShowing()) {
            panel.show();
        }
        synchronized (queues) {
            Iterator<Queue> it = queues.iterator();
            while (it.hasNext()) {
                it.next().run();
                it.remove();
            }
        }
    }

    private void showPanel(Runnable run) {
        if (run != welcomeLazy) {
            handler.removeCallbacks(welcomeInvoke);
        }
        showPanel(new Queue() {
            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public void run() {
                run.run();
            }
        });
    }

    private void showPanel(Queue newQueue) {
        synchronized (queues) {
            queues.removeIf(queue -> !queue.isRequired());
            queues.add(newQueue);
        }
        showPanel();
    }

    private interface Queue extends Runnable {
        boolean isRequired();
    }

}
