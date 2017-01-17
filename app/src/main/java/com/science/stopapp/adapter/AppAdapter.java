package com.science.stopapp.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.science.baserecyclerviewadapter.base.BaseCommonAdapter;
import com.science.baserecyclerviewadapter.base.ViewHolder;
import com.science.stopapp.R;
import com.science.stopapp.bean.AppInfo;

import java.util.List;

import static com.science.stopapp.util.DiffCallBack.BUNDLE_PAYLOAD;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class AppAdapter extends BaseCommonAdapter<List<AppInfo>> {

    private Activity mActivity;
    private ColorMatrixColorFilter mColorFilterGrey, mColorFilterNormal; // 设置图片灰度
    private Resources mResources;

    public AppAdapter(Activity activity, RecyclerView recyclerView) {
        super(activity, recyclerView);
        mActivity = activity;
        mResources = mActivity.getResources();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0); // 参数大于1将增加饱和度，0～1之间会减少饱和度。0值将产生一幅灰度图像。
        mColorFilterGrey = new ColorMatrixColorFilter(matrix);
        ColorMatrix matrixNormal = new ColorMatrix();
        matrixNormal.setSaturation(1); // 参数大于1将增加饱和度，0～1之间会减少饱和度。0值将产生一幅灰度图像。
        mColorFilterNormal = new ColorMatrixColorFilter(matrixNormal);
    }

    @Override
    public int getItemLayoutId() {
        return R.layout.item_app;
    }

    @Override
    public void convertCommon(final ViewHolder viewHolder, List<AppInfo> appInfo, final int position) {
        final AppInfo info = appInfo.get(position);
        viewHolder.setImageDrawable(R.id.iv_app_icon, info.getAppIcon());
        viewHolder.setText(R.id.tv_app_name, info.getAppName());
        viewHolder.setText(R.id.tv_app_package_name, info.getAppPackageName());

        ((TextView) viewHolder.getView(R.id.tv_app_name)).setTextColor(info.isEnable()
                ? mResources.getColor(R.color.textPrimary) : mResources.getColor(R.color.translucentBg));
        ((TextView) viewHolder.getView(R.id.tv_app_package_name)).setTextColor(info.isEnable()
                ? mResources.getColor(R.color.textSecondary) : mResources.getColor(R.color.translucentBg));
        ((ImageView) viewHolder.getView(R.id.iv_app_icon)).getDrawable().setColorFilter(info.isEnable()
                ? mColorFilterNormal : mColorFilterGrey);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position, List payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(viewHolder, position);
        } else {
            ViewHolder holder = (ViewHolder) viewHolder;
            Bundle payload = (Bundle) payloads.get(0);
            boolean isEnable = payload.getBoolean(BUNDLE_PAYLOAD);
            ((AppCompatCheckBox) holder.getView(R.id.cb_select_apps)).setChecked(false);
            ((TextView) holder.getView(R.id.tv_app_name)).setTextColor(isEnable
                    ? mResources.getColor(R.color.textPrimary) : mResources.getColor(R.color.translucentBg));
            ((TextView) holder.getView(R.id.tv_app_package_name)).setTextColor(isEnable
                    ? mResources.getColor(R.color.textSecondary) : mResources.getColor(R.color.translucentBg));
            ((ImageView) holder.getView(R.id.iv_app_icon)).getDrawable().setColorFilter(isEnable
                    ? mColorFilterNormal : mColorFilterGrey);
        }
    }
}
