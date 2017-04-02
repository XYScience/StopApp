package com.sscience.stopapp.widget;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.util.CommonUtil;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/2
 */
public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String foregroundPackageName = event.getPackageName().toString();
            if (!CommonUtil.getLauncherPackageName(this).equals(foregroundPackageName)) {
                return;
            }
            AppInfoDBController appInfoDBController = new AppInfoDBController(this);
            AppsRepository appsRepository = new AppsRepository(this);
            List<AppInfo> disableApps = appInfoDBController.getDisableApps(AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
            for (int i = 0; i < disableApps.size(); i++) {
                AppInfo appInfo = disableApps.get(i);
                if (appInfo.isEnable() == 1) {
                    appsRepository.commandSu(AppsRepository.COMMAND_DISABLE + appInfo.getAppPackageName(), null);
                    appInfoDBController.updateDisableApp(appInfo.getAppPackageName(), 0,
                            AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                    MyLogger.e("回到桌面已自动冻结的App：" + appInfo.getAppPackageName());
                }
            }
            appsRepository.cancelTask();
        }
    }

    @Override
    protected void onServiceConnected() {
        MyLogger.e("无障碍服务已开启");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLogger.e("无障碍服务已关闭");
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {

    }
}
