package com.pay.ioopos.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * 刷脸预览视图
 * @author    Moyq5
 * @since  2020/3/4 13:57
 */
public class FaceSurfaceView extends SurfaceView {
    private static Paint paint = new Paint();
    static {
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(6);
        paint.setColor(Color.WHITE);
    }

    public FaceSurfaceView(Context context) {
        super(context);
    }

    public FaceSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setKeepScreenOn(true);
    }

    @Override
    public void draw(Canvas canvas) {
        int radius = (getWidth() > getHeight() ? getHeight(): getWidth())/2;
        Path path = new Path();
        path.addCircle(radius, radius, radius, Path.Direction.CCW);
        path.close();
        canvas.clipPath(path);
        super.draw(canvas);
        canvas.drawCircle(radius, radius, radius, paint);// 去掉clipPath造成的锯齿
    }

}
