package com.pay.ioopos.fragment;

import static com.pay.ioopos.widget.Tip.TipType.FAIL;

import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.tencent.wxpayface.IWxPayfaceCallback;

import java.util.Map;

/**
 * 显示K12刷脸SDK版本信息
 * @author    Moyq5
 * @since  2020/3/25 14:12
 */
public class K12SdkInfoFragment extends K12AbstractFragment {

    private Fragment fragment;

    public K12SdkInfoFragment() {
        super("正在获取信息");
    }

    public K12SdkInfoFragment(Fragment fragment) {
        super("正在获取信息", fragment);
        this.fragment = fragment;
    }

    @Override
    protected void callSdkApi(Map<String, Object> params) {
        MyWxPayFace.getInstance().getSdkInfo(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
            if (info == null) {
                dispatch(FAIL, "获取失败：API返回空");
                return;
            }
            if (null == Looper.myLooper()) {
                Looper.prepare();
            }
            String code = (String) info.get("return_code");
            String msg = (String) info.get("return_msg");
            if (code == null || !code.equals("SUCCESS")) {
                dispatch(FAIL, "获取失败：" + msg);
                return;
            }
            setMainFragment(new K12SdkInfo(info, fragment));
            //dispatch(SUCCESS, "获取成功");
            }
        });
    }

    /**
     * sdk版本信息展示页
     * @author  Moyq5
     * @since    2020/3/25 16:31
     */
    public static class K12SdkInfo extends AbstractFragment implements KeyInfoListener {
        private final Fragment fragment;
        private final Map<String, Object> info;

        public K12SdkInfo(Map<String, Object> info, Fragment fragment) {
            this.info = info;
            this.fragment = fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view =  inflater.inflate(R.layout.fragment_k12_sdk_info, container, false);
            view.setFocusable(true);
            view.setOnKeyListener(new ViewKeyListener(this));
            view.requestFocus();

            TextView svView = view.findViewById(R.id.sdk_version);
            svView.setText((String)info.get("sdk_version"));
            TextView ffvView = view.findViewById(R.id.face_feature_version);
            ffvView.setText((String)info.get("face_feature_version"));
            TextView uidView = view.findViewById(R.id.user_info_dbcount);
            uidView.setText(String.valueOf(info.get("user_info_dbcount")));
            TextView uimView = view.findViewById(R.id.user_info_memcount);
            uimView.setText(String.valueOf(info.get("user_info_memcount")));
            TextView opidView = view.findViewById(R.id.offline_pay_info_dbcount);
            opidView.setText(String.valueOf(info.get("offline_pay_info_dbcount")));
            return view;

        }

        @Override
        public boolean onKeyUp(KeyInfo keyInfo) {
            switch (keyInfo) {
                case KEY_ENTER:
                case KEY_CANCEL:
                    if (null != fragment) {
                        setMainFragment(fragment);
                        return true;
                    }
            }
            return false;
        }
    }
}
