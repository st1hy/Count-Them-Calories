package com.github.st1hy.countthemcalories.database.unit;

import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.database.BuildConfig;

import java.math.BigDecimal;

import timber.log.Timber;

public class EnergyDensityUtils {
    public static final BigDecimal KJ_AT_GRAM_IN_KCAL_AT_GRAM = BigDecimal.valueOf(4184, 3);
    public static final BigDecimal KJ_AT_G_IN_KCAL_AT_100_GRAM = BigDecimal.valueOf(4184, 5);
    public static final BigDecimal KJ_AT_G_IN_KJ_AT_G = BigDecimal.ONE;
    public static final BigDecimal KJ_AT_G_IN_KJ_AT_100_G = BigDecimal.valueOf(1, 2);

    public static Enum<? extends EnergyDensityUnit>[] getUnits(@NonNull AmountUnitType unitType) {
        switch (unitType) {
            case VOLUME:
                return VolumetricEnergyDensityUnit.values();
            case MASS:
                return GravimetricEnergyDensityUnit.values();
            default:
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    public static String getString(@NonNull EnergyDensityUnit unit) {
        return unit.getAmountUnitType().name() + "@" + unit.name();
    }

    @NonNull
    public static EnergyDensityUnit fromString(@NonNull String string) {
        String[] split = string.split("@");
        if (split.length != 2) throw new IllegalArgumentException();
        AmountUnitType unitType = AmountUnitType.valueOf(split[0]);
        EnergyDensityUnit[] units = (EnergyDensityUnit[]) getUnits(unitType);
        for (EnergyDensityUnit unit : units) {
            if (unit.name().equals(split[1])) return unit;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @return returns Energy density of provided unit and string representation of big decimal value
     * or 0 if string is not in correct format
     */
    @NonNull
    public static EnergyDensity getOrZero(@NonNull EnergyDensityUnit unit, @NonNull String energyValue) {
        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal(energyValue);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) Timber.w("Value: '%s' is not valid BigInteger", energyValue);
            bigDecimal = BigDecimal.ZERO;
        }
        return new EnergyDensity(unit, bigDecimal);
    }
}
