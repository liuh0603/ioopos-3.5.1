package com.pay.ioopos.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * 二维码View
 * @author    Moyq5
 * @since  2020/6/22 19:16
 */
public class QrcodeView extends View {
    private static Hashtable<EncodeHintType, Object> hintMap;
    private static QRCodeWriter qrcodewriter = new QRCodeWriter();
    private static TextPaint paint;
    private BitMatrix byteMatrix;
    private int width = 50;
    private int height = 50;
    static {
        hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  // 矫错级别
        hintMap.put(EncodeHintType.MARGIN, 0);

        paint = new TextPaint();
        paint.setColor(Color.BLACK);

    }

    public QrcodeView(Context context) {
        super(context);
    }

    public QrcodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QrcodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == byteMatrix) {
            return;
        }
        canvas.scale(canvas.getWidth()/width, canvas.getHeight()/height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (byteMatrix.get(x, y)) {
                    canvas.drawRect(x, y, x + 1, y+1, paint);
                }
            }
        }
    }

    public void postInvalidate(String content) {
        try {
            byteMatrix = qrcodewriter.encode(content, BarcodeFormat.QR_CODE, width, height, hintMap);
            // 重新计算宽高，使二维码实现满填充
            int[] leftTop = byteMatrix.getTopLeftOnBit();
            int[] rightBottom = byteMatrix.getBottomRightOnBit();
            if (width != rightBottom[0] - leftTop[0] || height != rightBottom[1] - leftTop[1]) {
                width = rightBottom[0] - leftTop[0] + 1;
                height = rightBottom[1] - leftTop[1] + 1;
                if (width > height) {
                    height = width;
                } else if (width < height) {
                    width = height;
                }
                byteMatrix = qrcodewriter.encode(content, BarcodeFormat.QR_CODE, width, height, hintMap);
            }
        } catch (WriterException e) {
            return;
        }
        postInvalidate();
    }
}
