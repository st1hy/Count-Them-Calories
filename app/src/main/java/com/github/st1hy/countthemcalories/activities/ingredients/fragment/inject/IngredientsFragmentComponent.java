package com.github.st1hy.countthemcalories.activities.ingredients.fragment.inject;

import com.github.st1hy.countthemcalories.activities.ingredients.fragment.view.IngredientsFragment;
import com.github.st1hy.countthemcalories.application.inject.ApplicationComponent;
import com.github.st1hy.countthemcalories.core.inject.PerFragment;
import com.github.st1hy.countthemcalories.core.permissions.PermissionModule;

import dagger.Component;

@PerFragment
@Component(modules = {IngredientsFragmentModule.class, PermissionModule.class},
        dependencies = ApplicationComponent.class)
public interface IngredientsFragmentComponent {

    void inject(IngredientsFragment fragment);
}
