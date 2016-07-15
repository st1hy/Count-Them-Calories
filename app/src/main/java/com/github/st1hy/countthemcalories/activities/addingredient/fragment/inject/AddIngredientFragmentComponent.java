package com.github.st1hy.countthemcalories.activities.addingredient.fragment.inject;

import com.github.st1hy.countthemcalories.activities.addingredient.fragment.view.AddIngredientFragment;
import com.github.st1hy.countthemcalories.application.inject.ApplicationComponent;
import com.github.st1hy.countthemcalories.core.inject.PerFragment;

import dagger.Component;

@PerFragment
@Component(modules = AddIngredientFragmentModule.class, dependencies = ApplicationComponent.class)
public interface AddIngredientFragmentComponent {

    void inject(AddIngredientFragment fragment);
}
