package com.sscience.stopapp;

import android.app.Application;
import android.content.Context;

import com.science.myloggerlibrary.MyLogger;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/3/6
 */
public class AppInfoApplication extends Application {

    public static Context CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = this;
        MyLogger.IS_DEBUG = false;
    }
}
