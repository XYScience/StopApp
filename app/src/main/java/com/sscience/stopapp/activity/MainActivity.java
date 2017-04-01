package com.sscience.stopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sscience.stopapp.R;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.fragment.MainFragment;
import com.sscience.stopapp.presenter.DisableAppsPresenter;
import com.sscience.stopapp.util.CommonUtil;
import com.sscience.stopapp.widget.MoveFloatingActionButton;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public CoordinatorLayout mCoordinatorLayout;
    private MainFragment mMainFragment;
    private Set<AppInfo> mSelection; // 选择要操作的app(停用or移除列表)
    public MoveFloatingActionButton mFabDisable;
    public LinearLayout mLlEnableApp, mLlUninstallApp, mLlAddShortcut, mLlRemoveList, mLlCancelSelect;
    private long exitTime = 0;
    public String mRootStr;

    @Override
    protected int getContentLayout() {
        setTheme(R.style.AppTheme);
        return R.layout.activity_main;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getString(R.string.app_name));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mFabDisable = (MoveFloatingActionButton) findViewById(R.id.fab_disable);
        mLlEnableApp = (LinearLayout) findViewById(R.id.ll_enable_app);
        mLlUninstallApp = (LinearLayout) findViewById(R.id.ll_uninstall_app);
        mLlAddShortcut = (LinearLayout) findViewById(R.id.ll_add_shortcut);
        mLlRemoveList = (LinearLayout) findViewById(R.id.ll_remove_list);
        mLlCancelSelect = (LinearLayout) findViewById(R.id.ll_cancel_select);
        mSelection = new HashSet<>();
        mRootStr = getString(R.string.operate_success);

        mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mMainFragment == null) {
            // Create the fragment
            mMainFragment = MainFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame, mMainFragment);
            transaction.commit();
        }

        // Create the presenter
        new DisableAppsPresenter(MainActivity.this, mMainFragment);

        initListener();
    }

    private void initListener() {
        mFabDisable.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_enable_app:
                mRootStr = getString(R.string.enable_app_success);
                mMainFragment.batchApps(1);
                break;
            case R.id.ll_uninstall_app:
                mMainFragment.uninstallApp();
                break;
            case R.id.ll_add_shortcut:
                for (AppInfo appInfo : mSelection) {
                    CommonUtil.addDesktopShortcut(this, appInfo);
                }
                mMainFragment.mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                snackBarShow(mCoordinatorLayout, getString(R.string.add_shortcut_success));
                break;
            case R.id.ll_remove_list:
                mRootStr = getString(R.string.remove_list_success);
                mMainFragment.batchApps(2);
                break;
            case R.id.ll_cancel_select:
                mSelection.clear();
                mMainFragment.mDisableAppAdapter.notifyDataSetChanged();
                checkSelection();
                break;
            case R.id.fab_disable:
                mRootStr = getString(R.string.disable_success);
                mMainFragment.batchApps(0);
                break;

        }
    }

    public Set<AppInfo> getSelection() {
        return mSelection;
    }

    /**
     * 检查是否有选择了的app
     */
    public void checkSelection() {
        if (mSelection.isEmpty()) {
            mMainFragment.mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (mSelection.size() == 1) {
                mLlUninstallApp.setVisibility(View.VISIBLE);
            } else {
                mLlUninstallApp.setVisibility(View.GONE);
            }
            for (AppInfo appInfo : mSelection) {
                if (appInfo.isEnable() == 0) {
                    mLlEnableApp.setVisibility(View.VISIBLE);
                    break;
                } else {
                    mLlEnableApp.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_setting:
                SettingActivity.actionStartActivity(this, 1);
                break;
            case R.id.menu_about:
                AboutActivity.actionStartActivity(this);
                break;
            case R.id.menu_add:
                AppListActivity.actionStartActivity(this, 1, false);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 在App列表界面选择要停用的apps返回后更新主界面
            mMainFragment.reLoadDisableApps();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainFragment.cancelTask();
    }

    @Override
    public void onBackPressed() {
        if (mMainFragment.mSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mMainFragment.mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Toast.makeText(this, getString(R.string.quit_again), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }
}
