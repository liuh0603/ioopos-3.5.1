package com.pay.ioopos.channel.apay;

import android.util.Base64;
import android.util.Log;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.HttpUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝云支付请求类
 * @author    Moyq5
 * @since  2020/12/11 17:31
 */
public class ApayHttp {
    private static final String TAG = ApayHttp.class.getSimpleName();
    public static Map<String, Object> post(String method, String bizContent) throws Exception {
        ApayStore store = StoreFactory.apayStore();

        Map<String, Object> map = new TreeMap<>();
        map.put("b_app_id", store.getAppId());
        map.put("version", "1.0");
        map.put("method", method);
        map.put("req_id", "" + System.currentTimeMillis());
        map.put("charset", "UTF-8");
        map.put("timestamp", "" + System.currentTimeMillis()/1000);
        map.put("sign_type","RSA2");
        map.put("biz_content", bizContent);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry: map.entrySet()) {// 不包含空值
            if (null != entry.getValue() && !entry.getValue().toString().trim().isEmpty()) {
                sb.append(entry.getKey().trim()).append("=").append(entry.getValue().toString().trim()).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        Log.d(TAG, "签名内容: " + sb);
        String sign = sign(sb.toString(), store.getPriKey());

        map.put("sign", sign);

        String body = HttpUtils.post(store.getServerUrl(), JSON.toString(map));

        map = JSON.toObject(body, new TypeReference<TreeMap<String, Object>>(){});

        // 有签名或者返回成功时要强制验签
        if (null == (sign = (String)map.get("sign")) && !"10000".equals(map.get("code"))) {
            return map;
        }
        if (null == sign) {
            throw new Exception("接口返回签名值为空");
        }
        map.remove("sign");
        sb = new StringBuilder();
        for (Map.Entry<String, Object> entry: map.entrySet()) {// 不包含空值
            if (null != entry.getValue() && !entry.getValue().toString().trim().isEmpty()) {
                sb.append(entry.getKey().trim()).append("=").append(entry.getValue().toString().trim()).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        Log.d(TAG, "验签内容: " + sb);
        if (!verify(sb.toString(), sign, store.getPubKey())) {
            throw new Exception("本地验签未通过");
        }
        return map;
    }

    private static String sign(String data, String prikey) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decode(prikey, Base64.DEFAULT));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(priKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP);
    }

    private static boolean verify(String data, String sign, String pubKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(pubKey, Base64.DEFAULT));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign, Base64.DEFAULT));
    }
}
