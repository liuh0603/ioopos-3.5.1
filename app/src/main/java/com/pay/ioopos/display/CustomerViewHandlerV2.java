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
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_CARD;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_CARD_ORDERS;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_IMG;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_MOBILE;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_PREVIEW;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_SCAN;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_SURFACE_OUT;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_TIP;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_FACE_VERIFY_OUT;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_HIDE;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_LOG;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_MSG;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_PAY_AMOUNT;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_PAY_CASE;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_PAY_TIP;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_PAY_WAIT;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_SHOW;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_USER_INFO;
import static com.pay.ioopos.display.CustomerViewV2.SHOW_ACTION_WELCOME;

import android.graphics.PixelFormat;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardUser;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.common.StringUtils;
import com.pay.ioopos.widget.KeyboardView;
import com.pay.ioopos.widget.Tip;
import com.pay.ioopos.widget.TipViewAbstract;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author mo_yq5
 * @since 2022/1/18
 */
public class CustomerViewHandlerV2 extends CustomerViewHandlerAbstract implements KeyboardView.OnKeyBoardListener {

    private final RelativeLayout blankPanel;
    private final RelativeLayout welcomePanel;// 欢迎界面

    private final RelativeLayout payPanel;// 支付信息，金额、状态等
    private final RelativeLayout payWin;// 支付窗体
    private final LinearLayout payWinBg;// 支付窗体背景
    private Animation payWinOut;// 支付窗体离开
    private Animation payWinIn;// 支付窗体进入
    private final TextView payAmountView;// 支付金额
    private final TipViewAbstract payTip;// 支付状态提示
    private final Button faceVerifyBtn;// 进入刷脸按钮

    private final RelativeLayout facePanel;// 刷脸
    private final RelativeLayout faceVerifyWin;// 刷脸窗体
    private Animation faceVerifyWinIn;// 刷脸窗体进入
    private Animation faceVerifyWinOut;// 刷脸窗体离开
    private final Button faceConfirmBtn;// 确认支付按钮
    private final TextView faceTipView;// 刷脸状态提示
    private final RelativeLayout faceSurfaceWin;
    private Animation faceSurfaceWinOut;// 刷脸预览离开
    private final SurfaceView faceSurfaceView;// 刷脸预览
    private final ImageView faceImgView;// 用户头像
    private final TextView userInfoView;// 用户信息

    private final RelativeLayout mobilePanel;// 刷脸输入手机号界面
    private Animation mobileIn;// 手机号键盘进入
    private Animation mobileOut;// 手机号键盘离开
    private final FaceMobileView mobileView;// 手机号输入框

    private final RelativeLayout cardPanel;// 实体卡信息界面

    private final RelativeLayout msgPanel;// 通用信息界面

    private int flags = 0;
    private static final int FLAG_FACE_VERIFYING = 1;       // 人脸识别中
    private static final int FLAG_PAY_WIN_OUT = 4;          // 金额信息靠边显示中
    private static final int FLAG_FACE_MOBILE = 8;          // 手机号输入进行中
    private static final int FLAG_FACE_VERIFY_WIN_OUT = 16; // 人脸信息靠边显示中

    private final CustomerViewV2 view;

    private static String logStr;

