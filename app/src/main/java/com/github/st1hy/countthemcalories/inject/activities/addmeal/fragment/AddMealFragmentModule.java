package com.github.st1hy.countthemcalories.inject.activities.addmeal.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.AddMealFragment;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.model.AddMealModel;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.model.MealIngredientsListModel;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.presenter.AddMealPresenter;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.presenter.AddMealPresenterImp;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.presenter.IngredientsListPresenter;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.view.AddMealView;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.view.AddMealViewController;
import com.github.st1hy.countthemcalories.activities.addmeal.fragment.view.IngredientsListAdapter;
import com.github.st1hy.countthemcalories.activities.addmeal.view.AddMealMenuAction;
import com.github.st1hy.countthemcalories.core.headerpicture.PictureModel;
import com.github.st1hy.countthemcalories.core.headerpicture.SelectPicturePresenter;
import com.github.st1hy.countthemcalories.core.headerpicture.SelectPicturePresenterImp;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.Meal;
import com.github.st1hy.countthemcalories.inject.PerFragment;
import com.github.st1hy.countthemcalories.inject.activities.addmeal.fragment.ingredientitems.IngredientListComponentFactory;
import com.google.common.base.Preconditions;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.subjects.PublishSubject;

import static org.parceler.Parcels.unwrap;

@Module
public class AddMealFragmentModule {

    public static final String EXTRA_MEAL_PARCEL = "edit meal parcel";
    public static final String EXTRA_INGREDIENT_PARCEL = "edit ingredient parcel";

    private final AddMealFragment fragment;
    @Nullable
    private final Bundle savedState;

    public AddMealFragmentModule(AddMealFragment fragment, @Nullable Bundle savedState) {
        this.fragment = fragment;
        this.savedState = savedState;
    }

    @Provides
    public AddMealPresenter providePresenter(AddMealPresenterImp presenter) {
        return presenter;
    }

    @Provides
    public AddMealView provideView(AddMealViewController addMealViewController) {
        return addMealViewController;
    }

    @Provides
    @Named("savedState")
    public Bundle getSavedState() {
        return savedState;
    }

    @Provides
    @PerFragment
    public MealIngredientsListModel provideListModel(@Named("savedState") Bundle savedState,
                                                     @NonNull Meal meal,
                                                     @Nullable IngredientTemplate extraIngredient) {

        if (savedState != null) {
            MealIngredientsListModel listModel = Parcels.unwrap(savedState.getParcelable(MealIngredientsListModel.SAVED_INGREDIENTS));
            listModel.setExtraIngredient(extraIngredient);
            return listModel;
        } else {
            List<Ingredient> ingredients;
            if (meal.hasIngredients()) {
                ingredients = meal.getIngredients();
            } else {
                ingredients = new ArrayList<>(5);
            }
            return new MealIngredientsListModel(ingredients, extraIngredient);
        }
    }

    @Provides
    @PerFragment
    public Meal provideMeal(@Named("savedState") Bundle savedState) {
        if (savedState != null) {
            return unwrap(savedState.getParcelable(AddMealModel.SAVED_MEAL_STATE));
        } else {
            Bundle arguments = fragment.getArguments();
            Preconditions.checkNotNull(arguments);
            Meal editedMeal = Parcels.unwrap(arguments.getParcelable(EXTRA_MEAL_PARCEL));
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
    public IngredientTemplate provideExtraIngredientTemplate() {
        Bundle arguments = fragment.getArguments();
        Preconditions.checkNotNull(arguments);
        IngredientTemplate ingredientTemplate = unwrap(arguments.getParcelable(EXTRA_INGREDIENT_PARCEL));
        arguments.remove(EXTRA_INGREDIENT_PARCEL);
        return ingredientTemplate;
    }

    @Provides
    public FragmentActivity provideFragmentActivity() {
        return fragment.getActivity();
    }

    @Provides
    public Observable<AddMealMenuAction> menuActionObservable(PublishSubject<AddMealMenuAction> actionPublishSubject) {
        return actionPublishSubject.asObservable();
    }

    @Named("fragmentRootView")
    @Provides
    public View rootView() {
        return fragment.getView();
    }

    @Provides
    @PerFragment
    @Named("ingredientListAdapter")
    public IngredientsListAdapter ingredientsAdapter(IngredientsListAdapter adapter, IngredientsListPresenter listPresenter) {
        listPresenter.setNotifier(adapter);
        return adapter;
    }

    @Provides
    public RecyclerView recyclerView(@Named("fragmentRootView")View rootView,
                                     @Named("ingredientListAdapter") IngredientsListAdapter adapter,
                                     @Named("activityContext") Context context) {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.add_meal_ingredients_list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        return recyclerView;
    }

    @Provides
    @PerFragment
    public SelectPicturePresenter picturePresenter(SelectPicturePresenterImp presenter) {
        return presenter;
    }

    @Provides
    public PictureModel pictureModel(AddMealModel model) {
        return model;
    }

    @Provides
    public IngredientListComponentFactory ingredientListComponentFactory(AddMealFragmentComponent component) {
        return component;
    }
}