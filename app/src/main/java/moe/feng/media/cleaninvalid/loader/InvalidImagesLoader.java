package moe.feng.media.cleaninvalid.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.List;

import java9.util.stream.Collectors;
import java9.util.stream.Stream;
import moe.feng.media.cleaninvalid.BuildConfig;
import moe.feng.media.cleaninvalid.model.MediaItem;
import moe.feng.media.cleaninvalid.util.MediaStoreUtils;

public class InvalidImagesLoader extends AsyncTaskLoader<List<MediaItem>> {

    private static void DEBUG_LOG(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    private static final String TAG = InvalidImagesLoader.class.getSimpleName();

    public InvalidImagesLoader(Context context) {
        super(context);
    }

    @Override
    public List<MediaItem> loadInBackground() {
        Stream<MediaItem> stream = MediaStoreUtils.getAllImages(getContext().getContentResolver())
                .filter(item -> {
                    try {
                        final BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(item.path, opts);
                        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
                            DEBUG_LOG("Found " + item + " is invalid.");
                            return true;
                        }
                        if (opts.outMimeType == null) {
                            DEBUG_LOG("Found " + item + " is invalid.");
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        DEBUG_LOG("Found " + item + " is invalid.");
                        return true;
                    }
                });
        List<MediaItem> result = stream.collect(Collectors.toList());
        stream.close();
        return result;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
