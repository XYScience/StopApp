package com.science.stopapp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.science.stopapp.R;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.model.AppsRepository;
import com.science.stopapp.util.AppInfoComparator;
import com.science.stopapp.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.science.stopapp.model.AppsRepository.COMMAND_UNINSTALL;

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

    public AppsPresenter(Context context, AppsContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mAppsRepository = new AppsRepository(context);
        mAppInfos = new ArrayList<>();
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
    public List<String> getPackageNames() {
        List<String> packageNames = new ArrayList<>();
        for (AppInfo appInfo : mAppInfos) {
            packageNames.add(appInfo.getAppPackageName());
        }
        return packageNames;
    }

    @Override
    public void operationApps(final AppInfo appInfo, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(appInfo.getAppName());
        final String[] Items = {mContext.getString(R.string.add_disable_apps), mContext.getString(R.string.uninstall_app)};
        builder.setItems(Items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    addDisableApps(appInfo, dialogInterface);
                } else if (i == 1) {
                    uninstallApp(appInfo, position);
                }
            }
        });
        builder.show();
    }

    private void addDisableApps(AppInfo appInfo, DialogInterface dialogInterface) {
        Set<String> disableApps = new HashSet<>();
        disableApps = (Set<String>) SharedPreferenceUtil.get(mContext, DisableAppsPresenter.SP_DISABLE_APPS, disableApps);
        for (String packageName : disableApps) {
            if (appInfo.getAppPackageName().contains(packageName)) {
                mView.hadAddDisableApps();
                return;
            }
        }
        Set<String> addDisable = new HashSet<>();
        addDisable.add(appInfo.getAppPackageName());
        addDisableAppsSuccess(addDisable);
        dialogInterface.dismiss();
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
                mView.uninstallSuccess(appInfo.getAppName(), position);
            }
        });
    }

    @Override
    public void addDisableAppsSuccess(Set<String> packageNames) {
        Set<String> disableApps = new HashSet<>();
        disableApps = (Set<String>) SharedPreferenceUtil.get(mContext, DisableAppsPresenter.SP_DISABLE_APPS, disableApps);
        disableApps.addAll(packageNames);

        SharedPreferenceUtil.clear(mContext);
        SharedPreferenceUtil.put(mContext, DisableAppsPresenter.SP_DISABLE_APPS, disableApps);

        mView.addDisableAppsSuccess();
    }
}
