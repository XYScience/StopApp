package com.science.stopapp.fragment;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.science.stopapp.R;
import com.science.stopapp.activity.AppListActivity;
import com.science.stopapp.adapter.AppListAdapter;
import com.science.stopapp.base.BaseFragment;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.presenter.AppListContract;
import com.science.stopapp.presenter.AppListPresenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class AppListFragment extends BaseFragment implements AppListContract.View {

    public static final String TAB_CATEGORY = "tab_category";
    public static final String COMMAND_APP_LIST = "list packages";
    public static final String BUNDLE_APP_LIST = "app_list";
    private RecyclerView mRecyclerView;
    public AppListAdapter mAppListAdapter;
    private AppListContract.Presenter mPresenter;
    private List<AppInfo> mAppInfoList;
    private List<AppInfo> mAppInfoListNew;
    private Set<String> mCurrentDisableApps; // 保存当前页面停用的apps
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
        new AppListPresenter(getActivity(), this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mAppListAdapter = new AppListAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mAppListAdapter);

        mCurrentDisableApps = new HashSet<>();
        tabCategory = getArguments().getInt(TAB_CATEGORY);
        initListener();
    }

    private void initListener() {
        mAppListAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                mPresenter.operationApp(appInfo, position);
            }

            @Override
            public void onItemEmptyClick() {
                onLazyLoad();
            }
        });
    }

    @Override
    public void onLazyLoad() {
        if (tabCategory == 0) {
            mPresenter.commandSu(COMMAND_APP_LIST, "-3", null, -1);
        } else if (tabCategory == 1) {
            mPresenter.commandSu(COMMAND_APP_LIST, "-s", null, -1);
        }
    }

    @Override
    public void setPresenter(AppListContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void getAppList(List<AppInfo> appList) {
        mAppListAdapter.setData(false, appList);
        mAppInfoList = appList;
    }

    public List<String> getListApps() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < mAppInfoList.size(); i++) {
            list.add(mAppInfoList.get(i).getAppPackageName());
        }
        return list;
    }

    @Override
    public void disableOrEnableAppsSuccess(AppInfo appInfo, int position) {

    }

    @Override
    public void getRootFailed() {
        mAppListAdapter.showLoadFailed();
        snackBarShow(((AppListActivity) getActivity()).mCoordinatorLayout,
                getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
