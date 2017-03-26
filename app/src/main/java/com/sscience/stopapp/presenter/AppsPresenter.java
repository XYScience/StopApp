package com.sscience.stopapp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.util.AppInfoComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sscience.stopapp.model.AppsRepository.COMMAND_UNINSTALL;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/29
 */

public class AppsPresenter implements AppsContract.Presenter {

    private Context mContext;
    private AppsContract.View mView;
    private AppsRepository mAppsRepository;
    private List<AppInfo> mAppInfos;
    private AppInfoDBController mAppInfoDBController;

    public AppsPresenter(Context context, AppsContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mAppsRepository = new AppsRepository(context);
        mAppInfos = new ArrayList<>();
        mAppInfoDBController = new AppInfoDBController(mContext);
    }

    @Override
    public void start() {

    }

    @Override
    public void getApps(int appStyle) {
        mAppsRepository.getApps(appStyle, new AppsRepository.GetAppsCallback() {
            @Override
            public void onAppsLoaded(List<AppInfo> apps) {
                Collections.sort(apps, new AppInfoComparator());// 排序
                mAppInfos = apps;
                mView.getApps(apps);
            }
        });
    }

    @Override
    public List<AppInfo> getApps() {
        return mAppInfos;
    }

    @Override
    public void operationApps(final AppInfo appInfo, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(appInfo.getAppName());
        final String[] Items = {mContext.getString(R.string.add_disable_apps), mContext.getString(R.string.uninstall_app), mContext.getString(R.string.add_desktop_shortcut)};
        builder.setItems(Items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    addDisableApps(appInfo);
                    dialogInterface.dismiss();
                } else if (i == 1) {
                    uninstallApp(appInfo, position);
                } else if (i == 2) {
                    addDesttopShortcut(appInfo);
                }
            }
        });
        builder.show();
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
    private void addDesttopShortcut(AppInfo appInfo) {
        //创建一个添加快捷方式的Intent
        Intent addSC = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //创建单击快捷键启动本程序的Intent
        Intent launcherIntent = new Intent(ShortcutActivity.OPEN_APP_SHORTCUT);
        launcherIntent.putExtra(ShortcutActivity.EXTRA_PACKAGE_NAME, appInfo.getAppPackageName());
        // 是否允许重复创建
        addSC.putExtra("duplicate", false);
        //设置快捷键的标题
        addSC.putExtra(Intent.EXTRA_SHORTCUT_NAME, appInfo.getAppName());
        //设置快捷键的图标
        addSC.putExtra(Intent.EXTRA_SHORTCUT_ICON, appInfo.getAppIcon());
        //设置单击此快捷键启动的程序
        addSC.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        //向系统发送添加快捷键的广播
        mContext.sendBroadcast(addSC);
    }

    private void addDisableApps(AppInfo appInfo) {
        AppInfoDBController appInfoDBController = new AppInfoDBController(mContext);
        List<AppInfo> disableApps = appInfoDBController.getDisableApps(AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
        if (disableApps.contains(appInfo)) {
            mView.hadAddDisableApps();
            return;
        }
        List<AppInfo> appList = new ArrayList<>();
        appList.add(appInfo);
        addDisableAppsSuccess(appList);
    }

    private void uninstallApp(final AppInfo appInfo, final int position) {
        mAppsRepository.commandSu(COMMAND_UNINSTALL + appInfo.getAppPackageName(), new AppsRepository.GetAppsCmdCallback() {
            @Override
            public void onRootAppsLoaded(List<AppInfo> apps) {

            }

            @Override
            public void onRootError() {
                mView.getRootError();
            }

            @Override
            public void onRootSuccess() {
                mAppInfoDBController.deleteDisableApp(appInfo.getAppPackageName(), AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                mView.uninstallSuccess(appInfo.getAppName(), position);
            }
        });
    }

    @Override
    public void addDisableAppsSuccess(List<AppInfo> appList) {
        for (AppInfo appInfo : appList) {
            mAppInfoDBController.addDisableApp(appInfo, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
        }
        mView.addDisableAppsSuccess();
    }
}
