package com.pay.ioopos.widget;

import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.getColor;
import static com.pay.ioopos.common.AppFactory.getString;
import static com.pay.ioopos.common.AppFactory.iconTypeface;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.NONE;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.Future;


/**
 * 带警示图标的提示信息
 * @author    Moyq5
 * @since  2020/3/11 11:01
 */
public abstract class TipViewAbstract extends LinearLayout implements Tip {
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_TEXT = "text";
    private static final String PARAM_MSG = "msg";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_DETAIL = "detail";
    private final Bundle bundle = new Bundle();
    private Future<?> waitFuture;
    private Handler messageHandler;
    private int waitTimes = 0;

    public TipViewAbstract(Context context) {
        super(context);
        addView(context);
    }

    public TipViewAbstract(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(context);
    }

    public TipViewAbstract(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addView(context);
    }

    private void addView(Context context) {
        addView(View.inflate(context, layoutId(), null));
        messageHandler = new MessageHandler(this, bundle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TextView iconView = findViewById(R.id.tip_icon);
        iconView.setTypeface(iconTypeface());
        messageHandler.sendEmptyMessage(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopWait();
    }

    protected abstract int layoutId();

    @Override
    public void dispatch(TipType type, String msg) {
        synchronized (bundle) {
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, null);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(TipType type, String msg, String detail) {
        synchronized (bundle) {
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, detail);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(TipType type, int msgId) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            String msg = getString(msgId);
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, null);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(TipType type, int msgId, String detail) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            String msg = getString(msgId);
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, detail);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(int msgId) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            String msg = getString(msgId);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, null);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(int msgId, String detail) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            String msg = getString(msgId);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, detail);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(TipType type) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_DETAIL, null);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(String msg) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, null);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(String msg, String detail) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, detail);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public void dispatch(float textSize) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            bundle.putFloat(PARAM_SIZE, textSize);
        }

        messageHandler.sendEmptyMessage(0);
    }

    public void dispatch(TipType type, String msg, String detail, float textSize) {
        if (null == getContext()) {
            return;
        }
        synchronized (bundle) {
            bundle.putSerializable(PARAM_TYPE, type);
            bundle.putString(PARAM_MSG, msg);
            bundle.putString(PARAM_TEXT, msg);
            bundle.putString(PARAM_DETAIL, detail);
            bundle.putFloat(PARAM_SIZE, textSize);
        }

        messageHandler.sendEmptyMessage(0);
    }

    @Override
    public TipType getType() {
        return (TipType) bundle.getSerializable(PARAM_TYPE);
    }

    private void startWait() {
        waitTimes = 0;
        stopWait();
        waitFuture = TaskFactory.submit(() -> {
            try {
                while(!Thread.interrupted()) {
                    synchronized (bundle) {
                        TipType type = (TipType)bundle.getSerializable(PARAM_TYPE);
                        if (type == WAIT) {
                            bundle.putString(PARAM_MSG, bundle.getString(PARAM_TEXT) + ("...".substring(0, waitTimes%3 + 1)));
                        } else {
                            waitTimes = 0;
                        }
                    }
                    messageHandler.sendEmptyMessage(0);
                    waitTimes++;
                    Thread.sleep(700);
                }
            } catch (InterruptedException ignored) {
            }
        });
    }

    private void stopWait() {
        if (null != waitFuture) {
            waitFuture.cancel(true);
        }
    }

    private static class MessageHandler extends Handler {

        private final WeakReference<TipViewAbstract> viewWeakRef;
        private final WeakReference<Bundle> dataWeakRef;
        private TipType preType;
        private String preText;
        MessageHandler(TipViewAbstract view, Bundle bundle) {
            viewWeakRef = new WeakReference<>(view);
            dataWeakRef = new WeakReference<>(bundle);
        }

        @Override
        public void handleMessage(@NonNull Message arg) {
            TipViewAbstract view = viewWeakRef.get();
            if (null == view || !view.isAttachedToWindow()) {
                return;
            }
            Bundle bundle = dataWeakRef.get();

            Context context = view.getContext();

            TextView iconView = view.findViewById(R.id.tip_icon);
            TextView msgView = view.findViewById(R.id.tip_msg);
            TextView detailView = view.findViewById(R.id.tip_detail);

            TipType type = (TipType)bundle.getSerializable(PARAM_TYPE);
            String msg = bundle.getString(PARAM_MSG);
            String detail = bundle.getString(PARAM_DETAIL);
            float size = bundle.getFloat(PARAM_SIZE);

            if (size > 0) {
                iconView.setTextSize(size);
                msgView.setTextSize(size);
            }

            msgView.setText(msg);

            if (null == detail) {
                detailView.setVisibility(GONE);
            } else {
                detailView.setText(detail);
                detailView.setVisibility(VISIBLE);

            }

            String text = bundle.getString(PARAM_TEXT);
            if (view.isAttachedToWindow() && (type != WAIT || preType != WAIT || null == text || !text.equals(preText))) {
                if (null != text && !text.isEmpty()) {
                    displayLog(text + (WAIT == type ? "...":""));
                    preText = text;
                }
                if (null != detail && !detail.isEmpty()) {
                    displayLog(detail);
                }
            }


            if (type == NONE) {
                iconView.setVisibility(INVISIBLE);
                return;
            }
            if (type != WAIT) {
                view.stopWait();
            }
            iconView.setVisibility(VISIBLE);
            if (type == WAIT || type == TipType.WARN) {
                iconView.setTextColor(getColor(R.color.colorWarning));
                iconView.setText(R.string.glyphicon_exclamation_sign);
                if (type == WAIT && preType != WAIT) {
                    view.startWait();
                }
            } else if (type == SUCCESS) {
                iconView.setTextColor(getColor(R.color.colorSuccess));
                iconView.setText(R.string.glyphicon_ok_sign);
            } else if (type == FAIL) {
                iconView.setTextColor(getColor(R.color.colorDanger));
                iconView.setText(R.string.glyphicon_remove_sign);
            }

            preType = type;
        }

    }
}
