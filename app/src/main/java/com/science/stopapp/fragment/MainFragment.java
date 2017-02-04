package com.science.stopapp.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(mMainActivity);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, manager.getOrientation()));
        mDisableAppAdapter = new DisableAppAdapter(mMainActivity, recyclerView);
        recyclerView.setAdapter(mDisableAppAdapter);

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

    /**
     * 在用户应用/系统应用 界面选中添加应用后，回调界面刷新
     */
    public void refreshDisableApps() {
        mSwipeRefreshLayout.setRefreshing(true);
        mPresenter.start();
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

    public List<String> getDisableAppPackageNames() {
        return mPresenter.getDisableAppPackageNames();
    }

    @Override
    public void getRootSuccess(List<AppInfo> apps, List<AppInfo> appsNew) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(apps, appsNew), false);
        diffResult.dispatchUpdatesTo(mDisableAppAdapter);
        mDisableAppAdapter.setData(appsNew);
        mMainActivity.checkSelection();
        snackBarShow(mMainActivity.mCoordinatorLayout, "完成");
        mAppInfos = appsNew;
        setRefreshing(false);
        if (appsNew.isEmpty()) {
            mDisableAppAdapter.showLoadFailed(R.drawable.no_data, "", getResources().getString(R.string.no_disable_apps));
            snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.no_disable_apps));
        }
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
        mDisableAppAdapter.showLoadFailed();
        snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
