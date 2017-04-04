package com.sscience.stopapp.widget;

import com.sscience.stopapp.bean.ComponentInfo;

import java.text.Collator;
import java.util.Comparator;

/**
 * @author SScience
 * @description 根据应用名排序
 * @email chentushen.science@gmail.com
 * @data 2017/2/6
 */

public class ComponentComparator implements Comparator<ComponentInfo> {

    private final Collator sCollator = Collator.getInstance();

    @Override
    public int compare(ComponentInfo componentInfo1, ComponentInfo componentInfo2) {
        String sa = componentInfo1.getComponentName().substring(componentInfo1.getComponentName().lastIndexOf(".") + 1);
        String sb = componentInfo2.getComponentName().substring(componentInfo2.getComponentName().lastIndexOf(".") + 1);
        return sCollator.compare(sa, sb); // 参考自ApplicationInfo.java中的DisplayNameComparator
    }
}
