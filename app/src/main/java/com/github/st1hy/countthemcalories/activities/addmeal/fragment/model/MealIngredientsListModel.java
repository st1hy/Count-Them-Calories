package com.github.st1hy.countthemcalories.activities.addmeal.fragment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.List;

@Parcel(Parcel.Serialization.BEAN)
public class MealIngredientsListModel {

    public static final String SAVED_INGREDIENTS = "add meal ingredient list";

    private final List<Ingredient> ingredients;
    private IngredientTemplate extraIngredient;

    @ParcelConstructor
    public MealIngredientsListModel(@NonNull List<Ingredient> ingredients,
                                    IngredientTemplate extraIngredient) {
        this.ingredients = ingredients;
        this.extraIngredient = extraIngredient;
    }

    @NonNull
    public Ingredient getItemAt(int position) {
        return ingredients.get(position);
    }

    public int getItemsCount() {
        return ingredients.size();
    }

    /**
     * @return position of added item
     */
    public int addIngredient(@NonNull Ingredient ingredient) {
        ingredients.add(ingredient);
        return ingredients.size() - 1;
    }

    public void modifyIngredient(final int position,
                                @NonNull Ingredient ingredient) {
        ingredients.set(position, ingredient);
    }

    public void removeIngredient(int position) {
        ingredients.remove(position);
    }

    public int indexOf(@NonNull Ingredient ingredient) {
        return ingredients.indexOf(ingredient);
    }

    @NonNull
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setExtraIngredient(@Nullable IngredientTemplate extraIngredient) {
        this.extraIngredient = extraIngredient;
    }

    @Nullable
    public IngredientTemplate removeExtraIngredientType() {
        IngredientTemplate extra = extraIngredient;
        extraIngredient = null;
        return extra;
    }

    public IngredientTemplate getExtraIngredient() {
        return extraIngredient;
    }
}
