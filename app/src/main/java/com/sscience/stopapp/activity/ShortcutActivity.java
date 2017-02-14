package com.sscience.stopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sscience.stopapp.service.RootActionIntentService;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/7
 */

public class ShortcutActivity extends AppCompatActivity {

    public static final String OPEN_APP_SHORTCUT = "com.sscience.stopapp.OPEN_APP_SHORTCUT";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        Intent component = new Intent(this, RootActionIntentService.class);
        component.putExtra(EXTRA_PACKAGE_NAME, packageName);
        startService(component);
        finish();
    }
}
