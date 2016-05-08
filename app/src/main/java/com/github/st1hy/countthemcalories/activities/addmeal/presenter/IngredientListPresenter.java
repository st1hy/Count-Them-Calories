package com.github.st1hy.countthemcalories.activities.addmeal.presenter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addmeal.presenter.viewholder.IngredientItemViewHolder;
import com.github.st1hy.countthemcalories.activities.addmeal.model.MealIngredientsListModel;

import javax.inject.Inject;

import dagger.internal.Preconditions;

public class IngredientListPresenter extends RecyclerView.Adapter<IngredientItemViewHolder> {
    private final MealIngredientsListModel model;

    @Inject
    public IngredientListPresenter(@NonNull MealIngredientsListModel model) {
        this.model = model;
    }

    @Override
    public IngredientItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        Preconditions.checkNotNull(view);
        return new IngredientItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.add_meal_ingredient_item;
    }

    @Override
    public void onBindViewHolder(IngredientItemViewHolder holder, int position) {
//        holder.getName().setText;
    }

    @Override
    public int getItemCount() {
        return model.getItemsCount();
    }
}
