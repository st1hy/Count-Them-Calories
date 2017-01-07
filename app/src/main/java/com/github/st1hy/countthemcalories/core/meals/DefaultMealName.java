package com.github.st1hy.countthemcalories.core.meals;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

public interface DefaultMealName {

    @NonNull
    String getBestGuessMealNameAt(@NonNull DateTime time);
}
