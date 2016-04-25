package com.github.st1hy.countthemcalories.database;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class DatabaseGenerator {
    private static final String jodaTimeClassName = "org.joda.time.DateTime";
    private static final String jodaTimeConverterClassName = "com.github.st1hy.countthemcalories.database.property.JodaTimePropertyConverter";

    private static final String uriClassName = "android.net.Uri";
    private static final String uriConverterClassName = "com.github.st1hy.countthemcalories.database.property.UriPropertyConverter";

    private static final String energyDensityClassName = "com.github.st1hy.countthemcalories.database.unit.EnergyDensity";
    private static final String energyDensityConverterClassName = "com.github.st1hy.countthemcalories.database.property.EnergyDensityPropertyConverter";

    private static final String bigDecimalClassName = "java.math.BigDecimal";
    private static final String bigDecimalConverterClassName = "com.github.st1hy.countthemcalories.database.property.BigDecimalPropertyConverter";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1001, "com.github.st1hy.countthemcalories.database");
        schema.setDefaultJavaPackageDao("com.github.st1hy.countthemcalories.database");
        schema.setDefaultJavaPackageDao("com.github.st1hy.countthemcalories.database");

        Entity ingredientTemplate = schema.addEntity("IngredientTemplate");
        ingredientTemplate.setTableName("INGREDIENTS_TEMPLATE");
        ingredientTemplate.addIdProperty().index().unique().autoincrement().getProperty();
        ingredientTemplate.addStringProperty("name").index().notNull();
        ingredientTemplate.addStringProperty("imageUri").customType(uriClassName, uriConverterClassName);
        ingredientTemplate.addLongProperty("creationDate")
                .customType(jodaTimeClassName, jodaTimeConverterClassName).notNull();
        ingredientTemplate.addStringProperty("energyDensity")
                .customType(energyDensityClassName, energyDensityConverterClassName)
                .notNull();

        Entity ingredientComponent = schema.addEntity("Ingredient");
        ingredientComponent.setTableName("INGREDIENTS");
        ingredientComponent.addIdProperty().index().unique().autoincrement().getProperty();
        ingredientComponent.addStringProperty("amount")
                .customType(bigDecimalClassName, bigDecimalConverterClassName).notNull();
        Property isInMealId = ingredientComponent.addLongProperty("partOfMealId").getProperty();
        Property ingredientTypeId = ingredientComponent.addLongProperty("ingredientTypeId").getProperty();

        Entity meal = schema.addEntity("Meal");
        meal.setTableName("MEALS");
        meal.addIdProperty().index().unique().autoincrement().getProperty();
        meal.addStringProperty("name").index();
        meal.addLongProperty("creationDate").customType(jodaTimeClassName, jodaTimeConverterClassName)
                .notNull();
        Property consumptionDayId = meal.addLongProperty("consumptionDayId").getProperty();

        Entity day = schema.addEntity("Day");
        day.setTableName("DAYS");
        day.addIdProperty().index().unique().autoincrement().getProperty();
        day.addLongProperty("date").customType(jodaTimeClassName, jodaTimeConverterClassName)
                .unique().index().notNull();

        //what was eaten this day
        day.addToMany(meal, consumptionDayId, "eatenMeals");
        //when this meal was eaten
        meal.addToOne(day, consumptionDayId, "consumptionDay");
        //what ingredients is this meal is composed of
        meal.addToMany(ingredientComponent, isInMealId, "ingredients");
        //what meal is this ingredient part of
        ingredientComponent.addToOne(meal, isInMealId, "partOfMeal");
        //what type is this ingredient
        ingredientComponent.addToOne(ingredientTemplate, ingredientTypeId, "ingredientType");
        //What ingredients contain this type
        ingredientTemplate.addToMany(ingredientComponent, ingredientTypeId, "childIngredients");

        //what ingredient types is this meal composed of

//        ContentProvider ingredientsContentProvider = ingredientTemplate.addContentProvider();
//        ingredientsContentProvider.setBasePath("ingredients");
//        ContentProvider mealsContentProvider = meal.addContentProvider();
//        mealsContentProvider.setBasePath("meals");
//        ContentProvider dayContentProvider = day.addContentProvider();
//        dayContentProvider.setBasePath("days");

        new DaoGenerator().generateAll(schema, "database/src/main/java", "database/src/main/java", "database/src/androidTest/java");
    }

}
