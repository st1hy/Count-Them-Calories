package com.github.st1hy.countthemcalories.activities.addmeal.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.settings.model.SettingsModel;
import com.github.st1hy.countthemcalories.application.inject.TwoPlaces;
import com.github.st1hy.countthemcalories.core.Utils;
import com.github.st1hy.countthemcalories.database.Ingredient;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.unit.AmountUnit;
import com.github.st1hy.countthemcalories.database.unit.AmountUnitType;
import com.github.st1hy.countthemcalories.database.unit.EnergyDensity;
import com.github.st1hy.countthemcalories.database.unit.EnergyDensityUtils;
import com.github.st1hy.countthemcalories.database.unit.EnergyUnit;
import com.github.st1hy.countthemcalories.database.unit.Unit;
import com.github.st1hy.countthemcalories.inject.quantifier.context.AppContext;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.functions.Func1;

@Singleton
public class PhysicalQuantitiesModel {
    private final SettingsModel settingsModel;
    private final Resources resources;
    private final Context context;
    private Func1<Ingredient, Double> ingredientToEnergy;
    private Func1<Double, String> energyAsString;

    @Inject
    Utils utils;
    @Inject
    @TwoPlaces
    DecimalFormat decimalFormat;

    @Inject
    public PhysicalQuantitiesModel(@NonNull final SettingsModel settingsModel,
                                   @NonNull @AppContext Context context) {
        this.settingsModel = settingsModel;
        this.context = context;
        this.resources = context.getResources();
    }

    @NonNull
    public EnergyDensity convertToPreferred(@NonNull EnergyDensity energyDensity) {
        AmountUnit preferredAmountUnit = settingsModel.getAmountUnitFrom(energyDensity.getAmountUnitType());
        EnergyUnit preferredEnergyUnit = settingsModel.getEnergyUnit();
        return energyDensity.convertTo(preferredEnergyUnit, preferredAmountUnit);
    }

    /**
     * Formats energy density as "{value} {energy_unit} / {amount_unit}"
     */
    @NonNull
    public String format(@NonNull EnergyDensity energyDensity) {
        String energyUnit = resources.getString(energyDensity.getEnergyUnit().getNameRes());
        String amountUnit = resources.getString(energyDensity.getAmountUnit().getNameRes());
        String value = formatValue(energyDensity);
        return resources.getString(R.string.format_value_fraction, value, energyUnit, amountUnit);
    }

    /**
     * Formats energy density as "{value} {energy_unit} / {amount_unit}"
     */
    @NonNull
    public String formatValue(@NonNull EnergyDensity energyDensity) {
        return decimalFormat.format(energyDensity.getValue());
    }

    @NonNull
    public String formatUnit(@NonNull EnergyDensity energyDensity) {
        String energyUnit = resources.getString(energyDensity.getEnergyUnit().getNameRes());
        String amountUnit = resources.getString(energyDensity.getAmountUnit().getNameRes());
        return resources.getString(R.string.format_unit_fraction, energyUnit, amountUnit);
    }

    /**
     * Formats amount as "{amount} {unit_name}"
     */
    @NonNull
    public String format(double amount, @NonNull Unit unit) {
        return formatValueUnit(decimalFormat.format(amount), getUnitName(unit));
    }

    @NonNull
    private String formatValueUnit(@NonNull String value, @NonNull String unit) {
        return resources.getString(R.string.format_value_simple, value, unit);
    }

    @NonNull
    public String getUnitName(@NonNull Unit unit) {
        return settingsModel.getUnitName(unit);
    }

    @NonNull
    public String getEnergyUnitName() {
        return settingsModel.getUnitName(settingsModel.getEnergyUnit());
    }


    /**
     * Calculates energy from amount and energy density
     *
     * @param amount        how much substance it is
     * @param amountUnit    what unit is the amount in
     * @param energyDensity what is its energy density
     * @return amount of energy in unit of {@link EnergyDensity#getEnergyUnit()}
     */
    public double getEnergyAmountFrom(double amount, @NonNull AmountUnit amountUnit,
                                      @NonNull EnergyDensity energyDensity) {
        return energyDensity.getValue() * amount * amountUnit.getBase() / energyDensity.getAmountUnit().getBase();
    }

