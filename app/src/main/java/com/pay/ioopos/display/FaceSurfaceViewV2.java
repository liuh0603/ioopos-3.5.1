package com.pay.ioopos.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import com.sanstar.baidufacelib.camera.CameraPreview;

/**
 * 刷脸预览视图
 * @author    Moyq5
 * @since  2022/1/17 18:17
 */
public class FaceSurfaceViewV2 extends CameraPreview {
    public FaceSurfaceViewV2(Context context) {
        super(context);
    }

    public FaceSurfaceViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceSurfaceViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
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
    }

}
