package com.sscience.stopapp.fragment;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.adapter.DisableAppAdapter;
import com.sscience.stopapp.base.BaseFragment;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.presenter.DisableAppsContract;
import com.sscience.stopapp.util.DiffCallBack;

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

        mAppInfos = new ArrayList<>();
        initRefreshLayout(view);
        setSwipeRefreshEnable(false);
        initListener();
        mPresenter.start();
    }

    private void initListener() {
        mDisableAppAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                setRefreshing(true);
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
        setRefreshing(true);
        mPresenter.batchApps(isRemove);
    }

    /**
     * 在用户应用/系统应用 界面选中添加应用后，回调界面刷新
     */
    public void reLoadDisableApps() {
        setRefreshing(true);
        mPresenter.start();
    }

    /**
     * 点击ToolBar里的选择框时，更新列表的选择
     */
    public void reFreshAppAdapter() {
        mDisableAppAdapter.notifyDataSetChanged();
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
        setRefreshing(false);
        snackBarShow(mMainActivity.mCoordinatorLayout, "完成");
        mAppInfos = appsNew;
        if (appsNew.isEmpty()) {
            mDisableAppAdapter.showLoadFailed(R.drawable.no_data, "", getResources().getString(R.string.no_disable_apps));
            snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.no_disable_apps));
        }
    }

    @Override
    public void upDateItemIfLaunch(AppInfo appInfo, int position) {
        if (appInfo != null) {
            appInfo.setEnable(true);
            mDisableAppAdapter.updateItem(position, appInfo);
            mAppInfos.get(position).setEnable(true);
            mMainActivity.getSelection().add(appInfo.getAppPackageName());
            mMainActivity.checkSelection();
        }
        setRefreshing(false);
    }

    @Override
    public void getRootError() {
        setRefreshing(false);
        mDisableAppAdapter.showLoadFailed();
        snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
