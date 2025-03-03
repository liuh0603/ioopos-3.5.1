package com.pay.ioopos.display;

import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxFaceUserData;
import com.aggregate.pay.sanstar.bean.WxFaceUserInfo;
import com.aggregate.pay.sanstar.bean.WxFaceUserResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.sqlite.WxFaceUserStore;
import com.pay.ioopos.widget.Tip.TipType;
import com.pay.ioopos.worker.WxOfflineIniWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 客户屏布局
 * @author    Moyq5
 * @since  2022/1/x
 */
public class CustomerViewV2 extends RelativeLayout implements CustomerStrategy {
    public static final int SHOW_ACTION_SHOW = 0;
    public static final int SHOW_ACTION_HIDE = 1;
    public static final int SHOW_ACTION_WELCOME = 2;
    public static final int SHOW_ACTION_MSG = 3;
    public static final int SHOW_ACTION_PAY_AMOUNT = 4;
    public static final int SHOW_ACTION_PAY_WAIT = 5;
    public static final int SHOW_ACTION_PAY_CASE = 18;
    public static final int SHOW_ACTION_PAY_TIP = 6;
    public static final int SHOW_ACTION_FACE_SCAN = 7;
    public static final int SHOW_ACTION_FACE_TIP = 8;
    public static final int SHOW_ACTION_FACE_PREVIEW = 9;
    public static final int SHOW_ACTION_FACE_SURFACE_OUT = 10;
    public static final int SHOW_ACTION_FACE_VERIFY_OUT = 11;
    public static final int SHOW_ACTION_FACE_MOBILE = 12;
    public static final int SHOW_ACTION_FACE_IMG = 13;
    public static final int SHOW_ACTION_USER_INFO = 14;
    public static final int SHOW_ACTION_CARD = 15;
    public static final int SHOW_ACTION_CARD_ORDERS = 16;
    public static final int SHOW_ACTION_LOG = 17;

    private CustomerViewHandlerAbstract showHandler;

    private ScanFace scanFace;

    private String payAmount;
    private MsgInfo payTip;

    private String faceTipMsg; // 刷脸状态提示内容
    private Bitmap faceImg;// 用户头像
    private String userInfo;// 用户信息
    private Future<?> nameFuture;

    private CardInfo cardInfo;// 实体卡信息
    private CardOrder cardOrder;// 实体卡最近交易
    private Object[] cardOrders;// 实体卡交易记录
    private ArrayAdapter<Object> cardAdapter;

    private MsgInfo msgInfo;// 其它通用信息

    private SurfaceView faceSurfaceView;// 刷脸预览
    private SurfaceView faceSurfaceIRView;

