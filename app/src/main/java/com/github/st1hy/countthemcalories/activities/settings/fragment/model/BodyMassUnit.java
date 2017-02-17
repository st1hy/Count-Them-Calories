package com.github.st1hy.countthemcalories.activities.settings.fragment.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.database.unit.AmountUnit;
import com.github.st1hy.countthemcalories.database.unit.AmountUnitType;

import java.math.BigDecimal;

public enum BodyMassUnit implements AmountUnit {

    KG(1, BigDecimal.ONE, R.string.body_weight_unit_kg) {
        @NonNull
        @Override
        public AmountUnit getBaseUnit() {
            return KG;
        }
    },
    POUND(2, BigDecimal.valueOf(45359237, 8), R.string.body_weight_unit_pound) {
        @NonNull
        @Override
        public AmountUnit getBaseUnit() {
            return KG;
        }
    }
    ;
    private final int ordinal;
    private final BigDecimal base;
    @StringRes
    private final int stringResId;

    BodyMassUnit(int ordinal, BigDecimal base, @StringRes int stringResId) {
        this.ordinal = ordinal;
        this.base = base;
        this.stringResId = stringResId;
    }

    @NonNull
    @Override
    public BigDecimal getBase() {
        return base;
    }

    @StringRes
    @Override
    public int getNameRes() {
        return stringResId;
    }


    @NonNull
    @Override
    public AmountUnitType getType() {
        return AmountUnitType.MASS;
    }

    public int getOrdinal() {
        return ordinal;
    }

    @Nullable
    public static BodyMassUnit fromOrdinal(int ordinal) {
        for (BodyMassUnit unit : values()) {
            if (unit.ordinal == ordinal) return unit;
        }
        return null;
    }
}
