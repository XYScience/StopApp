package com.sscience.stopapp.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.sscience.stopapp.fragment.AppListFragment;

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

    public CoordinatorLayout mCoordinatorLayout;
    public ViewPager mViewPager;
    private AppCompatCheckBox mCbSelectAllApps;
    private List<Set<String>> mSelection;
    private FloatingActionButton mFabConfirm;

    public static void actionStartActivity(Activity activity) {
        Intent intent = new Intent(activity, AppListActivity.class);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_app_list;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getString(R.string.add_apps));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mCbSelectAllApps = (AppCompatCheckBox) findViewById(R.id.cb_select_all_apps);
        mFabConfirm = (FloatingActionButton) findViewById(R.id.fab);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(mViewPager);

        mSelection = new ArrayList<>();
        mSelection.add(new HashSet<String>());
        mSelection.add(new HashSet<String>());

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
                List<String> listApps = appListFragment.getPackageNames();
                if (listApps.size() != getSelection(0).size()) {
                    getSelection(0).addAll(listApps);
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
                Set<String> appInfos = new HashSet<>();
                appInfos.addAll(getSelection(0));
                appInfos.addAll(getSelection(1));
                ((MyPagerAdapter) mViewPager.getAdapter()).
                        getFragments(mViewPager.getCurrentItem()).addDisableApps(appInfos);
            }
        });
    }

    public Set<String> getSelection(int page) {
        return mSelection.get(page);
    }

    public Set<String> getSelection() {
        return getSelection(mViewPager.getCurrentItem());
    }

    public void checkSelection() {
        DecelerateInterpolator di = new DecelerateInterpolator();
        AccelerateInterpolator ai = new AccelerateInterpolator();
        if (mViewPager.getCurrentItem() == 0) {
            if (getSelection(0).size() == 0) {
                setInterpolator(mCbSelectAllApps, 0, di);
                setInterpolator(mFabConfirm, 0, di);
            } else {
                setInterpolator(mCbSelectAllApps, 1, ai);
                setInterpolator(mFabConfirm, 1, ai);
            }
        } else {
            setInterpolator(mCbSelectAllApps, 0, di);
        }
        if (getSelection(0).size() == 0 && (getSelection(1).size() == 0)) {
            setInterpolator(mFabConfirm, 0, di);
        } else {
            setInterpolator(mFabConfirm, 1, ai);
        }
    }
}