    private final BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String log = intent.getStringExtra("msg");
            Bundle bundle = new Bundle();
            bundle.putString("msg", log);
            Message msg = new Message();
            msg.what = SHOW_ACTION_LOG;
            msg.setData(bundle);
            showHandler.sendMessage(msg);
        }
    };

    private final BroadcastReceiver payReceiver = new BroadcastReceiver() {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onReceive(Context context, Intent intent) {
            String authCase = intent.getStringExtra(INTENT_PARAM_CASE);
            Bundle bundle = new Bundle();
            bundle.putString("case", authCase);
            Message msg = new Message();
            msg.what = SHOW_ACTION_PAY_CASE;
            msg.setData(bundle);
            showHandler.sendMessage(msg);
        }
    };

    public CustomerViewV2(Context context) {
        super(context);
        create(context);
    }

    public CustomerViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        create(context);
    }

    public CustomerViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context);
    }

    private void create(Context context) {

        View view = View.inflate(context, R.layout.layout_customer_panel_v2, null);
        addView(view);

        localRegister(logReceiver, new IntentFilter(App.ACTION_CUSTOM_DISPLAY_LOG));
        localRegister(payReceiver, new IntentFilter(PayIngAbstract.class.getName()));

        showHandler = new CustomerViewHandlerV2(this);
        faceSurfaceView = findViewById(R.id.surface_view);
        faceSurfaceIRView = findViewById(R.id.surface_ir_view);
        cardAdapter = new CardOrderAdapter(context, R.layout.layout_customer_card_adapter, new ArrayList<>());

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        localUnregister(logReceiver);
        localUnregister(payReceiver);
    }

    @Override
    public void show() {
        this.showHandler.sendEmptyMessage(SHOW_ACTION_SHOW);
    }

    /**
     * 欢迎（可能为初始界面）
     * @author  Moyq5
     * @since    2020/3/8 16:49
     */
    @Override
    public void showWelcome() {
        this.showHandler.sendEmptyMessage(SHOW_ACTION_WELCOME);
    }


    @Override
    public void showCard(CardInfo info) {
        this.cardInfo = info;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_CARD);
    }

    @Override
    public void showCard(CardInfo info, CardOrder order) {
        this.cardInfo = info;
        this.cardOrder = order;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_CARD);
    }

    @Override
    public void showCard(List<CardOrder> orders) {
        if (null == orders) {
            showMsg(TipType.WARN, "没有记录", null);
            return;
        }

        this.cardOrders = orders.stream().filter(CardOrder::isSuccess).sorted((o1, o2) -> o1.getOrderTime() > o2.getOrderTime() ? -1: 0).limit(10).toArray();

        if (cardOrders.length == 0) {
            showMsg(TipType.WARN,"没有交易记录", null);
            return;
        }

        this.showHandler.sendEmptyMessage(SHOW_ACTION_CARD_ORDERS);
    }

    @Override
    public void showMsg(TipType type, String msg, String detail) {
        this.msgInfo = new MsgInfo(type, msg, detail);
        this.showHandler.sendEmptyMessage(SHOW_ACTION_MSG);
    }

    /**
     * 等待支付（可能为初始界面）
     * @author  Moyq5
     * @since    2020/3/8 16:48
     */
    @Override
    public void showPayWait() {
        this.showHandler.sendEmptyMessage(SHOW_ACTION_PAY_WAIT);
    }


    /**
     * 开始刷脸（可能为初始界面）
     * @author  Moyq5
     * @since    2020/3/8 16:46
     */
    @Override
    public void showScanFace() {
        if (null == scanFace) {
            return;
        }
        if (!scanFace.isAvailable()) {
            scanFace.getScanListener().onError("刷脸：" + scanFace.message());
            return;
        }

        // 直接刷脸则不播"开始刷脸"，以免覆盖此前的"请支付xxx元"语音,
        if (!StoreFactory.settingStore().getSwitchFaceAutoScan()) {
            speak("开始刷脸");
        }

        this.faceTipMsg = "打开摄像头...";
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_TIP);
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_SCAN);

    }

    /**
     * 显示支付状态
     * @author  Moyq5
     * @since    2020/3/8 16:45
     */
    @Override
    public void showPay(TipType type, String msg, String detail) {
        this.payTip = new MsgInfo(type, msg, detail);
        this.showHandler.sendEmptyMessage(SHOW_ACTION_PAY_TIP);
    }

    @Override
    public SurfaceView getFaceSurface() {
        return this.faceSurfaceView;
    }

    @Override
    public SurfaceView getFaceIrSurface() {
        return this.faceSurfaceIRView;
    }

    @Override
    public void setScanFace(ScanFace scanFace) {
        this.scanFace = scanFace;
        if (null != scanFace) {
            scanFace.setCustomerPanel(this);
        }
    }

    /**
     * 显示刷脸提示
     * @author  Moyq5
     * @since    2020/3/8 17:06
     */
    @Override
    public void onFaceTip(String msg, boolean preview) {
        if (preview) {
            this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_PREVIEW);
        }
        this.faceTipMsg = msg;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_TIP);

    }

    /**
     * 输入手机号
     * @author  Moyq5
     * @since    2020/3/8 16:47
     */
    @Override
    public void onFaceMobile() {
        this.faceTipMsg = "请输入家长微信绑定的手机号后4位";
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_TIP);
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_MOBILE);
    }

    /**
     * 更新小头像显示
     * @author  Moyq5
     * @since    2020/3/8 17:14
     */
    @Override
    public void onFaceBitmap(Bitmap faceBitmap, float rectLeft, float rectRight, float rectTop, float rectBottom) {
        this.faceTipMsg = "刷脸成功";
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_TIP);

        int w = faceBitmap.getWidth();
        int h = faceBitmap.getHeight();

        int x = Float.valueOf(w * (rectLeft + (rectRight - rectLeft)/2)).intValue();// 脸中心x坐标
        int y = Float.valueOf(h * (rectTop + (rectBottom - rectTop)/2)).intValue();// 脸中心y坐标
        int rx = Float.valueOf(w * (rectRight - rectLeft)/2).intValue();// 脸x轴半径
        int ry = Float.valueOf(h * (rectBottom - rectTop)/2).intValue();// 脸y轴半径
        int r = rx;
        if (ry > r) { // 要大半径
            r = ry;
        }
        r = r + 60;//
        if (x + r > w) {
            r = w - x;
        }
        if (x - r < 0) {
            r = x;
        }
        if (y + r > h) {
            r = h - y;
        }
        if (y - r < 0) {
            r = y;
        }
        x = x - r;
        y = y - r;
        this.faceImg = Bitmap.createBitmap(faceBitmap, x, y, 2 * r, 2 * r);
        faceBitmap.recycle();
        showHandler.sendEmptyMessage(SHOW_ACTION_FACE_IMG);

    }

    /**
     * 更新金额显示
     * @param amount 金额，元
     */
    @Override
    public void onAmount(String amount) {
        this.payAmount = amount;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_PAY_AMOUNT);
    }

    /**
     * 显示人脸姓名
     * @param info 用户信息
     */
    @Override
    public void onUser(Object info) {
        //com.tencent.wxpayface.data.UserInfo;
        String userId = null;
        String userName = null;
        String outUserId = null;
        try {
            userId = (String)info.getClass().getMethod("getUserId").invoke(info);
            userName = (String)info.getClass().getMethod("getUserName").invoke(info);
            outUserId = (String)info.getClass().getMethod("getOutUserId").invoke(info);
        } catch (Exception ignored) {

        }

        if (StoreFactory.settingStore().getSwitchFaceAutoPay()) {
            showPay(WAIT, "正在支付", null);
            scanFace.credential();
            return;
        }

        showHandler.sendEmptyMessage(SHOW_ACTION_FACE_SURFACE_OUT);

        this.userInfo = userName;
        if (null == userName) {
            showQueryName(userId, outUserId);
        }

        scanFace.credential(() -> {
            speak("请确认支付");
            showHandler.sendEmptyMessage(SHOW_ACTION_USER_INFO);
        });

    }

    @Override
    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public void hide() {
        showHandler.sendEmptyMessage(SHOW_ACTION_HIDE);
    }

    public void cancelNameFuture() {
        if (null != nameFuture) {
            nameFuture.cancel(true);
        }
    }
    public ScanFace getScanFace() {
        return scanFace;
    }
    public MsgInfo getMsgInfo() {
        return msgInfo;
    }
    public String getPayAmount() {
        return payAmount;
    }
    public MsgInfo getPayTip() {
        return payTip;
    }
    public String getUserInfo() {
        return userInfo;
    }
    public CardInfo getCardInfo() {
        return cardInfo;
    }
    public CardOrder getCardOrder() {
        return cardOrder;
    }
    public ArrayAdapter<Object> getCardAdapter() {
        return cardAdapter;
    }
    public Object[] getCardOrders() {
        return cardOrders;
    }
    public String getFaceTipMsg() {
        return faceTipMsg;
    }
    public void setFaceTipMsg(String msg) {
        this.faceTipMsg = msg;
    }
    public Bitmap getFaceImg() {
        return faceImg;
    }
    /**
     * 查询显示用户信息
     * @param userId 微信用户id
     * @param outUserId 平台用户id
     */
    private void showQueryName(String userId, String outUserId) {
        WxFaceUserStore store = StoreFactory.wxFaceUserStore();
        ContentValues user = store.selUser(userId);
        if (null != user) {
            userInfo = user.get("wx_user_info") + "：" + user.get("wx_user_name");
            displayLog("部门 %s", user.get("wx_user_info"));
            displayLog("姓名 %s", user.get("wx_user_name"));
            if (showHandler.getShowAction() == SHOW_ACTION_USER_INFO) {
                showHandler.sendEmptyMessage(SHOW_ACTION_USER_INFO);
            }
            return;
        }
        nameFuture = TaskFactory.submit(() -> {
            String wxOrgId = WxOfflineIniWorker.getWxMerch().get("organization_id").toString();
            WxFaceUserData data = new WxFaceUserData();
            data.setWxOrgId(wxOrgId);
            data.setUserNo(outUserId);
            Client<WxFaceUserData, WxFaceUserResult> client = SanstarApiFactory.wxFaceUser(ApiUtils.initApi());
            Result<WxFaceUserResult> result = client.execute(data);
            if (result.getStatus() != Result.Status.OK) {
                return;
            }
            WxFaceUserResult userResult = result.getData();
            List<WxFaceUserInfo> list = userResult.getList();
            if (null == list || list.size() == 0) {
                return;
            }

            userInfo = list.get(0).getWxUserInfo() + "：" + list.get(0).getWxUserName();
            displayLog("部门 %s", list.get(0).getWxUserInfo());
            displayLog("姓名 %s", list.get(0).getWxUserName());
            if (showHandler.getShowAction() == SHOW_ACTION_USER_INFO) {
                showHandler.sendEmptyMessage(SHOW_ACTION_USER_INFO);
            }

            // 获取到的用户信息保存在本地以便下次使用
            ContentValues newUser = new ContentValues();
            newUser.put("wx_org_id", wxOrgId);
            newUser.put("wx_user_id", userId);
            newUser.put("wx_out_id", outUserId);
            newUser.put("wx_user_name", list.get(0).getWxUserName());
            newUser.put("wx_user_info", list.get(0).getWxUserInfo());
            store.modUser(newUser);
        });
    }

    public static class MsgInfo {
        private TipType type;
        private String msg;
        private String detail;
        MsgInfo(TipType type, String msg, String detail) {
            this.type = type;
            this.msg = msg;
            this.detail = detail;
        }

        public TipType getType() {
            return type;
        }

        public void setType(TipType type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

}
