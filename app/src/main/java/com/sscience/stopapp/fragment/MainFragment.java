package com.sscience.stopapp.fragment;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
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
import com.sscience.stopapp.widget.DragSelectTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private DragSelectTouchListener mDragSelectTouchListener;
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
        GridLayoutManager manager = new GridLayoutManager(mMainActivity, 4);
        mRecyclerView.setLayoutManager(manager);
//        recyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, manager.getOrientation()));
        mDisableAppAdapter = new DisableAppAdapter(mMainActivity, mRecyclerView);
        mRecyclerView.setAdapter(mDisableAppAdapter);

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
                Set<AppInfo> selection = mMainActivity.getSelection();
                if (selection.size() == 0) {
                    setRefreshing(true);
                    mPresenter.launchApp(appInfo, position);
                } else {
                    if (selection.contains(appInfo)) {
                        selection.remove(appInfo);
                    } else {
                        selection.add(appInfo);
                    }
                    mDisableAppAdapter.notifyItemChanged(position);
                    mMainActivity.checkSelection();

                }
            }

            @Override
            public void onItemLongClick(AppInfo appInfo, int position) {
                mDragSelectTouchListener.startDragSelection(position);
                mMainActivity.getSelection().add(appInfo);
                mDisableAppAdapter.notifyItemChanged(position);
                mMainActivity.checkSelection();
            }

            @Override
            public void onItemEmptyClick() {
                mPresenter.start();
            }
        });

        mDragSelectTouchListener = new DragSelectTouchListener()
                .withSelectListener(new DragSelectTouchListener.OnDragSelectListener() {
                    @Override
                    public void onSelectChange(int start, int end, boolean isSelected) {
                        for (int i = start; i <= end; i++) {
                            if (isSelected) {
                                mMainActivity.getSelection().add(mAppInfos.get(i));
                            } else {
                                mMainActivity.getSelection().remove(mAppInfos.get(i));
                            }
                        }
                        mDisableAppAdapter.notifyItemRangeChanged(start, end - start + 1);
                        mMainActivity.checkSelection();
                    }
                });
        mRecyclerView.addOnItemTouchListener(mDragSelectTouchListener);
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
        setSwipeRefreshEnable(false);
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
        mMainActivity.getSelection().clear();
        mPresenter.start();
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
            appInfo.setEnable(1);
            mDisableAppAdapter.updateItem(position, appInfo);
            mAppInfos.get(position).setEnable(1);
            mMainActivity.getSelection().add(appInfo);
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
