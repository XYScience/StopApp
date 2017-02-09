package com.science.stopapp.presenter;

import com.science.stopapp.base.BasePresenter;
import com.science.stopapp.base.BaseView;
import com.science.stopapp.bean.AppInfo;

import java.util.List;
import java.util.Set;

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
    }

    interface Presenter extends BasePresenter {
        void getApps(int appStyle);

        void operationApps(AppInfo appInfo, int position);

        void addDisableAppsSuccess(Set<String> packageNames);

        List<String> getPackageNames();
    }
}
