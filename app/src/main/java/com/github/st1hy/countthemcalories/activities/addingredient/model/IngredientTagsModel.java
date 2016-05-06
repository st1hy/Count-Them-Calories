package com.github.st1hy.countthemcalories.activities.addingredient.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.st1hy.countthemcalories.activities.tags.model.TagsModel;
import com.github.st1hy.countthemcalories.database.Tag;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class IngredientTagsModel {
    final TagsModel tagsModel;
    final ArrayList<Tag> tags;
    final ParcelableProxy parcelableProxy;

    private final Subject<Void, Void> onDataSetChanged = PublishSubject.<Void>create().toSerialized();

    public IngredientTagsModel(@NonNull TagsModel tagsModel, @Nullable Bundle savedState) {
        this.tagsModel = tagsModel;
        ParcelableProxy parcelableProxy = null;
        if (savedState != null) {
            parcelableProxy = savedState.getParcelable(ParcelableProxy.STATE_MODEL);
        }
        if (parcelableProxy != null) {
            tags = parcelableProxy.tags;
        } else {
            parcelableProxy = new ParcelableProxy();
            tags = new ArrayList<>(5);
        }
        this.parcelableProxy = parcelableProxy;
    }

    public int getSize() {
        return tags.size();
    }

    @NonNull
    public Tag getTagAt(int position) {
        return tags.get(position);
    }

    public void addTag(long tagId, @NonNull String tagName) {
        tags.add(new Tag(tagId, tagName));
        onDataSetChanged.onNext(null);
    }

    public void removeAt(int position) {
        tags.remove(position);
    }

    @NonNull
    public Collection<Long> getTagIds() {
        return Collections2.transform(tags, tagsToLongs());
    }

    public void onSaveState(@NonNull Bundle outState) {
        outState.putParcelable(ParcelableProxy.STATE_MODEL, parcelableProxy.snapshot(this));
    }

    @NonNull
    public Observable<Void> datasetChangedObservable() {
        return onDataSetChanged;
    }

    @NonNull
    private Function<Tag, Long> tagsToLongs() {
        return new Function<Tag, Long>() {
            @Nullable
            @Override
            public Long apply(Tag input) {
                return input.getId();
            }
        };
    }

    static class ParcelableProxy implements Parcelable {
        static String STATE_MODEL = "add ingredient tags model";
        ArrayList<Tag> tags;

        ParcelableProxy() {
        }

        ParcelableProxy snapshot(@NonNull IngredientTagsModel model) {
            this.tags = new ArrayList<>(model.tags);
            return this;
        }

        public static final Creator<ParcelableProxy> CREATOR = new Creator<ParcelableProxy>() {
            @Override
            public ParcelableProxy createFromParcel(Parcel source) {
                ParcelableProxy parcelableProxy = new ParcelableProxy();
                final int size = source.readInt();
                ArrayList<Tag> tags = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    long id = source.readLong();
                    String name = source.readString();
                    tags.add(new Tag(id, name));
                }
                parcelableProxy.tags = tags;
                return parcelableProxy;
            }

            @Override
            public ParcelableProxy[] newArray(int size) {
                return new ParcelableProxy[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(tags.size());
            for (Tag tag : tags) {
                dest.writeLong(tag.getId());
                dest.writeString(tag.getName());
            }
        }
    }

}