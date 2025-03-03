package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pay.ioopos.sqlite.StoreFactory;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 证书下载，兼容旧版本http库不能自动信息证书的情况，即手动下载和证书信任
 * @author mo_yq5
 * @since 2021-07-16
 */
public class SslCertLoadWorker extends Worker {
    public static final String PARAM_FORCE = "force";
    private static final String TAG = SslCertLoadWorker.class.getName();
    private static boolean isLoaded = false;
    private static SSLSocketFactory socketFactory;

    public SslCertLoadWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        boolean force = inputData.getBoolean(PARAM_FORCE, false);
        if (!force && isLoaded) {
            return Result.success();
        }
        try {
            socketFactory = loadCertificate();
        } catch (Exception e) {
            socketFactory = null;
            toast("HTTPS证书加载失败：" + e.getMessage());
            Log.w(TAG, "HTTPS证书加载失败：", e);
        }
        isLoaded = true;
        return Result.success();
    }


    private static SSLSocketFactory loadCertificate() throws Exception {
        final String serverUrl = StoreFactory.settingStore().getServerUrl();
        boolean isSSLHandshakeException = false;
        Request request = new Request.Builder() .url(serverUrl).get().build();
        try (Response ignored = new OkHttpClient.Builder().build().newCall(request).execute()) {
        } catch (SSLHandshakeException e) {
            isSSLHandshakeException = true;
        } catch (Exception e) {
            return null;
        }
        if (!isSSLHandshakeException) {
            return null;
        }
        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpsURLConnection)) {
            return null;
        }
        HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
        httpsConn.setSSLSocketFactory(trustAllSslSocketFactory());
        httpsConn.connect();
        Certificate[] certs = httpsConn.getServerCertificates();
        if (null == certs || certs.length == 0) {
            return null;
        }

        Certificate cer = certs[0];

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setCertificateEntry("trust", cer);
        KeyManagerFactory kf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kf.init(ks, null);
        TrustManagerFactory tf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");// SSL
        sslContext.init(kf.getKeyManagers(), tf.getTrustManagers(), new SecureRandom());

        return sslContext.getSocketFactory();

    }

    public static SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }

    private static SSLSocketFactory trustAllSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[1];
        trustAllCerts[0] = new TrustAllManager();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        return sc.getSocketFactory();
    }

    private static class TrustAllManager implements X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
    }
}
