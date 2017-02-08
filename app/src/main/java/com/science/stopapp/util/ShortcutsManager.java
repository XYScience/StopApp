package com.science.stopapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.science.stopapp.bean.AppInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.science.stopapp.activity.ShortcutActivity.EXTRA_PACKAGE_NAME;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
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
            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, appInfo.getAppPackageName())
                    .setShortLabel(appInfo.getAppName())
                    .setIcon(Icon.createWithBitmap(((BitmapDrawable) appInfo.getAppIcon()).getBitmap()))
                    .setIntent(
                            new Intent("com.science.stopapp.OPEN_APP_SHORTCUT")
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

            mShortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
        }
    }

    public void disableShortcut(String shortcutID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<String> list = new ArrayList<>();
            list.add(shortcutID);
            mShortcutManager.disableShortcuts(list);
        }
    }
}
