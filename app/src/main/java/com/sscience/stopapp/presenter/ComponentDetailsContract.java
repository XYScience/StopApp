package com.sscience.stopapp.presenter;

import com.sscience.stopapp.base.BasePresenter;
import com.sscience.stopapp.base.BaseView;
import com.sscience.stopapp.bean.ComponentInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public interface ComponentDetailsContract {

    interface View extends BaseView<Presenter> {
        void getComponent(List<ComponentInfo> list);

        void onRoot(boolean isRoot, ComponentInfo componentInfo, int position);
    }

    interface Presenter extends BasePresenter {
        void getComponent(int tabCategory, String packageName);

        void pmComponent(ComponentInfo componentInfo, int position);
    }
}
