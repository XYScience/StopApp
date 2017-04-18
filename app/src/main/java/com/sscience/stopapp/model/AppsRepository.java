package com.sscience.stopapp.model;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.bean.ComponentInfo;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACCESSIBILITY_SERVICE;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/27
 */

public class AppsRepository {

    public static final int APPS_FLAG_ALL = 0;
    public static final int APPS_FLAG_SYSTEM = 1;
    public static final int APPS_FLAG_USER = 2;
    public static final int APPS_FLAG_DISABLE = 3;
    public static final String COMMAND_APP_LIST = "pm list packages -d";
    public static final String COMMAND_DISABLE = "pm disable ";
    public static final String COMMAND_ENABLE = "pm enable ";
    public static final String COMMAND_UNINSTALL = "pm uninstall ";
    public static final String COMMAND_GET_ROOT = "chmod 777 ";
    private static String[] strRootManagerApp = new String[]{"com.sscience.stopapp", "eu.chainfire.supersu", "me.phh.superuser", "com.koushikdutta.superuser", "com.noshufou.android.su", "com.qihoo.permmgr", "com.kingroot.kinguser", "com.kingoapp.root", "com.shuame.rootgenius", "com.dianxinos.superuser", "com.miui.securitycenter", "com.miui.uac"};
    private Context mContext;
    private GetAppsAsyncTask mGetAppsAsyncTask;
    private AccessibilityAsyncTask mAccessibilityAsyncTask;

    public AppsRepository(Context context) {
        mContext = context;
    }

