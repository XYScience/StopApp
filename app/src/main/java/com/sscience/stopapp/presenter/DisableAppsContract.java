package com.sscience.stopapp.presenter;

import android.graphics.Bitmap;

import com.sscience.stopapp.base.BasePresenter;
import com.sscience.stopapp.base.BaseView;
import com.sscience.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public interface DisableAppsContract {

    interface View extends BaseView<Presenter> {
        /**
         * 获取apps，包括所有apps；系统apps；用户apps；停用apps
         *
         * @param apps
         */
        void getApps(List<AppInfo> apps);

        void upDateItemIfLaunch(AppInfo appInfo, int position);

        /**
         * 停用或启用app成功
         *
         * @param apps    停用或启用app成功后需要更新列表(app停用or启用状态)
         */
        void getRootSuccess(List<AppInfo> apps, List<AppInfo> appsNew);

        /**
         * 获取root失败
         */
        void getRootError();

        /**
         * 卸载app
         *
         * @param appName
         * @param position
         */
        void uninstallSuccess(String appName, int position);
    }

    interface Presenter extends BasePresenter {

        /**
         * 获取apps。
         *
         * @param appFlag 0：all；1：system；2：user；3：disable
         */
        void getDisableAppsFromRoot(int appFlag);

        void pmCommand(String cmd, int flag, AppInfo appInfo, int position);

        /**
         * 在停用(待停用)列表点击item启动app
         *
         * @param appInfo  点击启动的app信息
         * @param position 点击启动的位置
         */
        void launchApp(AppInfo appInfo, int position);

        /**
         * 停用(待停用)列表批量操作app：停用apps or 清除apps
         */
        void batchApps(int type);

        /**
         * 得到停用(待停用)列表apps包名
         *
         * @return
         */
        List<String> getDisableAppPackageNames();

        void uninstallApp(AppInfo appInfo, int position);

        void updateAppName(String packageName, String appName);

        void updateAppIcon(String packageName,  Bitmap appIcon);

        void updateHomeApps();

        void cancelTask();
    }
}
