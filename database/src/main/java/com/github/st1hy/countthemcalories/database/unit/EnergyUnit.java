package com.github.st1hy.countthemcalories.database.unit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.github.st1hy.countthemcalories.database.R;

import java.math.BigDecimal;

public enum EnergyUnit implements Unit {
    KJ(1, BigDecimal.ONE, R.string.unit_kj),
    KCAL(2, BigDecimal.valueOf(4184, 3), R.string.unit_kcal);

    private final int id;
    private final BigDecimal inKJ;
    private final int nameRes;

    EnergyUnit(int id, @NonNull BigDecimal inKJ, int nameRes) {
        this.id = id;
        this.inKJ = inKJ;
        this.nameRes = nameRes;
    }

    /**
     * @return base value of one converted into kJ
     */
    @NonNull
    public BigDecimal getBase() {
        return inKJ;
    }

    @StringRes
    public int getNameRes() {
        return nameRes;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public static EnergyUnit fromId(int id) {
        switch (id) {
            case 1:
                return KJ;
            case 2:
                return KCAL;
        }
        return null;
    }
}