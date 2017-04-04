package com.sscience.stopapp.presenter;

import android.content.Context;

import com.sscience.stopapp.bean.ComponentInfo;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.widget.ComponentComparator;

import java.util.Collections;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public class ComponentDetailsPresenter implements ComponentDetailsContract.Presenter {

    private ComponentDetailsContract.View mView;
    private AppsRepository mAppsRepository;

    public ComponentDetailsPresenter(Context context, ComponentDetailsContract.View view) {
        mView = view;
        mView.setPresenter(this);
        mAppsRepository = new AppsRepository(context);
    }

    @Override
    public void start() {

    }

    @Override
    public void getComponent(int tabCategory, String packageName) {
        List<ComponentInfo> mComponents = mAppsRepository.getComponentInfo(packageName, tabCategory);
        Collections.sort(mComponents, new ComponentComparator());// 排序
        mView.getComponent(mComponents);
    }

    @Override
    public void pmComponent(final ComponentInfo componentInfo, final int position) {
        final String cmd = (componentInfo.isEnable() ? AppsRepository.COMMAND_DISABLE : AppsRepository.COMMAND_ENABLE)
                + componentInfo.getPackageName() + "/"
                + componentInfo.getComponentName();
        mAppsRepository.getRoot(cmd, new GetRootCallback() {
            @Override
            public void onRoot(boolean isRoot) {
                mView.onRoot(isRoot, componentInfo, position);
            }
        });
    }
}