    /**
     * 获取apps
     *
     * @param appFlag  用户应用or系统应用or停用应用
     * @param callback 成功获取后的回掉
     */
    public void getApps(int appFlag, final GetAppsCallback callback) {
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
            if (appFlag == APPS_FLAG_DISABLE) {
                return getDisableApps(context);
            } else {
                return getInstalledApps(context, this, appFlag);
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

    /**
     * 通过root权限获取停用apps
     *
     * @param context
     * @return 返回停用apps
     */
    private static List<AppInfo> getDisableApps(Context context) {
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        List<AppInfo> appList = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command = COMMAND_APP_LIST + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            int i = process.waitFor();
            if (i == 0) { // 正确获取root权限
                appList = new ArrayList<>();
                String msg = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((msg = br.readLine()) != null) {
                    ApplicationInfo applicationInfo = packageManager.getPackageInfo(msg.replace("package:", ""), 0).applicationInfo;
                    appList.add(getAppInfo(applicationInfo, packageManager));
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

    /**
     * 通过packageManager.getInstalledApplications获取系统已安装应用
     *
     * @param context
     * @param asyncTask
     * @param appFlag   获取系统应用还是用户应用
     * @return
     */
    private static List<AppInfo> getInstalledApps(Context context, GetAppsAsyncTask asyncTask, int appFlag) {
        PackageManager packageManager = context.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> applications = packageManager
                .getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        List<AppInfo> appInfos = new ArrayList<>(); // 保存过滤查到的AppInfo
        switch (appFlag) {
            case APPS_FLAG_ALL:
                appInfos.clear();
                for (ApplicationInfo app : applications) {
                    if (asyncTask.isCancelled()) {
                        break;
                    }
                    // 目前还不知道有什么办法知道app是授权管理~~
                    if (!containsRootApp(app.packageName)) {
                        appInfos.add(getAppInfo(app, packageManager));
                    }
                }
                return appInfos;
            case APPS_FLAG_SYSTEM:
                appInfos.clear();
                for (ApplicationInfo app : applications) {
                    if (asyncTask.isCancelled()) {
                        break;
                    }
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        if (!containsRootApp(app.packageName)) {
                            appInfos.add(getAppInfo(app, packageManager));
                        }
                    }
                }
                return appInfos;
            case APPS_FLAG_USER:
                appInfos.clear();
                for (ApplicationInfo app : applications) {
                    if (asyncTask.isCancelled()) {
                        break;
                    }
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        if (!containsRootApp(app.packageName)) {
                            appInfos.add(getAppInfo(app, packageManager));
                        }
                    }
                }
                return appInfos;
            default:
                return appInfos;
        }
    }

    /**
     * 使用一个简单的循环方法比使用任何集合都更加高效
     *
     * @param targetValue
     * @return
     */
    private static boolean containsRootApp(String targetValue) {
        for (String s : strRootManagerApp) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    private static AppInfo getAppInfo(ApplicationInfo applicationInfo, PackageManager packageManager) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(applicationInfo.loadLabel(packageManager).toString());
        appInfo.setAppPackageName(applicationInfo.packageName);
        Drawable appDrawable = null;
        try {
            appDrawable = applicationInfo.loadIcon(packageManager);
        } catch (Exception e) {
            MyLogger.e(appInfo.getAppName());
            MyLogger.e(e.toString());
        }
        if (appDrawable instanceof BitmapDrawable) {
            appInfo.setAppIcon(((BitmapDrawable) appDrawable).getBitmap());
        } else {
            MyLogger.e("VectorDrawable:" + appInfo.getAppName());
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
     * 通用adb命令执行
     */
    public void getRoot(String cmd, final GetRootCallback callback) {
        GetRootAsyncTask getRootAsyncTask = new GetRootAsyncTask(mContext, cmd, callback);
        getRootAsyncTask.execute();
    }

    private static class GetRootAsyncTask extends AsyncTask<Boolean, Object, Boolean> {

        private Context context;
        private GetRootCallback callback;
        private WeakReference<Context> weakReference;
        private String cmd;

        public GetRootAsyncTask(Context context, String cmd, GetRootCallback callback) {
            this.context = context;
            this.callback = callback;
            this.cmd = cmd;
            weakReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Process process = null;
            DataOutputStream os = null;
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(cmd + "\n");
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
            if (context instanceof Activity) {
                Activity activity = (Activity) weakReference.get();
                if (activity != null && callback != null) {
                    callback.onRoot(isRoot);
                }
            } else if (context instanceof Service) {
                Service service = (Service) weakReference.get();
                if (service != null && callback != null) {
                    callback.onRoot(isRoot);
                }
            }
        }
    }

    /**
     * 自动开启无障碍服务
     */
    public void openAccessibilityServices(GetRootCallback callback) {
        mAccessibilityAsyncTask = new AccessibilityAsyncTask(mContext, callback);
        mAccessibilityAsyncTask.execute();
    }

    private static class AccessibilityAsyncTask extends AsyncTask<Boolean, Object, Boolean> {

        String cmd1 = "settings put secure enabled_accessibility_services " +
                "com.sscience.stopapp/com.sscience.stopapp.service.MyAccessibilityService";
        String cmd2 = "settings put secure accessibility_enabled 1";
        private StringBuilder sb;
        private Context mContext;
        private WeakReference<Context> weakReference;
        private GetRootCallback callback;
        private boolean isFirst;

        public AccessibilityAsyncTask(Context context, GetRootCallback callback) {
            mContext = context;
            this.weakReference = new WeakReference<>(context);
            this.callback = callback;
            sb = new StringBuilder();
            sb.append(cmd1);
            for (AccessibilityServiceInfo serviceInfo : ((AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE))
                    .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)) {
                // 注：必须为"settings put secure enabled_accessibility_services " + 包名 + "/" + 完整类名（包含包名），
                // 如果类名不包含包名，则无障碍列表正常是开启，点击进去详情则是关闭状态。
                String[] s = serviceInfo.getId().split("/");
                sb.append(":").append(s[0]).append("/").append(s[0]).append(s[1]);
            }
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Process process = null;
            DataOutputStream os = null;
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(sb.toString() + "\n");
                os.writeBytes(cmd2 + "\n");
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
            if (!isFirst && callback != null) {
                isFirst = true;
                callback.onRoot(isRoot);
            }
        }
    }

    /**
     * 通过XML解析AndroidManifest.xml获取组件信息
     *
     * @param packageName
     * @param tabCategory
     * @return
     */
    public List<ComponentInfo> getComponentInfo(String packageName, int tabCategory) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ComponentInfo> componentInfoList = new ArrayList();
        try {
            Context createPackageContext = mContext.createPackageContext(packageName, 0);
            AssetManager assets = createPackageContext.getAssets();
            XmlResourceParser openXmlResourceParser = assets.openXmlResourceParser(
                    ((Integer) AssetManager.class.getMethod("addAssetPath", new Class[]{String.class})
                            .invoke(assets, new Object[]{mContext.getPackageManager()
                                    .getApplicationInfo(packageName, 0).sourceDir})).intValue()
                    , "AndroidManifest.xml");
            Resources resources = new Resources(assets, createPackageContext.getResources().getDisplayMetrics(), null);
            while (true) {
                int next = openXmlResourceParser.next();
                if (next == XmlResourceParser.END_DOCUMENT) {
                    break;
                } else if (next == XmlResourceParser.START_TAG) {
                    if (!((tabCategory == 0 && openXmlResourceParser.getName().equals("activity"))
                            || (tabCategory == 0 && openXmlResourceParser.getName().equals("activity-alias"))
                            || (tabCategory == 1 && openXmlResourceParser.getName().equals("service"))
                            || (tabCategory == 2 && openXmlResourceParser.getName().equals("receiver"))
                            || (tabCategory == 3 && openXmlResourceParser.getName().equals("provider")))) {
                        continue;
                    }
                    String attributeValue = openXmlResourceParser.getAttributeValue(
                            "http://schemas.android.com/apk/res/android", "name");
                    if (attributeValue == null) {
                        for (int i2 = 0; i2 < openXmlResourceParser.getAttributeCount(); i2++) {
                            if (TextUtils.isEmpty(openXmlResourceParser.getAttributeName(i2))) {
                                int attributeNameResource = openXmlResourceParser.getAttributeNameResource(i2);
                                if (attributeNameResource != 0 && resources.getResourceEntryName(attributeNameResource).equals("name")) {
                                    attributeValue = openXmlResourceParser.getAttributeValue(i2);
                                    break;
                                }
                            }
                        }
                    }
                    if (attributeValue != null) {
                        ComponentInfo componentInfo = new ComponentInfo();
                        String componentName = "";
                        if (!attributeValue.contains(".")) {
                            componentName = packageName + "." + attributeValue;
                        } else if (attributeValue.startsWith(".")) {
                            componentName = packageName + attributeValue;
                        } else {
                            componentName = attributeValue;
                        }
                        componentInfo.setComponentName(componentName);
                        componentInfo.setEnable(packageManager.getComponentEnabledSetting(
                                new ComponentName(packageName, componentName)) <= 1);
                        componentInfo.setPackageName(packageName);
                        componentInfoList.add(componentInfo);
                    } else {
                        continue;
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        }
        return componentInfoList;
    }

    public void cancelTask() {
        if (mGetAppsAsyncTask != null) {
            mGetAppsAsyncTask.cancel(true);
            mAccessibilityAsyncTask = null;
        }
        if (mAccessibilityAsyncTask != null) {
            mAccessibilityAsyncTask.cancel(true);
            mAccessibilityAsyncTask = null;
        }
    }
}
