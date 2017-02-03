package com.science.stopapp.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.science.stopapp.R;
import com.science.stopapp.activity.MainActivity;
import com.science.stopapp.adapter.DisableAppAdapter;
import com.science.stopapp.base.BaseFragment;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.presenter.DisableAppsContract;
import com.science.stopapp.util.DiffCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class MainFragment extends BaseFragment implements DisableAppsContract.View {

    private DisableAppsContract.Presenter mPresenter;
    private RecyclerView mRecyclerView;
    private DisableAppAdapter mDisableAppAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MainActivity mMainActivity;
    private List<AppInfo> mAppInfos;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void doCreateView(View view) {
        mMainActivity = (MainActivity) getActivity();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(mMainActivity);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, manager.getOrientation()));
        mDisableAppAdapter = new DisableAppAdapter(mMainActivity, mRecyclerView);
        mRecyclerView.setAdapter(mDisableAppAdapter);

        mSwipeRefreshLayout = initRefreshLayout(view);
        setSwipeRefreshEnable(false);
        initListener();
        mPresenter.start();

        mAppInfos = new ArrayList<>();
    }

    private void initListener() {
        mDisableAppAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                mPresenter.launchApp(appInfo, position);
            }

            @Override
            public void onItemEmptyClick() {
                mPresenter.start();
            }
        });
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mPresenter.start();
    }

    @Override
    public void setPresenter(DisableAppsContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    public DisableAppsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onLazyLoad() {
    }

    @Override
    public void getApps(List<AppInfo> appList) {
        mAppInfos = appList;
        mDisableAppAdapter.setData(false, appList);
        mMainActivity.checkSelection();
        setSwipeRefreshEnable(true);
        setRefreshing(false);

        View view = LayoutInflater.from(mMainActivity).inflate(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent(), false);
        mDisableAppAdapter.setCustomNoDataView(view);
    }

    public List<AppInfo> getAppInfos() {
        return mAppInfos;
    }

    /**
     * 点击停用列表界面右下角的按钮，批量停用or删除app
     *
     * @param isRemove
     */
    public void batchApps(boolean isRemove) {
        mSwipeRefreshLayout.setRefreshing(true);
        mPresenter.batchApps(isRemove);
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public DisableAppAdapter getDisableAppAdapter() {
        return mDisableAppAdapter;
    }

    public List<String> getListDisableApps() {
        return mPresenter.getListDisableApps();
    }

    @Override
    public void getRootSuccess(List<AppInfo> apps, List<AppInfo> appsNew) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(apps, appsNew), true);
        diffResult.dispatchUpdatesTo(mDisableAppAdapter);
        apps = appsNew;
        mDisableAppAdapter.setData(apps);
        mMainActivity.checkSelection();
        snackBarShow(mMainActivity.mCoordinatorLayout, "完成");
        setRefreshing(false);
        mAppInfos = apps;
    }

    @Override
    public void upDateItemIfLaunch(AppInfo appInfo, int position) {
        mAppInfos.get(position).setEnable(true);
        mMainActivity.getSelection().add(appInfo.getAppPackageName());
        mMainActivity.checkSelection();
        appInfo.setEnable(true);
        mDisableAppAdapter.updateItem(position, appInfo);
    }

    @Override
    public void getRootError() {
        setRefreshing(false);
        mDisableAppAdapter.showLoadFailed(R.drawable.empty, "", getResources().getString(com.science.baserecyclerviewadapter.R.string.load_failed));
        snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
