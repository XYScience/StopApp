package com.science.stopapp.base;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.science.stopapp.R;
import com.science.stopapp.util.WeakHandler;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/5
 */

public abstract class BaseFragment extends Fragment implements WeakHandler.IHandler, SwipeRefreshLayout.OnRefreshListener {

    protected boolean isVisible;
    private boolean isFirst = false;
    public WeakHandler mWeakHandler;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected abstract int getContentLayout();

    protected abstract void doCreateView(View view);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getContentLayout(), container, false);
        doCreateView(view);
        isFirst = true;
        mWeakHandler = new WeakHandler(this);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWeakHandler.removeCallbacksAndMessages(null);
    }

    // weakReference start
    @Override
    public void handleMessage(Message msg) {
    }
    // weakReference end

    // refresh start
    protected SwipeRefreshLayout initRefreshLayout(View view) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.black);
        return mSwipeRefreshLayout;
    }

    protected boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    protected void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    protected void setSwipeRefreshEnable(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    protected boolean getSwipeRefreshLayout() {
        if (mSwipeRefreshLayout != null) {
            return mSwipeRefreshLayout.isEnabled();
        }
        return false;
    }

    @Override
    public void onRefresh() {
    }
    // refresh end

    // lazy load start

    /**
     * viewpager切换时调用，而且是在onCreateView之前调用
     *
     * @param isVisibleToUser true：用户可见
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInVisible();
        }
    }

    /**
     * 使用add(), hide()，show()添加fragment时
     * 刚开始add()时，当前fragment会调用该方法，但是目标fragment不会调用；
     * 所以先add()所有fragment，即先初始化控件，但不初始化数据。
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInVisible();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onVisible();
        }
    }

    private void onVisible() {
        if (isFirst && isVisible) {
            onLazyLoad();
            isFirst = false; // 控制fragment可见时，是否自动加载数据。
        }
    }

    /**
     * fragment可见时再加载数据
     */
    public abstract void onLazyLoad();

    private void onInVisible() {

    }
    // lazy load end

    // tip start
    public void snackBarShow(View view, int contentRes) {
        snackBarShow(view, getString(contentRes));
    }

    public void snackBarShow(View view, String content) {
        Snackbar.make(view, content, Snackbar.LENGTH_LONG).show();
    }
    // tip end

}
