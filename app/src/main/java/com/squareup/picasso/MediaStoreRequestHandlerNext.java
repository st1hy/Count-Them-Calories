package com.squareup.picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentUris.parseId;
import static android.provider.MediaStore.Images.Thumbnails.FULL_SCREEN_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;
import static android.provider.MediaStore.Images.Thumbnails.MINI_KIND;
import static com.squareup.picasso.MediaStoreRequestHandlerNext.PicassoKind.FULL;
import static com.squareup.picasso.MediaStoreRequestHandlerNext.PicassoKind.MICRO;
import static com.squareup.picasso.MediaStoreRequestHandlerNext.PicassoKind.MINI;
import static com.squareup.picasso.Picasso.LoadedFrom.DISK;

public class MediaStoreRequestHandlerNext extends ContentStreamRequestHandler {
    private static final String[] CONTENT_ORIENTATION = new String[] {
            MediaStore.Images.ImageColumns.ORIENTATION
    };
    private static final String[] CONTENT_DATA = new String[] {
            MediaStore.Images.ImageColumns.DATA
    };

    public MediaStoreRequestHandlerNext(Context context) {
        super(context);
    }

    @Override public boolean canHandleRequest(Request data) {
        final Uri uri = data.uri;
        return (SCHEME_CONTENT.equals(uri.getScheme())
                && MediaStore.AUTHORITY.equals(uri.getAuthority()));
    }

    @Override public Result load(Request request, int networkPolicy) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        int exifOrientation = getExifOrientation(contentResolver, request.uri);

        String mimeType = contentResolver.getType(request.uri);
        boolean isVideo = mimeType != null && mimeType.startsWith("video/");

        if (request.hasSize()) {
            PicassoKind picassoKind = getPicassoKind(request.targetWidth, request.targetHeight);
            if (!isVideo && picassoKind == FULL) {
                return new Result(null, getInputStream(request), DISK, exifOrientation);
            }

            long id = parseId(request.uri);

            BitmapFactory.Options options = createBitmapOptions(request);
            options.inJustDecodeBounds = true;

            calculateInSampleSize(request.targetWidth, request.targetHeight, picassoKind.width,
                    picassoKind.height, options, request);

            Bitmap bitmap;

            if (isVideo) {
                // Since MediaStore doesn't provide the full screen kind thumbnail, we use the mini kind
                // instead which is the largest thumbnail size can be fetched from MediaStore.
                int kind = (picassoKind == FULL) ? MediaStore.Video.Thumbnails.MINI_KIND : picassoKind.androidKind;
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, id, kind, options);
            } else {
                bitmap =
                        MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, picassoKind.androidKind, options);
            }

            if (bitmap != null) {
                return new Result(bitmap, null, DISK, exifOrientation);
            }
        }

        return new Result(null, getInputStream(request), DISK, exifOrientation);
    }

    static PicassoKind getPicassoKind(int targetWidth, int targetHeight) {
        if (targetWidth <= MICRO.width && targetHeight <= MICRO.height) {
            return MICRO;
        } else if (targetWidth <= MINI.width && targetHeight <= MINI.height) {
            return MINI;
        }
        return FULL;
    }

    static int getExifOrientation(ContentResolver contentResolver, Uri uri) {
        int exifOrientation = getExitOrientationFromFile(contentResolver, uri);
        if (exifOrientation == ExifInterface.ORIENTATION_UNDEFINED) {
            exifOrientation = getExifOrientationFromContentResolver(contentResolver, uri);
        }
        return exifOrientation;
    }

    static int getExitOrientationFromFile(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, CONTENT_DATA, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            String filePath = cursor.getString(0);
            ExifInterface exifInterface = new ExifInterface(filePath);
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

        } catch (Exception ignored) {
            // In case of error during reading exif, assume no rotation.
            return ExifInterface.ORIENTATION_UNDEFINED;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    static int getExifOrientationFromContentResolver(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, CONTENT_ORIENTATION, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }

            int rotation = cursor.getInt(0);
            switch (rotation) {
                case 90:
                    return ExifInterface.ORIENTATION_ROTATE_90;
                case 180:
                    return ExifInterface.ORIENTATION_ROTATE_180;
                case 270:
                    return ExifInterface.ORIENTATION_ROTATE_270;
                default:
                    return ExifInterface.ORIENTATION_NORMAL;
            }
        } catch (RuntimeException ignored) {
            // If the orientation column doesn't exist, assume no rotation.
            return ExifInterface.ORIENTATION_UNDEFINED;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    enum PicassoKind {
        MICRO(MICRO_KIND, 96, 96),
        MINI(MINI_KIND, 512, 384),
        FULL(FULL_SCREEN_KIND, -1, -1);

        final int androidKind;
        final int width;
        final int height;

        PicassoKind(int androidKind, int width, int height) {
            this.androidKind = androidKind;
            this.width = width;
            this.height = height;
        }
    }
}