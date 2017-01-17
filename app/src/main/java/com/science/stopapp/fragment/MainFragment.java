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
import com.science.myloggerlibrary.MyLogger;
import com.science.stopapp.R;
import com.science.stopapp.activity.MainActivity;
import com.science.stopapp.adapter.DisableAppAdapter;
import com.science.stopapp.base.BaseFragment;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.presenter.AppListContract;
import com.science.stopapp.presenter.AppListPresenter;
import com.science.stopapp.util.DiffCallBack;
import com.science.stopapp.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class MainFragment extends BaseFragment implements AppListContract.View {

    public static final String DISABLE_APPS = "disable_apps";
    private AppListContract.Presenter mPresenter;
    private RecyclerView mRecyclerView;
    private DisableAppAdapter mDisableAppAdapter;
    private List<AppInfo> mListDisableApps;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<AppInfo> mAppInfoListNew;
    private Set<String> mDisableApps;
    private MainActivity mMainActivity;

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

        mListDisableApps = new ArrayList<>();
        mDisableApps = new HashSet<>();
        getDisableAppsCmd();
        initListener();
        mSwipeRefreshLayout = initRefreshLayout(view);
        setSwipeRefreshEnable(false);
    }

    public void getDisableAppsCmd() {
        mListDisableApps.clear();
        mDisableApps = (Set<String>) SharedPreferenceUtil.get(mMainActivity, DISABLE_APPS, mDisableApps);
        if (mDisableApps.isEmpty()) {
            mPresenter.commandSu(AppListFragment.COMMAND_APP_LIST, "-d", null, -1);
        } else {
            mPresenter.commandSu(AppListFragment.COMMAND_APP_LIST, "", null, -1);
        }
    }

    private void initListener() {
        mDisableAppAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                mPresenter.disableApp(appInfo, position);
            }

            @Override
            public void onItemEmptyClick() {
                getDisableAppsCmd();
            }
        });
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        getDisableAppsCmd();
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
        setSwipeRefreshEnable(true);
        setRefreshing(false);
        if (mDisableApps.isEmpty()) {
            MyLogger.e("111111");
            for (AppInfo appInfo : appList) {
                mDisableApps.add(appInfo.getAppPackageName());
            }
            SharedPreferenceUtil.put(mMainActivity, DISABLE_APPS, mDisableApps);
            mListDisableApps = appList;
        } else {
            for (AppInfo appInfo : appList) {
                String packageName = appInfo.getAppPackageName();
                if (mDisableApps.contains(packageName)) {
                    mListDisableApps.add(appInfo);
                    MyLogger.e("222222:" + packageName);
                    if (appInfo.isEnable()) {
                        mMainActivity.getSelection().add(packageName);
                    }
                }
            }
        }
        MyLogger.e("333333:" + mListDisableApps.size());
        mDisableAppAdapter.setData(false, mListDisableApps);
        mMainActivity.checkSelection();

        View view = LayoutInflater.from(mMainActivity).inflate(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent(), false);
        mDisableAppAdapter.setCustomNoDataView(view);
    }

    public void diffAppsList(boolean isRemove) {
        isFirstCmd = true;
        try {
            mAppInfoListNew = new ArrayList<>();
            for (AppInfo info : mListDisableApps) {
                mAppInfoListNew.add(info.clone());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        boolean isNotCmd = true;
        for (int i = 0; i < mAppInfoListNew.size(); i++) {
            String packageName = mAppInfoListNew.get(i).getAppPackageName();
            if (mMainActivity.getSelection().contains(packageName)) {
                if (isRemove) {
                    if (!mAppInfoListNew.get(i).isEnable()) {
                        mPresenter.commandSu(AppListPresenter.COMMAND_ENABLE, packageName, mAppInfoListNew.get(i), i);
                        isNotCmd = false;
                    }
                    mAppInfoListNew.remove(i);
                    mMainActivity.getSelection().remove(packageName);
                    mDisableApps.remove(packageName);
                } else {
                    if (mAppInfoListNew.get(i).isEnable()) {
                        mPresenter.commandSu(AppListPresenter.COMMAND_DISABLE, packageName, mAppInfoListNew.get(i), i);
                    }
                    mAppInfoListNew.get(i).setEnable(!mAppInfoListNew.get(i).isEnable());
                }
            }
        }
        if (isRemove) {
            SharedPreferenceUtil.clear(mMainActivity);
            SharedPreferenceUtil.put(mMainActivity, DISABLE_APPS, mDisableApps);
            if (isNotCmd) {
                disableOrEnableAppsSuccess(null, -1);
            }
        }
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public DisableAppAdapter getDisableAppAdapter() {
        return mDisableAppAdapter;
    }

    public List<String> getListDisableApps() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < mListDisableApps.size(); i++) {
            list.add(mListDisableApps.get(i).getAppPackageName());
        }
        MyLogger.e("444444:" + list.size());
        return list;
    }

    private boolean isFirstCmd = true;

    @Override
    public void disableOrEnableAppsSuccess(AppInfo appInfo, int position) {
        if (isFirstCmd) {
            isFirstCmd = false;
            snackBarShow(mMainActivity.mCoordinatorLayout, "完成");

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(mListDisableApps, mAppInfoListNew), true);
            diffResult.dispatchUpdatesTo(mDisableAppAdapter);
            mListDisableApps = mAppInfoListNew;
            mDisableAppAdapter.setData(mListDisableApps);
            mMainActivity.checkSelection();
        }
    }

    @Override
    public void getRootFailed() {
        setRefreshing(false);
        mDisableAppAdapter.showLoadFailed(R.drawable.empty, "", getResources().getString(com.science.baserecyclerviewadapter.R.string.load_failed));
        snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }
}
