package com.science.stopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.widget.TextView;

import com.science.stopapp.R;
import com.science.stopapp.base.BaseActivity;
import com.science.stopapp.util.CommonUtil;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/2/6
 */

public class AboutActivity extends BaseActivity {

    public static void actionStartActivity(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentLayout() {
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        return R.layout.activity_about;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar("");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        final TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange() - (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
                ViewCompat.setAlpha(toolbarTitle, percentage);
            }
        });

        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version, CommonUtil.getAppVersion(this)));
    }
}
