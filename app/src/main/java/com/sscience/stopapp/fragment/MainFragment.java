package com.sscience.stopapp.fragment;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.MainActivity;
import com.sscience.stopapp.adapter.DisableAppAdapter;
import com.sscience.stopapp.base.BaseFragment;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.presenter.DisableAppsContract;
import com.sscience.stopapp.service.RootActionIntentService;
import com.sscience.stopapp.util.DiffCallBack;
import com.sscience.stopapp.util.SharedPreferenceUtil;
import com.sscience.stopapp.widget.DragSelectTouchListener;
import com.sscience.stopapp.widget.MoveFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private List<AppInfo> mAppList;
    private BottomSheetBehavior mSheetBehavior;

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
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity, manager.getOrientation()));
        mDisableAppAdapter = new DisableAppAdapter(mMainActivity, mRecyclerView);
        mRecyclerView.setAdapter(mDisableAppAdapter);

        mAppList = new ArrayList<>();
        initRefreshLayout(view);
        setSwipeRefreshEnable(false);
        initListener();
        mPresenter.start();

        View bottomSheet = mMainActivity.mCoordinatorLayout.findViewById(R.id.bottom_sheet);
        mSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        initBottomGridView();
    }

    private void initBottomGridView() {
        GridView gridMenu = mMainActivity.mGridMenu;
        int[] icon = {R.drawable.ic_play_circle_outline_black, R.drawable.ic_delete_black,
                R.drawable.ic_highlight_off_black, R.drawable.ic_open_in_new_black,
                R.drawable.ic_content_copy_black, R.drawable.ic_format_indent_decrease_black};
        String[] title = {getString(R.string.open_app), getString(R.string.uninstall_app),
                getString(R.string.cancel_select), getString(R.string.enable_app),
                getString(R.string.add_shortcut), getString(R.string.remove_list)};
        ArrayList<HashMap<String, Object>> gridMenuList = new ArrayList<>();
        for (int i = 0; i < icon.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("GridMenuIcon", icon[i]);
            map.put("GridMenuTitle", title[i]);
            gridMenuList.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), gridMenuList,
                R.layout.item_grid_menu, new String[]{"GridMenuIcon", "GridMenuTitle"},
                new int[]{R.id.iv_menu_icon, R.id.tv_menu_title});
        gridMenu.setAdapter(adapter);
        gridMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
                Toast.makeText(getActivity(), "item:" + item.get("GridMenuTitle"), Toast.LENGTH_SHORT).show();
            }
        });
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
                    if (selection.size() == 0) {
                        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
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

        mMainActivity.mFabRemove.setOnMoveListener(new MoveFloatingActionButton.OnMoveListener() {
            @Override
            public void onMove(boolean isMoveUp) {
                if (isMoveUp) {
                    mSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        mDragSelectTouchListener = new DragSelectTouchListener()
                .withSelectListener(new DragSelectTouchListener.OnDragSelectListener() {
                    @Override
                    public void onSelectChange(int start, int end, boolean isSelected) {
                        for (int i = start; i <= end; i++) {
                            if (isSelected) {
                                mMainActivity.getSelection().add(mAppList.get(i));
                            } else {
                                mMainActivity.getSelection().remove(mAppList.get(i));
                            }
                        }
                        mDisableAppAdapter.notifyItemRangeChanged(start, end - start + 1);
                        mMainActivity.checkSelection();
                    }

                    @Override
                    public void onItemLongClickUp() {
                        mSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        mAppList = appList;
        mDisableAppAdapter.setData(false, mAppList);
        mMainActivity.checkSelection();
        setSwipeRefreshEnable(false);
        setRefreshing(false);
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
    public void getRootSuccess(AppInfo appInfo, List<AppInfo> apps, List<AppInfo> appsNew) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(apps, appsNew), false);
        diffResult.dispatchUpdatesTo(mDisableAppAdapter);
        mAppList = appsNew;
        mDisableAppAdapter.setData(mAppList);
        mMainActivity.checkSelection();
        snackBarShow(mMainActivity.mCoordinatorLayout, appInfo == null
                ? mMainActivity.getString(R.string.enable_success) :
                mMainActivity.getString(R.string.disable_success));
        setRefreshing(false);
        if (mAppList.isEmpty()) {
            mDisableAppAdapter.showLoadFailed(R.drawable.empty, "", getResources().getString(R.string.no_disable_apps));
            snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.no_disable_apps));
        }
    }

    @Override
    public void upDateItemIfLaunch(AppInfo appInfo, int position) {
        if (appInfo != null) {
            appInfo.setEnable(1);
            mDisableAppAdapter.updateItem(position, appInfo);
            mAppList.get(position).setEnable(1);
            mMainActivity.getSelection().add(appInfo);
            mMainActivity.checkSelection();
        }
        setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 若已停用的app以Shortcut形势启动，则需更新主页app为启用
        Set<String> packageSet = new HashSet<>();
        packageSet = (Set<String>) SharedPreferenceUtil.get(getActivity()
                , RootActionIntentService.APP_SHORTCUT_PACKAGE_NAME, packageSet);
        if (packageSet != null && packageSet.size() != 0) {
            for (int i = 0; i < mAppList.size(); i++) {
                AppInfo appInfo = mAppList.get(i);
                if (packageSet.contains(appInfo.getAppPackageName()) || appInfo.isEnable() == 1) {
                    upDateItemIfLaunch(appInfo, i);
                }
            }
            SharedPreferenceUtil.remove(getActivity(), RootActionIntentService.APP_SHORTCUT_PACKAGE_NAME);
        }
    }

    @Override
    public void getRootError() {
        setRefreshing(false);
        mDisableAppAdapter.showLoadFailed();
        snackBarShow(mMainActivity.mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
    }

    public void cancelTask() {
        mPresenter.cancelTask();
    }
}
