package com.sscience.stopapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;

import com.sscience.stopapp.R;
import com.sscience.stopapp.adapter.MyPagerAdapter;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.fragment.AppListFragment;
import com.sscience.stopapp.util.ShortcutsManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private AppCompatCheckBox mCbSelectAllApps;
    private List<Set<AppInfo>> mSelection;
    private FloatingActionButton mFabConfirm;
    private DecelerateInterpolator mDecelerateInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator;
    private boolean isUninstallSuccess;

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
        mCbSelectAllApps = (AppCompatCheckBox) findViewById(R.id.cb_select_all_apps);
        mFabConfirm = (FloatingActionButton) findViewById(R.id.fab);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(mViewPager);

        mDecelerateInterpolator = new DecelerateInterpolator();
        mAccelerateInterpolator = new AccelerateInterpolator();
        mSelection = new ArrayList<>();
        mSelection.add(new HashSet<AppInfo>());
        mSelection.add(new HashSet<AppInfo>());

        initListener();
    }

    private void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                checkSelection();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mCbSelectAllApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppListFragment appListFragment = ((MyPagerAdapter) mViewPager.getAdapter()).getFragments(0);
                List<AppInfo> appList = appListFragment.getApps();
                if (appList.size() != getSelection(0).size()) {
                    getSelection(0).addAll(appList);
                    buttonView.setChecked(true);
                } else {
                    getSelection(0).clear();
                }
                appListFragment.reFreshAppAdapter();
                checkSelection();
            }
        });

        mFabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<AppInfo> appList = getSelection(0);
                appList.addAll(getSelection(1));
                if (getIntent().getBooleanExtra(EXTRA_MANUAL_SHORTCUT, false)) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        ShortcutManager manager = getSystemService(ShortcutManager.class);
                        if (appList.size() > manager.getMaxShortcutCountPerActivity() - 1) {
                            snackBarShow(mCoordinatorLayout, getString(R.string.shortcut_num_limit));
                        } else {
                            addAppShortcut(new ArrayList<>(appList));
                        }
                    }
                } else {
                    ((MyPagerAdapter) mViewPager.getAdapter()).getFragments(mViewPager.getCurrentItem())
                            .addDisableApps(new ArrayList<>(appList));
                }
            }
        });
    }

    public void addAppShortcut(List<AppInfo> infoList) {
        ShortcutsManager shortcutsManager = new ShortcutsManager(AppListActivity.this);
        shortcutsManager.addAppShortcut(infoList);
        Intent intent = new Intent(AppListActivity.this, SettingActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    public Set<AppInfo> getSelection(int page) {
        return mSelection.get(page);
    }

    public Set<AppInfo> getSelection() {
        return getSelection(mViewPager.getCurrentItem());
    }

    public void checkSelection() {
        if (mViewPager.getCurrentItem() == 0) {
            if (getSelection(0).size() == 0) {
                setInterpolator(mCbSelectAllApps, 0, mDecelerateInterpolator);
                setInterpolator(mFabConfirm, 0, mDecelerateInterpolator);
            } else {
                setInterpolator(mCbSelectAllApps, 1, mAccelerateInterpolator);
                setInterpolator(mFabConfirm, 1, mAccelerateInterpolator);
            }
        } else {
            setInterpolator(mCbSelectAllApps, 0, mDecelerateInterpolator);
        }
        if (getSelection(0).size() == 0 && (getSelection(1).size() == 0)) {
            setInterpolator(mFabConfirm, 0, mDecelerateInterpolator);
        } else {
            setInterpolator(mFabConfirm, 1, mAccelerateInterpolator);
        }
    }

    public void setUninstallSuccess() {
        isUninstallSuccess = true;
    }

    @Override
    public void onBackPressed() {
        if (isUninstallSuccess) {
            Intent intent = new Intent(this, MainActivity.class);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
