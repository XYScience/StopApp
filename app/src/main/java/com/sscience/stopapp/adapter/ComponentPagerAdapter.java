package com.sscience.stopapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.sscience.stopapp.R;
import com.sscience.stopapp.fragment.ComponentDetailsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class ComponentPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> tabNames;
    private List<ComponentDetailsFragment> mFragments;

    public ComponentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragments = new ArrayList<>();
        tabNames = new ArrayList<>();
        tabNames.add(context.getString(R.string.component_activity));
        tabNames.add(context.getString(R.string.component_service));
        tabNames.add(context.getString(R.string.component_receiver));
        tabNames.add(context.getString(R.string.component_provider));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ComponentDetailsFragment fragment = (ComponentDetailsFragment) super.instantiateItem(container, position);
        mFragments.add(fragment);
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        return ComponentDetailsFragment.newInstance(position);
    }

    public ComponentDetailsFragment getFragments(int position) {
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