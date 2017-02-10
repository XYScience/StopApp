package com.science.stopapp.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.science.myloggerlibrary.MyLogger;
import com.science.stopapp.bean.AppInfo;

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
    public static final String COMMAND_APP_LIST = "list packages ";
    public static final String COMMAND_DISABLE = "disable ";
    public static final String COMMAND_ENABLE = "enable ";
    public static final String COMMAND_UNINSTALL = "uninstall ";
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

    public interface GetRootCallback {
        void onRoot(Boolean isRoot);
    }

    public void getApps(final int appFlag, final GetAppsCallback callback) {
        new AsyncTask<Boolean, Boolean, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Boolean... params) {
                PackageManager packageManager = mContext.getPackageManager();
                // 查询所有已经安装的应用程序
                List<ApplicationInfo> applications = packageManager
                        .getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
                List<AppInfo> appInfos = new ArrayList<>(); // 保存过滤查到的AppInfo
                switch (appFlag) {
                    case APPS_FLAG_ALL:
                        appInfos.clear();
                        for (ApplicationInfo app : applications) {
                            if (!app.packageName.equals(mContext.getPackageName())) {
                                if (!app.packageName.contains("supersu")) {
                                    appInfos.add(getAppInfo(app, packageManager));
                                }
                            }
                        }
                        return appInfos;
                    case APPS_FLAG_SYSTEM:
                        appInfos.clear();
                        for (ApplicationInfo app : applications) {
                            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                appInfos.add(getAppInfo(app, packageManager));
                            }
                        }
                        return appInfos;
                    case APPS_FLAG_USER:
                        appInfos.clear();
                        for (ApplicationInfo app : applications) {
                            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                                if (!app.packageName.equals(mContext.getPackageName())) {
                                    if (!app.packageName.contains("supersu")) {
                                        appInfos.add(getAppInfo(app, packageManager));
                                    }
                                }
                            }
                        }
                        return appInfos;
                    default:
                        return appInfos;
                }
            }

            @Override
            protected void onPostExecute(List<AppInfo> appList) {
                callback.onAppsLoaded(appList);
            }
        }.execute();
    }

    private AppInfo getAppInfo(ApplicationInfo applicationInfo, PackageManager packageManager) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
        appInfo.setAppPackageName(applicationInfo.packageName);
        appInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
        appInfo.setEnable(applicationInfo.enabled);
        appInfo.setSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        return appInfo;
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
                        if (cmd.contains(COMMAND_APP_LIST)) { // 获取应用
                            String msg = "";
                            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            while ((msg = br.readLine()) != null) {
                                ApplicationInfo applicationInfo = packageManager.getPackageInfo(msg.replace("package:", ""), 0).applicationInfo;
                                appList.add(getAppInfo(applicationInfo, packageManager));
                            }
                        } else {
                            // 停用or启用or卸载
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

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     * <p>
     * command 命令： String apkRoot="chmod 777 "+getPackageCodePath();
     * RootCommand(apkRoot);
     *
     * @return 应用程序是/否获取Root权限
     */
    public void getRoot(final GetRootCallback callback) {
        new AsyncTask<Boolean, Object, Boolean>() {

            @Override
            protected Boolean doInBackground(Boolean... params) {
                Process process = null;
                DataOutputStream os = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("chmod 777 " + mContext.getPackageCodePath() + "\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    int i = process.waitFor();
                    if (i == 0) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        process.destroy();
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean isRoot) {
                callback.onRoot(isRoot);
            }
        }.execute();
    }
}
