package com.sscience.stopapp.bean;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public class ComponentInfo {

    public final static String COMPONENT_PACKAGE_NAME = "packageName";
    public final static String COMPONENT_NAME = "componentName";
    public final static String IS_ENABLE = "isEnable";

    public String packageName;
    public String componentName;
    public boolean isEnable;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
