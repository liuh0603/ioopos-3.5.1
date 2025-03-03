package com.pay.ioopos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.widget.Tip;
import com.pay.ioopos.widget.TipViewAbstract;

/**
 * 带图标提示信息抽象类
 * @author    Moyq5
 * @since  2020/3/30 14:25
 */
public abstract class AbstractTipFragment extends AbstractNetworkFragment implements Tip, Scheduled {

    private TipViewAbstract view;
    private TipType type;
    private String msg;
    private Integer msgId;
    private String detail;
    private Float textSize;
    public AbstractTipFragment(TipType type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public AbstractTipFragment(TipType type, int msgId) {
        this.type = type;
        this.msgId = msgId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = tipView();
        float size = null == textSize ? 0: textSize;
        if (null != msgId) {
            view.dispatch(type, getString(msgId), detail, size);
        } else {
            view.dispatch(type, msg, detail, size);
        }
        return view;

    }

    protected abstract TipViewAbstract tipView();

    @Override
    public void dispatch(TipType type, int msgId) {
        if (null == view) {
            this.type = type;
            this.msgId = msgId;
            this.detail = null;
            return;
        }
        view.dispatch(type, msgId);
    }

    @Override
    public void dispatch(TipType type, int msgId, String detail) {
        if (null == view) {
            this.type = type;
            this.msgId = msgId;
            this.detail = detail;
            return;
        }
        view.dispatch(type, msgId, detail);
    }

    @Override
    public void dispatch(TipType type, String msg) {
        if (null == view) {
            this.type = type;
            this.msg = msg;
            this.msgId = null;
            this.detail = null;
            return;
        }
        view.dispatch(type, msg);
    }

    @Override
    public void dispatch(TipType type, String msg, String detail) {
        if (null == view) {
            this.type = type;
            this.msg = msg;
            this.msgId = null;
            this.detail = detail;
            return;
        }
        view.dispatch(type, msg, detail);
    }

    @Override
    public void dispatch(int msgId) {
        if (null == view) {
            this.msgId = msgId;
            this.detail = null;
            return;
        }
        view.dispatch(msgId);
    }

    @Override
    public void dispatch(int msgId, String detail) {
        if (null == view) {
            this.msgId = msgId;
            this.detail = detail;
            return;
        }
        view.dispatch(msgId, detail);
    }

    @Override
    public void dispatch(String msg) {
        if (null == view) {
            this.msg = msg;
            this.msgId = null;
            this.detail = null;
            return;
        }
        view.dispatch(msg);
    }

    @Override
    public void dispatch(String msg, String detail) {
        if (null == view) {
            this.msg = msg;
            this.msgId = null;
            this.detail = detail;
            return;
        }
        view.dispatch(msg, detail);
    }

    @Override
    public void dispatch(TipType type) {
        if (null == view) {
            this.type = type;
            this.detail = null;
            return;
        }
        view.dispatch(type);
    }

    @Override
    public void dispatch(float textSize) {
        if (null == view) {
            this.textSize = textSize;
            return;
        }
        view.dispatch(textSize);
    }

    @Override
    public TipType getType() {
        if (null == view) {
            return null;
        }
        return view.getType();
    }

    @Override
    public boolean useNetwork() {
        return false;
    }

}
