package com.science.stopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;

import com.science.stopapp.R;
import com.science.stopapp.base.BaseActivity;
import com.science.stopapp.fragment.MainFragment;
import com.science.stopapp.presenter.AppListPresenter;

import java.util.HashSet;
import java.util.Set;

import static com.science.stopapp.R.id.fab;

public class MainActivity extends BaseActivity {

    public CoordinatorLayout mCoordinatorLayout;
    private MainFragment mMainFragment;
    private Set<String> mSelection;
    private AppCompatCheckBox mChSelectApps;
    private FloatingActionButton mFabDisable, mFabRemove;

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
        mFabDisable.setAlpha(0f);
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

        mFabDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public Set<String> getSelection() {
        return mSelection;
    }

    public void checkSelection() {
        if (getSelection().isEmpty()) {
            mChSelectApps.setClickable(false);
            mFabDisable.setClickable(false);
            ViewCompat.animate(mChSelectApps).alpha(0).setInterpolator(new DecelerateInterpolator());
            ViewCompat.animate(mFabDisable).alpha(0).setInterpolator(new DecelerateInterpolator());
        } else {
            mChSelectApps.setClickable(true);
            mFabDisable.setClickable(true);
            ViewCompat.animate(mChSelectApps).alpha(1).setInterpolator(new AccelerateInterpolator());
            ViewCompat.animate(mFabDisable).alpha(1).setInterpolator(new AccelerateInterpolator());
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
                mMainFragment.mDisableAppAdapter.notifyDataSetChanged();
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
        mMainFragment.getDisableApps();
    }
}
