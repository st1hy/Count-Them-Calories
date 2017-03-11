package com.github.st1hy.countthemcalories.activities.addmeal.fragment.inject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.st1hy.countthemcalories.activities.addmeal.AddMealActivity;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.model.AddMealModel;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.Meal;
import com.github.st1hy.countthemcalories.inject.PerFragment;
import com.github.st1hy.countthemcalories.inject.quantifier.bundle.FragmentSavedState;
import com.github.st1hy.countthemcalories.inject.quantifier.datetime.NewMealDate;

import org.joda.time.DateTime;

import java.math.BigDecimal;

import dagger.Module;
import dagger.Provides;

import static org.parceler.Parcels.unwrap;

@Module
public abstract class AddMealArgumentsModule {

    @Provides
    @PerFragment
    public static Meal provideMeal(@Nullable @FragmentSavedState Bundle savedState,
                                   Intent intent) {
        if (savedState != null) {
            return unwrap(savedState.getParcelable(AddMealModel.SAVED_MEAL_STATE));
        } else {
            Meal editedMeal = unwrap(intent.getParcelableExtra(AddMealActivity.EXTRA_MEAL_PARCEL));
            if (editedMeal != null) {
                return editedMeal;
            } else {
                editedMeal = new Meal();
                editedMeal.setName("");
                editedMeal.setImageUri(Uri.EMPTY);
                return editedMeal;
            }
        }
    }

    @Provides
    @Nullable
    public static IngredientTemplate provideExtraIngredientTemplate(Intent intent) {
        IngredientTemplate ingredientTemplate = unwrap(
                intent.getParcelableExtra(AddMealActivity.EXTRA_INGREDIENT_PARCEL)
        );
        intent.removeExtra(AddMealActivity.EXTRA_INGREDIENT_PARCEL);
        return ingredientTemplate;
    }

    @Provides
    @Nullable
    public static Ingredient extraIngredient(@Nullable IngredientTemplate extraTemplate) {
        if (extraTemplate != null) {
            return new Ingredient(extraTemplate, BigDecimal.ZERO);
        } else {
            return null;
        }
    }

    @Provides
    @Nullable
    @NewMealDate
    public static DateTime newMealDate(Intent intent) {
        return (DateTime) intent.getSerializableExtra(AddMealActivity.EXTRA_NEW_MEAL_DATE);
    }
}