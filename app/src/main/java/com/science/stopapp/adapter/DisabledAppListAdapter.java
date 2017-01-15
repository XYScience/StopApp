package com.science.stopapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.science.baserecyclerviewadapter.base.BaseCommonAdapter;
import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.science.stopapp.bean.AppInfo;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class DisabledAppListAdapter extends BaseCommonAdapter<AppInfo> {

    public DisabledAppListAdapter(Context context, RecyclerView recyclerView) {
        super(context, recyclerView);
    }

    @Override
    public void convertCommon(ViewHolder viewHolder, AppInfo appInfo, int i) {

    }

    @Override
    public int getItemLayoutId() {
        return 0;
    }
}
