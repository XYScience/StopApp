package com.sscience.stopapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/3/6
 */

public class AppInfoDBController {

    private final SQLiteDatabase mSQLiteDatabase;

    public AppInfoDBController(Context context) {
        AppInfoDBOpenHelper openHelper = new AppInfoDBOpenHelper(context);

        mSQLiteDatabase = openHelper.getWritableDatabase();
    }

    public List<AppInfo> getDisableApps(String tableName) {
        final Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + tableName, null);

        final List<AppInfo> list = new ArrayList<>();
        try {
            if (!cursor.moveToLast()) {
                return list;
            }
            do {
                AppInfo appInfo = new AppInfo();
                appInfo.setAppPackageName(cursor.getString(cursor.getColumnIndex(AppInfo.APP_PACKAGE_NAME)));
                appInfo.setAppName(cursor.getString(cursor.getColumnIndex(AppInfo.APP_NAME)));
                appInfo.setAppIcon(CommonUtil.getImage(cursor.getBlob(cursor.getColumnIndex(AppInfo.APP_ICON))));
                appInfo.setEnable(cursor.getInt(cursor.getColumnIndex(AppInfo.IS_ENABLE)));
                appInfo.setSystemApp(cursor.getInt(cursor.getColumnIndex(AppInfo.IS_SYSTEM_APP)));
                list.add(appInfo);
            } while (cursor.moveToPrevious());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return list;
    }

    public boolean searchApp(String tableName, String packageName) {
        boolean isExist = false;
        Cursor cursor = null;
        try {
            cursor = mSQLiteDatabase.query(tableName, null, AppInfo.APP_PACKAGE_NAME + " = ?",
                    new String[]{packageName}, null, null, null);
            isExist = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            MyLogger.e(e.toString());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return isExist;
    }

    public void addDisableApp(AppInfo appInfo, String tableName) {
        ContentValues cv = new ContentValues();
        cv.put(AppInfo.APP_PACKAGE_NAME, appInfo.getAppPackageName());
        cv.put(AppInfo.APP_NAME, appInfo.getAppName());
        cv.put(AppInfo.APP_ICON, CommonUtil.getBytes(appInfo.getAppIcon()));
        cv.put(AppInfo.IS_ENABLE, appInfo.isEnable());
        cv.put(AppInfo.IS_SYSTEM_APP, appInfo.isSystemApp());
        mSQLiteDatabase.insert(tableName, null, cv);
    }

    public void deleteDisableApp(String packageName, String tableName) {
        String deleteQuery = "DELETE FROM " + tableName
                + " where " + AppInfo.APP_PACKAGE_NAME + " = '" + packageName + "'";
        mSQLiteDatabase.execSQL(deleteQuery);
    }

    public void clearDisableApp(String tableName) {
        String deleteQuery = "DELETE FROM " + tableName;
        mSQLiteDatabase.execSQL(deleteQuery);
    }

    public void updateDisableApp(String packageName, int isEnable, String tableName) {
        ContentValues cv = new ContentValues();
        cv.put(AppInfo.IS_ENABLE, isEnable);
        String[] args = {packageName};
        mSQLiteDatabase.update(tableName, cv, AppInfo.APP_PACKAGE_NAME + "=?", args);
    }

}
