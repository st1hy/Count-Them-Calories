package com.github.st1hy.countthemcalories.activities.mealdetail.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.mealdetail.fragment.view.MealDetailFragment;
import com.github.st1hy.countthemcalories.activities.mealdetail.inject.DaggerMealDetailActivityComponent;
import com.github.st1hy.countthemcalories.activities.mealdetail.inject.MealDetailActivityComponent;
import com.github.st1hy.countthemcalories.activities.mealdetail.inject.MealDetailActivityModel;
import com.github.st1hy.countthemcalories.core.baseview.BaseActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MealDetailActivity extends BaseActivity implements MealDetailScreen {

    public static final String EXTRA_MEAL_PARCEL = "meal detail parcel";
    public static final int RESULT_EDIT = 0x51;
    public static final int RESULT_DELETE = 0x52;
    public static final String EXTRA_RESULT_MEAL_ID_LONG = "extra result meal id";

    MealDetailActivityComponent component;

    @Inject
    MealDetailFragment content;

    @NonNull
    protected MealDetailActivityComponent getComponent() {
        if (component == null) {
            component = DaggerMealDetailActivityComponent.builder()
                    .mealDetailActivityModel(new MealDetailActivityModel(this))
                    .build();
        }
        return component;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_detail_activity);
        ButterKnife.bind(this);
        getComponent().inject(this);
    }

    @Override
    public void editMealWithId(long mealId) {
        setResultAndFinish(RESULT_EDIT, mealId);
    }

    @Override
    public void deleteMealWithId(long mealId) {
        setResultAndFinish(RESULT_DELETE, mealId);
    }

    public void setResultAndFinish(int resultCode, long mealId) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_MEAL_ID_LONG, mealId);
        setResult(resultCode, intent);
        ActivityCompat.finishAfterTransition(this);
    }

    @OnClick(R.id.meal_detail_root)
    public void onClickedOutside() {
        ActivityCompat.finishAfterTransition(this);
    }
}
