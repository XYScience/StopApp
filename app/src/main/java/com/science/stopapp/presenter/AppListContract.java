package com.science.stopapp.presenter;

import com.science.stopapp.base.BasePresenter;
import com.science.stopapp.base.BaseView;
import com.science.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public interface AppListContract {

    interface View extends BaseView<Presenter> {
        void getAppList(List<AppInfo> appList);

        void disableOrEnableAppsSuccess(AppInfo appInfo, int position);

        void getRootFailed();
    }

    interface Presenter extends BasePresenter {
        void operationApp(AppInfo appInfo, int position);

        void commandSu(String cmd, String filter, AppInfo appInfo, int position);
    }
}
