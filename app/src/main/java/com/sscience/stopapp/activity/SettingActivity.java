package com.sscience.stopapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

import com.sscience.stopapp.R;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.util.SharedPreferenceUtil;
import com.sscience.stopapp.util.ShortcutsManager;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class SettingActivity extends BaseActivity {

    private CoordinatorLayout mCoordinatorLayout;
    private SwitchCompat mSwitchMaualShrtcut;

    public static void actionStartActivity(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar(getString(R.string.setting));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mSwitchMaualShrtcut = (SwitchCompat) findViewById(R.id.switch_manual_shortcut);

        mSwitchMaualShrtcut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (TextUtils.isEmpty((CharSequence) SharedPreferenceUtil
                        .get(SettingActivity.this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, ""))) {
                    snackBarShow(mCoordinatorLayout, getString(R.string.dont_add_shortcut));
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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sp = (String) SharedPreferenceUtil.get(this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, "");
        mSwitchMaualShrtcut.setChecked(ShortcutsManager.SP_MANUAL_SHORTCUT.equals(sp));
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_change_launcher_icon:
                changeLauncherIcon();
                break;
            case R.id.ll_add_shortcut_manual:
                AppListActivity.actionStartActivity(this, 2, true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            SharedPreferenceUtil.put(this, ShortcutsManager.SP_ADD_SHORTCUT_MODE, ShortcutsManager.SP_MANUAL_SHORTCUT);
            mSwitchMaualShrtcut.setChecked(true);
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
}
