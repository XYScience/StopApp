package com.sscience.stopapp.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sscience.stopapp.R;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.model.AppsRepository;
import com.sscience.stopapp.model.GetRootCallback;
import com.sscience.stopapp.util.SharedPreferenceUtil;
import com.sscience.stopapp.util.ShortcutsManager;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class SettingActivity extends BaseActivity {

    public static final String SP_DISPLAY_SYSTEM_APPS = "sp_display_system_apps";
    public static final String SP_AUTO_DISABLE_APPS = "sp_auto_disable_apps";
    private CoordinatorLayout mCoordinatorLayout;
    private SwitchCompat mSwitchManualShortcut, mSwitchDisplaySystemApps, mSwitchAutoDisableApps;
    private TextView mTvAutoDisableSub;
    private boolean isSetDisplaySystemApps = false;

    public static void actionStartActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SettingActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getString(R.string.setting));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mSwitchManualShortcut = (SwitchCompat) findViewById(R.id.switch_manual_shortcut);
        mSwitchDisplaySystemApps = (SwitchCompat) findViewById(R.id.switch_display_system_apps);
        mSwitchAutoDisableApps = (SwitchCompat) findViewById(R.id.switch_auto_disable);
        mTvAutoDisableSub = (TextView) findViewById(R.id.tv_auto_disable_sub);

        mSwitchManualShortcut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    if (TextUtils.isEmpty((CharSequence) SharedPreferenceUtil
                            .get(SettingActivity.this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, ""))) {
                        AppListActivity.actionStartActivity(SettingActivity.this, 2, true);
                        buttonView.setChecked(false);
                    } else {
                        if (isChecked) {
                            SharedPreferenceUtil.put(SettingActivity.this, ShortcutsManager.SP_ADD_SHORTCUT_MODE
                                    , ShortcutsManager.SP_MANUAL_SHORTCUT);
                        } else {
                            SharedPreferenceUtil.put(SettingActivity.this, ShortcutsManager.SP_ADD_SHORTCUT_MODE
                                    , ShortcutsManager.SP_AUTO_SHORTCUT);
                        }
                    }
                } else {
                    snackBarShow(mCoordinatorLayout, getString(R.string.nonsupport_app_shortcut));
                    buttonView.setChecked(false);
                }
            }
        });

        boolean spDisplaySystemApps = (boolean) SharedPreferenceUtil.get(this, SP_DISPLAY_SYSTEM_APPS, true);
        mSwitchDisplaySystemApps.setChecked(spDisplaySystemApps);
        int spAutoDisableCondition = (int) SharedPreferenceUtil.get(this, SP_AUTO_DISABLE_APPS, -1);
        mSwitchAutoDisableApps.setChecked(spAutoDisableCondition != -1);
        if (spAutoDisableCondition == 0) {
            mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_immediately)));
        } else if (spAutoDisableCondition == 30) {
            mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_30s)));
        } else if (spAutoDisableCondition == 60) {
            mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_60s)));
        } else if (spAutoDisableCondition == 666) {
            mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_action_back));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String spShortcut = (String) SharedPreferenceUtil.get(this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, "");
        mSwitchManualShortcut.setChecked(ShortcutsManager.SP_MANUAL_SHORTCUT.equals(spShortcut));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isSetDisplaySystemApps) {
            Intent intent = new Intent(this, MainActivity.class);
            setResult(RESULT_OK, intent);
        }
        finish();
        return true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_change_launcher_icon:
                changeLauncherIcon();
                break;
            case R.id.ll_add_shortcut_manual:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    AppListActivity.actionStartActivity(this, 2, true);
                } else {
                    snackBarShow(mCoordinatorLayout, getString(R.string.nonsupport_app_shortcut));
                }
                break;
            case R.id.ll_display_system_apps:
                isSetDisplaySystemApps = true;
                boolean spDisplaySystemApps = (boolean) SharedPreferenceUtil.get(this, SP_DISPLAY_SYSTEM_APPS, true);
                SharedPreferenceUtil.put(this, SP_DISPLAY_SYSTEM_APPS, !spDisplaySystemApps);
                mSwitchDisplaySystemApps.setChecked(!spDisplaySystemApps);
                break;
            case R.id.ll_auto_disable:
                autoDisabledApp();
                break;
        }
    }

    private void autoDisabledApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.auto_disable_conditions);
        final String[] items = new String[]{getString(R.string.auto_disable_close)
                , getString(R.string.auto_disable_immediately)
                , getString(R.string.auto_disable_30s), getString(R.string.auto_disable_60s)
                , getString(R.string.auto_disable_action_back)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_accessibility_service));
                    mSwitchAutoDisableApps.setChecked(false);
                    SharedPreferenceUtil.put(SettingActivity.this, SP_AUTO_DISABLE_APPS, -1);
                } else if (i == 1) {
                    mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_immediately)));
                    autoDisabledAppCmd(0);
                } else if (i == 2) {
                    mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_30s)));
                    autoDisabledAppCmd(30);
                } else if (i == 3) {
                    mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_back_to_desktop, getString(R.string.auto_disable_60s)));
                    autoDisabledAppCmd(60);
                } else if (i == 4) {
                    mTvAutoDisableSub.setText(getString(R.string.automatic_disable_app_action_back));
                    autoDisabledAppCmd(666);
                }
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void autoDisabledAppCmd(final int condition) {
        mSwitchAutoDisableApps.setChecked(true);
        AppsRepository appsRepository = new AppsRepository(this);
        appsRepository.openAccessibilityServices(new GetRootCallback() {
            @Override
            public void onRoot(boolean isRoot) {
                if (isRoot) {
                    SharedPreferenceUtil.put(SettingActivity.this, SP_AUTO_DISABLE_APPS, condition);
                } else {
                    mSwitchAutoDisableApps.setChecked(false);
                    snackBarShow(mCoordinatorLayout, getString(R.string.if_want_to_use_please_grant_app_root));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            SharedPreferenceUtil.put(this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, ShortcutsManager.SP_MANUAL_SHORTCUT);
            mSwitchManualShortcut.setChecked(true);
        }
    }

    /**
     * 弹窗选择切换小黑屋桌面图标
     */
    private void changeLauncherIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select);
        View view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.select_launcher_icon, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.show();
        view.findViewById(R.id.tv_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setComponentEnabled(new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainSquareIconActivity"),
                        new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainCircleIconActivity"));
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_square).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setComponentEnabled(new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainCircleIconActivity"),
                        new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainSquareIconActivity"));
                dialog.dismiss();
            }
        });
    }

    /**
     * 切换桌面图标
     *
     * @param componentDisabledName
     * @param componentEnabledName
     */
    private void setComponentEnabled(ComponentName componentDisabledName
            , ComponentName componentEnabledName) {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(componentDisabledName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(componentEnabledName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        snackBarShow(mCoordinatorLayout, getString(R.string.launcher_icon_had_change));
        // Find launcher and kill it (restart launcher)
        // MIUI8 Android6.0.6:android.content.pm.PackageManager$NameNotFoundException
//        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
//        Intent i = new Intent(Intent.ACTION_MAIN);
//        i.addCategory(Intent.CATEGORY_HOME);
//        i.addCategory(Intent.CATEGORY_DEFAULT);
//        List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
//        for (ResolveInfo res : resolves) {
//            if (res.activityInfo != null) {
//                am.killBackgroundProcesses(res.activityInfo.packageName);
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        if (isSetDisplaySystemApps) {
            Intent intent = new Intent(this, MainActivity.class);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