    public CustomerViewHandlerV2(CustomerViewV2 view) {
        this.view = view;
        blankPanel = view.findViewById(R.id.blank_panel);
        welcomePanel = view.findViewById(R.id.welcome_panel);
        payPanel = view.findViewById(R.id.pay_panel);
        facePanel = view.findViewById(R.id.face_panel);
        mobilePanel = view.findViewById(R.id.mobile_panel);
        cardPanel = view.findViewById(R.id.card_panel);
        msgPanel = view.findViewById(R.id.error_panel);

        payAmountView = payPanel.findViewById(R.id.amount);
        payWin = payPanel.findViewById(R.id.pay_win);
        payWinBg = payPanel.findViewById(R.id.pay_win_bg);
        payTip = payPanel.findViewById(R.id.pay_tip);

        faceVerifyBtn = payPanel.findViewById(R.id.btn_scan_face);
        faceVerifyBtn.setKeyListener(null);
        faceVerifyBtn.setOnKeyListener(null);
        faceVerifyBtn.setOnTouchListener(null);
        faceVerifyBtn.setFocusable(false);
        faceVerifyBtn.setFocusableInTouchMode(false);
        faceVerifyBtn.setOnClickListener(view_ -> {
            displayLog("刷脸支付...");
            view.showScanFace();
        });

        faceTipView = facePanel.findViewById(R.id.face_tip);
        faceTipView.setFocusable(false);
        faceTipView.setKeyListener(null);
        faceTipView.setOnKeyListener(null);

        faceVerifyWin = facePanel.findViewById(R.id.verify_win);
        faceSurfaceWin = faceVerifyWin.findViewById(R.id.surface_win);
        userInfoView =  faceVerifyWin.findViewById(R.id.user_info);
        faceImgView = faceVerifyWin.findViewById(R.id.face_img);

        faceConfirmBtn = faceVerifyWin.findViewById(R.id.btn_confirm_pay);
        faceConfirmBtn.setKeyListener(null);
        faceConfirmBtn.setOnKeyListener(null);
        faceConfirmBtn.setOnTouchListener(null);
        faceConfirmBtn.setFocusable(false);
        faceConfirmBtn.setFocusableInTouchMode(false);
        faceConfirmBtn.setOnClickListener(view_ -> {
            sendEmptyMessage(SHOW_ACTION_FACE_VERIFY_OUT);
            view.cancelNameFuture();
            view.getScanFace().credential();
        });

        faceSurfaceView = faceVerifyWin.findViewById(R.id.surface_view);
        faceSurfaceView.setFocusable(false);
        faceSurfaceView.setOnKeyListener(null);
        //faceSurface.getHolder().addCallback(new FaceSurfaceCallBack());
        //faceSurface.setZOrderOnTop(true);
        faceSurfaceView.setZOrderMediaOverlay(true);
        faceSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        mobileView = mobilePanel.findViewById(R.id.mobile_items);
        KeyboardView keyboardView = view.findViewById(R.id.key_board_view);
        keyboardView.setClickListener(this);

        createPayWinAnimation();
        createFaceVerifyWinAnimation();
        createFaceSurfaceWinAnimation();
        createMobileWinAnimation();

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
                if (null != view.getScanFace() && null != mobile) {
                    view.setFaceTipMsg("");
                    sendEmptyMessage(SHOW_ACTION_FACE_TIP);
                    view.getScanFace().finish(mobile);
                    checkSetMobilePanelOut();
                }
                break;
        }
    }

    @Override
    public void handleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case SHOW_ACTION_SHOW:
                view.setVisibility(VISIBLE);
                blankPanel.bringToFront();
                welcomePanel.bringToFront();
                break;
            case SHOW_ACTION_HIDE:
                view.setVisibility(INVISIBLE);
                break;
            case SHOW_ACTION_WELCOME:
                if (SHOW_ACTION_WELCOME == showAction) {
                    return;
                }
                flags = 0;
                blankPanel.bringToFront();
                welcomePanel.bringToFront();
                break;
            case SHOW_ACTION_MSG:
                TipViewAbstract msgView = msgPanel.findViewById(R.id.error_msg);
                msgView.dispatch(view.getMsgInfo().getType(), view.getMsgInfo().getMsg(), view.getMsgInfo().getDetail());
                blankPanel.bringToFront();
                msgPanel.bringToFront();
                break;
            case SHOW_ACTION_PAY_WAIT:
                flags = 0;
                blankPanel.bringToFront();
                payPanel.bringToFront();
                resetPayPanel();

                if (null != view.getScanFace()) {
                    SettingStore store = StoreFactory.settingStore();
                    if (store.getSwitchFacePay() && !store.getSwitchFaceAutoScan()) {
                        faceVerifyBtn.setVisibility(VISIBLE);
                    }
                }
                break;
            case SHOW_ACTION_FACE_SURFACE_OUT:
                checkSetSurfaceOut();
                break;
            case SHOW_ACTION_FACE_SCAN:
                faceVerifyBtn.setVisibility(GONE);

                if (!MyWxPayFace.IS_OFFLINE && !DEV_IS_BDFACE) {
                    view.getScanFace().verify();// 在线刷脸
                    break;
                }

                /*
                 * 以下为离线刷脸所特有
                 */

                flags |= FLAG_FACE_VERIFYING;

                blankPanel.bringToFront();
                payPanel.bringToFront();
                facePanel.bringToFront();
                resetPayPanel();
                resetFacePanel();

                payWin.startAnimation(payWinOut);
                faceVerifyWin.startAnimation(faceVerifyWinIn);
                view.getScanFace().verify();

                break;
            case SHOW_ACTION_PAY_AMOUNT:
                payAmountView.setText(view.getPayAmount());
                break;
            case SHOW_ACTION_USER_INFO:
                if (null != view.getUserInfo()) {
                    userInfoView.setText(view.getUserInfo());
                } else {
                    userInfoView.setText("");
                }
                payTip.setVisibility(GONE);
                userInfoView.setVisibility(VISIBLE);
                faceConfirmBtn.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_CARD:
                flags = 0;
                blankPanel.bringToFront();
                cardPanel.bringToFront();
                cardPanel.findViewById(R.id.card_info_panel).setVisibility(VISIBLE);
                cardPanel.findViewById(R.id.card_order_panel).setVisibility(INVISIBLE);
                CardUser user = view.getCardInfo().getUser();
                if (null == user) {
                    user = new CardUser();
                }
                TextView uidView = cardPanel.findViewById(R.id.card_uid);
                uidView.setText(view.getCardInfo().getCardUid());
                TextView nameView = cardPanel.findViewById(R.id.card_user_name);
                nameView.setText(StringUtils.encode(user.getUserName(), 1));
                TextView groupView = cardPanel.findViewById(R.id.card_user_group);
                groupView.setText(user.getUserGroup());
                TextView noView = cardPanel.findViewById(R.id.card_user_no);
                noView.setText(StringUtils.encode(user.getUserNo(), 3));
                TextView balanceView = cardPanel.findViewById(R.id.card_balance);
                balanceView.setText(BigDecimalUtils.fenToYuan(view.getCardInfo().getBalance()).toPlainString());
                TextView amountView = cardPanel.findViewById(R.id.last_amount);
                TextView timeView = cardPanel.findViewById(R.id.last_time);
                if (null != view.getCardOrder()) {
                    amountView.setText(BigDecimalUtils.fenToYuan(view.getCardOrder().getAmount()).toPlainString());
                    timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(view.getCardOrder().getOrderTime() * 1000)));
                } else {
                    amountView.setText("(无)");
                    timeView.setText("(无)");
                }

                break;
            case SHOW_ACTION_CARD_ORDERS:
                cardPanel.findViewById(R.id.card_info_panel).setVisibility(INVISIBLE);
                cardPanel.findViewById(R.id.card_order_panel).setVisibility(VISIBLE);
                ArrayAdapter<Object> cardAdapter = view.getCardAdapter();
                ListView cardListView = view.findViewById(R.id.card_order_list);
                cardListView.setFocusable(false);
                cardListView.setFocusableInTouchMode(false);
                cardListView.setAdapter(cardAdapter);
                cardAdapter.clear();
                cardAdapter.addAll(view.getCardOrders());
                cardListView.setAdapter(cardAdapter);
                break;
            case SHOW_ACTION_FACE_TIP:
                displayLog(view.getFaceTipMsg());
                faceTipView.setText(view.getFaceTipMsg());
                break;
            case SHOW_ACTION_FACE_PREVIEW:
                faceSurfaceView.setAlpha(1);
                break;
            case SHOW_ACTION_FACE_MOBILE:
                flags |= FLAG_FACE_MOBILE;
                checkSetSurfaceOut();
                mobileView.clear();
                mobilePanel.startAnimation(mobileIn);
                mobilePanel.bringToFront();
                break;
            case SHOW_ACTION_FACE_IMG:
                faceImgView.setPadding(0, 0, 0, 0);
                faceImgView.setImageBitmap(view.getFaceImg());
                faceImgView.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_FACE_VERIFY_OUT:
                checkSetPayWinIn();
                payTip.setVisibility(VISIBLE);
                checkSetVerifyWinOut();
                break;
            case SHOW_ACTION_PAY_TIP:
                checkSetMobilePanelOut();
                faceConfirmBtn.setVisibility(INVISIBLE);
                userInfoView.setVisibility(INVISIBLE);
                if (null != view.getScanFace()) {// 是否支持刷脸
                    faceVerifyBtn.setVisibility(GONE);
                    faceSurfaceView.setAlpha(0);
                    faceTipView.setText("");
                    checkSetVerifyWinOut();
                }
                checkSetPayWinIn();
                payTip.dispatch(view.getPayTip().getType(), view.getPayTip().getMsg(), view.getPayTip().getDetail());
                payTip.setVisibility(VISIBLE);
                break;
            case SHOW_ACTION_PAY_CASE:
                String authCase = msg.getData().getString("case");
                // 刷脸过程口进行了扫码或者刷卡等其它支付时，头像要显示默认头像
                if (!ScanCase.WX_FACE.equals(authCase)) {
                    faceSurfaceView.setAlpha(0);
                    faceTipView.setText("");
                    facePanel.bringToFront();
                }
                if (ScanCase.QRCODE.equals(authCase)) {
                    faceImgView.setImageDrawable(ContextCompat.getDrawable(App.getInstance(), R.drawable.ic_qr_code));
                    faceImgView.setPadding(50, 50, 50, 50);
                    faceImgView.setVisibility(VISIBLE);
                    //userInfoView.setText("");
                    userInfoView.setVisibility(INVISIBLE);
                } else if (ScanCase.NFC.equals(authCase)) {
                    faceImgView.setImageDrawable(ContextCompat.getDrawable(App.getInstance(), R.drawable.ic_card));
                    faceImgView.setPadding(50, 50, 50, 50);
                    faceImgView.setVisibility(VISIBLE);
                    //userInfoView.setText(authCode);
                    userInfoView.setVisibility(INVISIBLE);
                }
                break;
            case SHOW_ACTION_LOG:
                String logStr_ = msg.getData().getString("msg");
                if (null != logStr && logStr.equals(logStr_)) {// 不打印重复内容
                    return;
                }
                logStr = logStr_;

                TextView logView = view.findViewById(R.id.log);
                String text = logView.getText().toString();
                if (text.length() > 500) {
                    int index = text.indexOf(System.getProperty("line.separator",""), text.length() - 500);
                    if (index != -1) {
                        text = text.substring(index);
                        logView.setText(text);
                    }
                }
                logView.append(System.getProperty("line.separator") + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()) + " " + logStr);

                break;

        }
        showAction = msg.what;
    }

    private void createPayWinAnimation() {
        ScaleAnimation payWinBgOut = new ScaleAnimation(1f, 0.80f, 1f, 1f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        payWinBgOut.setDuration(100);
        payWinBgOut.setFillAfter(true);
        ScaleAnimation payWinBgIn = new ScaleAnimation(0.80f, 1f, 1f, 1f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        payWinBgIn.setDuration(100);
        payWinBgIn.setFillAfter(true);

        AnimationSet payWinOut_ = new AnimationSet(true);
        ScaleAnimation payWinOut_scale = new ScaleAnimation(1f, 0.8f, 1f, 0.8f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        TranslateAnimation payWinOut_trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -0.60f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        payWinOut_.addAnimation(payWinOut_scale);
        payWinOut_.addAnimation(payWinOut_trans);

        payWinOut = payWinOut_;
        payWinOut.setDuration(100);
        payWinOut.setFillAfter(true);
        payWinOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flags |= FLAG_PAY_WIN_OUT;
                payWinBg.startAnimation(payWinBgOut);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet payWinIn_ = new AnimationSet(true);
        ScaleAnimation payWinIn_scale = new ScaleAnimation(0.8f, 1f, 0.8f, 1f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        TranslateAnimation payWinIn_trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -0.60f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        payWinIn_.addAnimation(payWinIn_scale);
        payWinIn_.addAnimation(payWinIn_trans);

        payWinIn = payWinIn_;
        payWinIn.setDuration(100);
        payWinIn.setFillAfter(true);
        payWinIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                payWinBg.startAnimation(payWinBgIn);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void createFaceVerifyWinAnimation() {
        AnimationSet faceWinIn_ = new AnimationSet(true);
        ScaleAnimation faceWinIn_scale = new ScaleAnimation(0f, 1f, 0f, 1f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //AlphaAnimation faceWinIn_alpha = new AlphaAnimation(0f, 1f);
        faceWinIn_.addAnimation(faceWinIn_scale);
        //faceWinIn_.addAnimation(faceWinIn_alpha);

        faceVerifyWinIn = faceWinIn_;
        faceVerifyWinIn.setDuration(100);
        faceVerifyWinIn.setFillAfter(true);
        faceVerifyWinIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flags &= ~FLAG_FACE_VERIFY_WIN_OUT;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet faceWinOut_ = new AnimationSet(true);
        ScaleAnimation faceWinOut_scale = new ScaleAnimation(1f, 0.6f, 1f, 0.6f,  Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
        TranslateAnimation faceWinOut_trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.20f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -0.03f);
        faceWinOut_.addAnimation(faceWinOut_scale);
        faceWinOut_.addAnimation(faceWinOut_trans);
        faceVerifyWinOut = faceWinOut_; //new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        faceVerifyWinOut.setDuration(100);
        faceVerifyWinOut.setFillAfter(true);
        faceVerifyWinOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                faceVerifyWin.setBackground(null);
                userInfoView.setVisibility(INVISIBLE);
                faceConfirmBtn.setVisibility(INVISIBLE);
                faceSurfaceWin.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void createFaceSurfaceWinAnimation() {
        faceSurfaceWinOut = new ScaleAnimation(1f, 0.6f, 1f, 0.6f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        faceSurfaceWinOut.setDuration(100);
        faceSurfaceWinOut.setFillAfter(true);
        faceSurfaceWinOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //faceVerifyWin.setBackgroundResource(R.drawable.cus_3);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void createMobileWinAnimation() {

        mobileIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        mobileIn.setDuration(100);
        mobileIn.setFillAfter(true);
        mobileIn.setAnimationListener(new Animation.AnimationListener() {
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
        });

        mobileOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1);
        mobileOut.setDuration(100);
        mobileOut.setFillAfter(true);
        mobileOut.setAnimationListener(new Animation.AnimationListener() {
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
        });

    }

    private void resetPayPanel() {
        payWin.clearAnimation();
        payWinBg.clearAnimation();
        payTip.setVisibility(GONE);
        payTip.dispatch(Tip.TipType.NONE, "");
    }

    /**
     * 初始化刷脸布局，回到刷脸前状态
     * @author  Moyq5
     * @since    2020/3/18 19:30
     */
    private void resetFacePanel() {
        if (null == view.getScanFace()) {
            return;
        }
        faceTipView.setText("");
        faceVerifyBtn.setVisibility(GONE);
        faceConfirmBtn.setVisibility(INVISIBLE);
        userInfoView.setVisibility(INVISIBLE);
        faceImgView.setVisibility(INVISIBLE);

        faceSurfaceView.setAlpha(0);

        faceVerifyWin.setBackground(null);
        faceVerifyWin.clearAnimation();

        faceSurfaceWin.clearAnimation();

        mobilePanel.clearAnimation();

    }

    private void checkSetPayWinIn() {
        if ((flags & FLAG_PAY_WIN_OUT) > 0) {
            flags &=  ~FLAG_PAY_WIN_OUT;
            payWin.startAnimation(payWinIn);
        }
    }

    private void checkSetSurfaceOut() {
        if ((flags & FLAG_FACE_VERIFYING) > 0) {
            flags &= ~FLAG_FACE_VERIFYING;
            faceSurfaceWin.startAnimation(faceSurfaceWinOut);
        }
    }

    private void checkSetVerifyWinOut() {
        if ((flags & FLAG_FACE_VERIFY_WIN_OUT) == 0) {
            flags |= FLAG_FACE_VERIFY_WIN_OUT;
            faceVerifyWin.startAnimation(faceVerifyWinOut);
        }
    }

    private void checkSetMobilePanelOut() {
        if ((flags & FLAG_FACE_MOBILE) > 0) {
            flags &= ~FLAG_FACE_MOBILE;
            mobilePanel.startAnimation(mobileOut);
        }
    }

}
