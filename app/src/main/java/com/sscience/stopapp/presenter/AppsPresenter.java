package com.sscience.stopapp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.sscience.stopapp.R;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
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
        final String[] Items = {mContext.getString(R.string.add_disable_apps), mContext.getString(R.string.uninstall_app)};
        builder.setItems(Items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    addDisableApps(appInfo);
                    dialogInterface.dismiss();
                } else if (i == 1) {
                    uninstallApp(appInfo, position);
                }
            }
        });
        builder.show();
    }

    private void addDisableApps(AppInfo appInfo) {
        AppInfoDBController appInfoDBController = new AppInfoDBController(mContext);
        List<AppInfo> disableApps = appInfoDBController.getDisableApps();
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
                mAppInfoDBController.deleteDisableApp(appInfo.getAppPackageName());
                mView.uninstallSuccess(appInfo.getAppName(), position);
            }
        });
    }

    @Override
    public void addDisableAppsSuccess(List<AppInfo> appList) {
        for (AppInfo appInfo : appList) {
            mAppInfoDBController.addDisableApp(appInfo);
        }
        mView.addDisableAppsSuccess();
    }
}
