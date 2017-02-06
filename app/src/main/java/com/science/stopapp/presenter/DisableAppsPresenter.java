package com.science.stopapp.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.science.myloggerlibrary.MyLogger;
import com.science.stopapp.R;
import com.science.stopapp.activity.MainActivity;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.model.AppsRepository;
import com.science.stopapp.util.AppInfoComparator;
import com.science.stopapp.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class DisableAppsPresenter implements DisableAppsContract.Presenter {

    public static final String SP_DISABLE_APPS = "sp_disable_apps";
    public static final String COMMAND_APP_LIST = "list packages ";
    private static final String COMMAND_DISABLE = "disable ";
    private static final String COMMAND_ENABLE = "enable ";
    public static final int APP_STYLE_ALL = 0;
    public static final int APP_STYLE_SYSTEM = 1;
    public static final int APP_STYLE_USER = 2;
    private DisableAppsContract.View mView;
    private AppsRepository mAppsRepository;
    private List<AppInfo> mListDisableApps;
    private List<AppInfo> mListDisableAppsNew;
    private Set<String> mSetDisableApps;
    private Activity mActivity;
    private boolean isFirstCmd = true;

    public DisableAppsPresenter(Activity activity, DisableAppsContract.View view) {
        mActivity = activity;
        mView = view;
        mView.setPresenter(this);
        mListDisableApps = new ArrayList<>();
        mListDisableAppsNew = new ArrayList<>();
        mSetDisableApps = new HashSet<>();
        mAppsRepository = new AppsRepository(activity);
    }

    @Override
    public void start() {
        mSetDisableApps = (Set<String>) SharedPreferenceUtil.get(mActivity, SP_DISABLE_APPS, mSetDisableApps);
        if (mSetDisableApps.isEmpty()) {
            commandSu(COMMAND_APP_LIST + "-d", false, null, -1);
        } else {
            getApps(DisableAppsPresenter.APP_STYLE_ALL);
        }
    }

    @Override
    public void disableApp(final AppInfo appInfo, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.tip);
        builder.setMessage(appInfo.isEnable() ? mActivity.getString(R.string.whether_disable_app, appInfo.getAppName())
                : mActivity.getString(R.string.whether_enable_app, appInfo.getAppName()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(mActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appInfo.setEnable(!appInfo.isEnable());
                commandSu(appInfo.isEnable() ? COMMAND_ENABLE : COMMAND_DISABLE + appInfo.appPackageName,
                        false, null, -1);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void commandSu(final String cmd, final boolean isLaunchApp, final AppInfo appInfo, final int position) {
        mAppsRepository.commandSu(cmd, new AppsRepository.GetAppsCmdCallback() {
            @Override
            public void onRootAppsLoaded(List<AppInfo> apps) {
                getDisableApps(apps);
            }

            @Override
            public void onRootError() {
                mView.getRootError();
            }

            @Override
            public void onRootSuccess() {
                if (isFirstCmd) {
                    isFirstCmd = false;
                    updateApps(isLaunchApp, appInfo, position);
                }
            }
        });
    }

    /**
     * 停用or启用apps成功后更新列表
     *
     * @param isLaunchApp enable app包括在小黑屋列表(批量)启用并删除app和点击item启动app，
     *                    如果为true，则操作是点击item启动app。
     * @param appInfo
     * @param position
     */
    private void updateApps(boolean isLaunchApp, AppInfo appInfo, int position) {
        if (isLaunchApp) {
            mView.upDateItemIfLaunch(appInfo, position);
            launchAppIntent(appInfo.getAppPackageName());
        } else {
            mView.getRootSuccess(mListDisableApps, mListDisableAppsNew);
            mListDisableApps = mListDisableAppsNew;
        }
    }

    private void launchAppIntent(String packageName) {
        try {
            Intent resolveIntent = mActivity.getPackageManager().getLaunchIntentForPackage(packageName);
            mActivity.startActivity(resolveIntent);
        } catch (NullPointerException e) {
            MyLogger.e("启动app出错:" + e.toString());
        }
    }

    @Override
    public void getApps(int appStyle) {
        mAppsRepository.getApps(appStyle, new AppsRepository.GetAppsCallback() {
            @Override
            public void onAppsLoaded(List<AppInfo> apps) {
                getDisableApps(apps);
            }
        });
    }

    private void getDisableApps(List<AppInfo> appList) {
        mListDisableApps.clear();
        if (mSetDisableApps.isEmpty()) {
            for (AppInfo appInfo : appList) {
                mSetDisableApps.add(appInfo.getAppPackageName());
            }
            SharedPreferenceUtil.put(mActivity, SP_DISABLE_APPS, mSetDisableApps);
            mListDisableApps = appList;
        } else {
            for (AppInfo appInfo : appList) {
                String packageName = appInfo.getAppPackageName();
                if (mSetDisableApps.contains(packageName)) {
                    mListDisableApps.add(appInfo);
                    if (appInfo.isEnable()) {
                        ((MainActivity) mActivity).getSelection().add(packageName);
                    }
                }
            }
        }
        Collections.sort(mListDisableApps, new AppInfoComparator());// 排序
        mView.getApps(mListDisableApps);
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
        if (!appInfo.isEnable()) {
            isFirstCmd = true;
            mListDisableApps.get(position).setEnable(true);
            commandSu(COMMAND_ENABLE + appInfo.getAppPackageName(), true, appInfo, position);
        } else {
            launchAppIntent(appInfo.getAppPackageName());
        }
    }

    @Override
    public void batchApps(boolean isRemove) {
        try {
            mListDisableAppsNew = new ArrayList<>();
            for (AppInfo info : mListDisableApps) {
                mListDisableAppsNew.add(info.clone());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        isFirstCmd = true;
        boolean isNotCmd = true;
        for (int i = 0; i < mListDisableAppsNew.size(); i++) {
            String packageName = mListDisableAppsNew.get(i).getAppPackageName();
            if (((MainActivity) mActivity).getSelection().contains(packageName)) {
                if (isRemove) {
                    if (!mListDisableAppsNew.get(i).isEnable()) {
                        commandSu(DisableAppsPresenter.COMMAND_ENABLE + packageName, false, null, -1);
                        isNotCmd = false;
                    }
                    mListDisableAppsNew.remove(i);
                    ((MainActivity) mActivity).getSelection().remove(packageName);
                    mSetDisableApps.remove(packageName);
                    i--;
                } else {
                    if (mListDisableAppsNew.get(i).isEnable()) {
                        mListDisableAppsNew.get(i).setEnable(false);
                        commandSu(DisableAppsPresenter.COMMAND_DISABLE + packageName, false, null, -1);
                        ((MainActivity) mActivity).getSelection().remove(packageName);
                    }
                }
            }
        }
        if (isRemove) {
            SharedPreferenceUtil.clear(mActivity);
            SharedPreferenceUtil.put(mActivity, SP_DISABLE_APPS, mSetDisableApps);
            if (isNotCmd) {
                updateApps(false, null, -1);
            }
        }
    }
}
