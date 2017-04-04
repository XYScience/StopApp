package com.sscience.stopapp.fragment;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.science.baserecyclerviewadapter.interfaces.OnItemClickListener;
import com.sscience.stopapp.R;
import com.sscience.stopapp.activity.ComponentDetailsActivity;
import com.sscience.stopapp.adapter.ComponentDetailsAdapter;
import com.sscience.stopapp.base.BaseFragment;
import com.sscience.stopapp.bean.ComponentInfo;
import com.sscience.stopapp.presenter.ComponentDetailsContract;
import com.sscience.stopapp.presenter.ComponentDetailsPresenter;

import java.util.List;

import static com.sscience.stopapp.activity.ComponentDetailsActivity.EXTRA_APP_PACKAGE_NAME;
import static com.sscience.stopapp.fragment.AppListFragment.TAB_CATEGORY;

/**
 * @author SScience
 * @description
 * @email chentushen.science@gmail.com
 * @data 2017/4/4
 */
public class ComponentDetailsFragment extends BaseFragment implements ComponentDetailsContract.View {

    private ComponentDetailsContract.Presenter mPresenter;
    private ComponentDetailsAdapter mComponentDetailsAdapter;
    private String packageName;

    public static ComponentDetailsFragment newInstance(int tabCategory) {
        ComponentDetailsFragment fragment = new ComponentDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_CATEGORY, tabCategory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_app_list;
    }

    @Override
    protected void doCreateView(View view) {
        new ComponentDetailsPresenter(getActivity(), this);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        mComponentDetailsAdapter = new ComponentDetailsAdapter(getActivity(), mRecyclerView);
        mRecyclerView.setAdapter(mComponentDetailsAdapter);

        int tabCategory = getArguments().getInt(TAB_CATEGORY);
        packageName = getActivity().getIntent().getStringExtra(EXTRA_APP_PACKAGE_NAME);
        mPresenter.getComponent(tabCategory, packageName);

        initListener();
    }

    private void initListener() {
        mComponentDetailsAdapter.setOnItemClickListener(new OnItemClickListener<ComponentInfo>() {
            @Override
            public void onItemClick(ComponentInfo componentInfo, int position) {
                mPresenter.pmComponent(componentInfo, position);
            }
        });
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    public void setPresenter(ComponentDetailsContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    public void getComponent(List<ComponentInfo> list) {
        if (list.isEmpty()) {
            mComponentDetailsAdapter.showLoadFailed(R.drawable.empty
                    , getResources().getString(R.string.no_data), "");
        } else {
            mComponentDetailsAdapter.setData(list);
        }
    }

    @Override
    public void onRoot(boolean isRoot, ComponentInfo componentInfo, int position) {
        if (isRoot) {
            componentInfo.setEnable(!componentInfo.isEnable());
            mComponentDetailsAdapter.updateItem(position, componentInfo);
        } else {
            snackBarShow(((ComponentDetailsActivity) getActivity()).mCoordinatorLayout
                    , getString(R.string.if_want_to_use_please_grant_app_root));
        }
    }
}
