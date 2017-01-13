package com.github.st1hy.countthemcalories.inject.activities.tags.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.st1hy.countthemcalories.R;
import com.github.st1hy.countthemcalories.activities.tags.fragment.TagsFragment;
import com.github.st1hy.countthemcalories.activities.tags.fragment.model.TagsFragmentModel;
import com.github.st1hy.countthemcalories.activities.tags.fragment.presenter.TagsDaoAdapter;
import com.github.st1hy.countthemcalories.inject.PerFragment;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(includes = TagsFragmentBindings.class)
public class TagsFragmentModule {

    private final TagsFragment fragment;

    public TagsFragmentModule(@NonNull TagsFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @PerFragment
    public TagsFragmentModel provideFragmentModel() {
        return new TagsFragmentModel(fragment.getArguments());
    }

    @Provides
    @Named("fragmentRootView")
    public View rootView() {
        return fragment.getView();
    }

    @Provides
    @PerFragment
    public static RecyclerView recyclerView(@Named("fragmentRootView") View rootView,
                                            @Named("activityContext") Context context,
                                            TagsDaoAdapter adapter) {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.tags_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        return recyclerView;
    }


}
