package com.sscience.stopapp.presenter;

import android.content.Context;

import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.database.AppInfoDBController;
import com.sscience.stopapp.database.AppInfoDBOpenHelper;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetAppsCallback;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.widget.AppInfoComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sscience.stopapp.model.AppsRepository.COMMAND_ENABLE;
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
    public void getApps(int appFlag) {
        mAppsRepository.getApps(appFlag, new GetAppsCallback() {
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
    public void addDisableApps(AppInfo appInfo) {
        List<AppInfo> disableApps = mAppInfoDBController.getDisableApps(AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
        if (disableApps.contains(appInfo)) {
            mView.hadAddDisableApps();
            return;
        }
        List<AppInfo> appList = new ArrayList<>();
        appList.add(appInfo);
        addDisableAppsSuccess(appList);
    }

    @Override
    public void uninstallApp(final AppInfo appInfo, final int position) {
        mAppsRepository.getRoot(COMMAND_UNINSTALL + appInfo.getAppPackageName(), new GetRootCallback() {

            @Override
            public void onRoot(boolean isRoot) {
                if (isRoot) {
                    mAppInfoDBController.deleteDisableApp(appInfo.getAppPackageName(), AppInfoDBOpenHelper.TABLE_NAME_APP_INFO);
                    mView.uninstallSuccess(appInfo.getAppName(), position);
                } else {
                    mView.getRootError(COMMAND_UNINSTALL);
                }
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

    @Override
    public void ableApp(final AppInfo appInfo, final int position, final boolean isChecked) {
        mAppsRepository.getRoot(
                (isChecked ? AppsRepository.COMMAND_DISABLE : COMMAND_ENABLE) + appInfo.getAppPackageName(),
                new GetRootCallback() {
                    @Override
                    public void onRoot(boolean isRoot) {
                        if (!isRoot) {
                            mView.ableApp(appInfo, position, isChecked, false);
                            mView.getRootError(isChecked ? AppsRepository.COMMAND_DISABLE : COMMAND_ENABLE);
                        }
                    }
                });
    }
}
