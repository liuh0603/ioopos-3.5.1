package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DELETE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOT;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_UP;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.support.scan.ScanListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态IP设置
 * @author    Moyq5
 * @since  2020/3/30 16:06
 */
public abstract class AbstractIpFragment extends AbstractScanFragment implements ScanListener, OnKeyListener, InputFilter {

    private final List<EditText> editViews = new ArrayList<>();
    private int editIndex = 0;
    private final Fragment cancelFragment;
    public AbstractIpFragment() {
        this(null);
    }

    public AbstractIpFragment(Fragment cancelFragment) {
        super(BarcodeFormat.QR_CODE);
        this.cancelFragment = cancelFragment;
        setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(10, 15, 10, 10);

        uiExecute(() -> initLayout(layout));

        return layout;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo) {
            return true;
        }
        if (keyInfo == KEY_DELETE) {
            editViews.get(editIndex).setText("");
            return true;
        }
        if (keyInfo == KEY_UP) {
            if (editIndex > 0) {
                selectText(editIndex - 1);
            }
            return true;
        }
        if (keyInfo == KEY_DOWN || keyInfo == KEY_DOT) {
            if (editIndex < editViews.size() - 1) {
                selectText(editIndex + 1);
            }
            return true;
        }
        if (keyInfo == KEY_ENTER) {
            checkAndApply();
            return true;
        }
        if (keyInfo == KEY_CANCEL && null != cancelFragment) {
            setMainFragment(cancelFragment);
            return true;
        }

        return false;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (dest.length() >= 3 && !(dstart == 0 && dend == dest.length()) ) {
            return "";
        }
        int num;
        try {
            num = Integer.parseInt(source.toString());
        } catch (NumberFormatException e) {
            return "";
        }
        if (!(dstart == 0 && dend == dest.length())) {
            String next = dest.toString() + num;
            if (Integer.parseInt(next) > 255) {
                toast("有效值：0~255");
                return "";
            }
        }
        return null;
    }

    @Override
    public boolean onScan(Intent intent) {
        String code = intent.getStringExtra(INTENT_PARAM_CODE);
        if (null == code || code.isEmpty()) {
            toast("格式错误");
            return false;
        }
        String[] items = code.split("[;.]");
        if (items.length != 16) {
            toast("格式错误");
            return false;
        }
        for (int i = 0; i < items.length; i++) {
            int num;
            try {
                if ( (num = Integer.parseInt(items[i])) > 255 || num < 0) {
                    throw new NumberFormatException(items[i] + "不在0~255范围内");
                }
            } catch (NumberFormatException e) {
                toast("格式错误：" + e.getMessage());
                return false;
            }
            editViews.get(i).setText(String.valueOf(num));
        }
        selectText(3);
        toast("扫码成功");
        return false;
    }

    private void initLayout(LinearLayout layout) {
        if (null == getContext()) {
            return;
        }
        TextView titleView = new TextView(getContext());
        titleView.setText("扫码或者手动输入IP信息");
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setTextSize(30);
        layout.addView(titleView);

        LayoutParams labelParams = new LayoutParams(130, MATCH_PARENT, 1);
        labelParams.gravity = Gravity.CENTER;
        LayoutParams editParams = new LayoutParams(60, MATCH_PARENT, 1);
        editParams.gravity = Gravity.CENTER;
        LayoutParams dotParams = new LayoutParams(5, MATCH_PARENT, 1);
        dotParams.gravity = Gravity.CENTER;

        String ips = defaultIps();
        String[] items = null;
        if (null != ips) {
            items = ips.split("[;.]");
        }
        int itemIndex = 0;

        String[] labels = new String[] {"IP：", "子网掩码：", "网关：", "DNS："};
        for (String label : labels) {
            LinearLayout linear = new LinearLayout(getContext());
            linear.setOrientation(LinearLayout.HORIZONTAL);
            linear.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            linear.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(linear);

            TextView labelView = new TextView(getContext());
            labelView.setText(label);
            labelView.setLayoutParams(labelParams);
            labelView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            labelView.setTextSize(25);
            linear.addView(labelView);

            for (int j = 0; j < 4; j++) {
                EditText editView = new EditText(getContext());
                if (null != items && itemIndex < items.length) {
                    editView.setText(items[itemIndex++]);
                }
                editView.setFilters(new InputFilter[]{this});
                editView.setOnKeyListener(this);
                editView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editView.setSingleLine();
                editView.setMaxEms(3);
                editView.setLayoutParams(editParams);
                editView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                editView.setTextSize(25);
                linear.addView(editView);
                editViews.add(editView);
                if (j < 3) {
                    TextView dotView = new TextView(getContext());
                    dotView.setText(".");
                    dotView.setLayoutParams(dotParams);
                    dotView.setGravity(Gravity.BOTTOM);
                    dotView.setTextSize(25);
                    linear.addView(dotView);
                }
            }
        }

        if (null != items) {
            selectText(3);
        } else {
            selectText(0);
        }
    }

    private void checkAndApply() {
        EditText editView;
        StringBuilder sb = new StringBuilder();
        String item;
        int size = editViews.size();
        for (int i = 0; i < size; i++) {
            editView = editViews.get(i);
            item = editView.getText().toString();
            if (item.isEmpty()) {
                selectText(i);
                toast("请先完成填写");
                return;
            }
            sb.append(item);
            if (i > 0 && (i + 1)%4 == 0 && i < size - 1) {
                sb.append(";");
            } else if (i < size - 1) {
                sb.append(".");
            }
        }

        applyIps(sb.toString());
    }

    private void selectText(int editIndex) {
        this.editIndex = editIndex;
        EditText editView = editViews.get(editIndex);
        editView.selectAll();
        editView.requestFocus();
    }

    protected abstract String defaultIps();

    protected abstract void applyIps(String ips);

}
