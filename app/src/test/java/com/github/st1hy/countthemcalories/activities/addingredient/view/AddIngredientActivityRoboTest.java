package com.github.st1hy.countthemcalories.activities.addingredient.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.st1hy.countthemcalories.BuildConfig;
import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.addingredient.inject.AddIngredientTestComponent;
import com.github.st1hy.countthemcalories.activities.addingredient.presenter.AddIngredientPresenter;
import com.github.st1hy.countthemcalories.activities.addingredient.presenter.AddIngredientPresenterImp;
import com.github.st1hy.countthemcalories.activities.tags.view.TagsActivity;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.database.IngredientTemplate;
import com.github.st1hy.countthemcalories.database.Tag;
import com.github.st1hy.countthemcalories.testrunner.RxRobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.List;

import rx.plugins.TestRxPlugins;

import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RxRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AddIngredientActivityRoboTest {

    private AddIngredientActivity activity;

    @Before
    public void setup() {
        TestRxPlugins.registerImmediateHook();
        activity = Robolectric.setupActivity(AddIngredientTestActivity.class);
    }

    @Test
    public void testActivityStart() {
        assertThat(activity, notNullValue());
        assertThat(activity.presenter, notNullValue());
        assertThat(activity.toolbar, notNullValue());
        assertThat(activity.getComponent(null), notNullValue());
        assertThat(activity.component, notNullValue());
        assertThat(activity.energyDensityValue, notNullValue());
        assertThat(activity.ingredientImage, notNullValue());
        assertThat(activity.name, notNullValue());
        assertThat(activity.tagsRecycler, notNullValue());
        assertThat(activity.selectUnit, notNullValue());
    }

    @SuppressLint("SetTextI18n")
    @Test
    public void testSaveButtonAction() throws Exception {
        activity.name.setText("Name");
        activity.energyDensityValue.setText("300.6");

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.onCreateOptionsMenu(new RoboMenu());
        shadowActivity.clickMenuItem(R.id.action_save);

        assertTrue(shadowActivity.isFinishing());
        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_OK));

        AddIngredientTestComponent component = (AddIngredientTestComponent) activity.component;
        DaoSession daoSession = component.getDaoSession();
        List<IngredientTemplate> list = daoSession.getIngredientTemplateDao().loadAll();
        assertThat(list.size(), equalTo(1));
    }

    @Test
    public void testImageButton() throws Exception {
        activity.ingredientImage.performClick();
        ShadowAlertDialog shadowAlertDialog = shadowOf(RuntimeEnvironment.application).getLatestAlertDialog();
        assertThat(shadowAlertDialog, notNullValue());
        assertThat(shadowAlertDialog.getTitle().toString(), equalTo(activity.getString(R.string.add_ingredient_image_select_title)));
        assertThat(shadowAlertDialog.getItems(), arrayWithSize(2));
        shadowAlertDialog.clickOnItem(0);
        assertThat(shadowOf(activity).getNextStartedActivity(), hasAction(Intent.ACTION_CHOOSER));
    }

    @Test
    public void testSaveInstance() throws Exception {
        Bundle bundle = new Bundle();
        activity.onSaveInstanceState(bundle);
        assertThat(bundle.isEmpty(), equalTo(false));
    }

    @Test
    public void testOnStop() throws Exception {
        AddIngredientPresenter presenterMock = Mockito.mock(AddIngredientPresenterImp.class);
        activity.presenter = presenterMock;
        activity.onStop();
        verify(presenterMock).onStop();
    }

    @Test
    public void testClickSelectUnit() throws Exception {
        activity.selectUnit.performClick();
        ShadowAlertDialog shadowAlertDialog = shadowOf(RuntimeEnvironment.application).getLatestAlertDialog();
        assertThat(shadowAlertDialog, notNullValue());
        assertThat(shadowAlertDialog.getTitle().toString(), equalTo(activity.getString(R.string.add_ingredient_select_unit_dialog_title)));
        CharSequence selectedItem = shadowAlertDialog.getItems()[1];
        shadowAlertDialog.clickOnItem(1);
        assertTrue(shadowAlertDialog.hasBeenDismissed());
        assertEquals(selectedItem, activity.selectUnit.getText());
    }


    @Test
    public void testAddRemoveTag() throws Exception {
        assertThat(activity.tagsRecycler.getChildCount(), equalTo(1));

        activity.tagsRecycler.getChildAt(0).findViewById(R.id.add_ingredient_category_add).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.IntentForResult activityForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(activityForResult.intent, hasAction(TagsActivity.ACTION_PICK_TAG));
        assertThat(activityForResult.intent, hasComponent(new ComponentName(activity, TagsActivity.class)));

        AddIngredientTestComponent component = (AddIngredientTestComponent) activity.component;
        DaoSession daoSession = component.getDaoSession();
        final Tag tag = daoSession.getTagDao().loadAll().get(0);

        Intent result = new Intent();
        result.putExtra(TagsActivity.EXTRA_TAG_ID, tag.getId());
        result.putExtra(TagsActivity.EXTRA_TAG_NAME, tag.getName());
        shadowActivity.receiveResult(activityForResult.intent, Activity.RESULT_OK, result);

        assertThat(activity.tagsRecycler.getChildCount(), equalTo(2));
        View tagView = activity.tagsRecycler.getChildAt(0);
        TextView textView = (TextView) tagView.findViewById(R.id.add_ingredient_category_name);
        assertThat(textView.getText().toString(), equalTo(tag.getName()));

        tagView.findViewById(R.id.add_ingredient_category_remove).performClick();
        assertThat(activity.tagsRecycler.getChildCount(), equalTo(1));
    }
}