package com.sscience.stopapp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.sscience.stopapp.util.CommonUtil;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.sscience.stopapp.activity.AppListActivity.EXTRA_MANUAL_SHORTCUT;

/**
 * @author SScience
 * @description "添加应用"界面
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class AppListFragment extends BaseFragment implements AppsContract.View {

    public static final String TAB_CATEGORY = "tab_category";
    public AppListAdapter mAppListAdapter;
    private AppsContract.Presenter mPresenter;
    private AppListActivity mAppListActivity;

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
        mAppListActivity = ((AppListActivity) getActivity());
        new AppsPresenter(mAppListActivity, this);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mAppListActivity, layoutManager.getOrientation()));

        mAppListAdapter = new AppListAdapter(mAppListActivity, mRecyclerView);
        mRecyclerView.setAdapter(mAppListAdapter);

        int tabCategory = getArguments().getInt(TAB_CATEGORY);
        mPresenter.getApps(tabCategory == 0 ? DisableAppsPresenter.APP_STYLE_USER : DisableAppsPresenter.APP_STYLE_SYSTEM);
        initListener();
    }

    private void initListener() {
        mAppListAdapter.setOnItemClickListener(new OnItemClickListener<AppInfo>() {
            @Override
            public void onItemClick(AppInfo appInfo, int position) {
                operationApps(appInfo, position);
            }

            @Override
            public void onItemEmptyClick() {
                onLazyLoad();
            }
        });
    }

    private void operationApps(final AppInfo appInfo, final int position) {
        final boolean isAddShortcut = mAppListActivity.getIntent().getBooleanExtra(EXTRA_MANUAL_SHORTCUT, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getAppName());
        final String[] items;
        if (isAddShortcut) {
            items = new String[]{mAppListActivity.getString(R.string.add_app_app_shortcut)};
        } else {
            items = new String[]{mAppListActivity.getString(R.string.add_disable_apps)
                    , mAppListActivity.getString(R.string.uninstall_app)
                    , mAppListActivity.getString(R.string.add_desktop_shortcut)};
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (isAddShortcut) {
                        mAppListActivity.addAppShortcut(Arrays.asList(appInfo));
                    } else {
                        mPresenter.addDisableApps(appInfo);
                    }
                    dialogInterface.dismiss();
                } else if (i == 1) {
                    mPresenter.uninstallApp(appInfo, position);
                } else if (i == 2) {
                    if (CommonUtil.isLauncherActivity(mAppListActivity, appInfo.getAppPackageName())) {
                        CommonUtil.addDesktopShortcut(mAppListActivity, appInfo);
                        snackBarShow(mAppListActivity.mCoordinatorLayout, getString(R.string.add_shortcut_success));
                    } else {
                        snackBarShow(mAppListActivity.mCoordinatorLayout, getString(R.string.not_support_shortcut));
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    public void onLazyLoad() {
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
        snackBarShow(mAppListActivity.mCoordinatorLayout, getString(R.string.app_ready_add_disable));
    }

    @Override
    public void addDisableAppsSuccess() {
        Intent intent = new Intent(mAppListActivity, MainActivity.class);
        mAppListActivity.setResult(RESULT_OK, intent);
        mAppListActivity.finish();
    }

    @Override
    public void getRootError() {
        snackBarShow(mAppListActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }

    @Override
    public void uninstallSuccess(String appName, int position) {
        mAppListActivity.setUninstallSuccess();
        mAppListAdapter.removeData(position);
        snackBarShow(mAppListActivity.mCoordinatorLayout, getString(R.string.uninstall_success, appName));
    }

    public void addDisableApps(List<AppInfo> appList) {
        mPresenter.addDisableAppsSuccess(appList);
    }

    public List<AppInfo> getApps() {
        return mPresenter.getApps();
    }

    /**
     * 点击ToolBar里的选择框时，更新列表的选择
     */
    public void reFreshAppAdapter() {
        mAppListAdapter.notifyDataSetChanged();
    }
}
