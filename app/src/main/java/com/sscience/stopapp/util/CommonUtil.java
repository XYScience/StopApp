package com.sscience.stopapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class CommonUtil {

    public static int dipToPx(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int pxToDip(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue / scale + 0.5f);
    }

    public static View setTranslucentStatusBar(Activity activity, int statusBarColor) {
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity));
        statusBarView.setBackgroundColor(ContextCompat.getColor(activity, statusBarColor));
        contentView.addView(statusBarView, lp);
        return statusBarView;
    }

    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static int getWindowScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static String getAppVersion(Context context) {
        String version = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    //android 获取当前手机型号
    public static String getPhoneModel() {
        Build bd = new Build();
        return bd.MODEL;
    }

    //android 获取当前手机Android版本
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * 是否有启动界面，即在manifest里定义的 “action.MAIN”&“category.LAUNCHER”
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isLauncherActivity(Context context, String packageName) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        if (infoList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否安装
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 机型                    设置方法
     * 锤子（Smartisan OS）    九宫格/十六宫格模式不支持，需切换为安卓原生（设置 → 桌面设置项 → 安卓原生）
     * 华为（EMUI 4.0）        手机管家 → 权限管理 → 应用 → 平行空间 → 「创建桌面快捷方式」设为开启
     * 奇酷                    设置 → 桌面 → 快捷方式，启用
     * 小米（MIUI 7 部分版本）  安全中心 → 应用权限管理 → 应用管理 → 平行空间 → 桌面快捷方式，设为允许
     * vivo（Funtouch）        Funtouch 2.5 以下版本，自带桌面不支持
     * Funtouch 2.5            及以上版本：i管家 → 软件管理 → 桌面快捷方式管理 → 找到「平行空间」，设为允许
     * 金立（amigo）           自带桌面不支持第三方应用创建快捷方式
     * 一加（H2OS）            自带桌面不支持第三方应用创建快捷方式
     * OPPO（Color OS 3.0）    自带桌面不支持第三方应用创建快捷方式
     * ZUK（ZUI）              安全中心 → 权限管理 → 按权限管理 → 在桌面上创建快捷方式，找到「平行空间」设置允许
     */
    public static void addDesktopShortcut(Context context, AppInfo appInfo) {
        //创建单击快捷键启动本程序的Intent
        Intent launcherIntent = new Intent(ShortcutActivity.OPEN_APP_SHORTCUT);
        launcherIntent.putExtra(ShortcutActivity.EXTRA_PACKAGE_NAME, appInfo.getAppPackageName());
        try {
            //创建一个添加快捷方式的Intent
            Intent addSC = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 是否允许重复创建
            addSC.putExtra("duplicate", false);
            //设置快捷键的标题
            addSC.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.getAppName());
            //设置快捷键的图标
            addSC.putExtra(Intent.EXTRA_SHORTCUT_ICON, appInfo.getAppIcon()); // Intent传递数据大小有限制
            //设置单击此快捷键启动的程序
            addSC.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            //向系统发送添加快捷键的广播
            context.sendBroadcast(addSC);
        } catch (Exception e) {
            MyLogger.e(e.toString());
            //创建一个添加快捷方式的Intent
            Intent addSC = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 是否允许重复创建
            addSC.putExtra("duplicate", false);
            //设置快捷键的标题
            addSC.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.getAppName());
            //设置快捷键的图标
            addSC.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(
                    context, R.mipmap.ic_android));
            //设置单击此快捷键启动的程序
            addSC.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            //向系统发送添加快捷键的广播
            context.sendBroadcast(addSC);
        }
    }
}
