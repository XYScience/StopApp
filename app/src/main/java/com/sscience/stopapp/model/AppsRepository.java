package com.sscience.stopapp.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
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
    private GetAppsAsyncTask mGetAppsAsyncTask;

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

    /**
     * “添加应用”界面获取用户or系统apps
     *
     * @param appFlag  用户or系统apps
     * @param callback 成功获取后的回掉
     */
    public void getApps(final int appFlag, final GetAppsCallback callback) {
        mGetAppsAsyncTask = new GetAppsAsyncTask(mContext, appFlag, callback);
        mGetAppsAsyncTask.execute();
    }

    private static class GetAppsAsyncTask extends AsyncTask<Boolean, Boolean, List<AppInfo>> {

        private Context context;
        private int appFlag;
        private GetAppsCallback callback;
        private WeakReference<Context> weakReference;

        public GetAppsAsyncTask(Context context, int appFlag, GetAppsCallback callback) {
            this.context = context;
            this.appFlag = appFlag;
            this.callback = callback;
            weakReference = new WeakReference<>(context);
        }

        @Override
        protected List<AppInfo> doInBackground(Boolean... params) {
            PackageManager packageManager = context.getPackageManager();
            // 查询所有已经安装的应用程序
            List<ApplicationInfo> applications = packageManager
                    .getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            List<AppInfo> appInfos = new ArrayList<>(); // 保存过滤查到的AppInfo
            switch (appFlag) {
                case APPS_FLAG_ALL:
                    appInfos.clear();
                    for (ApplicationInfo app : applications) {
                        if (isCancelled()) {
                            break;
                        }
                        if (!app.packageName.equals(context.getPackageName())) {
                            if (!app.packageName.contains("supersu")) {
                                appInfos.add(getAppInfo(app, packageManager));
                            }
                        }
                    }
                    return appInfos;
                case APPS_FLAG_SYSTEM:
                    appInfos.clear();
                    for (ApplicationInfo app : applications) {
                        if (isCancelled()) {
                            break;
                        }
                        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            appInfos.add(getAppInfo(app, packageManager));
                        }
                    }
                    return appInfos;
                case APPS_FLAG_USER:
                    appInfos.clear();
                    for (ApplicationInfo app : applications) {
                        if (isCancelled()) {
                            break;
                        }
                        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            if (!app.packageName.equals(context.getPackageName())) {
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
            BaseActivity activity = (BaseActivity) weakReference.get();
            if (activity != null) {
                callback.onAppsLoaded(appList);
            }
        }
    }

    private static AppInfo getAppInfo(ApplicationInfo applicationInfo, PackageManager packageManager) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
        appInfo.setAppPackageName(applicationInfo.packageName);
        Drawable appDrawable = applicationInfo.loadIcon(packageManager);
        if (appDrawable instanceof BitmapDrawable) {
            appInfo.setAppIcon(((BitmapDrawable) appDrawable).getBitmap());
        } else {
            MyLogger.e("VectorDrawable:" + appInfo.getAppPackageName());
            appInfo.setAppIcon(drawableToBitmap(appDrawable));
        }
        appInfo.setEnable(applicationInfo.enabled ? 1 : 0);
        appInfo.setSystemApp((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ? 1 : 0);
        return appInfo;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 通过root，获取停用应用列表or停用应用or启用应用
     *
     * @param cmd
     * @param callback
     */
    public void commandSu(final String cmd, final GetAppsCmdCallback callback) {
        CommandSuAsyncTask commandSuAsyncTask = new CommandSuAsyncTask(mContext, cmd, callback);
        commandSuAsyncTask.execute();
    }

    private static class CommandSuAsyncTask extends AsyncTask<Boolean, Object, List<AppInfo>> {

        private Context context;
        private String cmd;
        private GetAppsCmdCallback callback;
        private WeakReference<Context> weakReference;

        public CommandSuAsyncTask(Context context, String cmd, GetAppsCmdCallback callback) {
            this.context = context;
            this.cmd = cmd;
            this.callback = callback;
            weakReference = new WeakReference<>(context);
        }

        @Override
        protected List<AppInfo> doInBackground(Boolean... params) {
            DataOutputStream dataOutputStream = null;
            BufferedReader errorStream = null;
            List<AppInfo> appList = null;
            PackageManager packageManager = context.getPackageManager();
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
            BaseActivity activity = (BaseActivity) weakReference.get();
            if (activity == null) {
                return;
            }
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
        GetRootAsyncTask getRootAsyncTask = new GetRootAsyncTask(mContext, callback);
        getRootAsyncTask.execute();
    }

    private static class GetRootAsyncTask extends AsyncTask<Boolean, Object, Boolean> {

        private Context context;
        private GetRootCallback callback;
        private WeakReference<Context> weakReference;

        public GetRootAsyncTask(Context context, GetRootCallback callback) {
            this.context = context;
            this.callback = callback;
            weakReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Process process = null;
            DataOutputStream os = null;
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("chmod 777 " + context.getPackageCodePath() + "\n");
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
            BaseActivity activity = (BaseActivity) weakReference.get();
            if (activity != null) {
                callback.onRoot(isRoot);
            }
        }
    }

    public void cancelTsk() {
        if (mGetAppsAsyncTask != null) {
            mGetAppsAsyncTask.cancel(true);
        }
    }
}
