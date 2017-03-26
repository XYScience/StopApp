package com.sscience.stopapp.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/7
 */

@TargetApi(Build.VERSION_CODES.N_MR1)
public class ShortcutsManager {

    private Context mContext;
    public static final String SP_ADD_SHORTCUT_MODE = "sp_shortcut"; // shortcut添加方式
    public static final String SP_MANUAL_SHORTCUT = "sp_manual_shortcut"; // shortcut手动添加
    public static final String SP_AUTO_SHORTCUT = "sp_auto_shortcut"; // shortcut自动添加
    private ShortcutManager mShortcutManager;
    private AppInfoDBController mDBController;

    public ShortcutsManager(Context context) {
        mContext = context;
        mShortcutManager = context.getSystemService(ShortcutManager.class);
        mDBController = new AppInfoDBController(context);
    }

    /**
     * 添加App Shortcut
     */
    public void addAppShortcut(List<AppInfo> appList) {
        List<AppInfo> appInfoDB = new ArrayList<>(
                mDBController.getDisableApps(AppInfoDBOpenHelper.TABLE_NAME_SHORTCUT_APP_INFO));

        for (AppInfo appInfo : appList) {
            if (!CommonUtil.isLauncherActivity(mContext, appInfo.getAppPackageName())) {
                continue;
            }
            if (!mDBController.searchApp(AppInfoDBOpenHelper.TABLE_NAME_SHORTCUT_APP_INFO, appInfo.getAppPackageName())) {
                appInfoDB.add(appInfo);
            }
        }

        mDBController.clearDisableApp(AppInfoDBOpenHelper.TABLE_NAME_SHORTCUT_APP_INFO);
        List<ShortcutInfo> shortcutList = new ArrayList<>();
        for (int i = appInfoDB.size() - 1; i >= 0; i--) {
            if (shortcutList.size() < 4) {
                shortcutList.add(getShortcut(appInfoDB.get(i)));
                mDBController.addDisableApp(appInfoDB.get(i), AppInfoDBOpenHelper.TABLE_NAME_SHORTCUT_APP_INFO);
            } else {
                removeShortcut(appInfoDB.get(i).getAppPackageName(), mContext.getString(R.string.shortcut_num_limit));
            }
        }
        mShortcutManager.setDynamicShortcuts(shortcutList);
    }

    /**
     * 构造App Shortcut Intent
     *
     * @param appInfo
     * @return
     */
    private ShortcutInfo getShortcut(AppInfo appInfo) {
        ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, appInfo.getAppPackageName())
                .setShortLabel(appInfo.getAppName())
                .setIcon(Icon.createWithBitmap(appInfo.getAppIcon()))
                .setIntent(
                        new Intent(ShortcutActivity.OPEN_APP_SHORTCUT)
                                .putExtra(ShortcutActivity.EXTRA_PACKAGE_NAME, appInfo.getAppPackageName())
                        // this dynamic shortcut set up a back stack using Intents, when pressing back, will go to MainActivity
                        // the last Intent is what the shortcut really opened
//                            new Intent[]{
//                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, mContext, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
//                                    new Intent(AppListActivity.ACTION_OPEN_DYNAMIC)
//                                    // intent's action must be set
//                            }
                )
                .build();

        return shortcut;
    }

    /**
     * 如果桌面有Pinning Shortcuts，且对应的app被用户卸载，则要disable并提示
     *
     * @param shortcutID
     */
    public void removeShortcut(String shortcutID, String test) {
        mShortcutManager.disableShortcuts(Arrays.asList(shortcutID), test);
        mShortcutManager.removeDynamicShortcuts(Arrays.asList(shortcutID));
    }
}
