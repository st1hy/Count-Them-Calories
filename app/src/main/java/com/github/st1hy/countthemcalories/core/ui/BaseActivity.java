package com.github.st1hy.countthemcalories.core.ui;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

import com.github.st1hy.countthemcalories.application.CaloriesCounterApplication;
import com.github.st1hy.countthemcalories.application.inject.ApplicationComponent;
import com.github.st1hy.countthemcalories.core.permissions.Permission;
import com.github.st1hy.countthemcalories.core.permissions.PermissionActor;
import com.github.st1hy.countthemcalories.core.permissions.PermissionSubject;

import rx.Observable;

public abstract class BaseActivity extends AppCompatActivity implements PermissionSubject {
    private final SparseArray<PermissionActor> pendingPermissionRequests = new SparseArray<>(4);

    protected final ApplicationComponent getAppComponent() {
        return ((CaloriesCounterApplication) getApplication()).getComponent();
    }

    @NonNull
    public static <T> T assertNotNull(@Nullable T t) {
        if (t == null) throw new NullPointerException();
        return t;
    }

    @TargetApi(23)
    @Override
    public Observable<Permission[]> requestPermission(@NonNull String[] permissions) {
        PermissionActor permissionActor = new PermissionActor(permissions);
        int requestId = permissionActor.hashCode();
        pendingPermissionRequests.put(requestId, permissionActor);
        requestPermissions(permissions, requestId);
        return permissionActor.asObservable();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionActor permissionActor = pendingPermissionRequests.get(requestCode);
        if (permissionActor != null) {
            pendingPermissionRequests.remove(requestCode);
            permissionActor.onRequestPermissionsResult(permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}