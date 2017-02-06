package com.science.stopapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.science.baserecyclerviewadapter.base.BaseCommonAdapter;
import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.science.stopapp.R;

import java.util.List;
import java.util.Map;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/7
 */

public class AboutAdapter extends BaseCommonAdapter<List<Map<String, String>>> {

    public AboutAdapter(Context context, RecyclerView recyclerView) {
        super(context, recyclerView);
    }

    @Override
    public void convertCommon(ViewHolder viewHolder, List<Map<String, String>> s, int i) {
        viewHolder.setText(R.id.tv_title, s.get(i).get("title"));
        viewHolder.setText(R.id.tv_subtitle, s.get(i).get("subtitle"));
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_about;
    }
}
