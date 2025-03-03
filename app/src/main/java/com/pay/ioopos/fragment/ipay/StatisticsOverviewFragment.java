package com.pay.ioopos.fragment.ipay;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.AbstractNetworkFragment;
import com.pay.ioopos.fragment.support.BindState;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 交易汇总
 * @author    Moyq5
 * @since  2020/3/27 9:57
 */
public class StatisticsOverviewFragment extends AbstractNetworkFragment implements BindState {
    private final float fontSize = 20;
    private JSONObject totalObj;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return view = inflater.inflate(R.layout.fragment_statistics_overview, container, false);
    }

    @Override
    protected void execute() throws Exception {
        Log.d("liuh StatisticsOverviewFragment", "execute " );

        Client<Void, String> client = SanstarApiFactory.statisticsOverviewV2(ApiUtils.initApi());

        showLoading();
        Result<String> apiResult = client.execute(null);
        hideLoading();

        if (apiResult.getStatus() != Result.Status.OK) {
            if ("C9997".equals(apiResult.getCode())) {// InterruptedIOException
                return;
            }
            onError("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        String overview = apiResult.getData();
        Log.d("liuh StatisticsOverviewFragment", "execute overview= " +overview);
        uiExecute(() -> {
            try {
                JSONObject obj = new JSONObject(overview);
                TableLayout table = view.findViewById(R.id.pay_overview);
                //addTitle(table,"今天统计");

                // 始终显示收款汇总
                addRow(table, new String[]{"今天收款", "成功收款", "收款笔数", "成功率"});

                totalObj = new JSONObject();
                List<String[]> rows = createRows(obj.getJSONObject("pay"));

                if (rows.size() == 0) {
                    addTitle(table,"（无记录）");
                } else {
                    rows.add(createRow("合计", totalObj));

                    rows.forEach(data -> addRow(table, data));
                }

                // 有退款汇总才显示

                totalObj = new JSONObject();
                rows = createRows(obj.getJSONObject("refund"));
                if (rows.size() == 0) {
                    return;
                }

                addTitle(table,"");
                addRow(table, new String[]{"今天退款", "成功退款", "退款笔数", "成功率"});

                rows.add(createRow("合计", totalObj));
                rows.forEach(data -> addRow(table, data));

            } catch (JSONException ignored) {

            }

        });

    }

    private List<String[]> createRows(JSONObject obj) throws JSONException {
        List<String[]> rows = new ArrayList<>();
        if (null == obj) {
            return rows;
        }
        Iterator<String>keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject item = obj.getJSONObject(key);
            String[] rowData = createRow(itemName(key, item.optString("name")), item);
            if (null != rowData) {
                rows.add(rowData);
            }
        }
        rows.sort((row1, row2) -> -1);
        return rows;
    }

    private String itemName(String key, String name){
        if (null != name && !name.isEmpty()) {
            return name;
        }
        switch(key) {
            case "alipay":
                return "支付宝";
            case "weixin":
                return "微信";
            case "unionpay":
                return "银联";
            case "jd":
                return "京东";
            case "other":
                return "其它";
            default:
                return "未知";
        }
    }

    private void addTitle(TableLayout table, String title) {
        Context context = getContext();
        if (null == context) {
            return;
        }
        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.weight = 1;
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(layoutParams);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView = new TextView(context);
        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        if (title.isEmpty()) {
            textView.setTextSize(1);
            textView.setHeight(10);
        } else {
            textView.setTextSize(fontSize);
        }
        textView.setTextSize(fontSize);
        textView.setText(title);
        row.addView(textView);
        table.addView(row);
    }

    private String[] createRow(String name, JSONObject object) throws JSONException {
        if (null == object) {
            return null;
        }
        int totCount = object.has("totCount") ? object.getInt("totCount"): 0;
        if (totCount == 0) {
            return null;
        }

        String totSum = object.has("totSum") ? object.getString("totSum"): "0.00";
        int sucCount = object.has("sucCount") ? object.getInt("sucCount"): 0;
        String sucSum = object.has("sucSum") ? object.getString("sucSum"): "0.00";
        String percent = new BigDecimal((double)sucCount/totCount + "").multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.DOWN).toString() + "%";

        final String[] rowData =new String[]{
                name,
                new BigDecimal(sucSum).setScale(2, RoundingMode.DOWN).toPlainString(),
                sucCount + "/" + totCount,
                percent
        };

        totCount = (!totalObj.has("totCount") ? totCount: (totalObj.getInt("totCount")) + totCount);
        totSum = new BigDecimal(totSum).add(new BigDecimal((!totalObj.has("totSum") ? "0": totalObj.getString("totSum")))).toString();
        sucCount = (!totalObj.has("sucCount") ? sucCount: (totalObj.getInt("sucCount")) + sucCount);
        sucSum = new BigDecimal(sucSum).add(new BigDecimal((!totalObj.has("sucSum") ? "0": totalObj.getString("sucSum")))).toString();
        totalObj.put("totCount", totCount);
        totalObj.put("totSum", totSum);
        totalObj.put("sucCount", sucCount);
        totalObj.put("sucSum", sucSum);

        return rowData;
    }

    private void addRow(TableLayout table, String[] values) {
        Context context = getContext();
        if (null == context) {
            return;
        }
        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.weight = 1;
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(layoutParams);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int length = values.length;
        for (int i = 0; i < length; i++) {
            TextView textView = new TextView(context);
            LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            if (i == 0 || i == 3) {
                params.weight = 9;
            } else {
                params.weight = 8;
            }

            textView.setLayoutParams(params);
            //textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(fontSize);
            textView.setText(values[i]);
            row.addView(textView);
        }
        table.addView(row);
    }

}
