package com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.st1hy.countthemcalories.activities.addingredient.fragment.model.IngredientTagsModel;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenter;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.presenter.AddIngredientPresenterImp;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientFragment;
import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientView;
import com.github.st1hy.countthemcalories.activities.addingredient.view.AddIngredientScreen;
import com.github.st1hy.countthemcalories.core.FragmentDepends;
import com.github.st1hy.countthemcalories.core.inject.PerFragment;
import com.github.st1hy.countthemcalories.core.permissions.PermissionSubject;
import com.github.st1hy.countthemcalories.database.parcel.IngredientTypeParcel;
import com.github.st1hy.countthemcalories.database.unit.AmountUnitType;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class AddIngredientFragmentModule {

    public static final String ARG_AMOUNT_UNIT = "amount unit type";
    public static final String ARG_EDIT_REQUEST_ID_LONG = "edit ingredient extra request id";
    public static final String ARG_EDIT_INGREDIENT_PARCEL = "edit ingredient extra parcel";

    private final AddIngredientFragment fragment;
    private final Bundle savedState;

    public AddIngredientFragmentModule(AddIngredientFragment fragment, @Nullable Bundle savedState) {
        this.fragment = fragment;
        this.savedState = savedState;
    }

    @Provides
    public AddIngredientView provideView() {
        return fragment;
    }

    @Provides
    public AddIngredientScreen provideScreen() {
        return FragmentDepends.checkIsSubclass(fragment.getActivity(), AddIngredientScreen.class);
    }

    @Provides
    @PerFragment
    public AddIngredientPresenter providePresenter(AddIngredientPresenterImp presenter) {
        return presenter;
    }

    @Provides
    public PermissionSubject providePermissionSubject() {
        return FragmentDepends.checkIsSubclass(fragment.getActivity(), PermissionSubject.class);
    }

    @Provides
    @PerFragment
    public IngredientTagsModel provideIngredientTagModel(@Nullable @Named("savedState") Bundle savedState) {
        return new IngredientTagsModel(savedState);
    }

    @Provides
    @Nullable
    @Named("savedState")
    public Bundle provideSavedStateBundle() {
        return savedState;
    }

    @Provides
    @Named("arguments")
    public Bundle provideArguments() {
        return fragment.getArguments();
    }

    @Provides
    public Resources provideResources() {
        return fragment.getResources();
    }

    @Provides
    public AmountUnitType provideAmountUnitType(@Named("arguments") Bundle arguments) {
        return (AmountUnitType) arguments.getSerializable(ARG_AMOUNT_UNIT);
    }

    @Provides
    @Nullable
    public IngredientTypeParcel provideParcelSource(@Named("arguments") Bundle arguments) {
        return arguments.getParcelable(ARG_EDIT_INGREDIENT_PARCEL);
    }
}