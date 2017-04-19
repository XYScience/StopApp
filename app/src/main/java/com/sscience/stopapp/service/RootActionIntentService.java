package com.sscience.stopapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.SettingActivity;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.presenter.DisableAppsPresenter;
import com.sscience.stopapp.util.CommonUtil;
import com.sscience.stopapp.util.SharedPreferenceUtil;
import com.sscience.stopapp.util.ShortcutsManager;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/8
 */

public class RootActionIntentService extends IntentService {

    private Handler mHandler;
    public static final String APP_UPDATE_HOME_APPS = "app_update_home_apps";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     */
    public RootActionIntentService() {
        super("RootActionIntentService");
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String packageName = intent.getStringExtra(ShortcutActivity.EXTRA_PACKAGE_NAME);
        MyLogger.e("-----------" + packageName);
        launchAppIntent(packageName);
        stopSelf();
    }

    private void launchAppIntent(String packageName) {
        if (!CommonUtil.isAppInstalled(RootActionIntentService.this, packageName)) {
            ShortcutsManager manager = new ShortcutsManager(RootActionIntentService.this);
            manager.removeShortcut(packageName, getString(R.string.app_had_uninstall));
        } else {
            try {
                // 存储启动的app，用于自动冻结
                SharedPreferenceUtil.put(this, DisableAppsPresenter.SP_LAUNCH_APP, packageName);
                Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(resolveIntent);
            } catch (NullPointerException e) {
                enableApp(packageName);
            }
            int spAutoDisable = (int) SharedPreferenceUtil.get(this, SettingActivity.SP_AUTO_DISABLE_APPS, -1);
            if (spAutoDisable != -1) {
                AppsRepository appsRepository = new AppsRepository(this);
                appsRepository.openAccessibilityServices(null);
            }
        }
    }

    private void enableApp(final String packageName) {
        new AppsRepository(RootActionIntentService.this).getRoot(AppsRepository.COMMAND_ENABLE + packageName, new GetRootCallback() {

            @Override
            public void onRoot(boolean isRoot) {
                if (isRoot) {
                    // 已停用的app启动，则需更新主页app为启用
                    SharedPreferenceUtil.put(RootActionIntentService.this, APP_UPDATE_HOME_APPS, true);
                    AppInfoDBController appInfoDBController = new AppInfoDBController(RootActionIntentService.this);
                    appInfoDBController.updateDisableApp(packageName, 1, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                    launchAppIntent(packageName);
                } else {
                    mHandler.post(new DisplayToast(RootActionIntentService.this
                            , getString(R.string.if_want_to_use_please_grant_app_root)));
                }
            }
        });
    }

    public class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;

        public DisplayToast(Context mContext, String text) {
            this.mContext = mContext;
            mText = text;
        }

        public void run() {
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
