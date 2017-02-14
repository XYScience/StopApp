package com.sscience.stopapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

import static com.sscience.stopapp.activity.ShortcutActivity.EXTRA_PACKAGE_NAME;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/7
 */

public class ShortcutsManager {

    private Context mContext;
    private ShortcutManager mShortcutManager;

    public ShortcutsManager(Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mShortcutManager = context.getSystemService(ShortcutManager.class);
        }
    }

    public void addShortcut(AppInfo appInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<ShortcutInfo> shortcutList = mShortcutManager.getDynamicShortcuts();
            if (shortcutList.size() == 3) {
                removeShortcut(shortcutList.get(shortcutList.size() - 1).getId());
                MyLogger.e("shortcut最多显示4个");
            }
            for (ShortcutInfo info : shortcutList) {
                if (appInfo.getAppPackageName().equals(info.getId())) {
                    MyLogger.e("已经存在的shortcut:" + info.getId());
                    return;
                }
            }
            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, appInfo.getAppPackageName())
                    .setShortLabel(appInfo.getAppName())
                    .setIcon(Icon.createWithBitmap(((BitmapDrawable) appInfo.getAppIcon()).getBitmap()))
                    .setIntent(
                            new Intent(ShortcutActivity.OPEN_APP_SHORTCUT)
                                    .putExtra(EXTRA_PACKAGE_NAME, appInfo.getAppPackageName())
                            // this dynamic shortcut set up a back stack using Intents, when pressing back, will go to MainActivity
                            // the last Intent is what the shortcut really opened
//                            new Intent[]{
//                                    new Intent(Intent.ACTION_MAIN, Uri.EMPTY, mContext, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
//                                    new Intent(AppListActivity.ACTION_OPEN_DYNAMIC)
//                                    // intent's action must be set
//                            }
                    )
                    .build();

            shortcutList.add(shortcut);
            mShortcutManager.setDynamicShortcuts(shortcutList);
        }
    }

    public void disableShortcut(String shortcutID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<String> list = new ArrayList<>();
            list.add(shortcutID);
            mShortcutManager.disableShortcuts(list);
        }
    }

    public void removeShortcut(String shortcutID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<String> list = new ArrayList<>();
            list.add(shortcutID);
            mShortcutManager.removeDynamicShortcuts(list);
        }
    }
}
