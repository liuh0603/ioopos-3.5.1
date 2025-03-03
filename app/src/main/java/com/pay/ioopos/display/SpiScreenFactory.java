package com.pay.ioopos.display;

import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.common.AppFactory.getColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.Log;

import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.common.LogUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class SpiScreenFactory {
    private static final String TAG = SpiScreenFactory.class.getSimpleName();
    private static final char[] CHARS = "0123456789ABCDEF".toCharArray();
    private static final String PATH = "/dev/sub_lcm";
    private static final Lock lock = new ReentrantLock();
    private static Future<?> future;
    private static Runnable preRunnable;
    private static boolean isFlushing = false;

    private static ByteBuffer flushBuffer;
    private static char[] flushChars;

    private static FileWriter fw;
    private static BufferedWriter bw;

    private static char[] welcomeFlushChars;

    public static void init() {
        if (!DEV_IS_SPI) {
            return;
        }
        try {
            fw = new FileWriter(PATH);
            bw = new BufferedWriter(fw);
            clean();
        } catch (IOException e) {
            Log.e(TAG, "init: ", e);
            LogUtils.log(e.getMessage(), Thread.currentThread(), e);
        }

    }

    public static void release() {
        if (!DEV_IS_SPI) {
            return;
        }
        if (null != fw) {
            try {
                fw.close();
            } catch (IOException ignored) {

            } finally {
                fw = null;
            }
        }

        if (null != bw) {
            try {
                bw.close();
            } catch (IOException  ignored) {

            } finally {
                bw = null;
            }
        }
    }


    public static SpiScreenCanvas getCanvas() {
        return SpiScreenCanvas.reset();
    }

    public static SpiScreenCanvas getCanvas(Bitmap reset) {
        return SpiScreenCanvas.reset(reset);
    }

    public static Bitmap createBitmap() {
        return SpiScreenCanvas.createBitmap();
    }

    public static void showWelcome(Context context) {
        if (null == context) {
            return;
        }

        if (null != welcomeFlushChars) {
            flush(welcomeFlushChars);
            return;
        }

        SpiScreenCanvas canvas = SpiScreenCanvas.reset();

        TextPaint paint = new TextPaint();
        paint.setColor(getColor(R.color.colorSuccess));
        paint.setTextSize(50f);
        paint.setAntiAlias(true);

        String text = "欢迎使用";
        int fontWidth = (int) paint.measureText(text);
        int startX = (canvas.getWidth() - fontWidth) / 2;
        int startY = canvas.getHeight()/ 2 + (int)(paint.descent() - paint.ascent())/2 - (int)paint.descent();
        canvas.drawText(text, startX, startY, paint);

        char[] chars = flush(canvas.getBitmap());
        if (null != chars) {
            welcomeFlushChars = chars.clone();
        }

    }

    public static void submit(Runnable runnable) {
        submit(runnable, false);
    }

    public static void submit(Runnable runnable, boolean merge) {
        lockInterruptibly(() -> {submit_(runnable, merge); return null;});
    }

    public static void clean() {
        flush((char[]) null);
    }

    public static void flush() {
        flush(SpiScreenCanvas.getInstance().getBitmap());
    }

    public static boolean isFlushing() {
        return isFlushing;
    }

    private static char[] flush(Bitmap bitmap) {
        return lockInterruptibly(() -> flush_(bitmap));
    }

    private static void flush(char[] flushChars) {
        lockInterruptibly(() -> { flush_(flushChars); return null;});
    }

    private static void submit_(Runnable runnable, boolean merge) {
        if (null == future) {
            future = TaskFactory.submit(runnable);
            return;
        }
        future.cancel(!isFlushing);
        if (merge) {
            Runnable dstPreRunnable = preRunnable::run;
            Runnable newRunnable = () -> {
                dstPreRunnable.run();
                runnable.run();
            };
            future = TaskFactory.submit(newRunnable);
        } else {
            future = TaskFactory.submit(runnable);
        }
        preRunnable = runnable::run;
    }

    private static char[] flush_(Bitmap bitmap) {
        int count = bitmap.getByteCount();
        if (null == flushChars || flushChars.length != count * 2) {
            flushChars = new char[count * 2];
        }
        if (null == flushBuffer || count != flushBuffer.capacity()) {
            flushBuffer = ByteBuffer.allocate(count);
        }
        flushBuffer.clear();

        bitmap.copyPixelsToBuffer(flushBuffer);

        byte[] byteArray = flushBuffer.array();

        for (int i = 0; i < count/2; i++) {
            flushChars[i * 4] = CHARS[(byteArray[2*i+1] & 0xf0) >> 4];
            flushChars[i * 4 + 1] = CHARS[byteArray[2*i+1] & 0x0f];
            flushChars[i * 4 + 2] = CHARS[(byteArray[2*i] & 0xf0) >> 4];
            flushChars[i * 4 + 3] = CHARS[byteArray[2*i] & 0x0f];
        }

        flush(flushChars);

        return flushChars;
    }

    private static void flush_(char[] flushChars) {
        if (null == bw) {
            return;
        }
        isFlushing = true;
        try {
            if (null == flushChars) {
                bw.write("1");// 清屏
                bw.flush();
            } else {
                for (int m = 0; m < 80; m++) {
                    bw.write(flushChars, m * 7680, 7680);
                    bw.flush();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "subsreen flush: ", e);
        } finally {
            isFlushing = false;
        }
    }

    private static <T> T lockInterruptibly(Supplier<T> supplier) {
        try {
            lock.lockInterruptibly();
            return supplier.get();
        } catch (InterruptedException ignored) {

        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {

            }
        }
        return null;
    }

    public static class SpiScreenCanvas extends Canvas {
        private static final int WIDTH = 480;
        private static final int HEIGHT = 320;
        private static final Paint paint = new Paint();

        private static final SpiScreenCanvas canvas = new SpiScreenCanvas(createBitmap());

        private final Bitmap bitmap;

        private SpiScreenCanvas(Bitmap bitmap) {
            super(bitmap);
            this.bitmap = bitmap;
        }

        public static SpiScreenCanvas getInstance() {
            return canvas;
        }

        public static SpiScreenCanvas reset() {
            canvas.drawBitmap(SubScreenLoader.getInstance().getDefBitmap(), 0, 0, paint);
            return canvas;
        }

        public static SpiScreenCanvas reset(Bitmap reset) {
            canvas.drawBitmap(reset, 0, 0, paint);
            return canvas;
        }

        public static Bitmap createBitmap() {
            Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
            bitmap.eraseColor(Color.WHITE);
            return bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

    }
}
