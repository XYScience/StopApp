package com.sscience.stopapp.base;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;

import com.sscience.stopapp.R;


/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1000;

    protected abstract int getContentLayout();

    protected abstract void doOnCreate(@Nullable Bundle savedInstanceState);

    private PermissionCallback mPermissionCallback;

    public interface PermissionCallback {
        void hasPermission();

        void noPermission();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayout());
        doOnCreate(savedInstanceState);
    }

    public Toolbar setToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && !title.equals(getString(R.string.app_name))) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Android6.0+权限检查 start

    /**
     * 权限请求入口
     *
     * @param permissionTip
     * @param permissionCallback
     * @param permissions
     */
    public void performCodeWithPermission(String permissionTip, PermissionCallback permissionCallback, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return;
        } else {
            mPermissionCallback = permissionCallback;
            if (checkPermissionGranted(permissions)) {
                // 有权限
                if (mPermissionCallback != null) {
                    mPermissionCallback.hasPermission();
                    mPermissionCallback = null;
                }
            } else {
                // 没有权限
                requestPermission(permissionTip, permissions);
            }
        }
    }

    private void requestPermission(final String permissionTip, final String[] permissions) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            //如果用户之前拒绝过此权限，再提示一次准备授权相关权限
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.tip))
                    .setMessage(permissionTip)
                    .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this, permissions, REQUEST_PERMISSION_CODE);
                        }
                    }).setNegativeButton(getString(R.string.deny), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mPermissionCallback != null) {
                        mPermissionCallback.noPermission();
                        mPermissionCallback = null;
                    }
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(BaseActivity.this, permissions, REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * 首次安装请求返回false；如果用户之前拒绝过权限，则返回true；用户在权限对话框中选择了"不再显示”后，则返回false
     * (MIUI阉割了这个方法/(ㄒoㄒ)/~~)
     *
     * @param permissions
     * @return
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        boolean flag = false;
        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, p)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 检测应用是否已经具有权限
     *
     * @param permissions
     * @return
     */
    private boolean checkPermissionGranted(String[] permissions) {
        boolean flag = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                flag = false; // 没有权限
                break;
            }
        }
        return flag;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (verifyPermissions(grantResults)) {
                if (mPermissionCallback != null) {
                    mPermissionCallback.hasPermission();
                    mPermissionCallback = null;
                }
            } else {
                if (mPermissionCallback != null) {
                    mPermissionCallback.noPermission();
                    mPermissionCallback = null;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    // Android6.0+权限检查 end

    // tip start
    public void snackBarShow(View view, int contentRes) {
        snackBarShow(view, getString(contentRes));
    }

    public void snackBarShow(View view, String content) {
        Snackbar.make(view, content, Snackbar.LENGTH_LONG).show();
    }
    // tip end

    /**
     * view渐显or渐隐动画
     *
     * @param view
     * @param alpha
     * @param interpolator
     */
    protected void setInterpolator(View view, float alpha, Interpolator interpolator) {
        view.setClickable(alpha == 1);
        ViewCompat.animate(view).alpha(alpha).setInterpolator(interpolator);
    }
}
