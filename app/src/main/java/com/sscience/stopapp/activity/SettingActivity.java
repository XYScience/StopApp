package com.sscience.stopapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sscience.stopapp.R;
import com.sscience.stopapp.base.BaseActivity;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class SettingActivity extends BaseActivity {

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

        final TextView tvChangeLauncherIcon = (TextView) findViewById(R.id.change_launcher_icon);
        tvChangeLauncherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLauncherIcon(tvChangeLauncherIcon);
            }
        });
    }

    private void changeLauncherIcon(final TextView tvChangeLauncherIcon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select);
        View view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.select_launcher_icon, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.show();
        view.findViewById(R.id.tv_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setComponentEnabled(tvChangeLauncherIcon, new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainSquareIconActivity"),
                        new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainCircleIconActivity"));
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_square).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setComponentEnabled(tvChangeLauncherIcon, new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainCircleIconActivity"),
                        new ComponentName(SettingActivity.this, "com.sscience.stopapp.MainSquareIconActivity"));
                dialog.dismiss();
            }
        });
    }

    /**
     * 切换桌面图标
     *
     * @param tvChangeLauncherIcon
     * @param componentDisabledName
     * @param componentEnabledName
     */
    private void setComponentEnabled(TextView tvChangeLauncherIcon, ComponentName componentDisabledName, ComponentName componentEnabledName) {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(componentDisabledName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(componentEnabledName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        snackBarShow(tvChangeLauncherIcon, getString(R.string.launcher_icon_had_change));
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
