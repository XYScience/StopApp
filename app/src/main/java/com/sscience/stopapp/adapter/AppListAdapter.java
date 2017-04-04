package com.sscience.stopapp.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.AppListActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppListAdapter extends AppAdapter implements Filterable {
    private AppListActivity mAppListActivity;
    private Resources mResources;
    private AppFilter mAppFilter;
    private List<AppInfo> mOriginalAppInfo;

    public AppListAdapter(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
        mAppListActivity = (AppListActivity) activity;
        mResources = mAppListActivity.getResources();
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_app;
    }

    @Override
    public void convertCommon(ViewHolder viewHolder, List<AppInfo> appInfo, int position) {
        super.convertCommon(viewHolder, appInfo, position);
        final AppInfo info = appInfo.get(position);
        viewHolder.setText(R.id.tv_app_package_name, info.getAppPackageName());
        ((TextView) viewHolder.getView(R.id.tv_app_name)).setTextColor(info.isEnable() == 1
                ? mResources.getColor(R.color.textPrimary)
                : mResources.getColor(R.color.translucentBg));
        ((ImageView) viewHolder.getView(R.id.iv_app_icon)).getDrawable().setColorFilter(info.isEnable() == 1
                ? mColorFilterNormal : mColorFilterGrey);
        ((TextView) viewHolder.getView(R.id.tv_app_package_name)).setTextColor(info.isEnable() == 1
                ? mResources.getColor(R.color.textSecondary)
                : mResources.getColor(R.color.translucentBg));
        AppCompatCheckBox cb = viewHolder.getView(R.id.cb_select_apps);
        cb.setOnCheckedChangeListener(null); // CheckBox在执行setChecked时会触发setOnCheckedChangeListener
        cb.setChecked(mAppListActivity.getSelection().contains(info));
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Set<AppInfo> appList = mAppListActivity.getSelection();
                if (isChecked) {
                    appList.add(info);
                } else {
                    appList.remove(info);
                }
                mAppListActivity.checkSelection();
            }
        });
    }

    @Override
    public Filter getFilter() {
        if (mAppFilter == null) {
            mAppFilter = new AppFilter();
            mOriginalAppInfo = getData();
        }
        return mAppFilter;
    }

    class AppFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<AppInfo> newAppInfo = new ArrayList<>();
            if (constraint != null && constraint.toString().trim().length() > 0) {
                for (AppInfo appInfo : mOriginalAppInfo) {
                    if (appInfo.getAppName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newAppInfo.add(appInfo);
                    }
                }
            } else {
                newAppInfo = mOriginalAppInfo;
            }
            FilterResults filterResults = new FilterResults();
            filterResults.count = newAppInfo.size();
            filterResults.values = newAppInfo;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<AppInfo> newAppInfo = (List) results.values;
            setData(false, newAppInfo);
            if (newAppInfo.size() > 0) {
                notifyDataSetChanged();
            } else {
                Toast.makeText(mAppListActivity, "暂时无此应用！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

