package com.sscience.stopapp.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class DisableAppAdapter extends AppAdapter {

    private MainActivity mMainActivity;

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
        viewHolder.getConvertView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mOnTouchListener != null) {
                    return mOnTouchListener.onTouch(v, event);
                }
                return false;
            }
        });

        viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongClickListener != null) {
                    mOnLongClickListener.onLongClick(info, position);
                }
                return false;
            }
        });
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

    public interface OnTouchListener {
        boolean onTouch(View v, MotionEvent event);
    }

    private OnTouchListener mOnTouchListener;

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public interface OnLongClickListener {
        void onLongClick(AppInfo appInfo, int position);
    }

    private OnLongClickListener mOnLongClickListener;

    public void setOnLongClickListener(OnLongClickListener nnLongClickListener) {
        mOnLongClickListener = nnLongClickListener;
    }
}
