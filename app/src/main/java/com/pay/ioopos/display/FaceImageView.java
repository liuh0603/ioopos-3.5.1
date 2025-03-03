package com.pay.ioopos.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class FaceImageView extends androidx.appcompat.widget.AppCompatImageView {

    public FaceImageView(Context context) {
        super(context);
    }

    public FaceImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
