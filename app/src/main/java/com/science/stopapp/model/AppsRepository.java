package com.science.stopapp.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.science.myloggerlibrary.MyLogger;
import com.science.stopapp.bean.AppInfo;
import com.science.stopapp.presenter.DisableAppsPresenter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/27
 */

public class AppsRepository {

    private static final int APPS_FLAG_ALL = 0;
    private static final int APPS_FLAG_SYSTEM = 1;
    private static final int APPS_FLAG_USER = 2;
    private Context mContext;

    public AppsRepository(Context context) {
        mContext = context;
    }

    public interface GetAppsCallback {
        void onAppsLoaded(List<AppInfo> apps);
    }

    public interface GetAppsCmdCallback {
        void onRootAppsLoaded(List<AppInfo> apps);

        void onRootError();

        void onRootSuccess();
    }

    public void getApps(final int appFlag, final GetAppsCallback callback) {
        new AsyncTask<Boolean, Boolean, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Boolean... params) {
                ArrayList<AppInfo> appListSystem = new ArrayList<>();
                ArrayList<AppInfo> appListUser = new ArrayList<>();
                PackageManager packageManager = mContext.getPackageManager();
                List<PackageInfo> packages = packageManager.getInstalledPackages(0);
                for (PackageInfo packageInfo : packages) {
                    AppInfo appInfo = new AppInfo();
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    if (!applicationInfo.packageName.equals(mContext.getPackageName())) {
                        appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
                        appInfo.setAppPackageName(applicationInfo.packageName);
                        appInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                        appInfo.setEnable(applicationInfo.enabled);
                        appInfo.setSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            appListSystem.add(appInfo);
                        } else {
                            appListUser.add(appInfo);
                        }
                    }
                }
                if (appFlag == APPS_FLAG_ALL) {
                    appListSystem.addAll(appListUser);
                    return appListSystem;
                } else if (appFlag == APPS_FLAG_SYSTEM) {
                    return appListSystem;
                } else {
                    return appListUser;
                }
            }

            @Override
            protected void onPostExecute(List<AppInfo> appList) {
                if (!appList.isEmpty()) {
                    callback.onAppsLoaded(appList);
                }
            }
        }.execute();
    }

    public void commandSu(final String cmd, final GetAppsCmdCallback callback) {
        new AsyncTask<Boolean, Object, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Boolean... params) {
                DataOutputStream dataOutputStream = null;
                BufferedReader errorStream = null;
                List<AppInfo> appList = null;
                PackageManager packageManager = mContext.getPackageManager();
                try {
                    // 申请su权限
                    Process process = Runtime.getRuntime().exec("su");
                    dataOutputStream = new DataOutputStream(process.getOutputStream());
                    // 执行pm install命令
                    String command = "pm " + cmd + "\n";
                    dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.flush();
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                    int i = process.waitFor();
                    if (i == 0) { // 正确获取root权限
                        appList = new ArrayList<>();
                        if (cmd.contains(DisableAppsPresenter.COMMAND_APP_LIST)) { // 获取应用
                            String msg = "";
                            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            while ((msg = br.readLine()) != null) {
                                AppInfo appInfo = new AppInfo();
                                ApplicationInfo applicationInfo = packageManager.getPackageInfo(msg.replace("package:", ""), 0).applicationInfo;
                                appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
                                appInfo.setAppPackageName(applicationInfo.packageName);
                                appInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                                appInfo.setEnable(applicationInfo.enabled);
                                appInfo.setSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                                appList.add(appInfo);
                            }
                        } else {
                            // 停用or启用
                            MyLogger.e(cmd + " success");
                        }
                    }
                } catch (Exception e) {
                    MyLogger.e(e.getMessage());
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        if (errorStream != null) {
                            errorStream.close();
                        }
                    } catch (IOException e) {
                        MyLogger.e(e.getMessage());
                    }
                }
                return appList;
            }

            @Override
            protected void onPostExecute(List<AppInfo> list) {
                if (list != null) {
                    if (list.isEmpty() && list.size() == 0) {
                        callback.onRootSuccess();
                    } else {
                        callback.onRootAppsLoaded(list);
                    }
                } else {
                    callback.onRootError();
                }
            }
        }.execute();
    }
}
