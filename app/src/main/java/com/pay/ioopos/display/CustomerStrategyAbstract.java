                                                                                                  package com.pay.ioopos.display;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;

import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.widget.Tip;

import java.util.List;

                                                                                                  /**
 * 客户屏控制抽象类
 * @author    Moyq5
 * @since  2020/7/1 15:39
 */
public class CustomerStrategyAbstract implements CustomerStrategy {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isShowing = false;
    private CustomerProvider provider;
    public CustomerStrategyAbstract(CustomerProvider provider) {
        this.provider = provider;
    }

    public CustomerProvider getProvider() {
        return provider;
    }

    public void setProvider(CustomerProvider provider) {
        this.provider = provider;
    }

    @Override
    public void show() {
        isShowing = true;
        handler.post(() -> {
            provider.getView().show();
        });
    }

    @Override
    public void showWelcome() {
        handler.post(() -> {
            provider.getView().showWelcome();
        });
    }

    @Override
    public void onAmount(String amountText) {
        provider.getView().onAmount(amountText);
    }

    @Override
    public void showPayWait() {
        handler.post(() -> {
            provider.getView().showPayWait();
        });
    }

    @Override
    public void setScanFace(ScanFace scanFace) {
        provider.getView().setScanFace(scanFace);
    }

    @Override
    public void showScanFace() {
        handler.post(() -> {
            provider.getView().showScanFace();
        });
    }

    @Override
    public void showPay(Tip.TipType type, String msg, String detail) {
        provider.getView().showPay(type, msg, detail);
    }

    @Override
    public SurfaceView getFaceSurface() {
        return provider.getView().getFaceSurface();
    }

    @Override
    public SurfaceView getFaceIrSurface() {
        return provider.getView().getFaceIrSurface();
    }

    @Override
    public void onFaceTip(String tip, boolean preview) {
        provider.getView().onFaceTip(tip, preview);
    }

    @Override
    public void onFaceBitmap(Bitmap bitmap, float left, float right, float top, float bottom) {
        provider.getView().onFaceBitmap(bitmap, left, right, top, bottom);
    }

    @Override
    public void onFaceMobile() {
        provider.getView().onFaceMobile();
    }

    @Override
    public void onUser(Object info) {
        provider.getView().onUser(info);
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void hide() {
        isShowing = false;
    }

    @Override
    public void showCard(CardInfo info) {
        handler.post(() -> {
            provider.getView().showCard(info);
        });
    }

    @Override
    public void showCard(CardInfo info, CardOrder order) {
        handler.post(() -> {
            provider.getView().showCard(info, order);
        });
    }

    @Override
    public void showCard(List<CardOrder> orders) {
        handler.post(() -> {
            provider.getView().showCard(orders);
        });
    }

    @Override
    public void showMsg(Tip.TipType type, String msg, String detail) {
        handler.post(() -> {
            provider.getView().showMsg(type, msg, detail);
        });
    }

}
