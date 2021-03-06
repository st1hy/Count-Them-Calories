package com.github.st1hy.countthemcalories.activities.overview.presenter;

import com.github.st1hy.countthemcalories.activities.overview.model.TimePeriodModel;
import com.github.st1hy.countthemcalories.core.BasicLifecycle;
import com.github.st1hy.countthemcalories.inject.PerActivity;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

@PerActivity
public class TimePeriodLoader implements BasicLifecycle {

    @Inject
    TimePeriodModel timePeriodModel;

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public TimePeriodLoader() {
    }

    @Override
    public void onStart() {
        subscriptions.add(
                Observable.interval(timeTillNextDayMillis(), TimeUnit.DAYS.toMillis(1L),
                        TimeUnit.MILLISECONDS)
                        .mergeWith(Observable.just(0L))
                        .subscribe(any -> {
                            DateTime end = DateTime.now().plusDays(1);
                            DateTime start = end.minusDays(30).withTimeAtStartOfDay();
                            timePeriodModel.refresh(start, end);
                        })
        );
    }

    private static long timeTillNextDayMillis() {
        DateTime now = DateTime.now();
        DateTime tomorrow = now.withTimeAtStartOfDay().plusDays(1);
        return new Interval(now, tomorrow).toDurationMillis();
    }

    @Override
    public void onStop() {
        subscriptions.clear();
    }
}
