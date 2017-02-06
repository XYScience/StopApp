package com.science.stopapp.util;

import android.text.TextUtils;

import com.science.stopapp.bean.AppInfo;

import java.text.Collator;
import java.util.Comparator;

/**
 * @author SScience
 * @description 根据应用名排序
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/2/6
 */

public class AppInfoComparator implements Comparator<AppInfo> {

    private final Collator sCollator = Collator.getInstance();

    @Override
    public int compare(AppInfo appInfo1, AppInfo appInfo2) {
        String sa = appInfo1.getAppName();
        if (TextUtils.isEmpty(sa)) {
            sa = appInfo1.getAppPackageName();
        }
        String sb = appInfo2.getAppName();
        if (TextUtils.isEmpty(sb)) {
            sb = appInfo2.getAppPackageName();
        }
        return sCollator.compare(sa, sb);
    }
}
