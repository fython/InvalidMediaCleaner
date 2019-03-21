package moe.feng.media.cleaninvalid.task;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Objects;

import java9.util.function.Consumer;
import java9.util.stream.Stream;

import androidx.annotation.NonNull;
import moe.feng.media.cleaninvalid.model.MediaItem;
import moe.feng.media.cleaninvalid.util.MediaStoreUtils;

public class CleanFilesTask extends AsyncTask<Stream<MediaItem>, Void, Integer> {

    private ContentResolver mContentResolver;
    private int mDeletedCount;

    private Consumer<Integer> mCallback;

    public CleanFilesTask(@NonNull Context context, @NonNull Consumer<Integer> callback) {
        Objects.requireNonNull(context);
        mContentResolver = context.getContentResolver();
        mCallback = Objects.requireNonNull(callback);
    }

    @Override
    protected Integer doInBackground(Stream<MediaItem>... lists) {
        final Stream<MediaItem> list = Objects.requireNonNull(lists[0]);
        mDeletedCount = 0;
        list.forEach(item -> mDeletedCount +=
                MediaStoreUtils.deleteImage(mContentResolver, item.id));
        return mDeletedCount;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        mCallback.accept(integer);
    }

}
