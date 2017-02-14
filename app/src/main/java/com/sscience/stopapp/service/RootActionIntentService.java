package com.sscience.stopapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.science.myloggerlibrary.MyLogger;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.ShortcutActivity;
import com.sscience.stopapp.bean.AppInfo;
import com.sscience.stopapp.model.AppsRepository;

import java.util.List;

import static com.sscience.stopapp.model.AppsRepository.COMMAND_ENABLE;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/8
 */

public class RootActionIntentService extends IntentService {

    private Handler mHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     */
    public RootActionIntentService() {
        super("RootActionIntentService");
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String packageName = intent.getStringExtra(ShortcutActivity.EXTRA_PACKAGE_NAME);
        MyLogger.e("-----------" + packageName);
        launchAppIntent(packageName);
        stopSelf();
    }

    public void enableApp(final String cmd, final String packageName) {
        new AppsRepository(RootActionIntentService.this).commandSu(cmd + packageName, new AppsRepository.GetAppsCmdCallback() {
            @Override
            public void onRootAppsLoaded(List<AppInfo> apps) {

            }

            @Override
            public void onRootError() {
                mHandler.post(new DisplayToast(RootActionIntentService.this, getString(R.string.if_want_to_use_please_grant_app_root)));
            }

            @Override
            public void onRootSuccess() {
                launchAppIntent(packageName);
            }
        });
    }

    private void launchAppIntent(String packageName) {
        try {
            Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            startActivity(resolveIntent);
        } catch (NullPointerException e) {
            enableApp(COMMAND_ENABLE, packageName);
        }
    }

    public class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;

        public DisplayToast(Context mContext, String text) {
            this.mContext = mContext;
            mText = text;
        }

        public void run() {
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }
}
