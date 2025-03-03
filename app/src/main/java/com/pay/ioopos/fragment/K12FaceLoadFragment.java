package com.pay.ioopos.fragment;

import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;

import android.os.Looper;
import android.os.RemoteException;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.tencent.wxpayface.IWxPayfaceCallback;

import java.util.Map;

/**
 * 更新K12本地人脸数据库
 * @author    Moyq5
 * @since  2020/3/25 14:14
 */
public class K12FaceLoadFragment extends K12AbstractFragment {

    public K12FaceLoadFragment() {
        super("正在更新");
    }

    public K12FaceLoadFragment(Fragment fragment) {
        super("正在更新", fragment);
    }

    @Override
    protected void callSdkApi(Map<String, Object> params) {
        MyWxPayFace.getInstance().manualUpdateFaceDatas(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
            if (info == null) {
                dispatch(FAIL, "更新失败：API返回空");
                return;
            }
            if (null == Looper.myLooper()) {
                Looper.prepare();
            }
            String code = (String) info.get("return_code");
            String msg = (String) info.get("return_msg");
            if (code == null || !code.equals("SUCCESS")) {
                dispatch(FAIL, "更新失败：" + msg);
                return;
            }
            dispatch(SUCCESS, "更新成功");
            }
        });
    }
}
