package com.science.stopapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.science.myloggerlibrary.MyLogger;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/2/7
 */

public class ShortcutActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AppInfo appInfo = (AppInfo) getIntent().getSerializableExtra("EXTRA_SHORTCUT");
        MyLogger.e("-----------");
        finish();
    }
}
