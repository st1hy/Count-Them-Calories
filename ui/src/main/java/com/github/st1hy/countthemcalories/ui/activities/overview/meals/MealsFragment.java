package com.github.st1hy.countthemcalories.ui.activities.overview.meals;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.countthemcalories.ui.R;
import com.github.st1hy.countthemcalories.ui.activities.overview.OverviewActivity;
import com.github.st1hy.countthemcalories.ui.activities.overview.meals.presenter.MealsPresenter;
import com.github.st1hy.countthemcalories.ui.core.baseview.BaseFragment;
import com.github.st1hy.countthemcalories.ui.inject.core.FragmentModule;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

public class MealsFragment extends BaseFragment {

    public static final String ARG_CURRENT_PAGE = "current page";

    @Inject
    MealsPresenter presenter;
    @Inject
    RecyclerView recyclerView; //injects adapter into recycler

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.overview_list, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OverviewActivity activity = (OverviewActivity) getActivity();
        Preconditions.checkNotNull(activity).getMealsFragmentComponentFactory()
                .newOverviewFragmentComponent(new FragmentModule(this, savedInstanceState))
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }
}