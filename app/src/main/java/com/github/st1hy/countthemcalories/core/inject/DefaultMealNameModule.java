package com.github.st1hy.countthemcalories.core.inject;

import com.github.st1hy.countthemcalories.core.meals.DefaultMealName;
import com.github.st1hy.countthemcalories.core.meals.DefaultMealNameImpl;
import com.github.st1hy.countthemcalories.core.meals.DefaultNameEn;
import com.github.st1hy.countthemcalories.core.meals.DefaultNamePl;
import com.github.st1hy.countthemcalories.core.meals.DefaultNameSelector;

import java.util.Locale;
import java.util.Map;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public abstract class DefaultMealNameModule {

    @Provides
    public static DefaultNameSelector defaultMealNameEngine(
            Map<String, DefaultNameSelector> supported,
            @Named("langCode") String langCode) {
        DefaultNameSelector defaultMealName = supported.get(langCode);
        if (defaultMealName == null) {
            defaultMealName = supported.get("en");
        }
        return defaultMealName;
    }

    @Provides
    @Named("langCode")
    public static String langCode() {
        return Locale.getDefault().getLanguage();
    }

    @Binds
    abstract DefaultMealName defaultMealName(DefaultMealNameImpl defaultMealName);

    @Binds
    @IntoMap
    @StringKey("pl")
    abstract DefaultNameSelector defaultMealNameEnginePL(DefaultNamePl engine);

    @Binds
    @IntoMap
    @StringKey("en")
    abstract DefaultNameSelector defaultMealNameEngineEN(DefaultNameEn engine);
}
