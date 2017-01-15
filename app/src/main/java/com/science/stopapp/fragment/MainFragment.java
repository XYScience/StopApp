package com.science.stopapp.fragment;

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
import com.science.stopapp.presenter.AppListContract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class MainFragment extends BaseFragment implements AppListContract.View {

    private AppListContract.Presenter mPresenter;
    private RecyclerView mRecyclerView;
    public DisableAppAdapter mDisableAppAdapter;
    private List<AppInfo> mListDisableApps;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void doCreateView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), manager.getOrientation()));
        mDisableAppAdapter = new DisableAppAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mDisableAppAdapter);

        mPresenter.commandSu(AppListFragment.COMMAND_APP_LIST, "-d", null, -1);
        initListener();
        initRefreshLayout(view);
    }

    private void initListener() {
        mDisableAppAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
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
    public void onRefresh() {
        super.onRefresh();
        mPresenter.commandSu(AppListFragment.COMMAND_APP_LIST, "-d", null, -1);
    }

    @Override
    public void setPresenter(AppListContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    public void getAppList(List<AppInfo> appList) {
        setRefreshing(false);
        mDisableAppAdapter.setData(false, appList);
        mListDisableApps = appList;

        mDisableAppAdapter.setCustomNoDataView(LayoutInflater.from(getActivity()).
                inflate(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent(), false));
    }

    public List<String> getListDisableApps() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < mListDisableApps.size(); i++) {
            list.add(mListDisableApps.get(i).getAppPackageName());
        }
        return list;
    }

    @Override
    public void disableOrEnableAppsSuccess(AppInfo appInfo, int position) {

    }

    @Override
    public void getRootFailed() {
        setRefreshing(false);
        mDisableAppAdapter.showLoadFailed();
        snackBarShow(((MainActivity) getActivity()).mCoordinatorLayout,
                getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
