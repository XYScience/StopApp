package com.science.stopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;

import com.science.stopapp.R;
import com.science.stopapp.base.BaseActivity;
import com.science.stopapp.fragment.MainFragment;
import com.science.stopapp.presenter.AppListPresenter;
import com.science.stopapp.util.CommonUtil;
import com.science.stopapp.util.ScrollAwareFABBehavior;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity {

    public CoordinatorLayout mCoordinatorLayout;
    private MainFragment mMainFragment;
    private Set<String> mSelection;
    private AppCompatCheckBox mChSelectApps;
    private FloatingActionButton mFabDisable, mFabRemove;
    private boolean isFirst = true;

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
        new AppListPresenter(MainActivity.this, mMainFragment);

        initListener();
    }

    private void initListener() {
        mFabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainFragment.diffAppsList(true);
            }
        });
        mFabDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainFragment.diffAppsList(false);
            }
        });
    }

    public Set<String> getSelection() {
        return mSelection;
    }

    public void checkSelection() {
        if (getSelection().isEmpty()) {
            DecelerateInterpolator interpolator = new DecelerateInterpolator();
            setInterpolator(mChSelectApps, 0, interpolator);
            setInterpolator(mFabDisable, 0, interpolator);
            setInterpolator(mFabRemove, 0, interpolator);
        } else {
            AccelerateInterpolator interpolator = new AccelerateInterpolator();
            setInterpolator(mChSelectApps, 1, interpolator);
            setInterpolator(mFabDisable, 1, interpolator);
            setInterpolator(mFabRemove, 1, interpolator);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mChSelectApps = (AppCompatCheckBox) menu.findItem(R.id.menu_select_all).getActionView();
        mChSelectApps.setAlpha(0);
        mChSelectApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mMainFragment.getListDisableApps().size() != getSelection().size()) {
                    getSelection().addAll(mMainFragment.getListDisableApps());
                    buttonView.setChecked(true);
                } else {
                    getSelection().clear();
                }
                mMainFragment.getDisableAppAdapter().notifyDataSetChanged();
                checkSelection();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_setting:
                SettingActivity.actionStartActivity(this);
                break;
            case R.id.menu_about:
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
            mMainFragment.getRefreshLayout().setRefreshing(true);
            mMainFragment.getDisableAppsCmd();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFirst) {
            isFirst = false;
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, CommonUtil.dipToPx(this, 16), mFabRemove.getHeight() + CommonUtil.dipToPx(this, 32));
            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.setBehavior(new ScrollAwareFABBehavior());
            mFabRemove.setLayoutParams(params);
        }
    }
}
