package com.github.st1hy.countthemcalories.activities.overview.model;

import android.database.Cursor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.github.st1hy.countthemcalories.activities.addmeal.model.PhysicalQuantitiesModel;
import com.github.st1hy.countthemcalories.core.rx.AbstractRxDatabaseModel;
import com.github.st1hy.countthemcalories.database.DaoSession;
import com.github.st1hy.countthemcalories.database.IngredientDao;
import com.github.st1hy.countthemcalories.database.IngredientTemplateDao;
import com.github.st1hy.countthemcalories.database.MealDao;
import com.github.st1hy.countthemcalories.inject.PerActivity;

import org.greenrobot.greendao.query.CursorQuery;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.Lazy;
import dagger.internal.SingleCheck;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

@PerActivity
public class TimePeriodModel extends AbstractRxDatabaseModel {

    private final Provider<CursorQuery> queryProvider = SingleCheck.provider(() -> {
        StringBuilder sql = new StringBuilder(512);
        sql.append("SELECT ");
        sql.append("M.").append(MealDao.Properties.CreationDate.columnName).append(", ");
        sql.append("I.").append(IngredientDao.Properties.Amount.columnName).append(", ");
        sql.append("IT.").append(IngredientTemplateDao.Properties.EnergyDensityAmount.columnName).append(", ");
        sql.append("IT.").append(IngredientTemplateDao.Properties.AmountType.columnName).append(" ");
        sql.append("from ").append(MealDao.TABLENAME).append(" M ");
        sql.append("join ").append(IngredientDao.TABLENAME).append(" I ");
        sql.append("on I.").append(IngredientDao.Properties.PartOfMealId.columnName)
                .append(" = M.").append(MealDao.Properties.Id.columnName).append(" ");
        sql.append("join ").append(IngredientTemplateDao.TABLENAME).append(" IT ");
        sql.append("on I.").append(IngredientDao.Properties.IngredientTypeId.columnName)
                .append(" = IT.").append(IngredientTemplateDao.Properties.Id.columnName).append(" ");
        sql.append("where M.").append(MealDao.Properties.CreationDate.columnName).append(" ");
        sql.append("between ").append("(?) and (?) ");
        sql.append("order by M.").append(MealDao.Properties.CreationDate.columnName).append(" asc;");
        return CursorQuery.internalCreate(dao(), sql.toString(), new Object[2]);
    });

    @Inject
    PhysicalQuantitiesModel quantityModel;
    private final PublishSubject<TimePeriod> subject = PublishSubject.create();

    @Inject
    public TimePeriodModel(@NonNull Lazy<DaoSession> session) {
        super(session);
    }

    @NonNull
    @CheckResult
    public Observable<TimePeriod> getRecentPeriod() {
        return subject.asObservable();
    }

    public void refresh(@NonNull final DateTime start, @NonNull final DateTime end) {
        loadData(start, end)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(subject::onNext)
                .subscribe();
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources") //Min Api 19, current min 16
    @NonNull
    @CheckResult
    private Observable<TimePeriod> loadData(@NonNull final DateTime start,
                                            @NonNull final DateTime end) {
        return fromDatabaseTask(() -> {
            CursorQuery query = queryProvider.get().forCurrentThread();
            query.setParameter(0, start.getMillis());
            query.setParameter(1, end.getMillis());
            Cursor cursor = query.query();
            try {
                return new TimePeriod.Builder(cursor, quantityModel, start, end).build();
            } finally {
                cursor.close();
            }
        });
    }

    @NonNull
    private MealDao dao() {
        return session().getMealDao();
    }

}