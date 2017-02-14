package com.sscience.stopapp.bean;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * @author SScience
 * @description 实现Clonable接口，覆盖并实现clone方法，除了调用父类中的clone方法得到新的对象，
 * 还要将该类中的引用变量也clone出来。
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppInfo implements Serializable, Cloneable {

    public String appName;
    public String appPackageName;
    public Drawable appIcon;
    public boolean isEnable;
    public boolean isSystemApp;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    @Override
    public AppInfo clone() throws CloneNotSupportedException {
        AppInfo appInfo = null;
        try {
            appInfo = (AppInfo) super.clone();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return appInfo;
    }
}
