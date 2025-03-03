package com.pay.ioopos.display;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static com.pay.ioopos.App.DEV_IS_801;
import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_METHOD;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxFaceUserData;
import com.aggregate.pay.sanstar.bean.WxFaceUserInfo;
import com.aggregate.pay.sanstar.bean.WxFaceUserResult;
import com.aggregate.pay.sanstar.enums.PayMethod;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.card.CardInfo;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.channel.card.CardUser;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.sqlite.WxFaceUserStore;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.StringUtils;
import com.pay.ioopos.widget.KeyboardView;
import com.pay.ioopos.widget.Tip.TipType;
import com.pay.ioopos.widget.TipViewHorizontal;
import com.pay.ioopos.widget.TipViewVertical;
import com.pay.ioopos.worker.WxOfflineIniWorker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 客户屏布局
 * @author    Moyq5
 * @since  2020/4/22 17:10
 */
public class CustomerView extends RelativeLayout implements CustomerStrategy, KeyboardView.OnKeyBoardListener  {
    private Context context;
    private ScanFace scanFace;
    private Future<?> nameFuture;

    private RelativeLayout welcomePanel;// 欢迎界面

    private RelativeLayout amountPanel;// 支付金额
    private RelativeLayout facePanel;// 刷脸
    private RelativeLayout processPanel;// 支付状态
    private RelativeLayout mobilePanel;// 刷脸输入手机号界面

    private RelativeLayout cardPanel;// 实体卡信息界面
    private RelativeLayout msgPanel;// 通用信息界面

    private Animation showIn;// 打开客户屏
    private Animation showOut;// 关闭客房屏

    private Animation amountOut;// 支付金额离开
    private Animation amountIn;// 支付金额进入
    private Animation faceIn;// 刷脸预览进入刷脸
    private Animation faceOut;// 刷脸预览离开刷脸
    private Animation mobileIn;// 手机号键盘进入
    private Animation mobileOut;// 手机号键盘离开
    private final Animation.AnimationListener mobileInListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mobilePanel.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private final Animation.AnimationListener mobileOutListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mobilePanel.setVisibility(GONE);
            mobilePanel.clearAnimation();// 才能恢复点击穿透
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private int faceTag = 0;
    private static final int FACE_TAG_FACING = 1<<0;// 是否在刷脸状态
    private static final int FACE_TAG_PREVIEW = 1<<1;// 预览是否正常，可能相机打开失败
    private static final int FACE_TAG_PAYIN = 1<<2;// 预览是否正常，可能相机打开失败
    private static final int FACE_TAG_MOBILE = 1<<3;// 是否输入手机号状态

    private FaceMobileView mobileView;// 手机号输入框

    private TextView amountText;// 支付金额
    private String amount;

    private Button faceBtn;// 进入刷脸按钮
    private Button payBtn;// 确认支付按钮
    private TipViewHorizontal payTip;// 支付状态提示
    private TipType payTipType;
    private String payTipMsg;
    private String payTipDetail;

    private TextView faceTip;// 刷脸状态提示
    private String faceTipMsg; // 刷脸状态提示内容

    private TextView userInfo;// 用户信息
    private SurfaceView faceSurface;// 刷脸预览
    private RelativeLayout faceView;// 预览视图容器

    private String userName;// 刷脸用户姓名

    private CardInfo cardInfo;// 实体卡信息
    private CardOrder cardOrder;// 实体卡最近交易
    private Object[] cardOrders;// 实体卡交易记录
    private ArrayAdapter<Object> cardAdapter;

