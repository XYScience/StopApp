package com.sscience.stopapp.presenter;

import com.sscience.stopapp.base.BasePresenter;
import com.sscience.stopapp.base.BaseView;
import com.sscience.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/29
 */

public interface AppsContract {

    interface View extends BaseView<Presenter> {
        void getApps(List<AppInfo> apps);

        void hadAddDisableApps();

        void addDisableAppsSuccess();

        void getRootError(String cmd);

        void uninstallSuccess(String appName, int position);

        void ableApp(AppInfo appInfo, int position, boolean isChecked, boolean isAbleApp);
    }

    interface Presenter extends BasePresenter {
        void getApps(int appStyle);

        void addDisableAppsSuccess(List<AppInfo> appList);

        void uninstallApp(AppInfo appInfo, int position);

        void addDisableApps(AppInfo appInfo);

        void ableApp(AppInfo appInfo, int position, boolean isChecked);

        List<AppInfo> getApps();
    }
}
