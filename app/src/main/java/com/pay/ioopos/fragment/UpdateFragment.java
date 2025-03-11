package com.pay.ioopos.fragment;

import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;
import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.pay.ioopos.App;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.service.UpdateService;
import com.pay.ioopos.common.HttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Response;

/**
 * 软件版本查检和升级
 * @author    Moyq5
 * @since  2020/3/25 19:20
 */
public class UpdateFragment extends TipVerticalFragment {

    private static final String TAG = UpdateFragment.class.getSimpleName();
    public UpdateFragment() {
        super(WAIT, "正在检查版本");
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        try {
            UpdateService.query((bool, date, url) -> {
                onChecked(bool);
                if (!bool) {
                    return;
                }
                download(url);
            });

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                return;
            }
            onError("升级失败：" + e.getMessage());
        }
    }

    private void download(String url) throws Exception {
        String dir = App.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
        String path = dir + File.separator + "ioopos-std.apk";

        File file = new File(path);
        if (!file.exists() && !file.createNewFile()) {
            return;
        }
        if (!file.setWritable(true)) {
            return;
        }
        byte[] bytes = new byte[2048];
        int length;
        Response res = HttpUtils.download(url);
        if (!res.isSuccessful()) {
            onError("code->" + res.code());
            return;
        }
        final long totalLength = Long.parseLong(res.header("content-length"));
        if (totalLength < 1024 * 1024) {// 正常不会小于1M
            onError("size->" + totalLength/1024/1024 + "M]");
            return;
        }
        long loadedLength = 0;
        try (InputStream is = Objects.requireNonNull(res.body()).byteStream(); FileOutputStream os = new FileOutputStream(file)) {
            while ((length = is.read(bytes)) != -1) {
                os.write(bytes, 0, length);
                onDownloading(totalLength, loadedLength += length);
            }
        }
        onDownload(path);
    }

    private void onChecked(boolean toUpdate) {
        if (!toUpdate) {
            dispatch(WARN,"当前版本不需要升级");
        }
    }

    private void onDownloading(long total, long loaded) {
        dispatch(WARN,String.format(Locale.CHINESE,"正在下载__%.2f/%.2fM__%.0f%s",
                (double)loaded/(1024*1024),
                (double)total/(1024*1024),
                (double)loaded/total*100,
                "%"));
    }

    @SuppressLint("WrongConstant")
    private void onDownload(String file) {
        dispatch(WAIT,"正在安装");
        Intent mIntent = new Intent("com.sanstar.quiet.install");
        mIntent.putExtra("Package_Path",file);
        mIntent.putExtra("Package_Name", packageName());
        mIntent.putExtra("Package_Class", MainActivity.class.getName());
        mIntent.addFlags(0x01000000);
        App.getInstance().sendBroadcast(mIntent);
    }

    public void onError(String msg) {
        dispatch(FAIL, msg);
    }

    private String packageName() {
        try {
            PackageManager packageManager = App.getInstance().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(App.getInstance().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            Log.e(TAG, "packageName: ", e);
            onError(e.getMessage());
        }
        return null;
    }

}
