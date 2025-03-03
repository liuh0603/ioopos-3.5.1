package com.pay.ioopos.support.scan.sp308;

import static com.pay.ioopos.App.DEV_IS_306_308;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.pay.ioopos.App;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.support.scan.ScanListener;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 相机扫码器-二维码、条码扫码场景
 * @author    Moyq5
 * @since  2020/2/18 10:42
 */
public class CameraScan extends ScanCaseAbstract {
    private static final String TAG = CameraScan.class.getSimpleName();
    private static final Lock lock = new ReentrantLock();
    private static final ExecutorService analysisService = Executors.newFixedThreadPool(5);
    private static byte[] yuvData;
    private static ListenableFuture<ProcessCameraProvider> listenableFuture;
    private static ProcessCameraProvider cameraProvider;
    private final List<Future<?>> futures = new Vector<>();
    private ScanListener listener;
    private final BarcodeFormat format;
    private LifecycleOwner owner;

    private final Runnable cameraBinder = () -> {
        boolean locked;
        try {
            locked = lock.tryLock(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return;
        }
        if (!locked) {
            return;
        }
        try {
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
            imageAnalysis.setAnalyzer(TaskFactory.pool(), this::imageAnalysis);
            getCameraProvider().unbindAll();
            getCameraProvider().bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis);
        } catch (Throwable e) {
            Log.e(TAG, "扫码器打开失败: ", e);
            if (null != listener) {
                listener.onError("扫码器打开失败");
            }
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {

            }
        }
    };

    public CameraScan() {
        this(BarcodeFormat.QR_CODE);
    }

    public CameraScan(BarcodeFormat format) {
        this.format = format;
    }

    @Override
    public void setScanListener(ScanListener listener) {
        super.setScanListener(listener);
        this.listener = listener;
    }

    @Override
    public void bindToLifecycle(LifecycleOwner lifecycleOwner) {
        if (!DEV_IS_306_308) {
            return;
        }
        owner = lifecycleOwner;
        TaskFactory.execute(() -> {
            try {
                lock.lockInterruptibly();
                super.bindToLifecycle(lifecycleOwner);
                getListenablefuture().addListener(() -> uiExecute(cameraBinder), TaskFactory.pool());
            } catch (Throwable e) {
                Log.e(TAG, "扫码器打开失败: ", e);
                if (null != listener) {
                    listener.onError("扫码器打开失败");
                }
            } finally {
                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException ignored) {

                }
            }
        });
    }

    @Override
    protected void onStart(LifecycleOwner owner) {

    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        futures.forEach(future -> future.cancel(true));
    }

    private ProcessCameraProvider getCameraProvider() throws InterruptedException, ExecutionException, TimeoutException {
        if (null == cameraProvider) {
            cameraProvider = getListenablefuture().get(500, TimeUnit.MILLISECONDS);
        }
        return cameraProvider;
    }

    private ListenableFuture<ProcessCameraProvider> getListenablefuture() {
        if (null == listenableFuture) {
            listenableFuture = ProcessCameraProvider.getInstance(App.getInstance());
        }
        return listenableFuture;
    }

    private void imageAnalysis(final ImageProxy image) {
        int width;
        int height;
        byte[] bytes;
        try {
            if (isScanned()|| owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                return;
            }
            width = image.getWidth();
            height = image.getHeight();
            bytes = getBytes(image);

        } finally {
            try {
                image.close();
            } catch (Throwable ignored) {

            }
        }
        if (null == bytes) {
            return;
        }
        try {
            futures.add(analysisService.submit(() -> analysis(bytes, width, height)));
        } catch (RejectedExecutionException e) {
            Log.e(TAG, "imageAnalysis: ", e);
        }
    }

    private void analysis(byte[] nv21, int width, int height) {
        if (isScanned()) {
            return;
        }
        LuminanceSource source = //rgbLuminanceSource(nv21, width, height);
                new PlanarYUVLuminanceSource(nv21, width, height, 0, 0, width, height, false);
        String result = decodeByZXing(source);
        if (null == result || result.isEmpty()) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_CODE, result);
        onScan(intent);
    }

    private String decodeByZXing(LuminanceSource source) {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            final Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            //hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result;
            if (null != format && format == BarcodeFormat.QR_CODE) {
                hints.put(DecodeHintType.POSSIBLE_FORMATS, format);
                result = new QRCodeReader().decode(binaryBitmap, hints);
            } else {
                //hints.put(DecodeHintType.POSSIBLE_FORMATS, format);
                //hints.put(DecodeHintType.ALLOWED_EAN_EXTENSIONS, false);
                //hints.put(DecodeHintType.ASSUME_GS1, false);
                //hints.put(DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT, false);
                //hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
                //hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                result = new MultiFormatReader().decode(binaryBitmap, hints);
            }
            return result.toString();
        } catch (Exception ignored) {

        }
        return null;
    }

    private synchronized static byte[] getBytes(ImageProxy imageProxy) {
        @SuppressLint("UnsafeExperimentalUsageError")
        android.media.Image image = imageProxy.getImage();
        if (null == image) {
            return null;
        }
        android.media.Image.Plane[] planes = image.getPlanes();
        // planes[0]可能为null
        if (null == planes[0] || null == planes[1] || null == planes[2]) {
            return null;
        }
        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer uBuffer = planes[1].getBuffer(); // U
        ByteBuffer vBuffer = planes[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        if (null == yuvData) {// synchronized 并使用单一对象防止创建对实例造成内存溢出
            yuvData = new byte[ySize + uSize + vSize];
        }

        try {
            //U and V are swapped
            yBuffer.get(yuvData, 0, ySize);
            vBuffer.get(yuvData, ySize, vSize);
            uBuffer.get(yuvData, ySize + vSize, uSize);
        } catch (IllegalStateException e) {// IllegalStateException: buffer is inaccessible
            Log.e(TAG, "getBytes: ", e);
            return null;
        }

        return yuvData;
    }


    private static LuminanceSource rgbLuminanceSource(byte[] nv21, int width, int height) {
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, width), 50, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap =  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return new RGBLuminanceSource(width, height, pixels);
    }

}
