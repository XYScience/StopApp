package com.sscience.stopapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public List<AppInfo> getDisableApps() {
        final Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + AppInfoDBOpenHelper.TABLE_NAME, null);

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
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    public void addDisableApp(AppInfo appInfo) {
        ContentValues cv = new ContentValues();
        cv.put(AppInfo.APP_PACKAGE_NAME, appInfo.getAppPackageName());
        cv.put(AppInfo.APP_NAME, appInfo.getAppName());
        cv.put(AppInfo.APP_ICON, CommonUtil.getBytes(appInfo.getAppIcon()));
        cv.put(AppInfo.IS_ENABLE, appInfo.isEnable());
        cv.put(AppInfo.IS_SYSTEM_APP, appInfo.isSystemApp());
        mSQLiteDatabase.insert(AppInfoDBOpenHelper.TABLE_NAME, null, cv);
    }

    public void deleteDisableApp(String packageName) {
        String deleteQuery = "DELETE FROM " + AppInfoDBOpenHelper.TABLE_NAME
                + " where " + AppInfo.APP_PACKAGE_NAME + " = '" + packageName + "'";
        mSQLiteDatabase.execSQL(deleteQuery);
    }

    public void updateDisableApp(String packageName, int isEnable) {
        ContentValues cv = new ContentValues();
        cv.put(AppInfo.IS_ENABLE, isEnable);
        String[] args = {packageName};
        mSQLiteDatabase.update(AppInfoDBOpenHelper.TABLE_NAME, cv, AppInfo.APP_PACKAGE_NAME + "=?", args);
    }

}
