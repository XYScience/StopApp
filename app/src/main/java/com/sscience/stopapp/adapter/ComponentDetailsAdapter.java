package com.sscience.stopapp.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;

import com.science.baserecyclerviewadapter.base.BaseCommonAdapter;
import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.sscience.stopapp.R;
import com.sscience.stopapp.bean.ComponentInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public class ComponentDetailsAdapter extends BaseCommonAdapter<List<ComponentInfo>> {

    public ComponentDetailsAdapter(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_component_details;
    }

    @Override
    public void convertCommon(ViewHolder viewHolder, List<ComponentInfo> list, int i) {
        ComponentInfo componentInfo = list.get(i);
        viewHolder.setText(R.id.tv_component, componentInfo.getComponentName()
                .substring(componentInfo.getComponentName().lastIndexOf(".") + 1));
        SwitchCompat sc = viewHolder.getView(R.id.switch_component);
        sc.setChecked(componentInfo.isEnable());
    }
}
