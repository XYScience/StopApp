package com.science.stopapp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.science.stopapp.R;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.model.AppsRepository;
import com.science.stopapp.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void addDisableApps(final AppInfo appInfo) {
        Set<String> disableApps = new HashSet<>();
        disableApps = (Set<String>) SharedPreferenceUtil.get(mContext, DisableAppsPresenter.SP_DISABLE_APPS, disableApps);
        for (String packageName : disableApps) {
            if (appInfo.getAppPackageName().contains(packageName)) {
                mView.hadAddDisableApps();
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.tip);
        builder.setMessage(appInfo.isEnable() ? mContext.getString(R.string.whether_add_disable_app, appInfo.getAppName())
                : mContext.getString(R.string.whether_enable_app, appInfo.getAppName()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Set<String> disableApps = new HashSet<>();
                disableApps.add(appInfo.getAppPackageName());
                addDisableAppsSuccess(disableApps);
                dialog.dismiss();
            }
        });
        builder.show();
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