    /**
     * Calculates and formats energy from amount and energy density
     *
     * @param amount        how much substance it is
     * @param amountUnit    what unit is the amount in
     * @param energyDensity what is its energy density
     * @return formatted value of energy
     */
    @NonNull
    public String formatEnergyCount(double amount, @NonNull AmountUnit amountUnit,
                                    @NonNull EnergyDensity energyDensity) {
        double energyAmount = getEnergyAmountFrom(amount, amountUnit, energyDensity);
        return decimalFormat.format(energyAmount);
    }


    @NonNull
    public String formatEnergyCountAndUnit(double amount, AmountUnit amountUnit, EnergyDensity energyDensity) {
        double energyAmount = getEnergyAmountFrom(amount, amountUnit, energyDensity);
        return format(energyAmount, energyDensity.getEnergyUnit());
    }

    @NonNull
    public String formatAsEnergy(float value) {
        EnergyUnit energyUnit = settingsModel.getEnergyUnit();
        value /= energyUnit.getBase();
        return formatValueUnit(decimalFormat.format(value), getUnitName(energyUnit));
    }

    /**
     * Convenience method for calling {@link #convertAmount(double, AmountUnit, AmountUnit)}
     * with "from" argument that's default database unit for current type.
     * <p/>
     * Internally it uses {@link EnergyDensityUtils#getDefaultAmountUnit(AmountUnitType)} for resolving
     * current unit of provided value
     */
    public double convertAmountFromDatabase(double databaseAmount,
                                            @NonNull AmountUnit targetUnit) {
        AmountUnit databaseUnit = EnergyDensityUtils.getDefaultAmountUnit(targetUnit.getType());
        return convertAmount(databaseAmount, databaseUnit, targetUnit);
    }

    /**
     * Convenience method for calling {@link #convertAmount(double, AmountUnit, AmountUnit)}
     * with "to" argument that's default database unit for current type.
     * <p/>
     * Internally it uses {@link EnergyDensityUtils#getDefaultAmountUnit(AmountUnitType)} for resolving
     * target unit of provided value
     */
    public double convertAmountToDatabase(double sourceAmount,
                                              @NonNull AmountUnit sourceUnit) {
        AmountUnit databaseUnit = EnergyDensityUtils.getDefaultAmountUnit(sourceUnit.getType());
        return convertAmount(sourceAmount, sourceUnit, databaseUnit);
    }

    public double convertAmount(double amount,
                                @NonNull AmountUnit from,
                                @NonNull AmountUnit to) {
        return amount * from.getBase() / to.getBase();
    }

    @NonNull
    public Func1<Ingredient, Double> mapToEnergy() {
        if (ingredientToEnergy == null) {
            ingredientToEnergy = ingredient -> {
                IngredientTemplate ingredientTemplate = ingredient.getIngredientTypeOrNull();
                EnergyDensity databaseEnergyDensity = EnergyDensity.from(ingredientTemplate);
                EnergyDensity energyDensity = convertToPreferred(databaseEnergyDensity);
                AmountUnit amountUnit = EnergyDensityUtils.getDefaultAmountUnit(ingredientTemplate.getAmountType());
                return getEnergyAmountFrom(ingredient.getAmount(), amountUnit, energyDensity);
            };
        }
        return ingredientToEnergy;
    }

    @NonNull
    public Func1<Double, String> energyAsString() {
        if (energyAsString == null) {
            energyAsString = decimal -> format(decimal, settingsModel.getEnergyUnit());
        }
        return energyAsString;
    }

    /**
     * @return function that creates moving sum on each element
     */
    @NonNull
    public Func1<Double, Double> sumAll() {
        return new Func1<Double, Double>() {
            double sum = 0.0;

            @Override
            public Double call(Double decimal) {
                if (decimal != null) sum = sum + decimal;
                return sum;
            }
        };
    }

    @NonNull
    public Func1<BigDecimal, BigDecimal> setScale(final int scale) {
        return decimal -> decimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    @NonNull
    public String formatTime(@NonNull DateTime date) {
        if (DateFormat.is24HourFormat(context)) {
            return date.toString("HH:mm");
        } else {
            return date.toString("HH:mm aa");
        }
    }

    @NonNull
    public String formatDate(@NonNull DateTime dateTime) {
        return dateTime.toString(getBestDateDatePattern("MM/dd"));
    }

    private String getBestDateDatePattern(String pattern) {
        if (utils.hasApi18()) {
            pattern = getBestDateDatePatternApi18(pattern);
        }
        return pattern;
    }

    @TargetApi(18)
    private String getBestDateDatePatternApi18(String pattern) {
        return DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern);
    }
}
