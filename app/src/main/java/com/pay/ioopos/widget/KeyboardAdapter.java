package com.pay.ioopos.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pay.ioopos.R;

/**
 * 九宫格键盘适配器
 */
public class KeyboardAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.layout_keyboard_item, null);
        }
        textView = convertView.findViewById(R.id.btn_keys);

        if (position == 9) {
            convertView.findViewById(R.id.imgDelete).setVisibility(View.VISIBLE);
            textView.setText("");
        } else if (position == 10) {
            textView.setText("0");
        } else if (position == 11) {
            textView.setText("确定");
        } else {
            textView.setText("" + (position + 1));
        }
        return convertView;
    }

}
