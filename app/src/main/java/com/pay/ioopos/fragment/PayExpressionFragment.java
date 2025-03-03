package com.pay.ioopos.fragment;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DIVIDE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOT;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_MULTIPLY;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.activity.ServerActivity;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.trade.CardShower;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 付款金额运算表达式
 * @author    Moyq5
 * @since  2020/3/30 11:56
 */
public class PayExpressionFragment extends AbstractFragment implements KeyInfoListener, Scheduled {
    private OnPayExpressionListener listener;
    private TextView expressionView;
    private KeyInfo firstKeyInfo;
    private final List<ExpInfo> exps = Collections.synchronizedList(new ArrayList<>());

    public PayExpressionFragment() {

    }

    public PayExpressionFragment(KeyInfo firstKeyInfo, OnPayExpressionListener listener) {
        this.firstKeyInfo = firstKeyInfo;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_expression, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        expressionView = view.findViewById(R.id.expression);

        try {
            if (null != firstKeyInfo) {
                onKeyInfo(firstKeyInfo);
            } else {
                dispatchChange();
            }
        } catch (Throwable e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        getCustomerHolder().showWelcome();

        if (ApiUtils.walletPaySupport()) {
            new CardShower(this).bind();
        }

        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        try {
            return  onKeyInfo(keyInfo);
        } catch (Throwable e) {
            Log.e(TAG, "onKey: ", e);
        }
        return true;
    }

    private boolean onKeyInfo(KeyInfo keyInfo) {
        if (null == keyInfo) {
            return true;
        }
        switch (keyInfo) {
            case KEY_MENU:
            case KEY_SEARCH:
                return false;
        }

        if (keyInfo == KeyInfo.KEY_ENTER) {
            dispatchFinish();
            return true;
        }

        switch (keyInfo) {
            case KEY_CANCEL:
                exps.clear();
                break;
            case KEY_DELETE:
                del();
                break;
            case KEY_NUM_0:
            case KEY_NUM_1:
            case KEY_NUM_2:
            case KEY_NUM_3:
            case KEY_NUM_4:
            case KEY_NUM_5:
            case KEY_NUM_6:
            case KEY_NUM_7:
            case KEY_NUM_8:
            case KEY_NUM_9:
            case KEY_DOT:
                num(keyInfo);
                break;
            case KEY_ADD:
            case KEY_SUBTRACT:
            case KEY_MULTIPLY:
            case KEY_DOWN:
            case KEY_DIVIDE:
                math(keyInfo);
                break;
        }

        if (exps.size() == 0) {
            expressionView.setText("");
            listener.onAmountChange(BigDecimal.ZERO);
            return true;
        }

        // 内置指令
        ExpInfo cur = exps.get(exps.size() - 1);
        if (cur.exp.endsWith("3.1415926")) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (cur.exp.endsWith("0.6180339")) {
            startActivity(new Intent(App.getInstance(), ServerActivity.class));
        } else if (cur.exp.endsWith("2.7182818")) {
            ((MainActivity)getActivity()).toggleMemInfo();
        }

        dispatchChange();
        return true;
    }

    public interface OnPayExpressionListener {
        void onAmountChange(BigDecimal amount);
        boolean onAmountFinish(BigDecimal amount);
    }

    private static class ExpInfo {
        private String exp = "";
        private KeyInfo math;
        private String value = "";
        private String result = "";
    }

    private void num(KeyInfo keyInfo) {
        if (exps.size() == 0) {
            ExpInfo nex = new ExpInfo();
            nex.math = null;
            nex.value = keyInfo == KEY_DOT ? "0.": keyInfo.getValue();// 加0避免NumberFormatException
            nex.result = nex.value;
            nex.exp = nex.value;
            exps.add(nex);
            return;
        }
        ExpInfo cur = exps.get(exps.size() - 1);
        if (keyInfo == KEY_DOT && cur.value.contains(".")) {// 一个数只能包含一个小数点
            return;
        }

        if (keyInfo == KEY_DOT) {// 加0避免NumberFormatException
            if (cur.exp.isEmpty() || (null != cur.math && cur.exp.endsWith(cur.math.getValue()))) {
                cur.exp += "0";
            }
            if (cur.value.isEmpty()) {
                cur.value = "0";
            }
            if (cur.result.isEmpty()) {
                cur.result = "0";
            }
        }
        cur.exp += keyInfo.getValue();
        cur.value += keyInfo.getValue();
        if (exps.size() == 1) {
            cur.result += keyInfo.getValue();
            return;
        }
        if (null == cur.math) {
            return;
        }
        ExpInfo pre = exps.get(exps.size() - 2);
        switch (cur.math) {
            case KEY_ADD:
                cur.result = new BigDecimal(pre.result).add(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_SUBTRACT:
                cur.result = new BigDecimal(pre.result).subtract(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_MULTIPLY:
            case KEY_DOWN:
                cur.result = new BigDecimal(pre.result).multiply(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_DIVIDE:
                if (new BigDecimal(cur.value).compareTo(BigDecimal.ZERO) > 0) {
                    cur.result = new BigDecimal(pre.result).divide(new BigDecimal(cur.value), 2, RoundingMode.DOWN).toPlainString();
                }
                break;
        }
    }

    private void math(KeyInfo math) {
        if (exps.size() == 0) {
            return;
        }
        ExpInfo pre = exps.get(exps.size() - 1);
        if (null == pre.value || pre.value.isEmpty()) {
            return;
        }
        ExpInfo nex = new ExpInfo();
        nex.math = math;
        nex.result = pre.result;
        switch (math) {
            case KEY_DIVIDE:
            case KEY_MULTIPLY:
            case KEY_DOWN:
                if (exps.size() > 1 && !pre.exp.endsWith(")") && pre.math != KEY_DIVIDE && pre.math != KEY_MULTIPLY && pre.math != KEY_DOWN) {
                    nex.exp = "(" + pre.exp + ")" + nex.math.getValue();
                    break;
                }
            default:
                nex.exp = pre.exp + nex.math.getValue();
                break;
        }
        exps.add(nex);
    }

    private void del() {
        if (exps.size() == 0) {
            return;
        }
        ExpInfo cur = exps.get(exps.size() - 1);
        if (cur.exp.startsWith("(") && cur.exp.endsWith(")")) {// 去掉最外层括号
            cur.exp = cur.exp.substring(1, cur.exp.length() - 1);
        } else {// 去掉运算符和最外层的括号
            cur.exp = cur.exp.substring(0, cur.exp.length() - 1);
            if (cur.exp.startsWith("(") && cur.exp.endsWith(")")) {
                cur.exp = cur.exp.substring(1, cur.exp.length() - 1);
            }
        }
        if (cur.exp.isEmpty()) {// 所有内容都已删除
            exps.remove(cur);
            return;
        }
        if (exps.size() == 1) {// 纯数字，不包含有运算符
            cur.result = cur.exp;
            return;
        }
        ExpInfo pre = exps.get(exps.size() - 2);
        if (cur.exp.equals(pre.exp)) {// 当前表达式与上一条表达一样
            exps.remove(cur);
            return;
        }
        if (cur.exp.endsWith(cur.math.getValue())) {// 新的表达开始（即比上个表达多个运算符）
            cur.value = "";
            cur.result = pre.result;
            return;
        }
        cur.value = cur.value.substring(0, cur.value.length() - 1);// 当前表达式新增数字
        switch (cur.math) {// 上个表达的结果与当前表达式新增值进行运算
            case KEY_ADD:
                cur.result = new BigDecimal(pre.result).add(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_SUBTRACT:
                cur.result = new BigDecimal(pre.result).subtract(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_MULTIPLY:
            case KEY_DOWN:
                cur.result = new BigDecimal(pre.result).multiply(new BigDecimal(cur.value)).toPlainString();
                break;
            case KEY_DIVIDE:
                if (new BigDecimal(cur.value).compareTo(BigDecimal.ZERO) > 0) {// 遇到输入除数为0时先不计算
                    cur.result = new BigDecimal(pre.result).divide(new BigDecimal(cur.value), 2, RoundingMode.DOWN).toPlainString();
                }
                break;
        }
    }

    private void dispatchFinish() {
        if (exps.size() > 0) {
            ExpInfo cur = exps.get(exps.size() - 1);
            listener.onAmountFinish(new BigDecimal(cur.result));
        } else {
            listener.onAmountFinish(BigDecimal.ZERO);
        }
    }

    private void dispatchChange() {
        if (exps.size() == 0) {
            expressionView.setText("");
            listener.onAmountChange(BigDecimal.ZERO);
            return;
        }
        ExpInfo cur = exps.get(exps.size() -1);
        expressionView.setText(cur.exp);
        try {
            listener.onAmountChange(new BigDecimal(cur.result));
        } catch (NumberFormatException e) {
            listener.onAmountChange(BigDecimal.ZERO);
        }
    }

}