    private MsgInfo msgInfo;// 其它通用信息

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PayMethod payMethod = (PayMethod)intent.getSerializableExtra(INTENT_PARAM_PAY_METHOD);
            // 刷脸过程口进行了扫码或者刷卡等其它支付时，头像要显示默认头像
            if (payMethod != PayMethod.FACE) {
                faceSurface.setAlpha(0);
                faceTip.setText("");
            }
        }
    };

    public CustomerView(Context context) {
        super(context);
        create(context);
    }

    public CustomerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create(context);
    }

    public CustomerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context);
    }

    private void create(Context context) {
        this.context = context;
        View view = View.inflate(context, R.layout.layout_customer_panel, null);
        addView(view);

        localRegister(context, receiver, new IntentFilter(PayIngAbstract.class.getName()));

        welcomePanel = findViewById(R.id.welcome_panel);
        amountPanel = findViewById(R.id.amount_panel);
        facePanel = findViewById(R.id.face_panel);
        processPanel = findViewById(R.id.process_panel);
        mobilePanel = findViewById(R.id.mobile_panel);
        cardPanel = findViewById(R.id.card_panel);
        msgPanel = findViewById(R.id.error_panel);

        if (DEV_IS_801) {
            facePanel.setPadding(0,150, 0, 0);
            processPanel.setPadding(0,0, 0, 200);
            amountPanel.setPadding(0,0, 0, 150);
        }

        amountText = findViewById(R.id.amount);

        faceBtn = findViewById(R.id.btn_scan_face);
        faceBtn.setKeyListener(null);
        faceBtn.setOnKeyListener(null);
        faceBtn.setOnTouchListener(null);
        faceBtn.setFocusable(false);
        faceBtn.setFocusableInTouchMode(false);
        faceBtn.setOnClickListener(view_ -> showScanFace());

        payBtn = findViewById(R.id.btn_confirm_pay);
        payBtn.setKeyListener(null);
        payBtn.setOnKeyListener(null);
        payBtn.setOnTouchListener(null);
        payBtn.setFocusable(false);
        payBtn.setFocusableInTouchMode(false);
        payBtn.setOnClickListener(view_ -> {
            if (null != nameFuture) {
                nameFuture.cancel(true);
            }
            showHandler.sendEmptyMessage(SHOW_ACTION_PAY_TIP_SHOW);
            scanFace.credential();
        });

        if (DEV_IS_801) {
            faceBtn.setWidth(250);
            payBtn.setWidth(250);
        }

        payTip = findViewById(R.id.proccess_tip);
        faceTip = findViewById(R.id.scan_face_tip);
        faceTip.setFocusable(false);
        faceTip.setKeyListener(null);
        faceTip.setOnKeyListener(null);
        userInfo =  findViewById(R.id.user_info);

        mobileView = findViewById(R.id.mobile_items);
        // 手机号数字键盘
        KeyboardView keyboardView = findViewById(R.id.key_board_view);
        keyboardView.setClickListener(this);
        if (DEV_IS_801) {
            keyboardView.setPadding(0,0,0,0);
        }

        faceSurface = findViewById(R.id.surface_view);
        faceSurface.setFocusable(false);
        faceSurface.setOnKeyListener(null);
        //faceSurface.getHolder().addCallback(new FaceSurfaceCallBack());
        //faceSurface.setZOrderOnTop(true);
        faceSurface.setZOrderMediaOverlay(true);
        faceSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);

        faceView = findViewById(R.id.surfaceView_container);
        view.post(() -> {
            int height = view.getHeight();
            int width = view.getWidth();
            int dstSize;
            if (width > height) {
                dstSize = (int)(height * 0.7);
            } else {
                dstSize = (int)(width * 0.7);
            }
            if (dstSize > 400) {
                dstSize = 400;
            }
            faceView.setLayoutParams(new LinearLayout.LayoutParams(dstSize, dstSize));
            resetFaceView();
        });
        //ImageView faceloading = findViewById(R.id.face_loading);
        //((AnimationDrawable)faceloading.getDrawable()).start();

        if (DEV_IS_801) {
            amountOut = new ViewSizeChangeAnimation(amountPanel, -1f, 0.5f);
        } else {
            amountOut = new ViewSizeChangeAnimation(amountPanel, 0.3f);
        }
        amountOut.setDuration(100);
        amountOut.setFillAfter(true);

        amountIn = new ViewSizeChangeAnimation(amountPanel);
        amountIn.setDuration(100);
        amountIn.setFillAfter(true);


        faceOut = new ScaleAnimation(2.5f, 1.0f, 2.5f, 1.0f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        faceOut.setDuration(100);
        faceOut.setFillAfter(true);

        faceIn = new ScaleAnimation(1.0f, 2.5f, 1.0f, 2.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        faceIn.setDuration(100);
        faceIn.setFillAfter(true);

        mobileIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        mobileIn.setDuration(100);
        mobileIn.setFillAfter(true);
        mobileIn.setAnimationListener(mobileInListener);

        mobileOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1);
        mobileOut.setDuration(100);
        mobileOut.setFillAfter(true);
        mobileOut.setAnimationListener(mobileOutListener);

        cardAdapter = new CardOrderAdapter(context, R.layout.layout_customer_card_adapter, new ArrayList<>());

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        localUnregister(context, receiver);
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

        this.cardOrders = orders.stream().filter(order -> order.isSuccess()).sorted((o1, o2) -> o1.getOrderTime() > o2.getOrderTime() ? -1: 0).limit(10).toArray();

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

        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_SCAN);

    }

    /**
     * 显示刷脸提示
     * @author  Moyq5
     * @since    2020/3/8 17:06
     */
    @Override
    public void onFaceTip(String msg, boolean preview) {
        this.faceTipMsg = msg;
        if (preview) {
            this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_PREVIEW);
        }
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
        this.showHandler.sendEmptyMessage(SHOW_ACTION_FACE_MOBILE);
    }


    /**
     * 显示支付状态
     * @author  Moyq5
     * @since    2020/3/8 16:45
     */
    @Override
    public void showPay(TipType type, String msg, String detail) {
        this.payTipType = type;
        this.payTipMsg = msg;
        this.payTipDetail = detail;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_PAY_TIP_DISPATCH);
    }

    @Override
    public SurfaceView getFaceSurface() {
        return this.faceSurface;
    }

    @Override
    public SurfaceView getFaceIrSurface() {
        return null;
    }

    @Override
    public void setScanFace(ScanFace scanFace) {
        this.scanFace = scanFace;
        if (null != scanFace) {
            scanFace.setCustomerPanel(this);
        }
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

        if (true) {// TODO 调试，关闭头像显示,后面代码将废弃
            return;
        }

        int w = faceBitmap.getWidth();
        int h = faceBitmap.getHeight();
        int x = Float.valueOf(w * (rectLeft + (rectRight - rectLeft)/2)).intValue();// 脸中心x坐标
        int y = Float.valueOf(h * (rectTop + (rectBottom - rectTop)/2)).intValue();// 脸中心y坐标
        int rx = Float.valueOf(w * (rectRight - rectLeft)/2).intValue();// 脸x轴半径
        int ry = Float.valueOf(h * (rectBottom - rectTop)/2).intValue();// 脸y轴半径
        int r = rx;
        if (ry > r) {
            r = ry;
        }
        r = r + 30;//
        x = x - r;
        if (x < 0) {
            x = 0;
        }
        y = y - r;
        if (y < 0) {
            y = 0;
        }
        Bitmap dstBitmap = Bitmap.createBitmap(faceBitmap, x, y, 2 * r, 2 * r);
        faceBitmap.recycle();

        showHandler.post(() -> {
            //mobileFace.setImageAlpha(255);
            //mobileFace.setImageBitmap(dstBitmap);
            //mobileFace.requestLayout();
        });
    }

    /**
     * 更新金额显示
     * @param amount 金额，元
     */
    @Override
    public void onAmount(String amount) {
        this.amount = amount;
        this.showHandler.sendEmptyMessage(SHOW_ACTION_AMOUNT);
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
        } catch (Exception e) {

        }

        showHandler.sendEmptyMessage(SHOW_ACTION_PAY_CHECK);

        if (StoreFactory.settingStore().getSwitchFaceAutoPay()) {
            showPay(WAIT, "正在支付", null);
            scanFace.credential();
            return;
        }


        this.userName = userName;
        if (null == userName) {
            showQueryName(userId, outUserId);
        }

        scanFace.credential(() -> {
            speak("请确认支付");
            showHandler.sendEmptyMessage(SHOW_ACTION_USER_NAME);
        });

    }

    @Override
    public boolean isShowing() {
        return (getVisibility()&View.VISIBLE) == View.VISIBLE;
    }


    public void log(String s) {
        if (true) {
            return;
        }
        showHandler.post(() -> {
            LinearLayout layout = findViewById(R.id.log);
            TextView textView = new TextView(context);
            textView.setText(s);
            layout.addView(textView);
            layout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                ScrollView scroll = (ScrollView)layout.getParent();
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            });

        });
    }

    @Override
    public void hide() {
        if (null != showOut) {
            startAnimation(showOut);
        } else {
            showHandler.sendEmptyMessage(SHOW_ACTION_HIDE);
        }
    }

    /**
     * 手机号键盘事件
     * @author  Moyq5
     * @since    2020/3/22 15:26
     */
    @Override
    public void onClick(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KEYCODE_1:
                mobileView.add(1);
                break;
            case KEYCODE_2:
                mobileView.add(2);
                break;
            case KEYCODE_3:
                mobileView.add(3);
                break;
            case KEYCODE_4:
                mobileView.add(4);
                break;
            case KEYCODE_5:
                mobileView.add(5);
                break;
            case KEYCODE_6:
                mobileView.add(6);
                break;
            case KEYCODE_7:
                mobileView.add(7);
                break;
            case KEYCODE_8:
                mobileView.add(8);
                break;
            case KEYCODE_9:
                mobileView.add(9);
                break;
            case KEYCODE_0:
                mobileView.add(0);
                break;
            case KEYCODE_DEL:
                mobileView.del();
                break;
            case KEYCODE_ENTER:
                String mobile = mobileView.mobile();
                if (null != scanFace && null != mobile) {
                    this.faceTipMsg = "";
                    showHandler.sendEmptyMessage(SHOW_ACTION_FACE_TIP);
                    scanFace.finish(mobile);
                    checkMobileOut();
                }
                break;
        }
    }

    private void checkPreview() {
        if ((faceTag&FACE_TAG_PREVIEW) == 0) {// 没在预览状态
            faceTag |=FACE_TAG_PREVIEW;
            faceSurface.setAlpha(0);
            faceTip.setText("");
        }
    }

    private void checkPayIn() {
        if ((faceTag& FACE_TAG_PAYIN) == 0) {// 没在支付状态
            faceTag |= FACE_TAG_PAYIN;
            amountPanel.startAnimation(amountIn);
        }
    }

    private void checkFaceOut(String msg) {
        if ((faceTag&FACE_TAG_FACING) > 0) {// 在刷脸
            faceTag &=~FACE_TAG_FACING;
            faceTip.setText(null == msg ? "": msg);
            faceView.startAnimation(faceOut);
        }
    }

    private void checkFaceOut() {
        checkFaceOut(null);
    }

    private void checkMobileOut() {
        if ((faceTag&FACE_TAG_MOBILE) > 0) {// 在输入手机号
            faceTag &=~FACE_TAG_MOBILE;
            mobilePanel.startAnimation(mobileOut);
        }
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
            userName = user.get("wx_user_info") + "：" + user.get("wx_user_name");
            if (showAction == SHOW_ACTION_USER_NAME) {
                showHandler.sendEmptyMessage(SHOW_ACTION_USER_NAME);
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

            userName = list.get(0).getWxUserInfo() + "：" + list.get(0).getWxUserName();
            if (showAction == SHOW_ACTION_USER_NAME) {
                showHandler.sendEmptyMessage(SHOW_ACTION_USER_NAME);
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

    private void resetProccessLayout() {
        faceTip.setText("");
        faceBtn.setVisibility(GONE);
        payBtn.setVisibility(GONE);
        payTip.setVisibility(GONE);
        payTip.dispatch(TipType.NONE, "");
        userInfo.setVisibility(GONE);
    }

    /**
     * 初始化刷脸布局，回到刷脸前状态
     * @author  Moyq5
     * @since    2020/3/18 19:30
     */
    private void resetFaceLayout() {
        if (null == scanFace) {
            return;
        }

        resetFaceView();

        faceSurface.setAlpha(0);

        mobilePanel.clearAnimation();
        amountPanel.clearAnimation();

        ViewGroup.LayoutParams params = amountPanel.getLayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;

    }


    private void resetFaceView() {
        faceView.clearAnimation();
        faceView.setScaleY(0.35f);
        faceView.setScaleX(0.35f);
        faceView.setPivotY(0);
        faceView.setPivotX(faceView.getWidth()/2);
        faceView.requestLayout();
    }

    public void setShowIn(Animation showIn) {
        this.showIn = showIn;
    }

    public void setShowOut(Animation showOut) {
        this.showOut = showOut;
    }

    public Animation getAmountOut() {
        return amountOut;
    }

    public void setAmountOut(Animation amountOut) {
        this.amountOut = amountOut;
    }

    public Animation getAmountIn() {
        return amountIn;
    }

    public void setAmountIn(Animation amountIn) {
        this.amountIn = amountIn;
    }

    public Animation getFaceIn() {
        return faceIn;
    }

    public void setFaceIn(Animation faceIn) {
        this.faceIn = faceIn;
    }

    public Animation getFaceOut() {
        return faceOut;
    }

    public void setFaceOut(Animation faceOut) {
        this.faceOut = faceOut;
    }

    public static class ViewSizeChangeAnimation extends Animation {
        int fromWidth = -1;
        int fromHeight = -1;
        int toWidth = -1;
        int toHeight = -1;
        float scaleWidth = -1;
        float scaleHeight = -1;
        final View view;

        public ViewSizeChangeAnimation(View view) {
            this.view = view;
        }

        public ViewSizeChangeAnimation(View view, float scaleWidth) {
            this.view = view;
            this.scaleWidth = scaleWidth;
        }

        public ViewSizeChangeAnimation(View view, float scaleWidth, float scaleHeight) {
            this.view = view;
            this.scaleWidth = scaleWidth;
            this.scaleHeight = scaleHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            synchronized (view) {
                if (interpolatedTime == 1) {
                    view.getLayoutParams().width = toWidth;
                    view.getLayoutParams().height = toHeight;
                    view.clearAnimation();
                } else {
                    view.getLayoutParams().width = fromWidth + (int) ((toWidth - fromWidth) * interpolatedTime);
                    view.getLayoutParams().height = fromHeight + (int) ((toHeight - fromHeight) * interpolatedTime);
                }
                view.requestLayout();
            }
        }

        @Override
        public void initialize(int width, int height, int toWidth, int toHeight) {
            super.initialize(width, height, toWidth, toHeight);
            this.fromWidth = width;
            this.fromHeight = height;
            if (this.scaleWidth != -1) {
                this.toWidth = (int)(width * scaleWidth);
            }
            if (this.scaleHeight != -1) {
                this.toHeight = (int)(height * scaleHeight);
            }
            if (this.toWidth == -1) {
                this.toWidth = toWidth;
            }
            if (this.toHeight == -1) {
                this.toHeight = toHeight;
            }
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }


    private int showAction = -1;
    private static final int SHOW_ACTION_SHOW = 0;
    private static final int SHOW_ACTION_HIDE = 1;
    private static final int SHOW_ACTION_WELCOME = 2;
    private static final int SHOW_ACTION_MSG = 3;
    private static final int SHOW_ACTION_AMOUNT = 4;
    private static final int SHOW_ACTION_PAY_WAIT = 5;
    private static final int SHOW_ACTION_PAY_CHECK = 6;
    private static final int SHOW_ACTION_PAY_TIP_SHOW = 7;
    private static final int SHOW_ACTION_PAY_TIP_DISPATCH = 8;
    private static final int SHOW_ACTION_FACE_SCAN = 9;
    private static final int SHOW_ACTION_FACE_TIP = 10;
    private static final int SHOW_ACTION_FACE_PREVIEW = 11;
    private static final int SHOW_ACTION_FACE_MOBILE = 12;
    private static final int SHOW_ACTION_USER_NAME = 13;
    private static final int SHOW_ACTION_CARD = 14;
    private static final int SHOW_ACTION_CARD_ORDERS = 15;

    private final Handler showHandler = new Handler(msg -> {
        switch (msg.what) {
            case SHOW_ACTION_SHOW:
                setVisibility(View.VISIBLE);
                if (null != showIn) {
                    startAnimation(showIn);
                }
                welcomePanel.bringToFront();
                amountPanel.setVisibility(VISIBLE);
                facePanel.setVisibility(VISIBLE);
                processPanel.setVisibility(VISIBLE);
                mobilePanel.setVisibility(VISIBLE);
                cardPanel.setVisibility(VISIBLE);
                msgPanel.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_HIDE:
                setVisibility(INVISIBLE);
                break;
            case SHOW_ACTION_WELCOME:
                if (SHOW_ACTION_WELCOME == showAction) {
                    return false;
                }
                faceTag = 0;
                welcomePanel.bringToFront();

                resetProccessLayout();
                resetFaceLayout();

                break;
            case SHOW_ACTION_MSG:
                TipViewVertical msgView = msgPanel.findViewById(R.id.error_msg);
                msgView.dispatch(msgInfo.type, msgInfo.msg, msgInfo.detail);
                msgPanel.bringToFront();
                break;
            case SHOW_ACTION_PAY_WAIT:
                faceTag = 0;
                processPanel.bringToFront();
                amountPanel.bringToFront();
                facePanel.bringToFront();

                resetProccessLayout();
                resetFaceLayout();

                if (null != scanFace) {
                    SettingStore store = StoreFactory.settingStore();
                    if (store.getSwitchFacePay() && !store.getSwitchFaceAutoScan()) {
                        faceBtn.setVisibility(VISIBLE);
                    }
                }
                break;
            case SHOW_ACTION_PAY_CHECK:
                checkFaceOut();
                checkPayIn();
                break;
            case SHOW_ACTION_FACE_SCAN:
                faceBtn.setVisibility(GONE);

                if (!MyWxPayFace.IS_OFFLINE) {
                    scanFace.verify();// 在线刷脸
                    break;
                }

                /*
                 * 以下为离线刷脸所特有
                 */

                faceTag |= FACE_TAG_FACING;
                faceTag &= ~FACE_TAG_PAYIN;

                processPanel.bringToFront();
                amountPanel.bringToFront();
                facePanel.bringToFront();

                resetProccessLayout();
                resetFaceLayout();

                amountPanel.clearAnimation();
                amountPanel.startAnimation(amountOut);
                faceView.clearAnimation();
                faceView.startAnimation(faceIn);
                faceTip.setText(faceTipMsg);
                scanFace.verify();

                break;
            case SHOW_ACTION_AMOUNT:
                amountPanel.setVisibility(VISIBLE);
                amountText.setText(amount);
                break;
            case SHOW_ACTION_USER_NAME:
                if (null != userName) {
                    userInfo.setText(userName);
                } else {
                    userInfo.setText("");
                }
                payTip.setVisibility(GONE);
                userInfo.setVisibility(VISIBLE);
                payBtn.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_CARD:
                faceTag = 0;
                cardPanel.bringToFront();
                cardPanel.findViewById(R.id.card_info_panel).setVisibility(View.VISIBLE);
                cardPanel.findViewById(R.id.card_order_panel).setVisibility(View.INVISIBLE);
                CardUser user = cardInfo.getUser();
                if (null == user) {
                    user = new CardUser();
                }
                TextView uidView = cardPanel.findViewById(R.id.card_uid);
                uidView.setText(cardInfo.getCardUid());
                TextView nameView = cardPanel.findViewById(R.id.card_user_name);
                nameView.setText(StringUtils.encode(user.getUserName(), 1));
                TextView groupView = cardPanel.findViewById(R.id.card_user_group);
                groupView.setText(user.getUserGroup());
                TextView noView = cardPanel.findViewById(R.id.card_user_no);
                noView.setText(StringUtils.encode(user.getUserNo(), 3));
                TextView balanceView = cardPanel.findViewById(R.id.card_balance);
                balanceView.setText(BigDecimalUtils.fenToYuan(cardInfo.getBalance()).toPlainString());
                TextView amountView = cardPanel.findViewById(R.id.last_amount);
                TextView timeView = cardPanel.findViewById(R.id.last_time);
                if (null != cardOrder) {
                    amountView.setText(BigDecimalUtils.fenToYuan(cardOrder.getAmount()).toPlainString());
                    timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(cardOrder.getOrderTime() * 1000)));
                } else {
                    amountView.setText("(无)");
                    timeView.setText("(无)");
                }

                break;
            case SHOW_ACTION_CARD_ORDERS:
                cardPanel.findViewById(R.id.card_info_panel).setVisibility(View.INVISIBLE);
                cardPanel.findViewById(R.id.card_order_panel).setVisibility(View.VISIBLE);
                ListView cardListView = findViewById(R.id.card_order_list);
                cardListView.setFocusable(false);
                cardListView.setFocusableInTouchMode(false);
                cardListView.setAdapter(cardAdapter);
                cardAdapter.clear();
                cardAdapter.addAll(cardOrders);
                cardListView.setAdapter(cardAdapter);
                break;
            case SHOW_ACTION_PAY_TIP_SHOW:
                userInfo.setText("");
                userInfo.setVisibility(GONE);
                payBtn.setVisibility(GONE);
                payTip.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_FACE_TIP:
                faceTip.setText(faceTipMsg);
                break;
            case SHOW_ACTION_FACE_PREVIEW:
                faceTag |= FACE_TAG_PREVIEW;
                faceSurface.setAlpha(1);
                break;
            case SHOW_ACTION_FACE_MOBILE:
                faceTag |= FACE_TAG_MOBILE;
                if (DEV_IS_801 || DEV_IS_BDFACE) {
                    checkPayIn();
                }
                checkFaceOut(faceTipMsg);
                mobileView.clear();
                mobilePanel.startAnimation(mobileIn);
                mobilePanel.bringToFront();
                break;
            case SHOW_ACTION_PAY_TIP_DISPATCH:
                checkMobileOut();
                payBtn.setVisibility(GONE);
                if (null != scanFace) {// 是否支持刷脸
                    userInfo.setVisibility(GONE);
                    faceBtn.setVisibility(GONE);
                    checkPreview();
                    checkFaceOut();
                }
                checkPayIn();
                payTip.dispatch(payTipType, payTipMsg, payTipDetail);
                payTip.setVisibility(VISIBLE);
                break;
        }
        showAction = msg.what;
        return false;
    });

    private static class MsgInfo {
        TipType type;
        String msg;
        String detail;
        MsgInfo(TipType type, String msg, String detail) {
            this.type = type;
            this.msg = msg;
            this.detail = detail;
        }
    }
}
