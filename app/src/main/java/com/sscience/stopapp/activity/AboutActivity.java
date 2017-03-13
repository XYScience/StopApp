package com.sscience.stopapp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.sscience.stopapp.R;
import com.sscience.stopapp.adapter.AboutAdapter;
import com.sscience.stopapp.base.BaseActivity;
import com.sscience.stopapp.util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sscience.stopapp.R.id.recyclerView;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/2/6
 */

public class AboutActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private AboutAdapter mAboutAdapter;

    public static void actionStartActivity(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentLayout() {
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        return R.layout.activity_about;
    }

    @Override
    protected void doOnCreate(@Nullable Bundle savedInstanceState) {
        setToolbar("");

        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version, CommonUtil.getAppVersion(this)));

        mRecyclerView = (RecyclerView) findViewById(recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, manager.getOrientation()));
        mAboutAdapter = new AboutAdapter(this, mRecyclerView);
        mRecyclerView.setAdapter(mAboutAdapter);

        List<Map<String, String>> aboutList = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("title", getString(R.string.about_app));
        map1.put("subtitle", getString(R.string.about_app_detail));
        aboutList.add(map1);
        Map<String, String> map2 = new HashMap<>();
        map2.put("title", getString(R.string.source_code_address));
        map2.put("subtitle", getString(R.string.source_code_address_detail));
        aboutList.add(map2);
        Map<String, String> map3 = new HashMap<>();
        map3.put("title", getString(R.string.report_issues));
        map3.put("subtitle", getString(R.string.report_issues_detail));
        aboutList.add(map3);
        Map<String, String> map4 = new HashMap<>();
        map4.put("title", getString(R.string.feedback_suggestions));
        map4.put("subtitle", getString(R.string.feedback_suggestions_detail));
        aboutList.add(map4);
        mAboutAdapter.setData(false, aboutList);

        initListener();
    }

    private void initListener() {
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        final TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange() - (int) getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
                ViewCompat.setAlpha(toolbarTitle, percentage);
            }
        });

        mAboutAdapter.setOnItemClickListener(new OnItemClickListener<Map<String, String>>() {
            @Override
            public void onItemClick(Map<String, String> maps, int i) {
                switch (i) {
                    case 0:
                        aboutApp();
                        break;
                    case 1:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/XYScience/StopApp")));
                        break;
                    case 2:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/XYScience/StopApp/issues")));
                        break;
                    case 3:
                        feedbackToEmail();
                        break;
                }
            }
        });

        FloatingActionButton fabShare = (FloatingActionButton) findViewById(R.id.fab_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content) +
                        "https://play.google.com/store/apps/details?id=com.sscience.stopapp、\n" +
                        "http://coolapk.com/apk/com.sscience.stopapp");
                startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
            }
        });
    }

    private void aboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_app);
        builder.setMessage(R.string.about_app_dialog);
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void feedbackToEmail() {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:chentushen.science@gmail.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_email_subject));
        data.putExtra(Intent.EXTRA_TEXT, "----------\n" + getString(R.string.send_email_app_name) + getString(R.string.app_name)
                + getString(R.string.send_email_app_version) + CommonUtil.getAppVersion(AboutActivity.this)
                + getString(R.string.send_email_phone_model) + CommonUtil.getPhoneModel()
                + getString(R.string.android_version) + CommonUtil.getAndroidVersion()
                + "\n----------"
                + getString(R.string.leave_feedback));
        startActivity(Intent.createChooser(data, getString(R.string.choose_email_app)));
    }
}
