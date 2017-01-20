package com.science.stopapp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.science.myloggerlibrary.MyLogger;
import com.science.stopapp.R;
import com.science.stopapp.bean.AppInfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.science.stopapp.fragment.AppListFragment.COMMAND_APP_LIST;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class AppListPresenter implements AppListContract.Presenter {

    private static final String TAG = AppListPresenter.class.getSimpleName() + "-----";
    public static final String COMMAND_DISABLE = "disable";
    public static final String COMMAND_ENABLE = "enable";
    private AppListContract.View mView;
    private Context mContext;

    public AppListPresenter(Context context, AppListContract.View mView) {
        mContext = context;
        this.mView = mView;
        this.mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void disableApp(final AppInfo appInfo, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.tip);
        builder.setMessage(appInfo.isEnable() ? mContext.getString(R.string.whether_disable_app, appInfo.getAppName())
                : mContext.getString(R.string.whether_enable_app, appInfo.getAppName()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appInfo.setEnable(!appInfo.isEnable());
                commandSu(appInfo.isEnable() ? COMMAND_ENABLE : COMMAND_DISABLE, appInfo.appPackageName, appInfo, false);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * -d：进行过滤以仅显示已停用的软件包。
     * -e：进行过滤以仅显示已启用的软件包。
     * -s：进行过滤以仅显示系统软件包。
     * -3：进行过滤以仅显示第三方软件包。
     * -i：查看软件包的安装程序。
     * -u：也包括卸载的软件包。
     */
    @Override
    public void commandSu(final String cmd, final String filter, final AppInfo appInfo, final boolean isLaunchApp) {
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
                    String command = "pm " + cmd + " " + filter + "\n";
                    dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.flush();
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                    int i = process.waitFor();
                    if (i == 0) { // 正确获取root权限
                        appList = new ArrayList<>();
                        if (cmd.equals(COMMAND_APP_LIST)) { // 获取应用
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
                            MyLogger.e(filter + " success");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        if (errorStream != null) {
                            errorStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                return appList;
            }

            @Override
            protected void onPostExecute(List<AppInfo> list) {
                if (list != null) {
                    if (list.isEmpty() && list.size() == 0) {
                        mView.disableOrEnableAppsSuccess(appInfo, isLaunchApp);
                    } else {
                        mView.getAppList(list);
                    }
                } else {
                    mView.getRootFailed();
                }
            }
        }.execute();
    }
}
