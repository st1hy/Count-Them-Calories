package com.github.st1hy.countthemcalories.activities.addingredient.view;

import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.activities.tags.fragment.model.Tags;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;

import rx.Observable;

public abstract class AddIngredientScreenDelegate implements AddIngredientScreen {

    protected abstract AddIngredientScreen getDelegate();

    @Override
    public void onIngredientTemplateCreated(@NonNull IngredientTemplate template) {
        getDelegate().onIngredientTemplateCreated(template);
    }

    @Override
    @NonNull
    @CheckResult
    public Observable.Transformer<Tags, Tags> selectTags() {
        return getDelegate().selectTags();
    }

    @Override
    public void showInWebBrowser(@NonNull Uri address) {
        getDelegate().showInWebBrowser(address);
    }

}
