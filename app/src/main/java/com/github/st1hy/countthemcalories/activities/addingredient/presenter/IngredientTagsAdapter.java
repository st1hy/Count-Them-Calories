package com.github.st1hy.countthemcalories.activities.addingredient.presenter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addingredient.model.tag.IngredientTagsModel;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientView;
import com.github.st1hy.countthemcalories.activities.addingredient.view.holder.AddNewTagViewHolder;
import com.github.st1hy.countthemcalories.activities.addingredient.view.holder.ItemTagViewHolder;
import com.github.st1hy.countthemcalories.activities.addingredient.view.holder.TagViewHolder;

import javax.inject.Inject;

import rx.functions.Action1;

public class IngredientTagsAdapter extends RecyclerView.Adapter<TagViewHolder> {
    static final int TAG = R.layout.add_ingredient_tag;
    static final int ADD_TAG = R.layout.add_ingredient_add_tag;
    static final int ADD_CATEGORY_FIELDS_SIZE = 1;

    final IngredientTagsModel model;
    final AddIngredientView view;

    @Override
    public int getItemViewType(int position) {
        if (position < model.getSize()) {
            return TAG;
        } else {
            return ADD_TAG;
        }
    }

    @Inject
    public IngredientTagsAdapter(@NonNull IngredientTagsModel model, @NonNull AddIngredientView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        switch (viewType) {
            case TAG:
                return new ItemTagViewHolder(view);
            case ADD_TAG:
                return new AddNewTagViewHolder(view);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TAG:
                onBindTag((ItemTagViewHolder) holder, position);
                break;
            case ADD_TAG:
                //noinspection ConstantConditions
                onBindAddTag((AddNewTagViewHolder) holder);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    void onBindTag(@NonNull ItemTagViewHolder viewHolder, int position) {
        viewHolder.setCategoryName(model.getTagAt(position).getName());
    }

    private void onBindAddTag(@NonNull AddNewTagViewHolder holder) {
        holder.addNewObservable().subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                view.openSelectTagScreen();
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.getSize() + ADD_CATEGORY_FIELDS_SIZE;
    }
}