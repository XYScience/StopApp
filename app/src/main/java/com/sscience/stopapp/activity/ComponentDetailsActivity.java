package com.sscience.stopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.sscience.stopapp.R;
import com.sscience.stopapp.adapter.ComponentPagerAdapter;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public class ComponentDetailsActivity extends BaseActivity {

    public static final String EXTRA_APP_NAME = "app_name";
    public static final String EXTRA_APP_PACKAGE_NAME = "app_package_name";
    public CoordinatorLayout mCoordinatorLayout;
    public ViewPager mViewPager;

    public static void actionStartActivity(Context context, AppInfo appInfo) {
        Intent intent = new Intent(context, ComponentDetailsActivity.class);
        intent.putExtra(EXTRA_APP_NAME, appInfo.getAppName());
        intent.putExtra(EXTRA_APP_PACKAGE_NAME, appInfo.getAppPackageName());
        context.startActivity(intent);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_component_details;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getIntent().getStringExtra(EXTRA_APP_NAME));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final ComponentPagerAdapter myPagerAdapter = new ComponentPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
