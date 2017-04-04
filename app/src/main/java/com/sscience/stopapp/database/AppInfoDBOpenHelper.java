package com.sscience.stopapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sscience.stopapp.bean.AppInfo;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/3/6
 */

public class AppInfoDBOpenHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "appInfo.db";
    // AppInfo Table Names
    public static final String TABLE_NAME_APP_INFO = "table_appInfo";
    // Shortcut App Table Names
    public static final String TABLE_NAME_SHORTCUT_APP_INFO = "table_shortcut_appInfo";

//    // Table create statement
//    private static final String CREATE_TABLE_APP_INFO =
//            "CREATE TABLE IF NOT EXISTS "
//                    + TABLE_NAME_APP_INFO
//                    + String.format(
//                    "("
//                            + "%s VARCHAR PRIMARY KEY, " // appPackageName
//                            + "%s VARCHAR, " // appName
//                            + "%s BLOB, " // appIcon
//                            + "%s INTEGER, " // isEnable
//                            + "%s INTEGER " // isSystemApp
//                            + ")"
//                    , AppInfo.APP_PACKAGE_NAME
//                    , AppInfo.APP_NAME
//                    , AppInfo.APP_ICON
//                    , AppInfo.IS_ENABLE
//                    , AppInfo.IS_SYSTEM_APP
//
//            );

    public AppInfoDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static String createTable(String tableName) {
        String createTable = "CREATE TABLE IF NOT EXISTS "
                + tableName
                + String.format(
                "("
                        + "%s VARCHAR PRIMARY KEY, " // appPackageName
                        + "%s VARCHAR, " // appName
                        + "%s BLOB, " // appIcon
                        + "%s INTEGER, " // isEnable
                        + "%s INTEGER " // isSystemApp
                        + ")"
                , AppInfo.APP_PACKAGE_NAME
                , AppInfo.APP_NAME
                , AppInfo.APP_ICON
                , AppInfo.IS_ENABLE
                , AppInfo.IS_SYSTEM_APP

        );
        return createTable;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable(TABLE_NAME_APP_INFO));
        db.execSQL(createTable(TABLE_NAME_SHORTCUT_APP_INFO));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            // on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_APP_INFO);
            // create new table
            onCreate(db);
        }
    }
}
