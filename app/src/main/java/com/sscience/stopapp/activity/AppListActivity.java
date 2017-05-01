package com.sscience.stopapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.sscience.stopapp.R;
import com.sscience.stopapp.adapter.AppListPagerAdapter;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.util.ShortcutsManager;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppListActivity extends BaseActivity {

    public static final String EXTRA_MANUAL_SHORTCUT = "extra_manual_shortcut";
    public CoordinatorLayout mCoordinatorLayout;
    public ViewPager mViewPager;
    private boolean isAbleApp;

    public static void actionStartActivity(Activity activity, int requestCode, boolean isManualShortcut) {
        Intent intent = new Intent(activity, AppListActivity.class);
        intent.putExtra(EXTRA_MANUAL_SHORTCUT, isManualShortcut);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_app_list;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {

        if (getIntent().getBooleanExtra(EXTRA_MANUAL_SHORTCUT, false)) {
            setToolbar(getString(R.string.add_shortcut_apps));
        } else {
            setToolbar(getString(R.string.add_apps));
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final AppListPagerAdapter myPagerAdapter = new AppListPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(mViewPager);

        initListener();
    }

    private void initListener() {
    }

    public void addAppShortcut(List<AppInfo> infoList) {
        ShortcutsManager shortcutsManager = new ShortcutsManager(AppListActivity.this);
        shortcutsManager.addAppShortcut(infoList);
    }

    public void removeAppShortcut(String appPackageName) {
        ShortcutsManager shortcutsManager = new ShortcutsManager(AppListActivity.this);
        shortcutsManager.removeShortcut(appPackageName, getString(R.string.shortcut_num_limit));
    }

    public void setAbleApp(boolean ableApp) {
        isAbleApp = ableApp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isAbleApp) {
                Intent intent = new Intent(this, MainActivity.class);
                setResult(RESULT_OK, intent);
            }
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isAbleApp) {
            Intent intent = new Intent(this, MainActivity.class);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
