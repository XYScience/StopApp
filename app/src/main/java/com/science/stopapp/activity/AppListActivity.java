package com.science.stopapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;

import com.science.stopapp.R;
import com.science.stopapp.adapter.MyPagerAdapter;
import com.science.stopapp.base.BaseActivity;

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
        setToolbar(getString(R.string.apps_list));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mCbSelectAllApps = (AppCompatCheckBox) findViewById(R.id.cb_select_all_apps);
        mFabConfirm = (FloatingActionButton) findViewById(R.id.fab);
        mFabConfirm.setAlpha(0f);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(mViewPager);

        mSelection = new ArrayList<>();
        mSelection.add(new HashSet<String>());
        mSelection.add(new HashSet<String>());

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
                List<String> listApps = myPagerAdapter.getFragments(mViewPager.getCurrentItem()).getPackageNameList();
                if (listApps.size() != getSelection().size()) {
                    getSelection().addAll(listApps);
                    buttonView.setChecked(true);
                } else {
                    getSelection().clear();
                }
                myPagerAdapter.getFragments(mViewPager.getCurrentItem()).mAppListAdapter.notifyDataSetChanged();
                checkSelection();
            }
        });

        mFabConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public Set<String> getSelection() {
        return mSelection.get(mViewPager.getCurrentItem());
    }

    public void checkSelection() {
        if (getSelection().size() == 0) {
            mCbSelectAllApps.setClickable(false);
            mFabConfirm.setClickable(false);
            ViewCompat.animate(mCbSelectAllApps).alpha(0).setInterpolator(new DecelerateInterpolator());
            ViewCompat.animate(mFabConfirm).alpha(0).setInterpolator(new DecelerateInterpolator());
        } else {
            mCbSelectAllApps.setClickable(true);
            mFabConfirm.setClickable(true);
            ViewCompat.animate(mCbSelectAllApps).alpha(1).setInterpolator(new AccelerateInterpolator());
            ViewCompat.animate(mFabConfirm).alpha(1).setInterpolator(new AccelerateInterpolator());
        }
    }
}
