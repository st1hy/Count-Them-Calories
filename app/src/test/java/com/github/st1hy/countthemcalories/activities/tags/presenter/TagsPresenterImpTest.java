package com.github.st1hy.countthemcalories.activities.tags.presenter;


import com.github.st1hy.countthemcalories.BuildConfig;
import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.tags.model.TagsActivityModel;
import com.github.st1hy.countthemcalories.activities.tags.model.TagsModel;
import com.github.st1hy.countthemcalories.activities.tags.presenter.viewholder.TagItemViewHolder;
import com.github.st1hy.countthemcalories.activities.tags.view.TagsView;
import com.github.st1hy.countthemcalories.core.event.DbProcessing;
import com.github.st1hy.countthemcalories.core.ui.Visibility;
import com.github.st1hy.countthemcalories.database.Tag;
import com.github.st1hy.countthemcalories.testrunner.RxRobolectricGradleTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.plugins.TestRxPlugins;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RxRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TagsPresenterImpTest {

    private TagsView view;
    private TagsModel model;
    private TagsActivityModel activityModel;
    private TagsPresenterImp presenter;

    @Before
    public void setup() {
        TestRxPlugins.registerImmediateHook();
        view = Mockito.mock(TagsView.class);
        model = Mockito.mock(TagsModel.class);
        activityModel = Mockito.mock(TagsActivityModel.class);
        presenter = new TagsPresenterImp(view, model, activityModel);
    }

    @Test
    public void testOnStart() throws Exception {
        when(model.getDbProcessingObservable()).thenReturn(Observable.just(DbProcessing.NOT_STARTED));
        when(model.getAllObservable()).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onStart();

        verify(model).getDbProcessingObservable();
        verify(model).getAllObservable();
        verify(view).setNoTagsButtonVisibility(Visibility.GONE);
        verify(view).setDataRefreshing(false);
        verifyNoMoreInteractions(model, view);
    }

    @Test
    public void testOnStartWithLoadedDataEmpty() throws Exception {
        when(model.getDbProcessingObservable()).thenReturn(Observable.just(DbProcessing.FINISHED));
        when(model.getItemCount()).thenReturn(0);
        when(model.getAllObservable()).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onStart();

        verify(model).getDbProcessingObservable();
        verify(model).getItemCount();
        verify(view).setNoTagsButtonVisibility(Visibility.VISIBLE);
        verify(view).setDataRefreshing(false);
        verifyNoMoreInteractions(model, view);
    }

    @Test
    public void testOnStartWithLoadedDataNotEmpty() throws Exception {
        when(model.getDbProcessingObservable()).thenReturn(Observable.just(DbProcessing.FINISHED));
        when(model.getItemCount()).thenReturn(10);

        presenter.onStart();

        verify(model).getDbProcessingObservable();
        verify(model).getItemCount();
        verify(view).setNoTagsButtonVisibility(Visibility.GONE);
        verify(view).setDataRefreshing(false);
        verifyNoMoreInteractions(model, view);
    }

    @Test
    public void testOnStartWithLoadingInProgress() throws Exception {
        when(model.getDbProcessingObservable()).thenReturn(Observable.just(DbProcessing.STARTED));

        presenter.onStart();

        verify(model).getDbProcessingObservable();
        verify(view).setNoTagsButtonVisibility(Visibility.GONE);
        verify(view).setDataRefreshing(true);
        verifyNoMoreInteractions(model, view);
    }

    @Test
    public void testAddTagClick() throws Exception {
        testOnStartWithLoadedDataNotEmpty();
        final String tagName = "Tag name";
        final int position = 230;
        when(view.showEditTextDialog(anyInt())).thenReturn(Observable.just(tagName));
        when(model.addNewAndRefresh(any(Tag.class))).thenReturn(Observable.just(position));

        presenter.onAddTagClicked(Observable.just(voidCallable().call()));

        verify(model).getNewTagDialogTitle();
        verify(view).showEditTextDialog(anyInt());
        verify(model).addNewAndRefresh(any(Tag.class));
        verify(view).setDataRefreshing(false);
        verify(view).scrollToPosition(position);
        verify(view).setNoTagsButtonVisibility(Visibility.GONE);
        verifyNoMoreInteractions(view, model);
    }

    static Callable<Void> voidCallable() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        };
    }

    @Test
    public void testAddTagError() throws Exception {
        testOnStartWithLoadedDataNotEmpty();
        final String tagName = "Tag name";
        when(view.showEditTextDialog(anyInt())).thenReturn(Observable.just(tagName));
        when(model.addNewAndRefresh(new Tag(null, tagName))).thenThrow(new Error());

        presenter.onAddTagClicked(Observable.just(voidCallable().call()));
        //Doesn't crash is good
    }


    @Test
    public void testRefresh() throws Exception {
        when(model.getAllObservable()).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onRefresh(Observable.just(voidCallable().call()));

        verify(model).getAllObservable();
    }

    @Test
    public void testItemCount() throws Exception {
        presenter.getItemCount();

        verify(model).getItemCount();
    }

    @Test
    public void testGetItemType() throws Exception {
        when(model.getItemCount()).thenReturn(1);
        assertEquals(R.layout.tags_item, presenter.getItemViewType(0));
        assertEquals(R.layout.tags_item_bottom_space, presenter.getItemViewType(1));
    }

    @Test
    public void testOnBindViewHolder() throws Exception {
        TagItemViewHolder mockViewHolder = Mockito.mock(TagItemViewHolder.class);
        when(model.getItemAt(0)).thenReturn(Mockito.mock(Tag.class));

        presenter.onBindViewHolder(mockViewHolder, 0);

        verify(model).getItemAt(0);
        verify(mockViewHolder).setName(anyString());
    }

    @Test
    public void testLongPressItem() throws Exception {
        final int deletePosition = 0x34;
        when(view.showRemoveTagDialog()).thenReturn(Observable.just(voidCallable().call()));
        Tag tag = Mockito.mock(Tag.class);
        when(model.getItemAt(deletePosition)).thenReturn(tag);
        when(model.removeAndRefresh(any(Tag.class))).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onItemLongClicked(deletePosition);

        verify(view).showRemoveTagDialog();
        verify(model).getItemAt(deletePosition);
        verify(model).removeAndRefresh(tag);
        verifyNoMoreInteractions(view, model);
    }

    @Test
    public void testOnStop() throws Exception {
        when(model.getDbProcessingObservable()).thenReturn(Observable.just(DbProcessing.FINISHED));
        when(model.getItemCount()).thenReturn(0);
        when(model.getAllObservable()).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onStart();
        presenter.onStop();

        assertThat(presenter.subscriptions.isUnsubscribed(), equalTo(false));
        assertThat(presenter.subscriptions.hasSubscriptions(), equalTo(false));
    }

    @Test
    public void testOnSearch() throws Exception {
        when(model.getAllFiltered("test")).thenReturn(Observable.just(Collections.<Tag>emptyList()));

        presenter.onSearch(Observable.<CharSequence>just("t","te","tes","test"));

        verify(model).getAllFiltered("test");
        verifyNoMoreInteractions(view, model);
    }

    @Test
    public void testOnItemClicked() throws Exception {
        final int position = 0x420;
        when(activityModel.isInSelectMode()).thenReturn(true);
        final Tag tag = new Tag(0x231L, "Name");
        when(model.getItemAt(position)).thenReturn(tag);

        presenter.onItemClicked(position);

        verify(activityModel).isInSelectMode();
        verify(model).getItemAt(position);
        verify(view).setResultAndReturn(tag.getId(), tag.getName());
    }

}