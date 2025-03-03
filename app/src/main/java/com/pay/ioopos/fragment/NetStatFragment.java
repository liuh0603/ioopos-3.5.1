package com.pay.ioopos.fragment;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_DELETE;

import android.net.TrafficStats;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 流量统计
 * @author    Moyq5
 * @since  2020/8/14 15:28
 */
public class NetStatFragment extends Fragment implements KeyInfoListener {
    private View view;
    private static long recentTime = -1;
    private static long recentTotal = -1;
    private static long recentTx = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_net_stat, container, false);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        long mobileBytes = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
        long totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        TextView totalView = view.findViewById(R.id.total_stat);
        totalView.setText(mbits(totalBytes));
        TextView mobileView = view.findViewById(R.id.mobile_stat);
        mobileView.setText(mbits(mobileBytes));
        TextView otherTxView = view.findViewById(R.id.other_stat);
        otherTxView.setText(mbits(totalBytes - mobileBytes));

        if (recentTime == -1) {
            recentReset();
        }
        recentShow();

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_DELETE) {
            recentReset();
            recentShow();
            return true;
        }
        return false;
    }

    private String mbits(long bytes) {
       return new BigDecimal("" + bytes).divide(new BigDecimal("1024").pow(2), 2, RoundingMode.DOWN).toPlainString() + "Mb";
    }

    private void recentReset() {
        recentTime = System.currentTimeMillis();
        recentTotal = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        recentTx = TrafficStats.getTotalTxBytes();
    }

    private void recentShow() {
        TextView timeView = view.findViewById(R.id.recent_time);
        timeView.setText("从" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(recentTime)) + "开始：");
        TextView recentTotalView = view.findViewById(R.id.recent_total);
        recentTotalView.setText(mbits(TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - recentTotal));
        TextView recentTxView = view.findViewById(R.id.recent_tx);
        recentTxView.setText(mbits(TrafficStats.getTotalTxBytes() - recentTx));
        TextView recentRxView = view.findViewById(R.id.recent_rx);
        recentRxView.setText(mbits(TrafficStats.getTotalRxBytes() - (recentTotal-recentTx)));
    }
}
