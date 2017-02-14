package com.sscience.stopapp.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.sscience.stopapp.bean.AppInfo;

import java.util.List;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/1/15
 */

public class DiffCallBack extends DiffUtil.Callback {

    public static final String BUNDLE_PAYLOAD = "bundle_payload";
    private List<AppInfo> mOldData, mNewData;

    public DiffCallBack(List<AppInfo> oldData, List<AppInfo> newData) {
        mOldData = oldData;
        mNewData = newData;
    }

    @Override
    public int getOldListSize() {
        return mOldData != null ? mOldData.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return mNewData != null ? mNewData.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).getAppPackageName().equals(mNewData.get(newItemPosition).getAppPackageName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (mOldData.get(oldItemPosition).isEnable() != mNewData.get(newItemPosition).isEnable()) {
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        AppInfo oldBean = mOldData.get(oldItemPosition);
        AppInfo newBean = mNewData.get(newItemPosition);
        Bundle payLoad = new Bundle();
        if (oldBean.isEnable() != newBean.isEnable()) {
            payLoad.putBoolean(BUNDLE_PAYLOAD, newBean.isEnable());
        }
        if (payLoad == null) {
            return null;
        }
        return payLoad;
    }
}
