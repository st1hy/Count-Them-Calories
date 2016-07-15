package com.github.st1hy.countthemcalories.core.withpicture.view;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.github.st1hy.countthemcalories.core.baseview.DialogView;

import rx.Observable;

public interface WithPictureView extends DialogView {

    void openCameraAndGetPicture();

    void pickImageFromGallery();

    @NonNull
    Observable<Void> getSelectPictureObservable();

    @NonNull
    Observable<Uri> getPictureSelectedObservable();

    @NonNull
    ImageView getImageView();

    void showImageOverlay();
}
