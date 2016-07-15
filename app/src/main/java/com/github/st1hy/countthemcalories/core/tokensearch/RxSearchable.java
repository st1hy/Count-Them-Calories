package com.github.st1hy.countthemcalories.core.tokensearch;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.functions.Func1;

public class RxSearchable implements Observable.OnSubscribe<SearchResult> {

    private static Func1<SearchResult, CharSequence> INTO_QUERY;

    final Searchable searchable;

    private RxSearchable(@NonNull Searchable searchable) {
        this.searchable = searchable;
    }

    @Override
    public void call(final Subscriber<? super SearchResult> subscriber) {
        searchable.setOnSearchChanged(new OnSearchChanged() {
            @Override
            public void onSearching(@NonNull String ingredientName, @NonNull List<String> tags) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(newResults(ingredientName, tags));
                }
            }
        });
        subscriber.onNext(newResults(searchable.getQuery(), searchable.getTokens()));
        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                searchable.setOnSearchChanged(null);
            }
        });
    }

    @NonNull
    private static SearchResult newResults(@NonNull String query, @NonNull List<String> tokens) {
        return new SearchResult(query, ImmutableList.copyOf(tokens));
    }

    @NonNull
    public static Observable<SearchResult> create(@NonNull final Searchable searchView) {
        return Observable.create(new RxSearchable(searchView));
    }



    @NonNull
    public static Func1<SearchResult, CharSequence> intoQuery() {
        if (INTO_QUERY == null) {
            INTO_QUERY = new Func1<SearchResult, CharSequence>() {
                @Override
                public CharSequence call(SearchResult searchResult) {
                    return searchResult.getQuery();
                }
            };
        }
        return INTO_QUERY;
    }
}
