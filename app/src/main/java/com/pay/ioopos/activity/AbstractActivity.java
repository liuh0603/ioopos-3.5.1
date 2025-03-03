package com.pay.ioopos.activity;

import static com.pay.ioopos.App.DEV_IS_801;
import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks;
import androidx.fragment.app.FragmentTransaction;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.display.CustomerActivity;
import com.pay.ioopos.display.CustomerHolder;
import com.pay.ioopos.display.CustomerHolderDef;
import com.pay.ioopos.display.CustomerHolderSpi;
import com.pay.ioopos.display.CustomerLayout;
import com.pay.ioopos.display.CustomerPanel;
import com.pay.ioopos.display.CustomerPresentation;
import com.pay.ioopos.display.CustomerProvider;
import com.pay.ioopos.display.CustomerStrategyAbstract;
import com.pay.ioopos.display.CustomerStrategyLayout;
import com.pay.ioopos.display.CustomerStrategyPresentation;
import com.pay.ioopos.fragment.AdminValidFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.AuthState;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.fragment.support.ErrorListener;
import com.pay.ioopos.fragment.support.NetworkState;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.keyboard.KeyboardFactory;

import java.util.concurrent.Future;

/**
 * Activity公共抽象类
 * @author    Moyq5
 * @since  2020/3/27 9:21
 */
public abstract class AbstractActivity extends AppCompatActivity implements KeyInfoListener, ErrorListener {
    private static final String TAG = AbstractActivity.class.getSimpleName();
    private final Handler mainHandler = new Handler();
    private static CustomerHolder customerHolder;
    private Future<?> mainFuture;
    private Fragment mainFragment;
    private Runnable mainCallback;
    private View currentFocus;
    protected static boolean hasSetPanel = false;
    private final FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentLifecycleCallbacks() {

        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            if (f instanceof NetworkState) {
                ((NetworkState)f).run();
            }
            if (v.isFocusable()) {
                currentFocus = v;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);
        if (getContentViewId() != 0) {
            setContentView(getContentViewId());
        }

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);

