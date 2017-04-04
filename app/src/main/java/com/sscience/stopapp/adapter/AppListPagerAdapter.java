package com.sscience.stopapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.sscience.stopapp.R;
import com.sscience.stopapp.fragment.AppListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppListPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> tabNames;
    private List<AppListFragment> mFragments;

    public AppListPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragments = new ArrayList<>();
        tabNames = new ArrayList<>();
        tabNames.add(context.getString(R.string.user_apps));
        tabNames.add(context.getString(R.string.system_apps));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        AppListFragment fragment = (AppListFragment) super.instantiateItem(container, position);
        mFragments.add(fragment);
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        return AppListFragment.newInstance(position);
    }

    public AppListFragment getFragments(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return tabNames.size();
    }

    /**
     * 这个函数就是给TabLayout的Tab设定Title
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return tabNames.get(position);
    }
}