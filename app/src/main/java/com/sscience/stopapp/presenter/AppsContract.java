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

        void getRootError();

        void uninstallSuccess(String appName, int position);

        void notSupportShortcut();
    }

    interface Presenter extends BasePresenter {
        void getApps(int appStyle);

        void operationApps(AppInfo appInfo, int position);

        void addDisableAppsSuccess(List<AppInfo> appList);

        List<AppInfo> getApps();
    }
}
