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
         * @param apps 停用或启用app成功后需要更新列表(app停用or启用状态)
         */
        void getRootSuccess(List<AppInfo> apps, List<AppInfo> appsNew);

        /**
         * 获取root失败
         */
        void getRootError();
    }

    interface Presenter extends BasePresenter {

        void disableApp(AppInfo appInfo, int position);

        /**
         * 调用软件包管理器 (pm)
         *
         * @param cmd         可用的软件包管理器命令：list packages; disable; install。
         *                    命令的具体操作：list packages -d(进行过滤以仅显示已停用的软件包);
         *                    disable package_or_component(停用给定软件包或组件)。
         * @param appInfo     命令enable or disable对应的app信息。
         * @param isLaunchApp 命令enable时，是否是启动app操作。
         *                    list packages:
         *                    -d：进行过滤以仅显示已停用的软件包。
         *                    -e：进行过滤以仅显示已启用的软件包。
         *                    -s：进行过滤以仅显示系统软件包。
         *                    -3：进行过滤以仅显示第三方软件包。
         *                    -i：查看软件包的安装程序。
         *                    -u：也包括卸载的软件包。
         * @param position    启动app时的列表位置
         */
        void commandSu(String cmd, boolean isLaunchApp, AppInfo appInfo, int position);

        /**
         * 通过packageManager.getInstalledPackages(0)获取所有apps。
         *
         * @param appStyle 0：all；1：system；2：user。
         */
        void getApps(int appStyle);

        /**
         * 在停用(待停用)列表点击item启动app
         *
         * @param appInfo  点击启动的app信息
         * @param position 点击启动的位置
         */
        void launchApp(AppInfo appInfo, int position);

        /**
         * 停用(待停用)列表批量操作app：停用apps or 清除apps
         *
         * @param isRemove
         */
        void batchApps(boolean isRemove);

        /**
         * 得到停用(待停用)列表apps
         *
         * @return
         */
        List<String> getListDisableApps();
    }
}
