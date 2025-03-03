package com.pay.ioopos.display;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pay.ioopos.R;

public class SubScreenLoader {
    private static final int WIDTH = 480;
    private static final int HEIGHT = 320;
    private boolean subScreenBackbgroudLoaded = false;

    private Bitmap defBitmap;

    private static SubScreenLoader loader = new SubScreenLoader();

    private SubScreenLoader() {

    }

    public static SubScreenLoader getInstance() {
        return loader;
    }

    public void load(Activity activity) {
        if (subScreenBackbgroudLoaded) {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        defBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.bg_306, options);
        defBitmap = Bitmap.createScaledBitmap(defBitmap.copy(Bitmap.Config.RGB_565, true),WIDTH,HEIGHT,true);
        subScreenBackbgroudLoaded = true;
    }

    public Bitmap getDefBitmap() {
        return defBitmap;
    }

}
