package com.sscience.stopapp.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class DisableAppAdapter extends AppAdapter implements Filterable {

    private MainActivity mMainActivity;
    private AppFilter mAppFilter;
    public List<AppInfo> mOriginalAppInfo;

    public DisableAppAdapter(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_disable;
    }

    @Override
    public void convertCommon(ViewHolder viewHolder, List<AppInfo> appInfo, final int position) {
        super.convertCommon(viewHolder, appInfo, position);
        final AppInfo info = appInfo.get(position);
        TextView tvAppName = viewHolder.getView(R.id.tv_app_name);
        ImageView ivAppIcon = viewHolder.getView(R.id.iv_app_icon);
        if (mMainActivity.getSelection().contains(info)) {
            tvAppName.setTextColor(mMainActivity.getResources().getColor(R.color.textPrepareColor));
            ivAppIcon.getDrawable().setColorFilter(mColorFilter50);
        } else {
            tvAppName.setTextColor(info.isEnable() == 1
                    ? mMainActivity.getResources().getColor(R.color.textPrimary)
                    : mMainActivity.getResources().getColor(R.color.translucentBg));
            ivAppIcon.getDrawable().setColorFilter(info.isEnable() == 1
                    ? mColorFilterNormal : mColorFilterGrey);
        }
//        AppCompatCheckBox cb = viewHolder.getView(R.id.cb_select_apps);
//        cb.setOnCheckedChangeListener(null); // CheckBox在执行setChecked时会触发setOnCheckedChangeListener
//        cb.setChecked(mMainActivity.getSelection().contains(info.getAppPackageName()));
//        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Set<String> selections = mMainActivity.getSelection();
//                if (isChecked) {
//                    selections.add(info.getAppPackageName());
//                } else {
//                    selections.remove(info.getAppPackageName());
//                }
//                mMainActivity.checkSelection();
//            }
//        });
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
                Toast.makeText(mMainActivity, R.string.search_no_app, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
