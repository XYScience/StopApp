package com.sscience.stopapp.model;

import com.sscience.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public interface GetAppsCallback {

    /**
     * 如果appList为null， 则未获取root权限
     *
     * @param appList
     */
    void onAppsLoaded(List<AppInfo> appList);
}
