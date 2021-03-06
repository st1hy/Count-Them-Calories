package com.github.st1hy.countthemcalories.activities.addmeal.view;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.widget.TimePicker;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addmeal.AddMealActivity;
import com.github.st1hy.countthemcalories.activities.ingredients.IngredientsActivity;
import com.github.st1hy.countthemcalories.activities.ingredients.view.IngredientActivityTest;
import com.github.st1hy.countthemcalories.application.CaloriesCounterApplication;
import com.github.st1hy.countthemcalories.core.headerpicture.TestPicturePicker;
import com.github.st1hy.countthemcalories.core.rx.RxImageLoadingIdlingResource;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.inject.ApplicationTestComponent;
import com.github.st1hy.countthemcalories.rules.ApplicationComponentRule;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.st1hy.countthemcalories.actions.CTCViewActions.setTime;
import static com.github.st1hy.countthemcalories.activities.ingredients.view.IngredientActivityTest.exampleIngredients;
import static com.github.st1hy.countthemcalories.core.headerpicture.HeaderPicturePickerUtils.injectUriOnMatch;
import static com.github.st1hy.countthemcalories.matchers.CTCMatchers.galleryIntentMatcher;
import static com.github.st1hy.countthemcalories.matchers.ImageViewMatchers.withDrawable;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class AddMealActivityTest {

    private final ApplicationComponentRule componentRule = new ApplicationComponentRule(getTargetContext());
    public final IntentsTestRule<AddMealActivity> main = new IntentsTestRule<>(AddMealActivity.class);

    @Rule
    public final TestRule rule = RuleChain.outerRule(componentRule).around(main);

    @Before
    public void onSetUp() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new ActivityResult(Activity.RESULT_OK, null));

        ApplicationTestComponent component = (ApplicationTestComponent) ((CaloriesCounterApplication) getTargetContext().getApplicationContext()).getComponent();
        DaoSession session = component.getDaoSession();
        IngredientActivityTest.addExampleIngredientsTagsAndJoin(session);
    }


    @Test
    public void testDisplaysTitle() {
        onView(allOf(withChild(ViewMatchers.withText(R.string.add_meal_title)), withId(R.id.image_header_toolbar)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testCanShowErrorOnSave() {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.action_save)).perform(click());
        onView(withText(R.string.add_meal_ingredients_empty_error)).check(matches(isDisplayed()));
        assertNoUnverifiedIntents();
        assertThat(main.getActivity().isFinishing(), equalTo(false));
    }

    @Test
    public void testSetMealName() {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_meal_name)).check(matches(isDisplayed()))
                .perform(typeTextIntoFocusedView("My meal"))
                .check(matches(withText("My meal")));
    }

    @Test
    public void testSelectImageFromGallery() {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        Intent intent = new Intent();
        intent.setData(TestPicturePicker.resourceToUri(getContext(), android.R.drawable.ic_input_add));
        intending(galleryIntentMatcher).respondWith(new ActivityResult(Activity.RESULT_OK, intent));
        RxImageLoadingIdlingResource rxImageLoadingIdlingResource = RxImageLoadingIdlingResource.registerAndGet();
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(R.string.add_meal_image_select_from_camera))
                .check(matches(isDisplayed()));
        onView(withText(R.string.add_meal_image_select_title))
                .check(matches(isDisplayed()));
        onView(withText(R.string.add_meal_image_select_from_gallery))
                .check(matches(isDisplayed()))
                .perform(click());
        intended(galleryIntentMatcher);
        onView(withId(R.id.image_header_image_view))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(any(BitmapDrawable.class))));
        rxImageLoadingIdlingResource.unregister();
    }

    @Test
    public void testSelectImageFromGalleryUserCanceled() {
        intending(galleryIntentMatcher).respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(R.string.add_meal_image_select_from_gallery))
                .check(matches(isDisplayed()))
                .perform(click());
        intended(galleryIntentMatcher);
        //If doesn't crash is good
    }

    @Test
    public void testRemoveImage() throws Exception {
        testSelectImageFromGallery();

        onView(withId(R.id.image_header_image_view)).perform(click());
        onView(withText(R.string.add_meal_image_remove)).perform(click());
        onView(withId(R.id.image_header_image_view))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(not(any(BitmapDrawable.class)))));
    }

    @Test
    public void testSelectImageFromCamera() {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        AddMealActivity activity = main.getActivity();
        final Uri uri = TestPicturePicker.resourceToUri(activity, android.R.drawable.ic_input_add);
        final Matcher<Intent> cameraIntentMatcher = allOf(
                hasAction(MediaStore.ACTION_IMAGE_CAPTURE),
                injectUriOnMatch(activity, uri)
        );
        intending(cameraIntentMatcher).respondWith(new ActivityResult(Activity.RESULT_OK, null));
        RxImageLoadingIdlingResource rxImageLoadingIdlingResource = RxImageLoadingIdlingResource.registerAndGet();
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(R.string.add_meal_image_select_from_camera))
                .check(matches(isDisplayed()))
                .perform(click());
        intended(cameraIntentMatcher);
        onView(withId(R.id.image_header_image_view))
                .check(matches(isDisplayed()))
                .check(matches(withDrawable(any(BitmapDrawable.class))));
        rxImageLoadingIdlingResource.unregister();
    }

    @Test
    public void testSelectImageFromCameraUserCanceled() {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        final Matcher<Intent> cameraIntentMatcher = hasAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intending(cameraIntentMatcher).respondWith(new ActivityResult(Activity.RESULT_CANCELED, null));
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(R.string.add_meal_image_select_from_camera))
                .check(matches(isDisplayed()))
                .perform(click());
        intended(cameraIntentMatcher);
        //If doesn't crash is good
    }

    @Test
    public void testAddIngredientFab() throws Exception {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_meal_fab_add_ingredient))
                .perform(click());
        testAddIngredient();
        onView(withId(R.id.add_meal_fab_add_ingredient))
                .perform(click());
        testAddIngredient2();
    }


    @Test
    public void testAddIngredientCancel() throws Exception {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_meal_fab_add_ingredient)).check(matches(isDisplayed()))
                .perform(click());
        intended(allOf(hasAction(IngredientsActivity.ACTION_SELECT_INGREDIENT),
                hasComponent(new ComponentName(getTargetContext(), IngredientsActivity.class))));
        onView(allOf(withId(R.id.ingredients_content),isAssignableFrom(RecyclerView.class)))
                .check(matches(isDisplayed()));
        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()));
        onView(withHint(R.string.add_meal_ingredient_amount_hint)).check(matches(isDisplayed()))
                .perform(typeTextIntoFocusedView("42.6"));
        onView(withId(R.id.add_meal_ingredient_remove)).check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));

        onView(withText(exampleIngredients[0].getName())).check(doesNotExist());
        onView(withText("42.6 g")).check(doesNotExist());
    }

    private void testAddIngredient() {
        intended(allOf(hasAction(IngredientsActivity.ACTION_SELECT_INGREDIENT),
                hasComponent(new ComponentName(getTargetContext(), IngredientsActivity.class))));
        onView(allOf(withId(R.id.ingredients_content), isAssignableFrom(RecyclerView.class))).check(matches(isDisplayed()));
        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()));
        onView(withHint(R.string.add_meal_ingredient_amount_hint))
                .perform(closeSoftKeyboard())
                .check(matches(isDisplayed()))
                .perform(typeTextIntoFocusedView("42.6"));
        onView(withId(R.id.add_meal_ingredient_accept)).check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));

        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()));
        onView(withText("42.6 g")).check(matches(isDisplayed()));
    }

    private void testAddIngredient2() {
        onView(allOf(withId(R.id.ingredients_content),isAssignableFrom(RecyclerView.class)))
                .check(matches(isDisplayed()));
        onView(withText(exampleIngredients[1].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(exampleIngredients[1].getName())).check(matches(isDisplayed()));
        onView(withHint(R.string.add_meal_ingredient_amount_hint)).check(matches(isDisplayed()))
                .perform(closeSoftKeyboard())
                .perform(typeTextIntoFocusedView("8"));
        onView(withId(R.id.add_meal_ingredient_accept)).check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));

        onView(withText(exampleIngredients[1].getName())).check(matches(isDisplayed()));
        onView(withText("8 ml")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditIngredient() throws Exception {
        testAddIngredientFab();

        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withHint(R.string.add_meal_ingredient_amount_hint)).check(matches(isDisplayed()))
                .perform(clearText())
                .perform(typeTextIntoFocusedView("12.06"));
        onView(withId(R.id.add_meal_ingredient_accept)).check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));

        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()));
        onView(withText("12.06 g")).check(matches(isDisplayed()));
    }

    @Test
    public void testRemoveIngredient() throws Exception {
        testAddIngredientFab();

        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withHint(R.string.add_meal_ingredient_amount_hint)).check(matches(isDisplayed()))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.add_meal_ingredient_remove)).check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));
        onView(withText(exampleIngredients[0].getName())).check(doesNotExist());
    }

    @Test
    public void testAddIngredientWithIncorrectValue() throws Exception {
        onView(withId(R.id.add_meal_name)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_meal_fab_add_ingredient)).check(matches(isDisplayed()))
                .perform(click());
        intended(allOf(hasAction(IngredientsActivity.ACTION_SELECT_INGREDIENT),
                hasComponent(new ComponentName(getTargetContext(), IngredientsActivity.class))));
        onView(allOf(withId(R.id.ingredients_content),isAssignableFrom(RecyclerView.class)))
                .check(matches(isDisplayed()));
        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()))
                .perform(click());
        onView(withText(exampleIngredients[0].getName()))
                .perform(closeSoftKeyboard())
                .check(matches(isDisplayed()));
        onView(withId(R.id.add_meal_ingredient_accept)).check(matches(isDisplayed()))
                .perform(click());
        onView(withHint(R.string.add_meal_ingredient_amount_hint))
                .check(matches(hasErrorText(getTargetContext().getString(R.string.add_meal_amount_error_empty))))
                .perform(typeTextIntoFocusedView("0"))
                .check(matches(hasErrorText(getTargetContext().getString(R.string.add_meal_amount_error_zero))))
                .perform(typeTextIntoFocusedView("1"));
        onView(withId(R.id.add_meal_ingredient_accept)).check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.image_header_image_view)).check(matches(isDisplayed()));

        onView(withText(exampleIngredients[0].getName())).check(matches(isDisplayed()));
        onView(withText("1 g")).check(matches(isDisplayed()));
    }

    @Test
    public void testChangeTime() {
        onView(withId(R.id.add_meal_time_value)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(12, 12));
        onView(withText("OK")).perform(click());
        onView(allOf(withId(R.id.add_meal_time_value), withText(startsWith("12:12")))).check(matches(isDisplayed()));
    }

}