        // 副屏显示
        if (null == customerHolder) {
            if (DEV_IS_SPI) {
                customerHolder = new CustomerHolderSpi(this);
            } else {
                customerHolder = new CustomerHolderDef();
            }
        }
        customerHolder.showWelcome();
    }

    protected abstract int getContentViewId();

    @Override
    protected void onResume() {
        super.onResume();

        if (DEV_IS_SPI) {
            return;
        }
        checkSetCustomerPanel();
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (DEV_IS_SPI) {
            return res;
        }
        if (DEV_IS_801) {
            Configuration configuration = res.getConfiguration();
            if (configuration.fontScale != 0.7f) {
                configuration.fontScale = 0.7f;
                res.updateConfiguration(configuration, res.getDisplayMetrics());
            }
            return res;
        } else if (DEV_IS_BDFACE) {
            Configuration configuration = res.getConfiguration();
            if (configuration.fontScale != 1f) {
                configuration.fontScale = 1f;
                res.updateConfiguration(configuration, res.getDisplayMetrics());
            }
            return res;
        }

        if (true) {// 以下代码备用
            return res;
        }


        Configuration configuration = res.getConfiguration();
        if (this instanceof CustomerActivity && configuration.fontScale != 1f) {
            configuration.fontScale = 1f;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
            return res;
        }

        if (this instanceof MainActivity && configuration.fontScale != 1.3f) {
            configuration.fontScale = 1.3f;
            //createConfigurationContext(configuration);
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        } else if (!(this instanceof MainActivity) && configuration.fontScale != 1f) {
            configuration.fontScale = 1f;
            //createConfigurationContext(configuration);
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return res;
    }

    @Nullable
    @Override
    public View getCurrentFocus() {
        View view = super.getCurrentFocus();
        if (null != view) {
            return currentFocus = view;
        }
        return currentFocus;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        currentFocus = getCurrentFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mainFuture) {
            mainFuture.cancel(true);
        }
        if (null != mainCallback) {
            mainHandler.removeCallbacks(mainCallback);
        }
        customerHolder.dismiss();

        FragmentManager fm = getSupportFragmentManager();
        fm.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
        myDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_FULLSCREEN;

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getDownTime() <= event.getEventTime() - 1000) {
            return true;
        }
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo) {
            return true;
        }

        Fragment fragment = mainFragment;
        if (isCustomerActivity()) {
            fragment = ((AbstractActivity)App.getInstance().getActivity()).mainFragment();
        }
        if (fragment instanceof View.OnKeyListener && !fragment.isDetached()) {
            View.OnKeyListener listener = (View.OnKeyListener)fragment;
            if (listener.onKey(null, keyCode, event)) {
                return true;
            }
        }

        if (keyInfo == KeyInfo.KEY_SEARCH) {
            if (!(this instanceof StatisticsActivity)) {
                uiExecute(this::myDestroy);
            }
            startActivity(new Intent(this, StatisticsActivity.class));
            KeyboardFactory.getKeyboard().showMessage("stat");
            return true;
        } else if (keyInfo == KeyInfo.KEY_MENU) {
            if (!(this instanceof SettingActivity)) {
                uiExecute(this::myDestroy);
            }
            startActivity(new Intent(this, SettingActivity.class));
            KeyboardFactory.getKeyboard().showMessage("menu");
            return true;
        }
        return onKeyUp(keyInfo);
    }

    protected void myDestroy() {
        applyFragment(null);
    }

    @Override
    public void onError(String msg) {
        setMainFragment(new TipVerticalFragment(FAIL, msg));
    }

    protected boolean isCustomerActivity() {
        return false;
    }

    public Fragment mainFragment() {
        return mainFragment;
    }

    public CustomerHolder getCustomerHolder() {
        return customerHolder;
    }

    public final void showLoading() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (null != progressBar) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public final void hideLoading() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (null != progressBar) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setMainFragment(Fragment fragment) {
        hideLoading();
        if (null != mainFuture) {
            mainFuture.cancel(true);
        }
        mainFuture = TaskFactory.submit(() -> {
            if (null != mainCallback) {
                mainHandler.removeCallbacks(mainCallback);
            }
            mainCallback = () -> {
                if (null != mainFragment) {
                    synchronized (mainFragment) {
                        if (checkFragment(fragment)) {
                            applyFragment(fragment);
                        }
                    }
                } else {
                    if (checkFragment(fragment)) {
                        applyFragment(fragment);
                    }
                }
            };
            mainHandler.post(mainCallback);
        });
   }

    private boolean checkFragment(Fragment f) {
        if (null == f) {
            return true;
        }
        AuthState state;
        if (f instanceof AuthState && (state = (AuthState)f).useAuth() && !state.isAuth()) {
            AdminValidFragment fragment = new AdminValidFragment();
            fragment.setPwdListener((pwd) -> {
                state.auth(true);
                applyFragment(f);
            });
            applyFragment(fragment);
            return false;
        }
        if (f instanceof BindState) {
            boolean unbind = false; // 绑定状态
            boolean unchecked = false; // 签到状态
            int serverType = App.getInstance().serverType();
            String merchNo;
            switch (serverType) {
                case SERVER_TYPE_A_PAY:
                    merchNo = StoreFactory.apayStore().getMid();
                    if (null == merchNo || merchNo.isEmpty()) {
                        unbind = true;
                    }
                    break;
                case SERVER_TYPE_C_PAY:
                    merchNo = StoreFactory.cpayStore().getOutMchId();
                    if (null == merchNo || merchNo.isEmpty()) {
                        unbind = true;
                    }
                    break;
                case SERVER_TYPE_I_PAY:
                    if (!ApiUtils.isBound()) {
                        unbind = true;
                    }
                    if (!unbind && !ApiUtils.isChecked() && (f instanceof CheckInState)) {
                        unchecked = true;
                    }
                    break;
            }
            if (unbind) {
                if (!isNetworkAvailable()) {
                    onError(getString(R.string.network_is_not_connected));
                    speak(getString(R.string.network_is_not_connected));
                    return false;
                }
                onError(getString(R.string.device_is_not_binded));
                speak(getString(R.string.device_is_not_binded));
                return false;
            }
            if (unchecked) {
                onError("设备未签到");
                speak("设备未签到");
                return false;
            }
        }
        if (f instanceof NetworkState && ((NetworkState)f).useNetwork() && !isNetworkAvailable()) {
            onError(getString(R.string.network_is_not_connected));
            speak(getString(R.string.network_is_not_connected));
            return false;
        }
        return true;
    }

    public void applyFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.disallowAddToBackStack();
        if (null == fragment) {
            if (null != mainFragment) {
                fragmentTransaction.remove(mainFragment);
                mainFragment = null;
            }
        } else {
            mainFragment = fragment;
            fragmentTransaction.replace(R.id.main_container, mainFragment);
        }
        try {
            fragmentTransaction.commitNowAllowingStateLoss();
        } catch (Throwable e) {
            Log.e(TAG, "applyFragment: ", e);
        }
    }

    private void checkSetCustomerPanel() {
        CustomerPanel panel = customerHolder.getPanel();
        if (null == panel) {
            //setViewCustomerPanel();// View模式
            //setPresentationCustomerPanel();// Presentation模式
            setActivityCustomerPanel();// Activity模式
        }

        if (null == panel) {
            return;
        }

        if (panel instanceof CustomerStrategyAbstract) {
            CustomerStrategyAbstract panelAbstract = (CustomerStrategyAbstract)panel;
            CustomerProvider provider = panelAbstract.getProvider();
            if (provider.getOwnerActivity() != App.getInstance().getActivity()) {
                provider.setOwnerActivity(App.getInstance().getActivity());
            }
        }
    }

    /**
     * 双屏同显View模式
     * @author  Moyq5
     * @since    2020/7/4 20:01
     */
    private void setViewCustomerPanel() {
        CustomerLayout provider = findViewById(R.id.customer);
        if (null != provider) {
            getCustomerHolder().setPanel(new CustomerStrategyLayout(provider));
        }
    }

    /**
     * 双屏异显Presentation模式
     * @author  Moyq5
     * @since    2020/7/4 20:02
     */
    private void setPresentationCustomerPanel() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        if (null != displays && displays.length > 1) {
            CustomerPresentation provider = new CustomerPresentation(this, displays[1]);
            provider.show();
            customerHolder.setPanel(new CustomerStrategyPresentation(provider));
        }
    }

    /**
     * 双屏异显Activity模式
     * @author  Moyq5
     * @since    2020/7/4 20:03
     */
    private void setActivityCustomerPanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasSetPanel && this instanceof MainActivity) {
            hasSetPanel = true;
            startActivity(new Intent(this, CustomerActivity.class), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && App.DEV_IS_K12) {
            super.startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(1).toBundle());
        } else {
            super.startActivity(intent);
        }
    }

}
