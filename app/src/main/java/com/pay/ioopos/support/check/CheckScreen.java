package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.AppFactory.getColor;

import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.display.SpiScreenFactory;
import com.pay.ioopos.display.SpiScreenFactory.SpiScreenCanvas;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 检查副屏显示
 * @author    Moyq5
 * @since  2020/6/16 20:35
 */
public class CheckScreen extends CheckAbstract {
    private final View.OnKeyListener keyListener = new ViewKeyListener(keyInfo -> {
        switch (keyInfo) {
            case KEY_NUM_1:
                SpiScreenFactory.showWelcome(App.getInstance());
                info("检查副屏显示：检查通过");
                stopSpeak("副屏显示正常", true);
                return true;
            case KEY_NUM_2:
                error("检查副屏显示：检查未通过");
                stopSpeak("副屏显示未通过", false);
                return true;
            case KEY_NUM_3:
                warn("检查副屏显示：忽略检查");
                stopSpeak("忽略副屏显示检查", false);
                return true;
        }
        return false;
    });

    public CheckScreen(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查副屏显示>>>>");
        stopSpeak("开始检查副屏显示");
        setOnKeyListener(keyListener);

        info("检查副屏显示：重置副屏...");
        if (App.DEV_IS_306_308) {// 旧款设备有版本要求
            String[] infos = android.os.Build.DISPLAY.split("_");
            boolean matched = false;
            for (String info: infos) {
                if (info.matches("V([0-9]+\\.)+[0-9]+")) {
                    final String[] curVers = info.replace("V", "").split("\\.");
                    final String[] minVers = "1.2.0".split("\\.");// 1.2.0以上才支持重置
                    boolean supported = false;
                    for (int i = 0; i < curVers.length; i++) {
                        if (i < minVers.length) {
                            if (Integer.parseInt(curVers[i]) < Integer.parseInt(minVers[i])) {
                                break;
                            } else if (Integer.parseInt(curVers[i]) > Integer.parseInt(minVers[i])) {
                                supported = true;
                                break;
                            }
                        } else {
                            supported = true;
                            break;
                        }
                    }
                    if (supported) {
                        SpiScreenFactory.clean();
                        info("检查副屏显示：已重置");
                    } else {
                        warn("检查副屏显示：重置失败，当前固件版本[" + info + "]不支持副屏重置，请升级到1.2.1以上");
                    }
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                warn("检查副屏显示：重置失败，当前固件版本不支持副屏重置，请升级到1.2.1以上");
            }
        } else {
            SpiScreenFactory.clean();
            info("检查副屏显示：已重置");
        }


        TaskFactory.execute(this::showBitmap);
        addSpeak("请核对副屏显示是否与图片一致，是请按1，否则请按2，忽略请按3");
        info("检查副屏显示：请核对副屏显示是否与图片一致，是请按1，否则请按2，忽略请按3");
    }

    @Override
    protected void onTimes(int times) {
        addSpeak("请核对副屏显示是否与图片一致，是请按1，否则请按2，忽略请按3");
    }

    @Override
    protected void onTimeout() {
        error("检查副屏显示：超时，检查未通过");
        stopSpeak("超时，副屏检查未通过", false);
    }

    @Override
    public void setPassed(Boolean passed) {
        super.setPassed(passed);
    }

    private void showBitmap() {
        SpiScreenCanvas canvas = canvasDraw();

        SpiScreenFactory.submit(SpiScreenFactory::flush);

        bitmap(canvas.getBitmap());

    }

    private SpiScreenCanvas canvasDraw() {
        SpiScreenCanvas canvas = SpiScreenFactory.getCanvas();

        canvas.drawColor(Color.BLACK);

        float fontSize = 30f;

        TextPaint paint = new TextPaint();
        paint.setColor(getColor(R.color.colorSuccess));
        paint.setTextSize(fontSize);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);

        String text = "左上";
        int startX = 0;
        int startY = -(int)paint.ascent();
        canvas.drawText(text, startX, startY, paint);

        text = "右上";
        int fontWidth = (int) paint.measureText(text);
        startX = canvas.getWidth() - fontWidth;
        //startY = -(int)paint.ascent();
        canvas.drawText(text, startX, startY, paint);

        text = "中间";
        fontWidth = (int) paint.measureText(text);
        startX = (canvas.getWidth() - fontWidth) / 2;
        startY = canvas.getHeight()/ 2 + (int)(paint.descent() - paint.ascent())/2 - (int)paint.descent();
        canvas.drawText(text, startX, startY, paint);

        text = "左下";
        startX = 0;
        startY = canvas.getHeight() - (int)paint.descent();
        canvas.drawText(text, startX, startY, paint);

        text = "右下";
        startX = canvas.getWidth() - fontWidth;
        //startY = bitmap.getHeight() - (int)paint.descent();
        canvas.drawText(text, startX, startY, paint);

        return canvas;
    }
}
