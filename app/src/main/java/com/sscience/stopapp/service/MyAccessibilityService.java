package com.sscience.stopapp.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.R;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.presenter.DisableAppsPresenter;
import com.sscience.stopapp.util.CommonUtil;
import com.sscience.stopapp.util.SharedPreferenceUtil;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/2
 */
public class MyAccessibilityService extends AccessibilityService {

    private AppInfoDBController mDBController;
    private AppsRepository mAppsRepository;
    private CountDownTimer mCountDownTimer;
    private boolean isBack;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                MyLogger.e("click");
            }
            backHomeDisableApp(event);
        }
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                isBack = true;
                break;

            case KeyEvent.KEYCODE_HOME:

                break;
        }
        return super.onKeyEvent(event);
    }

    /**
     * 回到桌面自动冻结(可一定时间后冻结)
     */
    private void backHomeDisableApp(AccessibilityEvent event) {
        String foregroundPackageName = event.getPackageName().toString();
        final String launchPackage = (String) SharedPreferenceUtil.get(this, DisableAppsPresenter.SP_LAUNCH_APP, "");
        if (TextUtils.equals(foregroundPackageName, launchPackage)) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        }
        if (!TextUtils.equals(foregroundPackageName, CommonUtil.getLauncherPackageName(this))) {
            return;
        }
        if (mCountDownTimer == null) {
            mCountDownTimer = new CountDownTimer(5 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mAppsRepository.getRoot(AppsRepository.COMMAND_DISABLE + launchPackage, new GetRootCallback() {
                        @Override
                        public void onRoot(boolean isRoot) {
                            if (isRoot) {
                                MyLogger.e("回到桌面已自动冻结的App：" + launchPackage);
                                // 切换到主界面时，更新app列表
                                SharedPreferenceUtil.put(MyAccessibilityService.this, RootActionIntentService.APP_UPDATE_HOME_APPS, true);
                                mDBController.updateDisableApp(launchPackage, 0, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                            } else {
                                Toast.makeText(MyAccessibilityService.this, getString(R.string.if_want_to_use_please_grant_app_root), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    mAppsRepository.cancelTask();
                }
            };
            mCountDownTimer.start();
        }
    }

    @Override
    protected void onServiceConnected() {
        MyLogger.e("无障碍服务已开启");
        mDBController = new AppInfoDBController(this);
        mAppsRepository = new AppsRepository(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLogger.e("无障碍服务已关闭");
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {

    }
}
