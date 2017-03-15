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
    private Set<AppInfo> mSelection;
    private FloatingActionButton mFabDisable, mFabRemove;
    private DecelerateInterpolator mDecelerateInterpolator ;
    private AccelerateInterpolator mAccelerateInterpolator ;
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
        mDecelerateInterpolator = new DecelerateInterpolator();
        mAccelerateInterpolator = new AccelerateInterpolator();

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

    public void checkSelection() {
        if (mSelection.isEmpty()) {
            setInterpolator(mFabDisable, 0, mDecelerateInterpolator);
            setInterpolator(mFabRemove, 0, mDecelerateInterpolator);
        } else {
            for (AppInfo appInfo : mMainFragment.getAppInfos()) {
                if (mSelection.contains(appInfo)) {
                    if (appInfo.isEnable() == 1) {
                        setInterpolator(mFabDisable, 1, mAccelerateInterpolator);
                        setFabMargins(mFabRemove.getHeight(), 32);
                        break;
                    } else {
                        setInterpolator(mFabDisable, 0, mDecelerateInterpolator);
                        setFabMargins(0, 16);
                    }
                }
            }
            setInterpolator(mFabRemove, 1, mAccelerateInterpolator);
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
            mMainFragment.reLoadDisableApps();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isWindowFocusChangedFirst) {
            isWindowFocusChangedFirst = false;
            setFabMargins(mFabRemove.getHeight(), 32);
        }
    }

    private void setFabMargins(int height, float bottom) {
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, CommonUtil.dipToPx(this, 16),
                height + CommonUtil.dipToPx(this, bottom));
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setBehavior(new ScrollAwareFABBehavior());
        mFabRemove.setLayoutParams(params);
    }
}
