package com.sscience.stopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.sscience.stopapp.R;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.fragment.MainFragment;
import com.sscience.stopapp.presenter.DisableAppsPresenter;
import com.sscience.stopapp.util.CommonUtil;
import com.sscience.stopapp.widget.ScrollAwareFABBehavior;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity {

    public CoordinatorLayout mCoordinatorLayout;
    private MainFragment mMainFragment;
    private Set<AppInfo> mSelection; // 选择要操作的app(停用or移除列表)
    private FloatingActionButton mFabDisable, mFabRemove;
    private boolean isWindowFocusChangedFirst = true;

    @Override
    protected int getContentLayout() {
        setTheme(R.style.AppTheme);
        return R.layout.activity_main;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getString(R.string.app_name));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mFabDisable = (FloatingActionButton) findViewById(R.id.fab_disable);
        mFabRemove = (FloatingActionButton) findViewById(R.id.fab_remove);
        mSelection = new HashSet<>();

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
        mFabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainFragment.batchApps(true);
            }
        });
        mFabDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainFragment.batchApps(false);
            }
        });
    }

    public Set<AppInfo> getSelection() {
        return mSelection;
    }

    /**
     * 检查是否有选择了的app，以显示还是隐藏停用or移除按钮
     */
    public void checkSelection() {
        DecelerateInterpolator di = new DecelerateInterpolator();
        if (mSelection.isEmpty()) {
            setInterpolator(mFabDisable, 0, di);
            setInterpolator(mFabRemove, 0, di);
        } else {
            AccelerateInterpolator ai = new AccelerateInterpolator();
            for (AppInfo appInfo : mSelection) {
                if (appInfo.isEnable() == 1) {
                    setInterpolator(mFabDisable, 1, ai);
                    setFabMargins(mFabDisable.getHeight(), 32);
                    break;
                } else {
                    setInterpolator(mFabDisable, 0, di);
                }
            }
            setInterpolator(mFabRemove, 1, ai);
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
//            case R.id.menu_setting:
//                SettingActivity.actionStartActivity(this);
//                break;
            case R.id.menu_about:
                AboutActivity.actionStartActivity(this);
                break;
            case R.id.menu_add:
                AppListActivity.actionStartActivity(this);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isWindowFocusChangedFirst) {
            isWindowFocusChangedFirst = false;
            setFabMargins(mFabDisable.getHeight(), 32);
        }
    }

    /**
     * 在选中多个apps并且包含已停用和未停用apps时，调整停用和移除的按钮位置
     *
     * @param height
     * @param bottom
     */
    private void setFabMargins(int height, float bottom) {
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, CommonUtil.dipToPx(this, 16),
                height + CommonUtil.dipToPx(this, bottom));
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setBehavior(new ScrollAwareFABBehavior());
        mFabDisable.setLayoutParams(params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainFragment.cancelTask();
    }
}
