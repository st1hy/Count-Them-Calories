package com.github.st1hy.countthemcalories.activities.mealdetail.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.inject.activities.mealdetail.fragment.MealDetailComponentFactory;
import com.github.st1hy.countthemcalories.inject.activities.mealdetail.fragment.MealDetailsModule;
import com.github.st1hy.countthemcalories.activities.mealdetail.fragment.presenter.LifecycleController;
import com.github.st1hy.countthemcalories.core.baseview.BaseFragment;

import javax.inject.Inject;

public class MealDetailFragment extends BaseFragment {

    public static final String ARG_MEAL_PARCEL = "meal detail parcel";

    private MealDetailComponentFactory componentFactory;

    @Inject
    LifecycleController controller;

    public void setComponentFactory(@NonNull MealDetailComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.meal_detail_content, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        componentFactory.newMealDetailComponent(new MealDetailsModule(this, savedInstanceState))
                .inject(this);
        componentFactory = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        controller.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        controller.onSaveState(outState);
    }


}