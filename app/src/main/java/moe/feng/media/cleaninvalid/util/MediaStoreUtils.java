package moe.feng.media.cleaninvalid.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import androidx.annotation.NonNull;
import java9.util.stream.Stream;
import moe.feng.media.cleaninvalid.model.MediaItem;

public final class MediaStoreUtils {

    private MediaStoreUtils() {
        throw new RuntimeException("Use static methods in this class only.");
    }

    private static final String TAG = MediaStoreUtils.class.getSimpleName();

    public static Stream<MediaItem> getAllImages(@NonNull ContentResolver cr) {
        final Cursor cur = Images.Media.query(
                cr, Images.Media.EXTERNAL_CONTENT_URI, null);
        if (cur == null) {
            throw new RuntimeException("Cannot get cursor for querying.");
        }
        return Stream.generate(() -> {
            if (cur.moveToNext()) {
                final MediaItem result = MediaItem.fromCursor(cur);
                if (cur.isLast() && !cur.isClosed()) {
                    cur.close();
                }
                return result;
            } else {
                return null;
            }
        }).limit(cur.getCount()).onClose(() -> {
            if (!cur.isClosed()) {
                cur.close();
            }
        });
    }

    public static int deleteImage(@NonNull ContentResolver cr, long id) {
        final Uri uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id);
        return cr.delete(uri, null, null);
    }

    public static int deleteImage(@NonNull ContentResolver cr, @NonNull String path) {
        return cr.delete(
                Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.MediaColumns.DATA + "='" + path + "'",
                null
        );
    }

}
