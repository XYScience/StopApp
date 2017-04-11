package com.sscience.stopapp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.activity.SettingActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetAppsCallback;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.widget.AppInfoComparator;
import com.sscience.stopapp.util.SharedPreferenceUtil;
import com.sscience.stopapp.util.ShortcutsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author SScience
 * @description 首页停用（待停用）apps列表
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class DisableAppsPresenter implements DisableAppsContract.Presenter {

    public static final String SP_DISABLE_APPS = "sp_disable_apps";
    public static final int CMD_FLAG_LAUNCH_APP = 0;
    public static final int CMD_FLAG_UNINSTALL = 1;
    public static final int CMD_FLAG_BATCH_APPS = 2;
    private DisableAppsContract.View mView;
    private AppsRepository mAppsRepository;
    private List<AppInfo> mListDisableApps;
    private List<AppInfo> mListDisableAppsNew;
    private Activity mActivity;
    private ShortcutsManager mShortcutsManager;
    private AppInfoDBController mAppInfoDBController;

    public DisableAppsPresenter(Activity activity, DisableAppsContract.View view) {
        mActivity = activity;
        mView = view;
        mView.setPresenter(this);
        mListDisableApps = new ArrayList<>();
        mListDisableAppsNew = new ArrayList<>();
        mAppsRepository = new AppsRepository(activity);
        mShortcutsManager = new ShortcutsManager(activity);
        mAppInfoDBController = new AppInfoDBController(activity);
    }

    @Override
    public void start() {
        List<AppInfo> disableApps = mAppInfoDBController.getDisableApps(AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
        if (disableApps.isEmpty() && disableApps.size() == 0) {
            getDisableAppsFromRoot(AppsRepository.APPS_FLAG_DISABLE);
        } else {
            getDisableApps(disableApps, false);
        }
    }

    @Override
    public void getDisableAppsFromRoot(int appFlag) {
        mAppsRepository.getApps(appFlag, new GetAppsCallback() {
            @Override
            public void onAppsLoaded(List<AppInfo> appList) {
                if (appList == null) {
                    mView.getRootError();
                } else {
                    getDisableApps(appList, true);
                }
            }
        });
    }

    @Override
    public void pmCommand(final String cmd, final int flag, final AppInfo appInfo, final int position) {
        mAppsRepository.getRoot(cmd, new GetRootCallback() {
            @Override
            public void onRoot(boolean isRoot) {
                if (isRoot) {
                    if (flag == CMD_FLAG_LAUNCH_APP) {
                        mAppInfoDBController.updateDisableApp(appInfo.getAppPackageName(), 1
                                , AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                        mListDisableApps.get(position).setEnable(1);
                        addShortcut(appInfo);
                        mView.upDateItemIfLaunch(appInfo, position);
                        launchAppIntent(appInfo.getAppPackageName());
                    } else if (flag == CMD_FLAG_UNINSTALL) {
                        mAppInfoDBController.deleteDisableApp(appInfo.getAppPackageName(), AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                        mView.uninstallSuccess(appInfo.getAppName(), position);
                    }
                } else {
                    mView.getRootError();
                }
            }
        });
    }

    private void launchAppIntent(String packageName) {
        try {
            Intent resolveIntent = mActivity.getPackageManager().getLaunchIntentForPackage(packageName);
            mActivity.startActivity(resolveIntent);
        } catch (NullPointerException e) {
            MyLogger.e("启动app出错:" + e.toString());
        }
    }

    private void getDisableApps(List<AppInfo> appList, boolean isFirst) {
        mListDisableApps.clear();
        for (AppInfo appInfo : appList) {
            if (isFirst) {
                mAppInfoDBController.addDisableApp(appInfo, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
            }
        }
        if ((boolean) SharedPreferenceUtil.get(mActivity, SettingActivity.SP_DISPLAY_SYSTEM_APPS, true)) {
            mListDisableApps = appList;
        } else {
            for (AppInfo appInfo : appList) {
                if (appInfo.isSystemApp() == 0) {
                    mListDisableApps.add(appInfo);
                }
            }
        }
        Collections.sort(mListDisableApps, new AppInfoComparator());// 排序
        mView.getApps(mListDisableApps);
    }

    private void addShortcut(AppInfo appInfo) {
        String sp = (String) SharedPreferenceUtil.get(mActivity, ShortcutsManager.SP_ADD_SHORTCUT_MODE, "");
        if (TextUtils.isEmpty(sp) || ShortcutsManager.SP_AUTO_SHORTCUT.equals(sp)) {
            mShortcutsManager.addAppShortcut(Arrays.asList(appInfo));
        }
    }

    @Override
    public List<String> getDisableAppPackageNames() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < mListDisableApps.size(); i++) {
            list.add(mListDisableApps.get(i).getAppPackageName());
        }
        return list;
    }

    @Override
    public void launchApp(AppInfo appInfo, int position) {
        if (appInfo.isEnable() == 0) {
            pmCommand(AppsRepository.COMMAND_ENABLE + appInfo.getAppPackageName(), CMD_FLAG_LAUNCH_APP,
                    appInfo, position);
        } else {
            addShortcut(appInfo);
            launchAppIntent(appInfo.getAppPackageName());
            mView.upDateItemIfLaunch(null, -1);
        }
    }

    @Override
    public void batchApps(final int type) {
        mAppsRepository.getRoot(AppsRepository.COMMAND_GET_ROOT + mActivity.getPackageCodePath()
                , new GetRootCallback() {
                    @Override
                    public void onRoot(boolean isRoot) {
                        if (!isRoot) {
                            mView.getRootError();
                            return;
                        }
                        try {
                            mListDisableAppsNew = new ArrayList<>();
                            for (AppInfo info : mListDisableApps) {
                                mListDisableAppsNew.add(info.clone());
                            }
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        int cmdNum = 0;
                        List<AppInfo> appList = new ArrayList<>(((MainActivity) mActivity).getSelection());
                        if (type == 0) { // 停用应用
                            if (appList.isEmpty()) {
                                appList = mListDisableApps;
                            }
                            for (int i = 0; i < appList.size(); i++) {
                                AppInfo appInfo = appList.get(i);
                                if (appInfo.isEnable() == 1) {
                                    cmdNum++;
                                    ((MainActivity) mActivity).getSelection().remove(appInfo);
                                    mListDisableAppsNew.get(mListDisableAppsNew.indexOf(appInfo)).setEnable(0);
                                    pmCommand(AppsRepository.COMMAND_DISABLE + appInfo.getAppPackageName(),
                                            CMD_FLAG_BATCH_APPS, null, -1);
                                    mAppInfoDBController.updateDisableApp(appInfo.getAppPackageName(), 0,
                                            AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                                }
                            }
                        } else if (type == 1) { // 启用应用
                            for (int i = 0; i < appList.size(); i++) {
                                AppInfo appInfo = appList.get(i);
                                if (appInfo.isEnable() == 0) {
                                    cmdNum++;
                                    ((MainActivity) mActivity).getSelection().remove(appInfo);
                                    mListDisableAppsNew.get(mListDisableAppsNew.indexOf(appInfo)).setEnable(1);
                                    pmCommand(AppsRepository.COMMAND_ENABLE + appInfo.getAppPackageName(),
                                            CMD_FLAG_BATCH_APPS, null, -1);
                                    mAppInfoDBController.updateDisableApp(appInfo.getAppPackageName(), 1,
                                            AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                                }
                            }
                        } else if (type == 2) { // 移除列表
                            for (int i = 0; i < appList.size(); i++) {
                                AppInfo appInfo = appList.get(i);
                                if (appInfo.isEnable() == 0) {
                                    cmdNum++;
                                    pmCommand(AppsRepository.COMMAND_ENABLE + appInfo.getAppPackageName(),
                                            CMD_FLAG_BATCH_APPS, null, -1);
                                }
                                ((MainActivity) mActivity).getSelection().remove(appInfo);
                                mListDisableAppsNew.remove(appInfo);
                                mAppInfoDBController.deleteDisableApp(appInfo.getAppPackageName(),
                                        AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                            }
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mView.getRootSuccess(null, mListDisableApps, mListDisableAppsNew);
                                mListDisableApps = mListDisableAppsNew;
                            }
                        }, cmdNum < 2 ? 1000 : 2000);
                    }
                });
    }

    @Override
    public void uninstallApp(final AppInfo appInfo, final int position) {
        pmCommand(AppsRepository.COMMAND_UNINSTALL + appInfo.getAppPackageName(), CMD_FLAG_UNINSTALL,
                appInfo, position);
    }

    @Override
    public void updateAppName(String packageName, String appName) {
        mAppInfoDBController.updateAppName(packageName, appName, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
    }

    @Override
    public void updateAppIcon(String packageName, Bitmap appIcon) {
        mAppInfoDBController.updateAppIcon(packageName, appIcon, AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
    }

    @Override
    public void cancelTask() {
        mAppsRepository.cancelTask();
    }
}
