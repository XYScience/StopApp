package com.sscience.stopapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.AppListActivity;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.adapter.AppListAdapter;
import com.sscience.stopapp.base.BaseFragment;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.presenter.AppsContract;
import com.sscience.stopapp.presenter.AppsPresenter;
import com.sscience.stopapp.presenter.DisableAppsPresenter;

import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppListFragment extends BaseFragment implements AppsContract.View {

    public static final String TAB_CATEGORY = "tab_category";
    public AppListAdapter mAppListAdapter;
    private AppsContract.Presenter mPresenter;
    private int tabCategory;

    public static AppListFragment newInstance(int tabCategory) {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_CATEGORY, tabCategory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_app_list;
    }

    @Override
    protected void doCreateView(View view) {
        new AppsPresenter(getActivity(), this);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mAppListAdapter = new AppListAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAppListAdapter);

        tabCategory = getArguments().getInt(TAB_CATEGORY);
        initListener();
    }

    private void initListener() {
        mAppListAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                mPresenter.operationApps(appInfo, position);
            }

            @Override
            public void onItemEmptyClick() {
                onLazyLoad();
            }
        });
    }

    @Override
    public void onLazyLoad() {
        mPresenter.getApps(tabCategory == 0 ? DisableAppsPresenter.APP_STYLE_USER : DisableAppsPresenter.APP_STYLE_SYSTEM);
    }

    @Override
    public void setPresenter(AppsContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void getApps(List<AppInfo> appList) {
        MyLogger.e("appList.size():" + appList.size());
        mAppListAdapter.setData(false, appList);
    }

    @Override
    public void hadAddDisableApps() {
        snackBarShow(((AppListActivity) getActivity()).mCoordinatorLayout, getString(R.string.app_ready_add_disable));
    }

    @Override
    public void addDisableAppsSuccess() {
//        snackBarShow(((AppListActivity) getActivity()).mCoordinatorLayout, R.string.add_finish);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void getRootError() {
        snackBarShow(((AppListActivity) getActivity()).mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }

    @Override
    public void uninstallSuccess(String appName, int position) {
        mAppListAdapter.removeData(position);
        snackBarShow(((AppListActivity) getActivity()).mCoordinatorLayout, getString(R.string.uninstall_success, appName));
    }

    public void addDisableApps(Set<String> packageNames) {
        mPresenter.addDisableAppsSuccess(packageNames);
    }

    public List<String> getPackageNames() {
        return mPresenter.getPackageNames();
    }

    /**
     * 点击ToolBar里的选择框时，更新列表的选择
     */
    public void reFreshAppAdapter() {
        mAppListAdapter.notifyDataSetChanged();
    }
}
